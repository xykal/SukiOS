package app.sukios

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Data ikon hasil generator harus berbentuk benar dan tidak keluar dari kisi 24x24. */
class GlyphDataTest {

    private val cmd = Regex("([MLCZHV])([^MLCZHV]*)")
    private val per = mapOf('M' to 2, 'L' to 2, 'C' to 6, 'H' to 1, 'V' to 1, 'Z' to 0)

    private fun check(name: String, d: String) {
        if (d.isBlank()) return
        assertTrue("$name: path harus diawali M", d.trimStart().startsWith("M"))
        assertTrue("$name: hanya M L C H V Z, angka, spasi, titik, minus", d.all { it in "MLCHVZ0123456789 .-" })
        var used = 0
        for (m in cmd.findAll(d)) {
            val nums = m.groupValues[2].trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.map { it.toDouble() }
            val need = per.getValue(m.groupValues[1][0])
            if (need == 0) assertTrue("$name: Z tanpa angka", nums.isEmpty())
            else assertTrue("$name: ${m.value} butuh kelipatan $need angka", nums.size >= need && nums.size % need == 0)
            nums.forEach { assertTrue("$name: koordinat $it di luar kisi", it in -0.5..24.5) }
            used += nums.size
        }
        assertTrue("$name: path kosong", used > 0)
    }

    @Test
    fun every_glyph_is_well_formed_and_inside_the_grid() {
        GlyphKind.values().forEach {
            assertTrue("${it.name}: tanpa gambar", it.stroke.isNotBlank() || it.fill.isNotBlank())
            check(it.name + ".stroke", it.stroke)
            check(it.name + ".fill", it.fill)
        }
    }

    @Test
    fun the_set_has_the_icons_the_shell_needs() {
        val names = GlyphKind.values().map { it.name }.toSet()
        assertEquals(GlyphKind.values().size, names.size)
        listOf(
            "HOME", "APPS", "SEARCH", "SETTINGS", "TERMINAL", "FLASK", "INFO", "CLOSE", "MIN", "MAX", "RESTORE", "WINDOW",
            "WIFI", "BOLT", "SHIELD", "SHIELD_CHECK", "TRASH", "PIN", "EXTERNAL", "FULLSCREEN", "OK_CIRCLE", "FAIL_CIRCLE",
            "WAIT_CIRCLE", "ALERT", "PLAY", "REFRESH", "POWER", "VOLUME", "LOCK", "SPARK", "CHART", "FOLDER", "DESKTOP",
        ).forEach { assertTrue("ikon $it hilang", it in names) }
    }
}
