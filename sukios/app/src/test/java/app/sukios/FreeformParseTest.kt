package app.sukios

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

/**
 * Pengurai dump tugas. Fixture disusun dari bentuk dump Android yang dikenal (gaya Android 10 dan gaya
 * Android 11 ke atas), BUKAN dari perangkat nyata: kecocokannya dengan perangkat kall UNVERIFIED sampai
 * potongan dump asli masuk lewat laporan diagnostik.
 */
class FreeformParseTest {

    private val modern = """
ACTIVITY MANAGER ACTIVITIES (dumpsys activity activities)
Display #0 (activities from top to bottom):
  * Task{4e5b1b6 #114 type=standard A=10213:com.example.notes U=0 visible=true mode=freeform translucent=false sz=1}
    mBounds=Rect(300, 60 - 1700, 900)
    userId=0 effectiveUid=u0a213 mCallingUid=2000 mUserSetupComplete=true
    intent={act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10000000 cmp=com.example.notes/.MainActivity}
    * Hist  #0: ActivityRecord{c0ffee1 u0 com.example.notes/.MainActivity t114}
  * Task{7a7a7a1 #113 type=standard A=10161:com.google.android.youtube U=0 visible=true mode=fullscreen translucent=false sz=1}
    mBounds=Rect(0, 0 - 2400, 1080)
    * Hist  #0: ActivityRecord{abc1234 u0 com.google.android.youtube/com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity t113}
  * Task{11a #1 type=home U=0 visible=false mode=fullscreen translucent=false sz=1}
    * Hist  #0: ActivityRecord{a1b2 u0 app.sukios/.SukiHomeActivity t1}
"""

    private val legacy = """
ACTIVITY MANAGER ACTIVITIES (dumpsys activity activities)
Display #0 (activities from top to bottom):
  Stack #5: type=standard mode=freeform
  isSleeping=false
  mBounds=Rect(200, 100 - 1500, 800)

    * TaskRecord{9c4e1 #77 A=com.example.maps U=0 StackId=5 sz=1}
      userId=0 effectiveUid=u0a99 mCallingUid=2000 mUserSetupComplete=true
      mBounds=Rect(210, 110 - 1490, 790)
      * Hist #0: ActivityRecord{3b2a u0 com.example.maps/.MapsActivity t77}
  Stack #1: type=home mode=fullscreen
    * TaskRecord{f1 #1 I=app.sukios/.SukiHomeActivity U=0 StackId=1 sz=1}
      * Hist #0: ActivityRecord{e7 u0 app.sukios/.SukiHomeActivity t1}
"""

    @Test
    fun modern_dump_yields_mode_bounds_and_package_per_task() {
        val rows = FreeformParse.parse(modern)
        assertEquals(listOf(114, 113, 1), rows.map { it.taskId })
        val notes = rows[0]
        assertEquals("com.example.notes", notes.pkg)
        assertEquals(".MainActivity", notes.activity)
        assertEquals("freeform", notes.mode)
        assertEquals("standard", notes.type)
        assertEquals(true, notes.visible)
        assertEquals(PxRect(300, 60, 1700, 900), notes.bounds)
        assertTrue(notes.isFreeform)
        val yt = FreeformParse.findTask(rows, "com.google.android.youtube")
        assertNotNull(yt)
        assertTrue(yt!!.isFullscreen)
        assertFalse(yt.isFreeform)
        assertEquals("home", rows[2].type)
        assertEquals("app.sukios", rows[2].pkg)
    }

    @Test
    fun legacy_dump_takes_the_mode_from_the_stack_header_and_its_own_bounds() {
        val rows = FreeformParse.parse(legacy)
        assertEquals(listOf(77, 1), rows.map { it.taskId })
        val maps = rows[0]
        assertEquals("com.example.maps", maps.pkg)
        assertEquals("freeform", maps.mode)
        assertEquals("standard", maps.type)
        assertEquals(PxRect(210, 110, 1490, 790), maps.bounds)
        assertEquals("fullscreen", rows[1].mode)
        assertEquals("home", rows[1].type)
    }

