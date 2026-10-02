package app.sukios

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.IBinder
import app.sukios.shell.ISukiShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

/**
 * SukiShell — akses lanjutan SukiOS lewat Shizuku.
 *
 * Peran Shizuku di sini HANYA kurir binder. Perintah tidak dijalankan lewat
 * API yang sudah dihapus (newProcess hilang di Shizuku 13.x); perintah
 * dijalankan oleh UserService milik SukiOS sendiri (SukiShellService) yang
 * berjalan dengan identitas shell (uid 2000) atau root (uid 0 via Sui).
 *
 * Kontrak perintah:
 *  - Argumen selalu berbentuk daftar (argv), tidak pernah string gabungan.
 *    Jadi tidak ada shell parsing dan tidak ada celah injeksi.
 *  - Hasil selalu dilaporkan apa adanya: kode keluar, stdout, stderr.
 *  - Kalau Shizuku tidak ada, semua fungsi mengembalikan SukiResult dengan
 *    kode 127 dan catatan jelas. Tidak ada kegagalan yang disembunyikan.
 */
data class SukiShellState(
    val installed: Boolean = false,
    val binderAlive: Boolean = false,
    val version: Int = 0,
    val uid: Int = -1,
    val granted: Boolean = false,
    val serviceBound: Boolean = false,
    val protocol: Int = 0,
    val note: String = "Belum diperiksa",
) {
    val isShell: Boolean get() = uid == 2000
    val isRoot: Boolean get() = uid == 0
    val ready: Boolean get() = binderAlive && granted && serviceBound
}

data class SukiResult(val code: Int, val out: String, val err: String) {
    val ok: Boolean get() = code == 0
    fun short(limit: Int = 400): String {
        val text = (out.ifBlank { err }).trim()
        return if (text.length <= limit) text else text.take(limit) + " ..."
    }
    fun full(): String = buildString {
        append("kode=").append(code)
        if (out.isNotBlank()) append("\n--stdout--\n").append(out.trim())
        if (err.isNotBlank()) append("\n--stderr--\n").append(err.trim())
    }
}

object SukiShell {

    const val PKG = "moe.shizuku.privileged.api"
    const val REQ = 4201
    private const val TAG = "sukios-shell"
    private const val PROTO = 1

    val state = MutableStateFlow(SukiShellState())

    private var app: Context? = null
    @Volatile private var binder: ISukiShell? = null
    private var args: Shizuku.UserServiceArgs? = null
    private var binding = false

