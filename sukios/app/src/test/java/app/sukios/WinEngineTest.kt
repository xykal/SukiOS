package app.sukios

import androidx.compose.runtime.snapshots.Snapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

/**
 * Bukti perilaku mesin jendela. Angka acuan: layar 2400x1080 px, kepadatan 2,75, jadi
 * margin 22 px, taskbar 148,5 px, area kerja kiri/atas 22 dan kanan 2378, bawah 909,5.
 */
class WinEngineTest {

    private val sw = 2400f
    private val sh = 1080f
    private val eps = 0.01f

    private fun engine(density: Float = 2.75f, max: Int = 8) =
        WinEngine().apply {
            this.density = density
            maxWindows = max
        }

    private fun WinEngine.openKind(kind: WinKind): Win = open(kind, kind.title, sw, sh)!!

    @Test
    fun open_places_window_inside_work_area_and_focuses_it() {
        val e = engine()
        val w = e.openKind(WinKind.SETTINGS)
        val work = e.workArea(sw, sh)
        assertTrue(w.x >= work.left - eps)
        assertTrue(w.y >= work.top - eps)
        assertTrue(w.x + w.w <= work.right + eps)
        assertTrue(w.y + w.h <= work.bottom + eps)
        assertEquals(w.id, e.focusedId)
    }

    @Test
    fun opening_the_same_kind_again_restores_and_focuses_the_existing_window() {
        val e = engine()
        val a = e.openKind(WinKind.SETTINGS)
        e.openKind(WinKind.LAB)
        e.toggleMinimize(a.id)
        val again = e.open(WinKind.SETTINGS, "Setelan", sw, sh)
        assertSame(a, again)
        assertFalse(a.minimized)
        assertEquals(a.id, e.focusedId)
        assertEquals(2, e.count)
    }

    @Test
    fun window_limit_blocks_new_windows_and_says_why() {
        val e = engine(max = 2)
        e.openKind(WinKind.SETTINGS)
        e.openKind(WinKind.LAB)
        assertNull(e.open(WinKind.TERMINAL, "Terminal", sw, sh))
        assertTrue(e.lastBlockedReason.contains("2"))
        assertEquals(2, e.count)
        assertNotNull(e.open(WinKind.SETTINGS, "Setelan", sw, sh))
    }

    @Test
    fun external_app_windows_are_never_created_by_the_engine() {
        assertNull(engine().open(WinKind.APP, "luar", sw, sh))
    }

    @Test
    fun title_bar_cannot_be_dragged_under_the_taskbar_or_out_of_reach() {
        val e = engine()
        val w = e.openKind(WinKind.SETTINGS)
        val work = e.workArea(sw, sh)
        e.moveTo(w.id, 100f, 100_000f, sw, sh)
        assertEquals(work.bottom - WIN_GRAB_DP * 2.75f, w.y, eps)
        e.moveTo(w.id, 100f, -100_000f, sw, sh)
        assertEquals(work.top, w.y, eps)
        e.moveTo(w.id, -100_000f, 200f, sw, sh)
        assertEquals(work.left - w.w * 0.5f, w.x, eps)
        e.moveTo(w.id, 100_000f, 200f, sw, sh)
        assertEquals(work.right - w.w * 0.5f, w.x, eps)
    }

    @Test
    fun minimum_size_is_in_dp_so_it_scales_with_density() {
        val e = engine(density = 2f)
        val w = e.openKind(WinKind.SETTINGS)
        e.resize(w.id, 1f, 1f, sw, sh)
        assertEquals(WIN_MIN_W_DP * 2f, w.w, eps)
        assertEquals(WIN_MIN_H_DP * 2f, w.h, eps)
    }

