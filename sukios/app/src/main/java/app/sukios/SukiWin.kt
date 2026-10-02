package app.sukios

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * WinEngine — mesin jendela SukiOS.
 *
 * Murni geometri + tumpukan fokus; tidak memakai kelas Android, jadi perilakunya
 * dibuktikan lewat uji unit di CI (WinEngineTest), bukan ditebak.
 *
 * Aturan yang dijaga:
 *  - Tidak ada jendela di bawah taskbar (area kerja = layar - taskbar - margin).
 *  - Ukuran minimum dihormati, tetapi tidak pernah melebihi area kerja: pada layar
 *    sangat kecil minimum mengalah, tidak melempar exception.
 *  - Snap dan maximize mengingat geometri bebas terakhir; menggeser jendela yang
 *    sedang snap mengembalikan ukuran aslinya.
 *  - Setiap properti yang dibaca UI adalah state Compose (lihat Win). Tanpa itu
 *    jendela berubah di memori tetapi layar tidak pernah digambar ulang.
 */
class WinEngine {

    /** Batas jendela diatur Beranda saat mulai (mode Go = 3, normal = 8). */
    var maxWindows: Int = 8

    /** Piksel per dp, diisi UI. Semua batas dp di mesin ini dikonversi lewat nilai ini. */
    var density: Float = 1f
        set(value) {
            field = if (value >= MIN_DENSITY) value else MIN_DENSITY
        }

    val list = mutableStateListOf<Win>()
    var focusedId by mutableStateOf(-1)
        private set
    var lastBlockedReason by mutableStateOf("")

    private var nextId = 1
    private var nextZ = 1

    val count: Int get() = list.size
    val canOpenMore: Boolean get() = list.size < maxWindows

    private val margin: Float get() = WIN_MARGIN_DP * density
    private val minW: Float get() = WIN_MIN_W_DP * density
    private val minH: Float get() = WIN_MIN_H_DP * density
    private val grab: Float get() = WIN_GRAB_DP * density

    private fun find(id: Int): Win? = list.firstOrNull { it.id == id }

    /** coerceIn melempar bila batas bawah > batas atas; di sini batas atas yang mengalah. */
    private fun clamp(v: Float, lo: Float, hi: Float): Float = v.coerceIn(lo, if (hi < lo) lo else hi)

    /** Ukuran yang muat di area kerja; minimum mengalah bila area kerja lebih kecil dari minimum. */
    private fun fit(v: Float, min: Float, max: Float): Float {
        val hi = if (max < 1f) 1f else max
        return v.coerceIn(if (min > hi) hi else min, hi)
    }

    fun open(kind: WinKind, title: String, sw: Float, sh: Float, pkg: String? = null): Win? {
        if (!kind.internalWin) return null
        list.firstOrNull { it.kind == kind }?.let { existing ->
            existing.minimized = false
            focus(existing.id)
            return existing
        }
        if (!canOpenMore) {
            lastBlockedReason = "Batas $maxWindows jendela tercapai. Tutup satu dulu."
            return null
        }
        val work = workArea(sw, sh)
        val w = fit(work.width * 0.66f, minW, work.width)
        val h = fit(work.height * 0.84f, minH, work.height)
        val cascade = (list.size % 5) * 26f * density
        val win = Win(
            id = nextId++,
            kind = kind,
            title = title,
            x = clamp(work.left + work.width * 0.12f + cascade, work.left, work.right - w - margin),
            y = clamp(work.top + work.height * 0.14f + cascade, work.top, work.bottom - h - margin),
            w = w,
            h = h,
            z = nextZ++,
            pkg = pkg,
        )
        list.add(win)
        focusedId = win.id
        lastBlockedReason = ""
        return win
    }

