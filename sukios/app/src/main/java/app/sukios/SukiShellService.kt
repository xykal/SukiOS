package app.sukios

import android.os.Process
import android.util.Base64
import app.sukios.shell.ISukiShell
import java.io.InputStream

/**
 * SukiShellService — sisi yang berjalan sebagai shell (uid 2000).
 *
 * Kelas ini dijalankan oleh Shizuku di dalam proses terpisah, BUKAN di dalam
 * proses aplikasi. Karena itu:
 *  - Tidak boleh menyentuh Context aplikasi (getContentResolver, registerReceiver
 *    dan sejenisnya tidak berfungsi di sini).
 *  - Komunikasi keluar hanya lewat kontrak ISukiShell.
 *
 * Inilah bedanya dengan menambal perintah lewat API lama: kode SukiOS sendiri
 * yang jalan dengan hak akses shell, jadi kita bisa menambah kemampuan tanpa
 * menunggu API pihak lain.
 */
class SukiShellService : ISukiShell.Stub() {

    override fun getUid(): Int = Process.myUid()

    override fun getPid(): Int = Process.myPid()

    override fun getProtocolVersion(): Int = 1

    override fun destroy() {
        // Diminta Shizuku: bersihkan diri lalu keluar. Tanpa ini proses akan
        // menggantung dan pemanggilan berikutnya memakai kode lama.
        Process.killProcess(Process.myPid())
    }

    override fun deviceSummary(): String {
        val sb = StringBuilder()
        sb.append("uid=").append(Process.myUid()).append('\n')
        sb.append("pid=").append(Process.myPid()).append('\n')
        for (key in listOf("ro.build.version.release", "ro.build.version.sdk", "ro.product.model",
            "ro.product.manufacturer", "ro.product.cpu.abi", "ro.build.characteristics")) {
            val v = execRaw(listOf("getprop", key))
            val value = decodePart(v, 1).trim()
            sb.append(key).append('=').append(if (value.isEmpty()) "?" else value).append('\n')
        }
        return sb.toString()
    }

    override fun exec(argv: MutableList<String>): String = execRaw(argv)

    /** Jalan bersama untuk perintah dari luar maupun dari dalam service. */
    private fun execRaw(argv: List<String>): String {
        if (argv.isEmpty()) return encode(126, "", "argv kosong")
        return try {
            val pb = ProcessBuilder(argv.toList())
            pb.redirectErrorStream(false)
            val proc = pb.start()
            proc.outputStream.close()

            // stderr dibaca di thread terpisah supaya kedua pipa tidak saling mengunci.
            var errText = ""
            val errThread = Thread {
                errText = proc.errorStream.readTextSafe()
            }
            errThread.start()

            val outText = proc.inputStream.readTextSafe()
            val code = proc.waitFor()
            errThread.join(1200)

            encode(code, outText, errText)
        } catch (t: Throwable) {
            encode(127, "", t.message ?: t.javaClass.simpleName)
        }
    }

    private fun InputStream.readTextSafe(): String = try {
        readBytes().toString(Charsets.UTF_8)
    } catch (t: Throwable) {
        ""
    }

    /** Balasan: kode keluar, stdout (Base64), stderr (Base64) — satu per baris. */
    private fun encode(code: Int, out: String, err: String): String {
        val o = Base64.encodeToString(out.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val e = Base64.encodeToString(err.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return "$code\n$o\n$e"
    }

    private fun decodePart(raw: String, index: Int): String = runCatching {
        val parts = raw.split("\n")
        if (parts.size <= index) "" else String(Base64.decode(parts[index], Base64.NO_WRAP), Charsets.UTF_8)
    }.getOrDefault("")
}
