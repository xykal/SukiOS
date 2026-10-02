package app.sukios

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

/**
 * Pengurai dump tugas. Fixture meniru bentuk dump yang DIPERIKSA terhadap sumber AOSP:
 * Android 10 (ActivityStack.dump: "Stack #", "Task id #", "mBounds=" sebelum "* TaskRecord{"),
 * Android 11 (kepala "* Task{... visible= type= mode= ... A=}"), dan Android 12 sampai 15
 * (Task.toFullString: "type= A= U= visible= visibleRequested= mode= translucent= sz="; mBounds hanya bila tidak kosong).
 * Yang belum terbukti: bentuknya di ROM pabrikan perangkat kall (UNVERIFIED sampai laporan diagnostik masuk).
 */
class FreeformParseTest {

    private val modern = """
ACTIVITY MANAGER ACTIVITIES (dumpsys activity activities)
Display #0 (activities from top to bottom):
  * Task{4e5b1b6 #114 type=standard A=com.example.notes U=0 visible=true visibleRequested=true mode=freeform translucent=false sz=1}
    mBounds=Rect(300, 60 - 1700, 900)
    isSleeping=false
    topResumedActivity=ActivityRecord{c0ffee1 u0 com.example.notes/.MainActivity t114}
    * Hist  #0: ActivityRecord{c0ffee1 u0 com.example.notes/.MainActivity t114}

  * Task{7a7a7a1 #113 type=standard A=com.google.android.youtube U=0 visible=true visibleRequested=true mode=fullscreen translucent=false sz=1}
    isSleeping=false
    * Hist  #0: ActivityRecord{abc1234 u0 com.google.android.youtube/com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity t113}

  * Task{11a #1 type=home U=0 visible=false visibleRequested=false mode=fullscreen translucent=false sz=1}
    * Hist  #0: ActivityRecord{a1b2 u0 app.sukios/.SukiHomeActivity t1}
"""

    private val android11 = """
Display #0 (activities from top to bottom):
  Stack #12: type=standard mode=freeform
  isSleeping=false
  mBounds=Rect(300, 60 - 1700, 900)
    * Task{c1d2 #12 visible=true type=standard mode=freeform translucent=false A=com.example.maps U=0 StackId=12 sz=1}
      mBounds=Rect(300, 60 - 1700, 900)
      mMinWidth=-1 mMinHeight=-1
      * Hist #0: ActivityRecord{3b2a u0 com.example.maps/.MapsActivity t12}
  Stack #1: type=home mode=fullscreen
  isSleeping=false
  mBounds=Rect(0, 0 - 0, 0)
    * Task{f1 #1 visible=false type=home mode=fullscreen translucent=false I=app.sukios/.SukiHomeActivity U=0 StackId=1 sz=1}
      mBounds=Rect(0, 0 - 0, 0)
      * Hist #0: ActivityRecord{e7 u0 app.sukios/.SukiHomeActivity t1}
"""

    private val legacy = """
ACTIVITY MANAGER ACTIVITIES (dumpsys activity activities)
Display #0 (activities from top to bottom):
  Stack #5: type=standard mode=freeform
  isSleeping=false
  mBounds=Rect(200, 100 - 1500, 800)

    Task id #77
    mBounds=Rect(210, 110 - 1490, 790)
    mMinWidth=-1
    mMinHeight=-1
    mLastNonFullscreenBounds=null
    * TaskRecord{9c4e1 #77 A=com.example.maps U=0 StackId=5 sz=1}
      userId=0 effectiveUid=u0a99 mCallingUid=2000 mUserSetupComplete=true
      * Hist #0: ActivityRecord{3b2a u0 com.example.maps/.MapsActivity t77}
  Stack #1: type=home mode=fullscreen
  isSleeping=false
  mBounds=Rect(0, 0 - 0, 0)

    Task id #1
    mBounds=Rect(0, 0 - 0, 0)
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
        assertNull(yt.bounds)
        assertEquals("home", rows[2].type)
        assertEquals("app.sukios", rows[2].pkg)
    }

    @Test
    fun android_11_dump_has_stack_headers_and_token_order_visible_type_mode() {
        val rows = FreeformParse.parse(android11)
        assertEquals(listOf(12, 1), rows.map { it.taskId })
        assertEquals("com.example.maps", rows[0].pkg)
        assertEquals("freeform", rows[0].mode)
        assertEquals("standard", rows[0].type)
        assertEquals(PxRect(300, 60, 1700, 900), rows[0].bounds)
        assertEquals("fullscreen", rows[1].mode)
        assertEquals("home", rows[1].type)
        assertNull("kotak kosong bukan kotak", rows[1].bounds)
    }

    @Test
    fun android_10_task_bounds_come_before_the_task_header_and_belong_to_that_task() {
        val rows = FreeformParse.parse(legacy)
        assertEquals(listOf(77, 1), rows.map { it.taskId })
        val maps = rows[0]
        assertEquals("com.example.maps", maps.pkg)
        assertEquals("freeform", maps.mode)
        assertEquals("standard", maps.type)
        assertEquals(PxRect(210, 110, 1490, 790), maps.bounds)
        assertEquals("fullscreen", rows[1].mode)
        assertEquals("home", rows[1].type)
        assertNull(rows[1].bounds)
    }

    @Test
    fun nested_tasks_with_undefined_mode_inherit_the_mode_of_their_root_task() {
        val dump = """
  * Task{aa #5 type=standard A=com.example.nest U=0 visible=true visibleRequested=true mode=freeform translucent=false sz=1}
    mBounds=Rect(100, 50 - 900, 700)
    * Task{bb #6 type=standard A=com.example.nest U=0 rootTaskId=5 visible=true visibleRequested=true mode=undefined translucent=false sz=1}
      * Hist  #0: ActivityRecord{cc u0 com.example.nest/.Main t6}
"""
        val rows = FreeformParse.parse(dump)
        assertEquals(listOf(5, 6), rows.map { it.taskId })
        assertTrue(rows[0].isFreeform)
        assertTrue("anak mewarisi mode induk", rows[1].isFreeform)
        assertEquals("com.example.nest", rows[1].pkg)
        assertEquals(PxRect(100, 50, 900, 700), rows[0].bounds)
    }

    @Test
    fun external_windows_are_visible_freeform_tasks_of_other_apps_only() {
        assertEquals(listOf(114), FreeformParse.externalWindows(FreeformParse.parse(modern), "app.sukios").map { it.taskId })
        assertEquals(listOf(77), FreeformParse.externalWindows(FreeformParse.parse(legacy), "app.sukios").map { it.taskId })
        assertEquals(listOf(12), FreeformParse.externalWindows(FreeformParse.parse(android11), "app.sukios").map { it.taskId })
        assertTrue(FreeformParse.externalWindows(FreeformParse.parse(modern), "com.example.notes").isEmpty())
        val hidden = modern.replace("visible=true visibleRequested=true mode=freeform", "visible=false visibleRequested=false mode=freeform")
        assertTrue(FreeformParse.externalWindows(FreeformParse.parse(hidden), "app.sukios").isEmpty())
    }

    @Test
    fun an_activity_line_before_its_task_header_merges_into_one_row() {
        val dump = """
  ResumedActivity: ActivityRecord{c0ffee1 u0 com.example.notes/.MainActivity t114}
  * Task{4e5b1b6 #114 type=standard A=com.example.notes U=0 visible=true mode=freeform translucent=false sz=1}
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