    @Test
    fun resize_does_not_throw_when_the_work_area_is_smaller_than_the_minimum() {
        // Regresi: Float.coerceIn(min, max) melempar bila min > max. Terjadi pada layar kecil,
        // mode split-screen, atau sebelum layar selesai diukur.
        val e = engine(density = 3f)
        val w = e.openKind(WinKind.SETTINGS)
        e.resize(w.id, 5000f, 5000f, 300f, 200f)
        val work = e.workArea(300f, 200f)
        assertEquals(work.width, w.w, eps)
        assertEquals(work.height, w.h, eps)
    }

    @Test
    fun left_edge_resize_keeps_the_right_edge_fixed_even_at_the_minimum() {
        val e = engine()
        val w = e.openKind(WinKind.SETTINGS)
        w.x = 500f
        w.y = 200f
        w.w = 1000f
        w.h = 700f
        e.resizeEdge(w.id, 5000f, 0f, -1, 0, sw, sh)
        assertEquals(1500f, w.x + w.w, eps)
        assertEquals(WIN_MIN_W_DP * 2.75f, w.w, eps)
        assertEquals(700f, w.h, eps)
    }

    @Test
    fun right_edge_resize_grows_without_moving_the_left_edge() {
        val e = engine()
        val w = e.openKind(WinKind.SETTINGS)
        w.x = 500f
        w.w = 1000f
        e.resizeEdge(w.id, 400f, 0f, 1, 0, sw, sh)
        assertEquals(500f, w.x, eps)
        assertEquals(1400f, w.w, eps)
    }

    @Test
    fun top_edge_resize_keeps_the_bottom_edge_fixed_and_stays_in_the_work_area() {
        val e = engine()
        val w = e.openKind(WinKind.SETTINGS)
        w.y = 300f
        w.h = 600f
        e.resizeEdge(w.id, 0f, -1000f, 0, -1, sw, sh)
        assertEquals(900f, w.y + w.h, eps)
        assertEquals(e.workArea(sw, sh).top, w.y, eps)
    }

    @Test
    fun snap_fills_the_requested_half_of_the_work_area() {
        val e = engine()
        val w = e.openKind(WinKind.SETTINGS)
        val work = e.workArea(sw, sh)
        e.snap(w.id, SnapZone.LEFT, sw, sh)
        assertEquals(SnapZone.LEFT, w.snap)
        assertFalse(w.maximized)
        assertEquals(work.left, w.x, eps)
        assertEquals(work.width / 2f, w.w, eps)
        assertEquals(work.height, w.h, eps)
        e.snap(w.id, SnapZone.RIGHT, sw, sh)
        assertEquals(work.right, w.x + w.w, eps)
        assertEquals(work.left + work.width / 2f, w.x, eps)
    }

    @Test
    fun dragging_a_snapped_or_maximized_window_restores_its_free_size() {
        // Regresi: ukuran bebas tidak pernah disimpan saat snap, sehingga jendela yang digeser
        // keluar dari snap menciut ke ukuran minimum.
        val e = engine()
        val w = e.openKind(WinKind.SETTINGS)
        val w0 = w.w
        val h0 = w.h
        e.snap(w.id, SnapZone.LEFT, sw, sh)
        e.snap(w.id, SnapZone.RIGHT, sw, sh)
        e.moveBy(w.id, 40f, 10f, sw, sh)
        assertNull(w.snap)
        assertEquals(w0, w.w, eps)
        assertEquals(h0, w.h, eps)

        e.snap(w.id, SnapZone.MAX, sw, sh)
        assertTrue(w.maximized)
        e.moveBy(w.id, 40f, 10f, sw, sh)
        assertFalse(w.maximized)
        assertEquals(w0, w.w, eps)
        assertEquals(h0, w.h, eps)
    }

