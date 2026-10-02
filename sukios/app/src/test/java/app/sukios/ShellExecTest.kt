package app.sukios

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Bukti untuk tiga pagar ShellExec: tanpa shell parsing, batas waktu, batas keluaran.
 * Memakai program POSIX standar yang ada di runner CI (echo, sleep, head, yes, cat, false).
 */
class ShellExecTest {

    @Test
    fun arguments_are_never_interpreted_by_a_shell() {
        val marker = File("/tmp/suki-shell-injection-marker")
        marker.delete()
        val payload = "a;b && \$(id) | `id` > ${marker.path} \n x"
        val r = ShellExec.run(listOf("echo", payload))
        assertEquals(0, r.code)
        assertEquals(payload + "\n", r.out)
        assertFalse("payload tidak boleh dieksekusi", marker.exists())
    }

    @Test
    fun empty_or_blank_command_is_rejected_with_126() {
        assertEquals(ShellExec.CODE_BAD_ARGS, ShellExec.run(emptyList()).code)
        assertEquals(ShellExec.CODE_BAD_ARGS, ShellExec.run(listOf("  ")).code)
    }

    @Test
    fun nul_in_an_argument_is_rejected_with_126() {
        assertEquals(ShellExec.CODE_BAD_ARGS, ShellExec.run(listOf("echo", "a\u0000b")).code)
    }

    @Test
    fun missing_program_reports_127_with_a_reason() {
        val r = ShellExec.run(listOf("suki-no-such-program-xyz"))
        assertEquals(ShellExec.CODE_NOT_RUNNABLE, r.code)
        assertTrue(r.err.isNotBlank())
    }

    @Test
    fun exit_code_is_passed_through() {
        assertEquals(1, ShellExec.run(listOf("false")).code)
    }

    @Test
    fun hanging_command_is_killed_at_the_deadline() {
        val start = System.nanoTime()
        val r = ShellExec.run(listOf("sleep", "30"), timeoutMs = 400)
        val ms = (System.nanoTime() - start) / 1_000_000
        assertEquals(ShellExec.CODE_TIMEOUT, r.code)
        assertTrue("timeout harus disebut", r.err.contains("timeout"))
        assertTrue("harus kembali jauh sebelum 30 detik, ternyata $ms ms", ms < 8_000)
    }

    @Test
    fun output_is_capped_and_marked_as_truncated() {
        val r = ShellExec.run(listOf("head", "-c", "3000000", "/dev/zero"), timeoutMs = 20_000, maxBytes = 1024)
        assertEquals(0, r.code)
        assertTrue(r.out.contains("dipotong"))
        assertTrue("keluaran melebihi batas: ${r.out.length}", r.out.length < 1024 + 200)
    }

    @Test
    fun endless_output_is_stopped_by_the_deadline_and_still_capped() {
        val r = ShellExec.run(listOf("yes"), timeoutMs = 500, maxBytes = 4096)
        assertEquals(ShellExec.CODE_TIMEOUT, r.code)
        assertTrue("keluaran melebihi batas: ${r.out.length}", r.out.length < 4096 + 200)
    }

    @Test
    fun stdin_is_closed_so_interactive_programs_do_not_hang() {
        val r = ShellExec.run(listOf("cat"), timeoutMs = 3_000)
        assertEquals(0, r.code)
        assertEquals("", r.out)
    }

    @Test
    fun wire_format_round_trips_unicode_and_newlines() {
        val reply = ShellReply(3, "héllo — dunia\nbaris dua\n", "galat\n")
        assertEquals(reply, ShellExec.parse(ShellExec.encode(reply)))
    }

    @Test
    fun malformed_replies_become_visible_failures() {
        assertEquals(ShellExec.CODE_NOT_RUNNABLE, ShellExec.parse(null).code)
        assertEquals(ShellExec.CODE_NOT_RUNNABLE, ShellExec.parse("cuma satu baris").code)
        val broken = ShellExec.parse("0\n!!bukan-base64!!\n")
        assertTrue(broken.out.contains("rusak"))
    }

    @Test
    fun default_cap_keeps_the_binder_reply_far_below_the_one_megabyte_limit() {
        // Dua aliran penuh, Base64 membesar 4/3, String di Parcel = 2 byte per karakter.
        val worstChars = 2 * ((ShellExec.MAX_STREAM_BYTES + 3) / 3 * 4)
        assertTrue("balasan terburuk ${worstChars * 2} byte", worstChars * 2 < 512 * 1024)
    }
}
