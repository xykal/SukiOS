package app.sukios

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/** Bukti kontras WCAG 2.x untuk token Aurora: teks 4,5:1, ikon/elemen non-teks 3:1. Hanya memakai angka ARGB mentah. */
class ThemeContrastTest {

    private fun lin(c: Int): Double {
        val v = c / 255.0
        return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }

    private fun lum(argb: Long): Double {
        val r = ((argb shr 16) and 0xFF).toInt()
        val g = ((argb shr 8) and 0xFF).toInt()
        val b = (argb and 0xFF).toInt()
        return 0.2126 * lin(r) + 0.7152 * lin(g) + 0.0722 * lin(b)
    }

    private fun ratio(a: Long, b: Long): Double {
        val x = lum(a)
        val y = lum(b)
        return (maxOf(x, y) + 0.05) / (minOf(x, y) + 0.05)
    }

    private val surfaces = mapOf(
        "bg" to C_BG, "surface" to C_SURFACE, "elevated" to C_ELEVATED, "overlay" to C_OVERLAY,
        "chrome" to C_CHROME, "sheet" to C_SHEET,
    )

    @Test
    fun body_text_tokens_pass_aa_on_every_surface() {
        for ((sn, s) in surfaces) for ((tn, t) in mapOf("text" to C_TEXT, "dim" to C_DIM, "faint" to C_FAINT)) {
            val r = ratio(t, s)
            assertTrue("$tn di $sn hanya ${"%.2f".format(r)}:1", r >= 4.5)
        }
    }

    @Test
    fun accent_text_and_status_text_pass_aa_on_the_lightest_surface() {
        val worst = surfaces.values.maxByOrNull { lum(it) }!!
        ACCENT_SPECS.forEach {
            val r = ratio(it.text, worst)
            assertTrue("teks aksen ${it.id} hanya ${"%.2f".format(r)}:1", r >= 4.5)
        }
        assertTrue(ratio(C_RED_TEXT, worst) >= 4.5)
        assertTrue(ratio(C_GREEN, worst) >= 4.5)
        assertTrue(ratio(C_AMBER, worst) >= 4.5)
    }

    @Test
    fun text_on_accent_fills_passes_aa() {
        ACCENT_SPECS.forEach {
            val r = ratio(it.on, it.fill)
            assertTrue("${it.id}: teks di atas isi tombol hanya ${"%.2f".format(r)}:1", r >= 4.5)
        }
    }

    @Test
    fun accent_glows_are_visible_as_non_text_elements() {
        ACCENT_SPECS.forEach {
            val r = ratio(it.main, C_SURFACE)
            assertTrue("${it.id}: ikon beraksen hanya ${"%.2f".format(r)}:1", r >= 3.0)
        }
    }

    @Test
    fun accent_ids_are_unique_and_the_default_is_first() {
        assertTrue(ACCENT_SPECS.map { it.id }.toSet().size == ACCENT_SPECS.size)
        assertTrue(ACCENT_SPECS.first().id == "aurora")
        assertTrue(WALL_SPECS.map { it.id }.toSet().size == WALL_SPECS.size)
        assertTrue(WALL_SPECS.first().id == "aurora")
    }
}
