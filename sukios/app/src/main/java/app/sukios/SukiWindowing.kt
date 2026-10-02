package app.sukios

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class OutcomeKind { WINDOWED, UNVERIFIED, FULLSCREEN, RUNNING_FULLSCREEN, BLOCKED, FAILED }

/** Hasil satu upaya membuka aplikasi sebagai jendela. Pesannya jujur: tidak ada "berhasil" tanpa bukti. */
data class Outcome(val kind: OutcomeKind, val message: String, val tone: Tone, val detail: String = "")

/**
 * SukiWindowing — jalur tunggal membuka aplikasi pihak ketiga sebagai jendela mengambang.
 *
 * Perintahnya `am start --windowingMode 5` lewat SukiShell (Shizuku). Hasilnya diverifikasi dari dump tugas
 * sistem: mode tugas harus "freeform". Kalau tidak, hasilnya dilaporkan apa adanya, tidak dianggap berhasil:
 *  - RUNNING_FULLSCREEN: aplikasi SUDAH berjalan layar penuh sebelum diminta; Android tidak memindahkan tugas
 *    yang sudah ada, jadi pengguna ditawari menghentikannya dulu (tidak pernah dimatikan diam-diam);
 *  - FULLSCREEN: aplikasi baru, tetapi perangkat tetap membukanya layar penuh (ROM menolak jendela).
 * Kotak jendela hanya dikoreksi bila sistem menaruhnya di tempat yang tidak masuk akal (WindowBounds).
 */
object SukiWindowing {

    @Volatile var last: Outcome? = null
    @Volatile var lastDump: String = ""

    private val lock = Mutex()
    private val ERROR_TEXT = Regex("""Error type|Error:|Exception|Permission Denial""")

    suspend fun open(app: SukiApp, entry: AppEntry): Outcome = lock.withLock {
        val out = try {
            openLocked(app, entry)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Outcome(OutcomeKind.FAILED, "Gagal membuka ${entry.label}: ${e.message ?: e.javaClass.simpleName}", Tone.ERR)
        }
        last = out
        out
    }

    private suspend fun openLocked(app: SukiApp, entry: AppEntry): Outcome {
        if (!SukiShell.state.value.ready) {
            return Outcome(
                OutcomeKind.BLOCKED,
                "Jendela butuh Shizuku aktif. ${SukiShell.notReadyReason()}",
                Tone.WARN,
            )
        }
        if (!SukiAuto.state.value.coreOn) SukiAuto.ensure(app)

        val density = app.resources.displayMetrics.density
        val sw = SukiRuntime.screenW.toInt().takeIf { it > 0 } ?: app.resources.displayMetrics.widthPixels
        val sh = SukiRuntime.screenH.toInt().takeIf { it > 0 } ?: app.resources.displayMetrics.heightPixels
        val work = WindowBounds.workArea(sw, sh, density)

        // Sudah berjalan? Dicatat sebelum peluncuran supaya kegagalan bisa dibedakan dari penolakan ROM.
        val before = taskNow(entry.pkg)

        val start = SukiShell.io { launchFreeform(entry.component) }
        val said = start.out + "\n" + start.err
        if (!start.ok || ERROR_TEXT.containsMatchIn(said)) {
            return Outcome(OutcomeKind.FAILED, "Gagal membuka ${entry.label}: ${start.short(140)}", Tone.ERR, said.trim())
        }

        val task: TaskRow = waitForTask(entry) ?: return Outcome(
            OutcomeKind.UNVERIFIED,
            "Perintah jendela dikirim untuk ${entry.label}, tetapi belum bisa dipastikan. Lihat Setelan > Jendela.",
            Tone.WARN,
            "tugas tidak ditemukan di dump",
        )
        if (!task.isFreeform) {
            if (before != null && before.mode.isNotEmpty() && !before.isFreeform) {
                return Outcome(
                    OutcomeKind.RUNNING_FULLSCREEN,
                    "${entry.label} sudah berjalan layar penuh; Android tidak memindahkannya ke jendela.",
                    Tone.WARN,
                    "mode=${before.mode}",
                )
            }
            return Outcome(
                OutcomeKind.FULLSCREEN,
                "${entry.label} terbuka layar penuh: perangkat ini menolak mode jendela. Lihat Setelan > Jendela.",
                Tone.WARN,
                "mode=${task.mode.ifEmpty { "?" }}",
            )
        }
        if (WindowBounds.needsFix(task.bounds, work, density)) {
            val b = task.bounds
            val target = if (b == null) WindowBounds.initial(work, entry.portraitOnly, SukiTasks.windows.value.size, density)
            else WindowBounds.clampInto(b, work, density)
            SukiShell.io { taskResize(task.taskId, target) }
        }
        SukiTasks.refresh(app.packageName)
        return Outcome(OutcomeKind.WINDOWED, "${entry.label} dibuka sebagai jendela.", Tone.OK, "tugas #${task.taskId}")
    }

    private suspend fun taskNow(pkg: String): TaskRow? {
        val dump = SukiShell.io { dumpTasks() }
        return if (dump.ok) FreeformParse.findTask(FreeformParse.parse(dump.out), pkg) else null
    }

    /**
     * Tunggu tugas aplikasi tampil dalam mode jendela. Bila hanya terlihat tugas non-jendela (mungkin tugas lama
     * yang belum digantikan), polling dilanjutkan sampai habis; hasil terakhir yang terbaca dikembalikan.
     */
    private suspend fun waitForTask(entry: AppEntry, polls: Int = 5): TaskRow? {
        var seen: TaskRow? = null
        for (i in 0 until polls) {
            delay(if (i == 0) FIRST_WAIT_MS else NEXT_WAIT_MS)
            val dump = SukiShell.io { dumpTasks() }
            if (!dump.ok) continue
            val row = FreeformParse.findTask(FreeformParse.parse(dump.out), entry.pkg)
            if (row != null && row.mode.isNotEmpty()) {
                lastDump = SukiAuto.excerpt(dump.out, entry.pkg)
                seen = row
                if (row.isFreeform) return row
            }
        }
        return seen
    }

    fun report(): String = buildString {
        append("terakhir : ").append(last?.let { "${it.kind} - ${it.message} ${it.detail}".trim() } ?: "belum ada").append('\n')
        if (lastDump.isNotBlank()) append("--- potongan dump tugas ---\n").append(lastDump).append('\n')
    }

    private const val FIRST_WAIT_MS = 450L
    private const val NEXT_WAIT_MS = 350L
}

/**
 * SukiTasks — jendela mengambang aplikasi lain yang sedang tampak, dibaca berkala dari sistem lewat SukiShell.
 * Hanya berjalan saat Beranda terlihat dan SukiShell siap; kalau pembacaan gagal, daftar dibiarkan apa adanya.
 */
object SukiTasks {

    val windows = MutableStateFlow<List<TaskRow>>(emptyList())

    suspend fun poll(app: SukiApp) {
        while (true) {
            if (SukiRuntime.visible && SukiShell.state.value.ready) {
                refresh(app.packageName)
            } else if (!SukiShell.state.value.ready && windows.value.isNotEmpty()) {
                windows.value = emptyList()
            }
            delay(POLL_MS)
        }
    }

    suspend fun refresh(ownPkg: String) {
        val dump = SukiShell.io { dumpTasks() }
        if (!dump.ok) return
        val list = FreeformParse.externalWindows(FreeformParse.parse(dump.out), ownPkg)
        if (list != windows.value) windows.value = list
    }

    private const val POLL_MS = 3_000L
}
