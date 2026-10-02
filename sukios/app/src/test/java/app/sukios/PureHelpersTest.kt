package app.sukios

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class PureHelpersTest {

    private fun app(label: String, pkg: String) = AppEntry(label, pkg, "$pkg.Main", false, null)

    private val apps = listOf(
        app("Chrome", "com.android.chrome"),
        app("Kalender", "com.google.calendar"),
        app("Shizuku", "moe.shizuku.privileged.api"),
    )

    @Test
    fun search_matches_label_or_package_ignoring_case_and_spaces() {
        assertEquals(listOf("Chrome"), filterApps(apps, "  CHROME ").map { it.label })
        assertEquals(listOf("Shizuku"), filterApps(apps, "privileged").map { it.label })
        assertTrue(filterApps(apps, "zzz").isEmpty())
    }

    @Test
    fun blank_query_returns_everything_up_to_the_limit() {
        assertEquals(3, filterApps(apps, "").size)
        assertEquals(2, filterApps(apps, "   ", limit = 2).size)
    }

    @Test
    fun utc_stamp_ignores_device_time_zone_and_language() {
        val zone = TimeZone.getDefault()
        val locale = Locale.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jakarta"))
            Locale.setDefault(Locale.forLanguageTag("ar"))
            assertEquals("1970-01-01 00:00:00", Fmt.utcStamp(0L))
            assertEquals("2026-10-02 15:40:00", Fmt.utcStamp(1_790_955_600_000L))
        } finally {
            TimeZone.setDefault(zone)
            Locale.setDefault(locale)
        }
    }

    @Test
    fun brand_string_is_exact_and_untranslated() {
        assertEquals("XyVerse Technology Global", Brand.COMPANY)
        assertEquals("Powered by XyVerse Technology Global", Brand.poweredBy("Powered by"))
        assertEquals("Didukung oleh XyVerse Technology Global", Brand.poweredBy("Didukung oleh"))
        assertEquals("xykal — XyVerse Technology Global", Brand.CREDIT)
        assertEquals("Copyright (c) 2026 xykal — XyVerse Technology Global", Brand.COPYRIGHT)
    }
}
