package app.sukios

/** Satu tugas (task) Android seperti terbaca dari `dumpsys activity activities`. */
data class TaskRow(
    val taskId: Int,
    val pkg: String,
    val activity: String,
    /** Mode jendela menurut sistem: "freeform", "fullscreen", "pinned", ... Kosong bila format tidak memberinya. */
    val mode: String,
    val type: String,
    val visible: Boolean?,
    val bounds: PxRect?,
) {
    val isFreeform: Boolean get() = mode == MODE_FREEFORM
    val isFullscreen: Boolean get() = mode == MODE_FULLSCREEN

    companion object {
        const val MODE_FREEFORM = "freeform"
        const val MODE_FULLSCREEN = "fullscreen"
    }
}

/**
 * FreeformParse — pengurai keluaran `dumpsys activity activities`. Murni, diuji di JVM.
 *
 * Format dump berubah antar versi Android, jadi pengurai ini toleran: ia mencari pola yang stabil
 * (kepala tugas "Task{hash #id ...}" / "TaskRecord{...}", "ActivityRecord{hash u0 pkg/kelas tID}",
 * "mode=...", "mBounds=Rect(l, t - r, b)"), dan setiap bagian yang tidak ditemukan dibiarkan kosong,
 * bukan ditebak. Hasil kosong berarti "tidak bisa dipastikan", bukan "gagal".
 *
 * Bentuk dump diperiksa terhadap SUMBER AOSP (ActivityStack.dump / Task.toFullString / RootWindowContainer
 * .dumpActivities) untuk Android 10, 11 dan 14; fixture ujinya meniru bentuk itu. Yang belum terbukti adalah
 * perilaku di ROM pabrikan kall: Laboratorium menyimpan potongan dump mentah supaya itu bisa diperiksa.
 */
object FreeformParse {

    private val TASK_HEAD = Regex("""Task(?:Record)?\{[0-9a-fA-F]+\s+#(\d+)\b([^}]*)""")
    private val STACK_HEAD = Regex("""Stack\s+#(\d+):(.*)""")
    private val TASK_ID_LINE = Regex("""^\s*Task id #(\d+)\s*$""")
    private val ROOT_ID = Regex("""\brootTaskId=(\d+)""")
    private val ACTIVITY = Regex("""ActivityRecord\{[0-9a-fA-F]+\s+u\d+\s+([A-Za-z0-9_.]+)/([A-Za-z0-9_.${'$'}]+)\s+t(\d+)""")
    private val BOUNDS = Regex("""mBounds=Rect\((-?\d+),\s*(-?\d+)\s*-\s*(-?\d+),\s*(-?\d+)\)""")
    private val MODE = Regex("""\bmode=([A-Za-z-]+)""")
    private val TYPE = Regex("""\btype=([A-Za-z-]+)""")
    private val AFFINITY = Regex("""\bA=(?:\d+:)?([A-Za-z0-9_.]+)""")
    private val VISIBLE = Regex("""\bvisible=(true|false)""")

    private class Builder(val id: Int) {
        var affinity = ""
        var actPkg = ""
        var activity = ""
        var mode = ""
        var type = ""
        var visible: Boolean? = null
        var bounds: PxRect? = null
        var rootId: Int? = null

        fun build() = TaskRow(id, actPkg.ifEmpty { affinity }, activity, mode, type, visible, bounds)
    }

    fun parse(dump: String): List<TaskRow> {
        val rows = ArrayList<Builder>()
        var current: Builder? = null
        var stackMode = ""
        var stackType = ""

        fun builderFor(id: Int): Builder = rows.firstOrNull { it.id == id } ?: Builder(id).also { rows.add(it) }

        for (line in dump.lineSequence()) {
            val st = STACK_HEAD.find(line)
            if (st != null) {
                stackMode = MODE.find(st.groupValues[2])?.groupValues?.get(1) ?: ""
                stackType = TYPE.find(st.groupValues[2])?.groupValues?.get(1) ?: ""
                current = null
                continue
            }
            // Android 10 menulis "Task id #N" dan mBounds SEBELUM kepala "* TaskRecord{...}".
            val idLine = TASK_ID_LINE.find(line)
            val idOnly = idLine?.groupValues?.get(1)?.toIntOrNull()
            if (idOnly != null) {
                current = builderFor(idOnly)
                continue
            }
            val head = TASK_HEAD.find(line)
            val headId = head?.groupValues?.get(1)?.toIntOrNull()
            if (head != null && headId != null) {
                val rest = head.groupValues[2]
                val b = builderFor(headId)
                b.affinity = AFFINITY.find(rest)?.groupValues?.get(1) ?: b.affinity
                b.mode = MODE.find(rest)?.groupValues?.get(1) ?: stackMode.ifEmpty { b.mode }
                b.type = TYPE.find(rest)?.groupValues?.get(1) ?: stackType.ifEmpty { b.type }
                b.visible = VISIBLE.find(rest)?.groupValues?.get(1)?.toBooleanStrictOrNull() ?: b.visible
                b.rootId = ROOT_ID.find(rest)?.groupValues?.get(1)?.toIntOrNull() ?: b.rootId
                current = b
                continue
            }
            val act = ACTIVITY.find(line)
            val actTask = act?.groupValues?.get(3)?.toIntOrNull()
            if (act != null && actTask != null) {
                val b = builderFor(actTask)
                if (b.activity.isEmpty()) {
                    b.actPkg = act.groupValues[1]
                    b.activity = act.groupValues[2]
                }
                continue
            }
            val bm = BOUNDS.find(line)
            val cur = current
            if (bm != null && cur != null && cur.bounds == null) {
                val v = bm.groupValues.drop(1).map { it.toInt() }
                // Kotak kosong ("Rect(0, 0 - 0, 0)") berarti tugas tidak punya kotak sendiri (layar penuh).
                if (v[2] > v[0] && v[3] > v[1]) cur.bounds = PxRect(v[0], v[1], v[2], v[3])
            }
        }
        // Tugas bersarang (Android 12+) bisa melaporkan mode "undefined"; ia ikut mode tugas induknya.
        for (b in rows) {
            val root = b.rootId
            if ((b.mode.isEmpty() || b.mode == "undefined") && root != null && root != b.id) {
                val parentMode = rows.firstOrNull { it.id == root }?.mode.orEmpty()
                if (parentMode.isNotEmpty() && parentMode != "undefined") b.mode = parentMode
            }
        }
        return rows.map { it.build() }
    }

    /** Tugas paling atas milik [pkg] (dump menulis dari atas ke bawah). */
    fun findTask(rows: List<TaskRow>, pkg: String): TaskRow? = rows.firstOrNull { it.pkg == pkg }

    /** Jendela mengambang milik aplikasi lain yang tampak, untuk ditampilkan di taskbar. */
    fun externalWindows(rows: List<TaskRow>, ownPkg: String): List<TaskRow> =
        rows.filter {
            it.isFreeform && it.pkg.isNotEmpty() && it.pkg != ownPkg &&
                (it.type.isEmpty() || it.type == "standard") && it.visible != false
        }.distinctBy { it.taskId }
}