    @Test
    fun external_windows_are_visible_freeform_tasks_of_other_apps_only() {
        assertEquals(listOf(114), FreeformParse.externalWindows(FreeformParse.parse(modern), "app.sukios").map { it.taskId })
        assertEquals(listOf(77), FreeformParse.externalWindows(FreeformParse.parse(legacy), "app.sukios").map { it.taskId })
        assertTrue(FreeformParse.externalWindows(FreeformParse.parse(modern), "com.example.notes").isEmpty())
        val hidden = modern.replace("visible=true mode=freeform", "visible=false mode=freeform")
        assertTrue(FreeformParse.externalWindows(FreeformParse.parse(hidden), "app.sukios").isEmpty())
    }

    @Test
    fun an_activity_line_before_its_task_header_merges_into_one_row() {
        val dump = """
  ResumedActivity: ActivityRecord{c0ffee1 u0 com.example.notes/.MainActivity t114}
  * Task{4e5b1b6 #114 type=standard A=10213:com.example.notes U=0 visible=true mode=freeform translucent=false sz=1}
    * Hist  #0: ActivityRecord{c0ffee1 u0 com.example.notes/.MainActivity t114}
"""
        val rows = FreeformParse.parse(dump)
        assertEquals(1, rows.size)
        assertEquals("freeform", rows[0].mode)
        assertEquals("com.example.notes", rows[0].pkg)
    }

    @Test
    fun the_package_comes_from_the_top_activity_not_the_task_affinity() {
        val dump = """
  * Task{4e5b1b6 #9 type=standard A=10213:com.example.shared U=0 visible=true mode=freeform translucent=false sz=2}
    * Hist  #1: ActivityRecord{aa u0 com.example.notes/.EditActivity t9}
    * Hist  #0: ActivityRecord{bb u0 com.example.shared/.Launcher t9}
"""
        val row = FreeformParse.parse(dump).single()
        assertEquals("com.example.notes", row.pkg)
        assertEquals(".EditActivity", row.activity)
    }

    @Test
    fun a_dump_without_task_headers_still_gives_tasks_with_unknown_mode() {
        val rows = FreeformParse.parse("* Hist #0: ActivityRecord{3b2a u0 com.example.maps/.MapsActivity t77}")
        assertEquals(1, rows.size)
        assertEquals("", rows[0].mode)
        assertNull(rows[0].bounds)
        assertFalse(rows[0].isFreeform)
        assertFalse(rows[0].isFullscreen)
    }

    @Test
    fun empty_garbage_and_truncated_input_never_throw() {
        assertTrue(FreeformParse.parse("").isEmpty())
        assertTrue(FreeformParse.parse("tidak ada apa-apa di sini\n\n   \n").isEmpty())
        FreeformParse.parse(modern.take(modern.length / 2))
        FreeformParse.parse("Task{ #1 Task{ #x ActivityRecord{ u0 / t mBounds=Rect(1, 2 - ")
        assertTrue(FreeformParse.findTask(emptyList(), "a.b") == null)
    }

    @Test
    fun random_line_soup_never_throws() {
        val rnd = Random(11)
        val frags = listOf(
            "Task{ab12 #5 type=standard A=1:p.q U=0 visible=true mode=freeform}", "Stack #3: type=standard mode=freeform",
            "ActivityRecord{c u0 p.q/.R t5}", "mBounds=Rect(1, 2 - 30, 40)", "mode=", "#", "{", "}", "t99", "u0", "Rect(", "\n",
        )
        repeat(400) {
            val sb = StringBuilder()
            repeat(rnd.nextInt(12) + 1) { sb.append(frags[rnd.nextInt(frags.size)]).append(if (rnd.nextBoolean()) "\n" else " ") }
            FreeformParse.externalWindows(FreeformParse.parse(sb.toString()), "app.sukios")
        }
    }
}
