package app.sukios

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Bukti kontrol di AndroidManifest: SEMUA activity terkunci mendatar, izin tidak bertambah diam-diam,
 * dan hanya activity yang memang harus bisa dipanggil dari luar yang diekspor.
 */
class ManifestTest {

    private val ns = "http://schemas.android.com/apk/res/android"

    private val root: Element = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        .newDocumentBuilder().parse(File("src/main/AndroidManifest.xml")).documentElement

    private fun elements(tag: String): List<Element> {
        val nodes = root.getElementsByTagName(tag)
        return (0 until nodes.length).map { nodes.item(it) as Element }
    }

    @Test
    fun every_activity_is_locked_to_landscape() {
        val acts = elements("activity")
        assertTrue("manifest harus punya activity", acts.size >= 2)
        acts.forEach {
            assertEquals("activity ${it.getAttributeNS(ns, "name")}", "sensorLandscape", it.getAttributeNS(ns, "screenOrientation"))
        }
    }

    @Test
    fun the_permission_list_is_exactly_the_reviewed_one() {
        val perms = elements("uses-permission").map { it.getAttributeNS(ns, "name").removePrefix("android.permission.") }.toSet()
        assertEquals(
            setOf(
                "QUERY_ALL_PACKAGES", "SYSTEM_ALERT_WINDOW", "EXPAND_STATUS_BAR", "REQUEST_DELETE_PACKAGES",
                "ACCESS_NETWORK_STATE", "WRITE_SECURE_SETTINGS",
            ),
            perms,
        )
    }

    @Test
    fun only_the_home_and_probe_activities_are_exported() {
        val exported = elements("activity").filter { it.getAttributeNS(ns, "exported") == "true" }
            .map { it.getAttributeNS(ns, "name") }.toSet()
        assertEquals(setOf(".SukiHomeActivity", ".ProbeActivity"), exported)
        elements("service").forEach { assertEquals("false", it.getAttributeNS(ns, "exported")) }
    }

    @Test
    fun backups_stay_off() {
        assertEquals("false", elements("application").single().getAttributeNS(ns, "allowBackup"))
    }
}