    fun close(id: Int) {
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) list.removeAt(idx)
        if (focusedId == id) focusedId = topMostId()
    }

    fun closeAll() {
        list.clear()
        focusedId = -1
    }

    fun focus(id: Int) {
        val win = find(id) ?: return
        win.z = nextZ++
        focusedId = id
    }

    fun toggleMinimize(id: Int) {
        val win = find(id) ?: return
        win.minimized = !win.minimized
        if (!win.minimized) focus(id) else focusedId = topMostId()
    }

    fun minimizeAll() {
        list.forEach { it.minimized = true }
        focusedId = -1
    }

    fun toggleMax(id: Int, sw: Float, sh: Float) {
        val win = find(id) ?: return
        if (win.maximized) restoreFree(win, workArea(sw, sh)) else snap(id, SnapZone.MAX, sw, sh)
        focus(id)
    }

    fun moveTo(id: Int, x: Float, y: Float, sw: Float, sh: Float) {
        val win = find(id) ?: return
        val work = workArea(sw, sh)
        if (win.maximized || win.snap != null) releaseSize(win, work)
        win.x = clamp(x, work.left - win.w * 0.5f, work.right - win.w * 0.5f)
        win.y = clamp(y, work.top, work.bottom - grab)
    }

    fun moveBy(id: Int, dx: Float, dy: Float, sw: Float, sh: Float) {
        val win = find(id) ?: return
        moveTo(id, win.x + dx, win.y + dy, sw, sh)
    }

    /**
     * Ubah ukuran dari satu tepi atau sudut. dirX/dirY: -1 tepi kiri/atas, 0 tidak ikut,
     * 1 tepi kanan/bawah. Sisi yang berlawanan tetap diam, termasuk saat ukuran sudah
     * mentok minimum (memanggil moveBy lalu resize membuat jendela ikut meluncur).
     */
    fun resizeEdge(id: Int, dx: Float, dy: Float, dirX: Int, dirY: Int, sw: Float, sh: Float) {
        val win = find(id) ?: return
        val work = workArea(sw, sh)
        val right = win.x + win.w
        val bottom = win.y + win.h
        val newW = if (dirX == 0) win.w else fit(win.w + dx * dirX, minW, work.width)
        val newH = if (dirY == 0) win.h else fit(win.h + dy * dirY, minH, work.height)
        if (focusedId != id) focus(id)
        win.maximized = false
        win.snap = null
        win.w = newW
        win.h = newH
        if (dirX < 0) win.x = right - newW
        if (dirY < 0) {
            val top = (bottom - newH).coerceAtLeast(work.top)
            win.y = top
            win.h = (bottom - top).coerceAtLeast(1f)
        }
    }

    fun resize(id: Int, w: Float, h: Float, sw: Float, sh: Float) {
        val win = find(id) ?: return
        val work = workArea(sw, sh)
        win.maximized = false
        win.snap = null
        win.w = fit(w, minW, work.width)
        win.h = fit(h, minH, work.height)
        win.x = clamp(win.x, work.left - win.w * 0.5f, work.right - grab)
        win.y = clamp(win.y, work.top, work.bottom - grab)
    }

    /** Kotak untuk satu zona snap di dalam area kerja. Dipakai snap() dan pratinjau saat bilah judul digeser. */
    fun boundsFor(zone: SnapZone, sw: Float, sh: Float): Bounds {
        val work = workArea(sw, sh)
        val halfW = work.width / 2f
        val halfH = work.height / 2f
        val midX = work.left + halfW
        val midY = work.top + halfH
        return when (zone) {
            SnapZone.MAX -> Bounds(work.left, work.top, work.width, work.height)
            SnapZone.LEFT -> Bounds(work.left, work.top, halfW, work.height)
            SnapZone.RIGHT -> Bounds(midX, work.top, halfW, work.height)
            SnapZone.TOP -> Bounds(work.left, work.top, work.width, halfH)
            SnapZone.BOTTOM -> Bounds(work.left, midY, work.width, halfH)
            SnapZone.TL -> Bounds(work.left, work.top, halfW, halfH)
            SnapZone.TR -> Bounds(midX, work.top, halfW, halfH)
            SnapZone.BL -> Bounds(work.left, midY, halfW, halfH)
            SnapZone.BR -> Bounds(midX, midY, halfW, halfH)
        }
    }

    fun snap(id: Int, zone: SnapZone, sw: Float, sh: Float) {
        val win = find(id) ?: return
        rememberFree(win)
        val b = boundsFor(zone, sw, sh)
        win.x = b.x
        win.y = b.y
        win.w = b.w
        win.h = b.h
        win.maximized = zone == SnapZone.MAX
        win.snap = if (zone == SnapZone.MAX) null else zone
        win.minimized = false
        focus(id)
    }

    /** Zona yang akan dipakai bila geseran dilepas sekarang; null bila jendela masih di tengah. Pratinjau memakai aturan yang sama. */
    fun previewZone(id: Int, sw: Float, sh: Float, density: Float = this.density): SnapZone? {
        val win = find(id) ?: return null
        // Tepi kiri/kanan sempit (24 dp): jendela lebar yang digeser sedikit tidak boleh langsung menempel.
        val edge = 24f * density
        val topEdge = 40f * density
        val work = workArea(sw, sh)
        return when {
            win.y <= work.top + topEdge -> SnapZone.MAX
            win.x <= work.left + edge -> SnapZone.LEFT
            win.x + win.w >= work.right - edge -> SnapZone.RIGHT
            else -> null
        }
    }

    /** Snap berdasarkan posisi jendela: dipakai saat pengguna melepas geseran bilah judul. */
    fun snapFromPosition(id: Int, sw: Float, sh: Float, density: Float = this.density) {
        val zone = previewZone(id, sw, sh, density) ?: return
        snap(id, zone, sw, sh)
    }

    /** Pindah fokus ke jendela yang paling lama tidak dipakai (Alt+Tab sederhana). */
    fun cycle() {
        val visible = list.filter { !it.minimized }.sortedBy { it.z }
        if (visible.isEmpty()) return
        val current = visible.indexOfFirst { it.id == focusedId }
        focus(visible[(current + 1) % visible.size].id)
    }

    fun topMostId(): Int = list.filter { !it.minimized }.maxByOrNull { it.z }?.id ?: -1

    /** Area kerja = layar dikurangi taskbar dan margin. Dipakai semua perhitungan geometri. */
    fun workArea(sw: Float, sh: Float): WorkRect {
        val m = margin
        return WorkRect(m, m, sw - m, sh - TASKBAR_DP * density - m)
    }

    /** Simpan geometri bebas hanya saat jendela memang bebas, supaya snap berantai tidak menimpanya. */
    private fun rememberFree(win: Win) {
        if (!win.maximized && win.snap == null) win.free = Bounds(win.x, win.y, win.w, win.h)
    }

    private fun releaseSize(win: Win, work: WorkRect) {
        win.maximized = false
        win.snap = null
        val b = win.free ?: return
        win.w = fit(b.w, minW, work.width)
        win.h = fit(b.h, minH, work.height)
    }

    private fun restoreFree(win: Win, work: WorkRect) {
        win.maximized = false
        win.snap = null
        val b = win.free ?: return
        win.w = fit(b.w, minW, work.width)
        win.h = fit(b.h, minH, work.height)
        win.x = clamp(b.x, work.left - win.w * 0.5f, work.right - win.w * 0.5f)
        win.y = clamp(b.y, work.top, work.bottom - grab)
    }

    private companion object {
        const val MIN_DENSITY = 0.5f
    }
}