    private val conn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binding = false
            binder = if (service != null && service.pingBinder()) {
                runCatching { ISukiShell.Stub.asInterface(service) }.getOrNull()
            } else null
            val proto = runCatching { binder?.protocolVersion ?: 0 }.getOrDefault(0)
            state.value = state.value.copy(
                serviceBound = binder != null,
                protocol = proto,
                note = if (binder != null) "SukiShell siap (protokol $proto)" else "Binder kosong",
            )
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
            state.value = state.value.copy(serviceBound = false, protocol = 0, note = "Service terputus")
        }
    }

    fun init(ctx: Context) {
        if (app != null) return
        app = ctx.applicationContext
        runCatching {
            Shizuku.addBinderReceivedListenerSticky { refresh() }
            Shizuku.addBinderDeadListener {
                binder = null
                state.value = state.value.copy(
                    binderAlive = false, serviceBound = false, granted = false,
                    note = "Binder Shizuku mati (Shizuku dihentikan?)",
                )
            }
            Shizuku.addRequestPermissionResultListener { _, grant ->
                state.value = state.value.copy(
                    granted = grant == PackageManager.PERMISSION_GRANTED,
                    note = if (grant == PackageManager.PERMISSION_GRANTED) "Izin diberikan" else "Izin ditolak",
                )
                if (grant == PackageManager.PERMISSION_GRANTED) bind()
            }
        }
        refresh()
    }

    fun refresh() {
        val ctx = app ?: return
        val installed = runCatching {
            ctx.packageManager.getPackageInfo(PKG, 0)
            true
        }.getOrDefault(false)

        val alive = runCatching { Shizuku.pingBinder() }.getOrDefault(false)
        val version = if (alive) runCatching { Shizuku.getVersion() }.getOrDefault(0) else 0
        val uid = if (alive) runCatching { Shizuku.getUid() }.getOrDefault(-1) else -1
        val granted = if (alive) runCatching {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false) else false

        val note = when {
            !installed -> "Aplikasi Shizuku belum terpasang"
            !alive -> "Shizuku terpasang, tapi servisnya belum jalan"
            !granted -> "Servis jalan (v$version). Izin SukiOS belum diberikan"
            else -> "Servis jalan (v$version), izin diberikan"
        }

        state.value = state.value.copy(
            installed = installed,
            binderAlive = alive,
            version = version,
            uid = uid,
            granted = granted,
            note = note,
        )
        if (alive && granted) bind() else unbind()
    }

    fun requestPermission(): String {
        val alive = runCatching { Shizuku.pingBinder() }.getOrDefault(false)
        if (!alive) return "Shizuku belum jalan. Buka aplikasi Shizuku dulu."
        if (runCatching { Shizuku.isPreV11() }.getOrDefault(true)) {
            return "Versi Shizuku terlalu tua (butuh 11+). Perbarui aplikasinya."
        }
        if (runCatching { Shizuku.checkSelfPermission() }.getOrDefault(-1) == PackageManager.PERMISSION_GRANTED) {
            bind()
            return "Izin sudah ada."
        }
        return runCatching {
            Shizuku.requestPermission(REQ)
            "Permintaan izin dikirim. Setujui dialog dari Shizuku."
        }.getOrElse { "Gagal meminta izin: ${it.message}" }
    }

    fun openShizukuApp(): Boolean {
        val ctx = app ?: return false
        return runCatching {
            ctx.startActivity(
                ctx.packageManager.getLaunchIntentForPackage(PKG)
                    ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/download/"))
            )
            true
        }.getOrDefault(false)
    }

    private fun bind() {
        val ctx = app ?: return
        if (binding || binder != null) return
        val a = args ?: Shizuku.UserServiceArgs(ComponentName(ctx, SukiShellService::class.java))
            .daemon(false)
            .tag(TAG)
            .version(PROTO)
            .processNameSuffix("shell")
            .debuggable(false)
            .also { args = it }
        binding = true
        runCatching { Shizuku.bindUserService(a, conn) }.onFailure {
            binding = false
            state.value = state.value.copy(note = "Gagal mengikat SukiShell: ${it.message}")
        }
    }

    fun unbind() {
        val a = args ?: return
        runCatching { Shizuku.unbindUserService(a, conn, true) }
        binder = null
        state.value = state.value.copy(serviceBound = false)
    }

    /**
     * Jalankan perintah di thread IO. SELALU dipakai dari UI: eksekusi memanggil binder dan
     * membuat proses, yang di thread utama bisa menahan layar (ANR) bila perintahnya lambat.
     */
    suspend fun <T> io(block: SukiShell.() -> T): T = withContext(Dispatchers.IO) { block(this@SukiShell) }

    /** Jalankan satu perintah (memblokir). Tidak pernah melempar; panggil lewat [io] dari UI. */
    fun run(vararg argv: String): SukiResult {
        val b = binder
        if (b == null) {
            return SukiResult(ShellExec.CODE_NOT_RUNNABLE, "", notReadyReason())
        }
        return runCatching {
            val reply = ShellExec.parse(b.exec(argv.toList()))
            SukiResult(reply.code, reply.out, reply.err)
        }.getOrElse { SukiResult(ShellExec.CODE_NOT_RUNNABLE, "", "Gagal memanggil SukiShell: ${it.message}") }
    }

    private fun notReadyReason(): String = when {
        !state.value.installed -> "Shizuku belum terpasang."
        !state.value.binderAlive -> "Servis Shizuku belum jalan."
        !state.value.granted -> "Izin SukiOS untuk Shizuku belum diberikan."
        else -> "SukiShell belum tersambung."
    }

    // ------------------------------------------------------------------
    // Perintah tetap. Argumen divalidasi di sini agar tidak pernah ada
    // string bebas yang masuk ke sisi shell.
    // ------------------------------------------------------------------

    fun identity(): SukiResult = run("id")

    fun deviceSummary(): SukiResult {
        val b = binder ?: return SukiResult(ShellExec.CODE_NOT_RUNNABLE, "", notReadyReason())
        return runCatching { SukiResult(0, b.deviceSummary(), "") }
            .getOrElse { SukiResult(ShellExec.CODE_NOT_RUNNABLE, "", "deviceSummary gagal: ${it.message}") }
    }

    fun forceResizable(on: Boolean): SukiResult =
        run("settings", "put", "global", "force_resizable_activities", if (on) "1" else "0")

    fun readForceResizable(): SukiResult =
        run("settings", "get", "global", "force_resizable_activities")

    fun allowOverlay(pkg: String): SukiResult {
        if (!ShellArgs.isPackage(pkg)) return SukiResult(ShellExec.CODE_BAD_ARGS, "", "Nama paket tidak valid")
        return run("appops", "set", pkg, "SYSTEM_ALERT_WINDOW", "allow")
    }

    fun displays(): SukiResult = run("dumpsys", "display")

    fun windowSize(): SukiResult = run("wm", "size")

    fun forceStop(pkg: String): SukiResult {
        if (!ShellArgs.isPackage(pkg)) return SukiResult(ShellExec.CODE_BAD_ARGS, "", "Nama paket tidak valid")
        return run("am", "force-stop", pkg)
    }

    fun launchOnDisplay(component: String, displayId: Int): SukiResult {
        if (!ShellArgs.isComponent(component)) {
            return SukiResult(ShellExec.CODE_BAD_ARGS, "", "Komponen tidak valid")
        }
        if (!ShellArgs.isDisplayId(displayId)) return SukiResult(ShellExec.CODE_BAD_ARGS, "", "Id display tidak valid")
        return run("am", "start", "--display", displayId.toString(), "-n", component)
    }

    /** Tombol kembali global — berguna saat app pihak ketiga menutupi layar. */
    fun globalBack(): SukiResult = run("input", "keyevent", "4")

    fun globalHome(): SukiResult = run("input", "keyevent", "3")

    fun systemProperty(key: String): SukiResult {
        if (!ShellArgs.isProperty(key)) return SukiResult(ShellExec.CODE_BAD_ARGS, "", "Nama properti tidak valid")
        return run("getprop", key)
    }

    /** Cermin status bar — hanya kalau shell tersedia. */
    fun expandStatusBar(): SukiResult = run("cmd", "statusbar", "expand-notifications")
}
