package app.sukios

import android.os.Process
import app.sukios.shell.ISukiShell

/**
 * SukiShellService — sisi yang berjalan sebagai shell (uid 2000).
 *
 * Kelas ini dijalankan oleh Shizuku di dalam proses terpisah, BUKAN di dalam
 * proses aplikasi. Karena itu:
 *  - Tidak boleh menyentuh Context aplikasi (getContentResolver, registerReceiver
 *    dan sejenisnya tidak berfungsi di sini).
 *  - Komunikasi keluar hanya lewat kontrak ISukiShell.
 *
 * Eksekusi perintah didelegasikan ke ShellExec (tanpa shell parsing, dengan batas
 * waktu dan batas keluaran) supaya perilakunya bisa diuji di JVM.
 */
class SukiShellService : ISukiShell.Stub() {

    override fun getUid(): Int = Process.myUid()

    override fun getPid(): Int = Process.myPid()

    override fun getProtocolVersion(): Int = 1

    override fun destroy() {
        // Diminta Shizuku: keluar. Tanpa ini proses menggantung dan pemanggilan
        // berikutnya memakai kode lama.
        Process.killProcess(Process.myPid())
    }

    override fun deviceSummary(): String {
        val sb = StringBuilder()
        sb.append("uid=").append(Process.myUid()).append('\n')
        sb.append("pid=").append(Process.myPid()).append('\n')
        for (key in SUMMARY_PROPS) {
            val value = ShellExec.run(listOf("getprop", key), timeoutMs = PROP_TIMEOUT_MS).out.trim()
            sb.append(key).append('=').append(value.ifEmpty { "?" }).append('\n')
        }
        return sb.toString()
    }

    override fun exec(argv: MutableList<String>): String {
        val reply = try {
            ShellExec.run(argv)
        } catch (e: RuntimeException) {
            // Elemen null dari klien yang salah memanggil: dilaporkan sebagai gagal, bukan dibiarkan melempar.
            ShellReply(ShellExec.CODE_NOT_RUNNABLE, "", e.message ?: e.javaClass.simpleName)
        }
        return ShellExec.encode(reply)
    }

    private companion object {
        const val PROP_TIMEOUT_MS = 3_000L

        val SUMMARY_PROPS = listOf(
            "ro.build.version.release",
            "ro.build.version.sdk",
            "ro.product.model",
            "ro.product.manufacturer",
            "ro.product.cpu.abi",
            "ro.build.characteristics",
        )
    }
}
