package app.sukios

import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class StepStatus { WAIT, RUN, OK, WARN, FAIL }

data class StepState(val id: String, val label: String, val status: StepStatus, val detail: String = "")

data class AutoState(
    val steps: List<StepState> = emptyList(),
    val running: Boolean = false,
    val coreOn: Boolean = false,
    val probing: Boolean = false,
)

/**
 * SukiAuto — persiapan otomatis lewat Shizuku.
 *
 * Alurnya: Shizuku hidup -> minta izin (sekali per sesi binder) -> SukiShell tersambung -> jalankan
 * SetupPlan (hanya langkah yang belum terpenuhi) -> verifikasi tiap langkah dengan API Android, bukan
 * dengan percaya pada kode keluar perintah -> uji jendela nyata bila diminta.
 */
object SukiAuto {

    val state = MutableStateFlow(AutoState())

    private val lock = Mutex()
    private val probeLock = Mutex()
    @Volatile private var askedThisBinder = false

    /** Potongan dump tugas dari uji jendela terakhir, untuk laporan diagnostik. */
    @Volatile var lastDumpExcerpt: String = ""

    /** Dipanggil tiap status Shizuku berubah dan saat Beranda tampil. Aman dipanggil berulang. */
    suspend fun onShellChanged(app: SukiApp, s: SukiShellState) {
        refreshCore(app)
        if (!s.binderAlive) {
            askedThisBinder = false
            return
        }
        if (!s.granted) {
            if (!askedThisBinder) {
                askedThisBinder = true
                SukiRuntime.say(SukiShell.requestPermission())
            }
            return
        }
        if (s.ready) ensure(app)
    }

    fun refreshCore(app: SukiApp) {
        val on = SetupPlan.CORE_KEYS.all { readGlobal(app, it) == "1" }
        if (state.value.coreOn != on) state.update { it.copy(coreOn = on) }
    }

    fun readGlobal(app: SukiApp, key: String): String? =
        runCatching { Settings.Global.getString(app.contentResolver, key) }.getOrNull()

