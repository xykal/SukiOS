package app.sukios

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Penjaga kebersihan sumber. Gradle menjalankan uji unit dengan direktori kerja = folder modul,
 * jadi jalur relatif di bawah menunjuk ke sumber aplikasi.
 */
class SourceHygieneTest {

    private val sources: List<File> = File("src/main/java").walkTopDown().filter { it.extension == "kt" }.toList()

    @Test
    fun sources_are_found() {
        assertTrue("sumber tidak ditemukan dari ${File(".").absolutePath}", sources.size >= 10)
    }

    @Test
    fun product_copy_never_addresses_the_end_user_as_the_developer() {
        // Nama panggilan pengembang pernah bocor ke layar persiapan; pengguna akhir bukan dia.
        val hits = sources.flatMap { f ->
            f.readLines().withIndex().filter { Regex("\\bkall\\b", RegexOption.IGNORE_CASE).containsMatchIn(it.value) }
                .map { "${f.name}:${it.index + 1}" }
        }
        assertTrue("kata 'kall' muncul di: $hits", hits.isEmpty())
    }

    @Test
    fun no_emoji_in_sources() {
        val emoji = Regex("[\\x{1F000}-\\x{1FAFF}]")
        val hits = sources.filter { emoji.containsMatchIn(it.readText()) }.map { it.name }
        assertTrue("emoji ditemukan di: $hits", hits.isEmpty())
    }

    @Test
    fun about_window_credits_the_brand_from_the_single_source() {
        val about = File("src/main/java/app/sukios/SukiApps.kt").readText()
        assertTrue("Tentang harus memakai Brand.COMPANY", about.contains("Brand.COMPANY"))
        assertTrue("Tentang harus menampilkan Brand.COPYRIGHT", about.contains("Brand.COPYRIGHT"))
    }
}