    @Test
    fun maximize_toggle_restores_the_exact_geometry() {
        val e = engine()
        val w = e.openKind(WinKind.SETTINGS)
        val work = e.workArea(sw, sh)
        val x0 = w.x
        val y0 = w.y
        val w0 = w.w
        val h0 = w.h
        e.toggleMax(w.id, sw, sh)
        assertTrue(w.maximized)
        assertEquals(work.width, w.w, eps)
        assertEquals(work.height, w.h, eps)
        e.toggleMax(w.id, sw, sh)
        assertFalse(w.maximized)
        assertEquals(x0, w.x, eps)
        assertEquals(y0, w.y, eps)
        assertEquals(w0, w.w, eps)
        assertEquals(h0, w.h, eps)
    }

    @Test
    fun releasing_a_drag_near_an_edge_snaps_and_in_the_middle_does_not() {
        fun placed(x: (Win, WorkRect) -> Float, y: Float): Win {
            val e = engine()
            val w = e.openKind(WinKind.SETTINGS)
            e.moveTo(w.id, x(w, e.workArea(sw, sh)), y, sw, sh)
            e.snapFromPosition(w.id, sw, sh)
            return w
        }
        assertEquals(SnapZone.LEFT, placed({ _, _ -> 0f }, 300f).snap)
        assertEquals(SnapZone.RIGHT, placed({ w, work -> work.right - w.w + 5f }, 300f).snap)
        assertTrue(placed({ _, _ -> 600f }, 30f).maximized)
        val middle = placed({ _, _ -> 600f }, 300f)
        assertNull(middle.snap)
        assertFalse(middle.maximized)
    }

    @Test
    fun focus_cycle_visits_every_visible_window_in_turn() {
        val e = engine()
        val a = e.openKind(WinKind.SETTINGS)
        val b = e.openKind(WinKind.LAB)
        val c = e.openKind(WinKind.TERMINAL)
        val seen = (1..3).map {
            e.cycle()
            e.focusedId
        }
        assertEquals(listOf(a.id, b.id, c.id), seen)
    }

    @Test
    fun minimizing_or_closing_the_focused_window_hands_focus_to_the_next_top_window() {
        val e = engine()
        val a = e.openKind(WinKind.SETTINGS)
        val b = e.openKind(WinKind.LAB)
        e.toggleMinimize(b.id)
        assertTrue(b.minimized)
        assertEquals(a.id, e.focusedId)
        e.toggleMinimize(b.id)
        assertFalse(b.minimized)
        assertEquals(b.id, e.focusedId)
        e.close(b.id)
        assertEquals(a.id, e.focusedId)
        e.closeAll()
        assertEquals(0, e.count)
        assertEquals(-1, e.focusedId)
    }

    @Test
    fun every_property_the_ui_reads_is_observable_by_compose() {
        // Regresi inti: dulu Win berisi field biasa, jadi menggeser jendela mengubah memori tetapi
        // tidak pernah memicu penggambaran ulang. Pembacaan state tercatat di snapshot; field biasa tidak.
        val e = engine()
        val w = e.openKind(WinKind.SETTINGS)
        val reads = HashSet<Any>()
        val snapshot = Snapshot.takeSnapshot(readObserver = { reads.add(it) })
        try {
            snapshot.enter {
                listOf<Any?>(w.title, w.x, w.y, w.w, w.h, w.z, w.minimized, w.maximized, w.snap)
            }
        } finally {
            snapshot.dispose()
        }
        assertEquals(9, reads.size)
    }

    @Test
    fun moving_a_window_is_reported_to_snapshot_observers() {
        val e = engine()
        val w = e.openKind(WinKind.SETTINGS)
        Snapshot.sendApplyNotifications()
        val changed = HashSet<Any>()
        val handle = Snapshot.registerApplyObserver { objects, _ -> changed.addAll(objects) }
        try {
            e.moveBy(w.id, 30f, 30f, sw, sh)
            Snapshot.sendApplyNotifications()
        } finally {
            handle.dispose()
        }
        assertTrue("perubahan geometri harus sampai ke observer", changed.isNotEmpty())
    }

