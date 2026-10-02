package app.sukios

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Base64
import java.util.concurrent.TimeUnit

/** Hasil satu perintah sebelum dikodekan untuk binder. */
data class ShellReply(val code: Int, val out: String, val err: String)

/**
 * ShellExec — menjalankan satu perintah dengan tiga pagar:
 *  1. tanpa shell parsing: argv diteruskan apa adanya ke ProcessBuilder;
 *  2. batas waktu: proses yang menggantung dimatikan dan dilaporkan dengan kode 124;
 *  3. batas keluaran per aliran: sisanya dibuang dan ditandai, supaya balasan tidak
 *     melewati batas transaksi binder (sekitar 1 MB; String di Parcel = 2 byte per karakter).
 *
 * Murni JVM, jadi ketiga pagar dibuktikan di CI lewat ShellExecTest.
 */
object ShellExec {

    const val TIMEOUT_MS = 15_000L
    const val MAX_STREAM_BYTES = 64 * 1024

    const val CODE_TIMEOUT = 124
    const val CODE_BAD_ARGS = 126
    const val CODE_NOT_RUNNABLE = 127

    private const val READER_GRACE_MS = 1_000L

    fun run(
        argv: List<String>,
        timeoutMs: Long = TIMEOUT_MS,
        maxBytes: Int = MAX_STREAM_BYTES,
    ): ShellReply {
        if (argv.isEmpty() || argv[0].isBlank()) return ShellReply(CODE_BAD_ARGS, "", "argv kosong")
        if (argv.any { it.indexOf('\u0000') >= 0 }) {
            return ShellReply(CODE_BAD_ARGS, "", "argumen mengandung karakter NUL")
        }

        val proc = try {
            ProcessBuilder(argv).start()
        } catch (e: IOException) {
            return ShellReply(CODE_NOT_RUNNABLE, "", e.message ?: e.javaClass.simpleName)
        } catch (e: RuntimeException) {
            return ShellReply(CODE_NOT_RUNNABLE, "", e.message ?: e.javaClass.simpleName)
        }

        closeStdin(proc)
        // Dua aliran dibaca bersamaan; membaca satu per satu bisa mengunci proses
        // yang menulis banyak ke aliran yang belum dibaca.
        val out = Drain(proc.inputStream, maxBytes).also { it.start() }
        val err = Drain(proc.errorStream, maxBytes).also { it.start() }

        val finished = waitFor(proc, timeoutMs)
        if (!finished) proc.destroyForcibly()
        out.join(READER_GRACE_MS)
        err.join(READER_GRACE_MS)

        val code = if (finished) proc.exitValue() else CODE_TIMEOUT
        val note = if (finished) "" else "\n[timeout setelah $timeoutMs ms, proses dihentikan]"
        return ShellReply(code, out.text(), err.text() + note)
    }

    /** Balasan binder: kode keluar, stdout, stderr (Base64), satu per baris. */
    fun encode(reply: ShellReply): String {
        val enc = Base64.getEncoder()
        val o = enc.encodeToString(reply.out.toByteArray(Charsets.UTF_8))
        val e = enc.encodeToString(reply.err.toByteArray(Charsets.UTF_8))
        return "${reply.code}\n$o\n$e"
    }

    fun parse(raw: String?): ShellReply {
        if (raw == null) return ShellReply(CODE_NOT_RUNNABLE, "", "Balasan kosong dari SukiShell")
        val parts = raw.split("\n")
        if (parts.size < 3) {
            return ShellReply(CODE_NOT_RUNNABLE, "", "Balasan tidak dikenal: ${raw.take(120)}")
        }
        val code = parts[0].trim().toIntOrNull() ?: CODE_NOT_RUNNABLE
        return ShellReply(code, decode(parts[1]), decode(parts[2]))
    }

    private fun decode(b64: String): String = try {
        String(Base64.getDecoder().decode(b64), Charsets.UTF_8)
    } catch (e: IllegalArgumentException) {
        "[keluaran tidak terbaca: Base64 rusak]"
    }

    private fun waitFor(proc: Process, timeoutMs: Long): Boolean = try {
        proc.waitFor(timeoutMs, TimeUnit.MILLISECONDS)
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        false
    }

    private fun closeStdin(proc: Process) {
        try {
            proc.outputStream.close()
        } catch (e: IOException) {
            // Proses sudah keluar sebelum stdin sempat ditutup; tidak ada yang perlu dilakukan.
        }
    }

    /** Membaca satu aliran sampai habis, tetapi hanya menyimpan [max] byte pertama. */
    private class Drain(private val input: InputStream, private val max: Int) : Thread("suki-shell-drain") {
        private val buf = ByteArrayOutputStream()
        private val lock = Any()
        private var dropped = 0L

        init {
            isDaemon = true
        }

        override fun run() {
            val chunk = ByteArray(4096)
            try {
                while (true) {
                    val n = input.read(chunk)
                    if (n < 0) break
                    synchronized(lock) {
                        val room = (max - buf.size()).coerceAtLeast(0)
                        val keep = if (n < room) n else room
                        buf.write(chunk, 0, keep)
                        dropped += (n - keep)
                    }
                }
            } catch (e: IOException) {
                // Pipa ditutup saat proses dihentikan; byte yang sudah terbaca tetap dilaporkan.
            }
        }

        fun text(): String = synchronized(lock) {
            val body = String(buf.toByteArray(), Charsets.UTF_8)
            if (dropped > 0) "$body\n[dipotong: $dropped byte lagi tidak ditampilkan]" else body
        }
    }
}
