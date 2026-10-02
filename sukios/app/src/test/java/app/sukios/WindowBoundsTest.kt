package app.sukios

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class WindowBoundsTest {

    private fun inside(b: PxRect, w: PxRect) = b.l >= w.l && b.t >= w.t && b.r <= w.r && b.b <= w.b

    @Test
    fun work_area_leaves_room_for_the_taskbar_and_margins() {
        // 2400x1080 px pada kepadatan 2,75: margin 22 px, taskbar 148,5 -> 149 px.
        assertEquals(PxRect(22, 22, 2378, 909), WindowBounds.workArea(2400, 1080, 2.75f))
    }

    @Test
    fun work_area_survives_tiny_or_zero_screens() {
        val w = WindowBounds.workArea(0, 0, 3f)
        assertTrue(w.w >= 1 && w.h >= 1)
        assertNotNull(WindowBounds.workArea(10, 10, 0f))
    }

    @Test
    fun initial_box_is_inside_the_work_area_for_every_cascade_step_and_density() {
        for (density in listOf(1f, 1.5f, 2f, 2.75f, 3.5f)) {
            val work = WindowBounds.workArea((2400 * density / 2.75f).toInt(), (1080 * density / 2.75f).toInt(), density)
            for (portrait in listOf(false, true)) for (i in 0..9) {
                val b = WindowBounds.initial(work, portrait, i, density)
                assertTrue("d=$density p=$portrait i=$i $b dalam $work", inside(b, work))
                assertTrue(b.w > 0 && b.h > 0)
            }
        }
    }

    @Test
    fun portrait_only_apps_get_a_narrower_box_than_landscape_capable_ones() {
        val work = WindowBounds.workArea(2400, 1080, 2.75f)
        assertTrue(WindowBounds.initial(work, true, 0, 2.75f).w < WindowBounds.initial(work, false, 0, 2.75f).w)
    }

    @Test
    fun only_boxes_that_hide_the_taskbar_leave_the_screen_or_are_tiny_need_a_fix() {
        val work = WindowBounds.workArea(2400, 1080, 2.75f)
        assertFalse(WindowBounds.needsFix(null, work, 2.75f))
        assertFalse(WindowBounds.needsFix(PxRect(300, 60, 1700, 880), work, 2.75f))
        assertTrue(WindowBounds.needsFix(PxRect(300, 60, 1700, 1080), work, 2.75f))
        assertTrue(WindowBounds.needsFix(PxRect(-200, 60, 1000, 800), work, 2.75f))
        assertTrue(WindowBounds.needsFix(PxRect(300, 60, 2500, 800), work, 2.75f))
        assertTrue(WindowBounds.needsFix(PxRect(300, 60, 500, 200), work, 2.75f))
    }

    @Test
    fun clamping_always_lands_inside_the_work_area() {
        val rnd = Random(5)
        repeat(2000) {
            val density = 1f + rnd.nextFloat() * 3f
            val sw = 800 + rnd.nextInt(2400)
            val sh = 400 + rnd.nextInt(1400)
            val work = WindowBounds.workArea(sw, sh, density)
            val l = rnd.nextInt(6000) - 2000
            val t = rnd.nextInt(4000) - 1500
            val b = PxRect(l, t, l + rnd.nextInt(4000) - 500, t + rnd.nextInt(3000) - 500)
            val c = WindowBounds.clampInto(b, work, density)
            assertTrue("$b -> $c di luar $work", inside(c, work))
            assertTrue(c.w >= 0 && c.h >= 0)
        }
    }

    @Test
    fun a_box_that_already_fits_is_left_alone() {
        val work = WindowBounds.workArea(2400, 1080, 2.75f)
        val b = PxRect(400, 100, 1600, 800)
        assertEquals(b, WindowBounds.clampInto(b, work, 2.75f))
    }
}
