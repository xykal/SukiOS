package app.sukios

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * WinEngine — mesin jendela SukiOS.
 *
 * Ini murni geometri + tumpukan fokus. Tidak ada ketergantungan Android di
 * dalamnya, jadi perilakunya bisa dinalar dan diuji tanpa perangkat.
 *
 * Aturan yang dijaga mesin ini:
 *  - Tidak ada jendela di bawah taskbar (area kerja dikurangi tinggi taskbar).
 *  - Jendela tidak pernah lebih kecil dari WIN_MIN_W x WIN_MIN_H.
 *  - Snap mengisi setengah / seperempat area kerja, maximize mengisi penuh.
 *  - Fokus selalu di atas: setiap operasi menyentuh jendela akan menaikkan
 *    nilainya di tumpukan.
 */
enum class WinKind(val title: String, val internalWin: Boolean) {
    SETTINGS("Setelan", true),
    LAB("Laboratorium", true),
    TERMINAL("Terminal", true),
    APPS("Aplikasi", true),
    ABOUT("Tentang SukiOS", true),
    DIAG("Diagnostik", true),
    APP("Aplikasi luar", false),
}

enum class SnapZone { LEFT, RIGHT, TOP, BOTTOM, TL, TR, BL, BR, MAX }

data class Win(
    val id: Int,
    val kind: WinKind,
    var title: String,
    var x: Float,
    var y: Float,
    var w: Float,
    var h: Float,
    var minimized: Boolean = false,
    var maximized: Boolean = false,
    var z: Int = 1,
    val pkg: String? = null,
    var savedX: Float = 0f,
    var savedY: Float = 0f,
    var savedW: Float = 0f,
    var savedH: Float = 0f,
    var scroll: Int = 0,
)

class WinEngine {

    /** Batas jendela diatur Beranda saat mulai (mode Go = 3, normal = 8). */
    var maxWindows: Int = 8

    val list = mutableStateListOf<Win>()
    var focusedId by mutableStateOf(-1)
        private set
    var lastBlockedReason by mutableStateOf("")

    private var nextId = 1
    private var nextZ = 1

    val count: Int get() = list.size
    val canOpenMore: Boolean get() = list.size < maxWindows