    @Test
    fun random_operations_never_throw_and_keep_geometry_finite() {
        // Termasuk layar berukuran 0 (belum diukur), kepadatan berubah-ubah, dan layar lebih kecil
        // dari ukuran minimum. Benih tetap supaya kegagalan bisa diulang persis.
        val rnd = Random(20261002L)
        val e = engine()
        val kinds = WinKind.entries.filter { it.internalWin }
        val zones = SnapZone.entries
        repeat(4000) { step ->
            val w = rnd.nextInt(2800).toFloat()
            val h = rnd.nextInt(1600).toFloat()
            e.density = 0.5f + rnd.nextFloat() * 3.5f
            e.maxWindows = 1 + rnd.nextInt(8)
            val ids = e.list.map { it.id }
            val id = if (ids.isEmpty()) -1 else ids[rnd.nextInt(ids.size)]
            val dx = (rnd.nextFloat() - 0.5f) * 4000f
            val dy = (rnd.nextFloat() - 0.5f) * 4000f
            when (rnd.nextInt(14)) {
                0, 1 -> e.open(kinds[rnd.nextInt(kinds.size)], "x", w, h)
                2 -> e.close(id)
                3 -> e.focus(id)
                4 -> e.toggleMinimize(id)
                5 -> e.toggleMax(id, w, h)
                6 -> e.moveBy(id, dx, dy, w, h)
                7 -> e.moveTo(id, dx, dy, w, h)
                8 -> e.resize(id, dx, dy, w, h)
                9 -> e.resizeEdge(id, dx, dy, rnd.nextInt(3) - 1, rnd.nextInt(3) - 1, w, h)
                10 -> e.snap(id, zones[rnd.nextInt(zones.size)], w, h)
                11 -> e.snapFromPosition(id, w, h)
                12 -> e.cycle()
                else -> if (rnd.nextInt(20) == 0) e.minimizeAll() else e.cycle()
            }
            e.list.forEach { win ->
                assertTrue("langkah $step: x tidak hingga", win.x.isFinite())
                assertTrue("langkah $step: y tidak hingga", win.y.isFinite())
                assertTrue("langkah $step: lebar harus positif", win.w.isFinite() && win.w > 0f)
                assertTrue("langkah $step: tinggi harus positif", win.h.isFinite() && win.h > 0f)
            }
        }
    }

    @Test
    fun preview_zone_names_what_a_release_would_snap_to_and_bounds_match() {
        val e = engine()
        val w = e.openKind(WinKind.SETTINGS)
        val work = e.workArea(sw, sh)
        assertNull(e.previewZone(w.id, sw, sh))

        e.moveTo(w.id, work.left, 300f, sw, sh)
        assertEquals(SnapZone.LEFT, e.previewZone(w.id, sw, sh))
        val expected = e.boundsFor(SnapZone.LEFT, sw, sh)
        e.snapFromPosition(w.id, sw, sh)
        assertEquals(expected.x, w.x, eps)
        assertEquals(expected.y, w.y, eps)
        assertEquals(expected.w, w.w, eps)
        assertEquals(expected.h, w.h, eps)

        e.moveTo(w.id, 800f, work.top, sw, sh)
        assertEquals(SnapZone.MAX, e.previewZone(w.id, sw, sh))
        assertNull(e.previewZone(9999, sw, sh))
    }

    @Test
    fun zone_bounds_stay_inside_the_work_area_for_every_zone() {
        val e = engine()
        val work = e.workArea(sw, sh)
        SnapZone.values().forEach { z ->
            val b = e.boundsFor(z, sw, sh)
            assertTrue("$z x", b.x >= work.left - eps)
            assertTrue("$z y", b.y >= work.top - eps)
            assertTrue("$z kanan", b.x + b.w <= work.right + eps)
            assertTrue("$z bawah", b.y + b.h <= work.bottom + eps)
            assertTrue("$z ukuran", b.w > 0f && b.h > 0f)
        }
    }
}