    private fun satisfied(app: SukiApp, step: PlanStep): Boolean = when (step.check) {
        CheckKind.GLOBAL_SETTING -> readGlobal(app, step.key) == step.expect
        CheckKind.OVERLAY -> Settings.canDrawOverlays(app)
        CheckKind.SECURE_SETTINGS ->
            app.checkSelfPermission(ShellArgs.PERM_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
        CheckKind.HOME -> app.index.isCurrentLauncher()
    }

    private fun plan(app: SukiApp): List<PlanStep> =
        SetupPlan.build(Build.VERSION.SDK_INT, app.packageName, "${app.packageName}/.SukiHomeActivity")

    private fun setStep(id: String, status: StepStatus, detail: String) {
        state.update { s -> s.copy(steps = s.steps.map { if (it.id == id) it.copy(status = status, detail = detail) else it }) }
    }

    /** Jalankan langkah yang belum terpenuhi. Mengembalikan true bila setelan inti (jendela) sudah menyala. */
    suspend fun ensure(app: SukiApp, force: Boolean = false): Boolean = lock.withLock {
        val steps = plan(app)
        state.update { s ->
            s.copy(steps = steps.map { st ->
                val done = satisfied(app, st)
                StepState(st.id, st.label, if (done) StepStatus.OK else StepStatus.WAIT, if (done) "sudah aktif" else "")
            })
        }
        val pending = steps.filter { !satisfied(app, it) }
        if (pending.isEmpty() && !force) {
            refreshCore(app)
            return@withLock state.value.coreOn
        }
        state.update { it.copy(running = true) }
        try {
            for (step in pending) {
                setStep(step.id, StepStatus.RUN, "menjalankan...")
                var ok = false
                var err = ""
                for (cmd in step.commands) {
                    val r = SukiShell.io { runChecked(*cmd.toTypedArray()) }
                    if (!r.ok) err = r.short(160)
                    delay(150)
                    if (satisfied(app, step)) {
                        ok = true
                        break
                    }
                }
                val status = if (ok) StepStatus.OK else if (step.optional) StepStatus.WARN else StepStatus.FAIL
                setStep(step.id, status, if (ok) "berhasil" else err.ifBlank { "tidak terverifikasi" })
            }
        } finally {
            state.update { it.copy(running = false) }
            refreshCore(app)
        }
        state.value.coreOn
    }

    /**
     * Uji jendela nyata: buka ProbeActivity milik SukiOS sebagai jendela, lalu ukur dari DUA sisi:
     * dari dalam (isInMultiWindowMode di activity itu sendiri) dan dari sistem (mode tugas di dumpsys).
     * Salah satu saja yang menyatakan mode jendela sudah cukup untuk lulus.
     */
    suspend fun probe(app: SukiApp): ProbeRecord = probeLock.withLock {
        state.update { it.copy(probing = true) }
        try {
            if (!SukiShell.state.value.ready) {
                // Belum diuji sama sekali: jangan disimpan sebagai hasil uji.
                return@withLock ProbeRecord(ProbeRecord.VERDICT_NOT_STARTED, System.currentTimeMillis(), SukiShell.notReadyReason())
            }
            if (!state.value.coreOn) ensure(app)
            SukiRuntime.probeSeen = null
            val comp = "${app.packageName}/.ProbeActivity"
            val start = SukiShell.io { launchFreeform(comp) }
            if (!start.ok || ERROR_TEXT.containsMatchIn(start.out + start.err)) {
                return@withLock record(app, ProbeRecord.VERDICT_NOT_STARTED, "am start gagal: ${start.short(160)}")
            }
            var seen: ProbeSeen? = null
            for (i in 0 until PROBE_POLLS) {
                delay(PROBE_STEP_MS)
                seen = SukiRuntime.probeSeen
                if (seen != null && i >= 2) break
            }
            val dump = SukiShell.io { dumpTasks() }
            val row = if (dump.ok) {
                FreeformParse.parse(dump.out).firstOrNull { it.pkg == app.packageName && it.activity.endsWith("ProbeActivity") }
            } else {
                null
            }
            lastDumpExcerpt = if (dump.ok) excerpt(dump.out, "ProbeActivity") else "dumpsys gagal: ${dump.short(120)}"
            val inside = seen?.inMultiWindow == true
            val system = row?.isFreeform == true
            val note = "dari dalam: ${seen?.let { "multi-window=${it.inMultiWindow} ${it.widthDp}x${it.heightDp}dp" } ?: "tidak terlihat"}; " +
                "dari sistem: ${row?.let { "mode=${it.mode.ifEmpty { "?" }}" } ?: "tugas tidak ditemukan"}"
            val verdict = when {
                inside || system -> ProbeRecord.VERDICT_OK
                seen != null || row != null -> ProbeRecord.VERDICT_FULLSCREEN
                else -> ProbeRecord.VERDICT_NOT_STARTED
            }
            record(app, verdict, note)
        } finally {
            state.update { it.copy(probing = false) }
        }
    }

    private fun record(app: SukiApp, verdict: String, note: String): ProbeRecord {
        val now = System.currentTimeMillis()
        app.prefs.setProbe(WindowStatusLogic.formatProbe(verdict, now, note))
        return ProbeRecord(verdict, now, note)
    }

    /** Baris di sekitar kemunculan pertama [needle], maksimal 24 baris, supaya laporan tetap pendek. */
    fun excerpt(dump: String, needle: String): String {
        val lines = dump.lines()
        val at = lines.indexOfFirst { it.contains(needle) }
        if (at < 0) return "(tidak ada baris yang memuat $needle)"
        return lines.subList((at - 8).coerceAtLeast(0), (at + 16).coerceAtMost(lines.size)).joinToString("\n")
    }

    private val ERROR_TEXT = Regex("""Error type|Error:|Exception|Permission Denial""")
    private const val PROBE_POLLS = 14
    private const val PROBE_STEP_MS = 250L
}