    fun open(kind: WinKind, title: String, sw: Float, sh: Float, pkg: String? = null): Win? {
        if (!kind.internalWin) return null
        list.firstOrNull { it.kind == kind }?.let { existing ->
            if (existing.minimized) existing.minimized = false
            focus(existing.id)
            return existing
        }
        if (!canOpenMore) {
            lastBlockedReason = "Batas $maxWindows jendela tercapai. Tutup satu dulu."
            return null
        }
        val work = workArea(sw, sh)
        val w = (work.width * 0.62f).coerceAtLeast(WIN_MIN_W.toFloat())
        val h = (work.height * 0.68f).coerceAtLeast(WIN_MIN_H.toFloat())
        val offset = (list.size % 5) * 26f
        val win = Win(
            id = nextId++,
            kind = kind,
            title = title,
            x = (work.left + work.width * 0.18f + offset).coerceAtMost(work.right - w - 8f),
            y = (work.top + work.height * 0.14f + offset).coerceAtMost(work.bottom - h - 8f),
            w = w,
            h = h,
            z = nextZ++,
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
        val win = list.firstOrNull { it.id == id } ?: return
        win.z = nextZ++
        focusedId = id
    }

    fun toggleMinimize(id: Int) {
        val win = list.firstOrNull { it.id == id } ?: return
        win.minimized = !win.minimized
        if (!win.minimized) focus(id) else focusedId = topMostId()
    }

    fun minimizeAll() {
        list.forEach { it.minimized = true }
        focusedId = -1
    }

    fun toggleMax(id: Int, sw: Float, sh: Float) {
        val win = list.firstOrNull { it.id == id } ?: return
        val work = workArea(sw, sh)
        if (!win.maximized) {
            win.savedX = win.x; win.savedY = win.y; win.savedW = win.w; win.savedH = win.h
            win.x = work.left; win.y = work.top; win.w = work.width; win.h = work.height
            win.maximized = true
        } else {
            win.x = win.savedX; win.y = win.savedY; win.w = win.savedW; win.h = win.savedH
            win.maximized = false
        }
        focus(id)
    }

    fun moveTo(id: Int, x: Float, y: Float, sw: Float, sh: Float) {
        val win = list.firstOrNull { it.id == id } ?: return
        val work = workArea(sw, sh)
        if (win.maximized) {
            // Keluar dari maximize kalau digeser: jendela menempel ke kursor.
            win.maximized = false
            win.w = win.savedW.coerceAtLeast(WIN_MIN_W.toFloat())
            win.h = win.savedH.coerceAtLeast(WIN_MIN_H.toFloat())
        }
        win.x = x.coerceIn(work.left - win.w * 0.5f, work.right - win.w * 0.5f)
        win.y = y.coerceIn(work.top - 8f, work.bottom - 40f)
    }

    fun moveBy(id: Int, dx: Float, dy: Float, sw: Float, sh: Float) {
        val win = list.firstOrNull { it.id == id } ?: return
        moveTo(id, win.x + dx, win.y + dy, sw, sh)
    }

    fun resizeBy(id: Int, dw: Float, dh: Float, sw: Float, sh: Float) {
        val win = list.firstOrNull { it.id == id } ?: return
        resize(id, win.w + dw, win.h + dh, sw, sh)
    }

    fun resize(id: Int, w: Float, h: Float, sw: Float, sh: Float) {
        val win = list.firstOrNull { it.id == id } ?: return
        val work = workArea(sw, sh)
        win.maximized = false
        win.w = w.coerceIn(WIN_MIN_W.toFloat(), work.width)
        win.h = h.coerceIn(WIN_MIN_H.toFloat(), work.height)
        win.x = win.x.coerceAtMost(work.right - 40f)
        win.y = win.y.coerceAtMost(work.bottom - 32f)
    }

    fun snap(id: Int, zone: SnapZone, sw: Float, sh: Float) {
        val win = list.firstOrNull { it.id == id } ?: return
        val work = workArea(sw, sh)
        val halfW = work.width / 2f
        val halfH = work.height / 2f
        when (zone) {
            SnapZone.MAX -> { win.x = work.left; win.y = work.top; win.w = work.width; win.h = work.height; win.maximized = true }
            SnapZone.LEFT -> { win.x = work.left; win.y = work.top; win.w = halfW; win.h = work.height; win.maximized = false }
            SnapZone.RIGHT -> { win.x = work.left + halfW; win.y = work.top; win.w = halfW; win.h = work.height; win.maximized = false }
            SnapZone.TOP -> { win.x = work.left; win.y = work.top; win.w = work.width; win.h = halfH; win.maximized = false }
            SnapZone.BOTTOM -> { win.x = work.left; win.y = work.top + halfH; win.w = work.width; win.h = halfH; win.maximized = false }
            SnapZone.TL -> { win.x = work.left; win.y = work.top; win.w = halfW; win.h = halfH; win.maximized = false }
            SnapZone.TR -> { win.x = work.left + halfW; win.y = work.top; win.w = halfW; win.h = halfH; win.maximized = false }
            SnapZone.BL -> { win.x = work.left; win.y = work.top + halfH; win.w = halfW; win.h = halfH; win.maximized = false }
            SnapZone.BR -> { win.x = work.left + halfW; win.y = work.top + halfH; win.w = halfW; win.h = halfH; win.maximized = false }
        }
        focus(id)
    }

    /** Snap berdasarkan posisi jendela: dipakai saat pengguna melepas geseran. */
    fun snapFromPosition(id: Int, sw: Float, sh: Float, density: Float) {
        val win = list.firstOrNull { it.id == id } ?: return
        val edge = 96f * density
        val topEdge = 40f * density
        val work = workArea(sw, sh)
        when {
            win.y <= work.top + topEdge -> snap(id, SnapZone.MAX, sw, sh)
            win.x <= work.left + edge -> snap(id, SnapZone.LEFT, sw, sh)
            win.x + win.w >= work.right - edge -> snap(id, SnapZone.RIGHT, sw, sh)
        }
    }

    /** Pindah fokus ke jendela berikutnya (Alt+Tab sederhana). */
    fun cycle() {
        val visible = list.filter { !it.minimized }.sortedBy { it.z }
        if (visible.isEmpty()) return
        val current = visible.indexOfFirst { it.id == focusedId }
        val next = visible[(current + 1) % visible.size]
        focus(next.id)
    }

    fun topMostId(): Int = list.filter { !it.minimized }.maxByOrNull { it.z }?.id ?: -1

    /** Area kerja = layar dikurangi taskbar. Dipakai semua perhitungan geometri. */
    fun workArea(sw: Float, sh: Float): WorkRect {
        val bar = TASKBAR_DP * (sh / 400f).coerceIn(1.4f, 3.0f) // perkiraan kepadatan yang stabil
        return WorkRect(8f, 8f, sw - 8f, sh - bar - 8f)
    }
}

data class WorkRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = (right - left).coerceAtLeast(1f)
    val height: Float get() = (bottom - top).coerceAtLeast(1f)
}
