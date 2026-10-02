package app.sukios

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Ukuran minimum jendela, jarak tepi, dan bagian bilah judul yang harus tetap terjangkau. Satuan dp. */
const val WIN_MIN_W_DP = 320
const val WIN_MIN_H_DP = 220
const val WIN_MARGIN_DP = 8
const val WIN_GRAB_DP = 28

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

data class Bounds(val x: Float, val y: Float, val w: Float, val h: Float)

/**
 * Satu jendela. Semua properti yang dibaca composable memakai mutableStateOf:
 * mutableStateListOf pada WinEngine hanya memberi tahu perubahan isi daftar,
 * bukan perubahan field di dalam elemennya.
 */
@Stable
class Win(
    val id: Int,
    val kind: WinKind,
    title: String,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    z: Int,
    val pkg: String? = null,
) {
    var title by mutableStateOf(title)
    var x by mutableStateOf(x)
    var y by mutableStateOf(y)
    var w by mutableStateOf(w)
    var h by mutableStateOf(h)
    var z by mutableStateOf(z)
    var minimized by mutableStateOf(false)
    var maximized by mutableStateOf(false)

    /** Zona snap setengah/seperempat layar; null saat bebas atau maximize penuh. */
    var snap by mutableStateOf<SnapZone?>(null)

    /** Geometri bebas terakhir, dipulihkan saat jendela dilepas dari snap/maximize. Tidak dibaca UI. */
    var free: Bounds? = null
}

data class WorkRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = (right - left).coerceAtLeast(1f)
    val height: Float get() = (bottom - top).coerceAtLeast(1f)
}
