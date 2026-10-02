package app.sukios

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Preferensi SukiOS.
 *
 * Semua nilai berbentuk StateFlow supaya UI ikut berubah tanpa perlu
 * me-refresh manual. Isi disimpan ke SharedPreferences biasa (tanpa
 * enkripsi) — tidak ada data sensitif di sini, hanya tampilan dan saklar.
 */
class SukiPrefs(ctx: Context) {

    private val sp = ctx.getSharedPreferences("sukios", Context.MODE_PRIVATE)

    private fun flowB(key: String, def: Boolean) = MutableStateFlow(sp.getBoolean(key, def))
    private fun flowS(key: String, def: String) = MutableStateFlow(sp.getString(key, def) ?: def)
    private fun flowI(key: String, def: Int) = MutableStateFlow(sp.getInt(key, def))

    /** Kunci orientasi mendatar (mode desktop). */
    val lockLandscape = flowB("lock_landscape", true)

    /** Sembunyikan status bar + navigation bar. */
    val fullDesktop = flowB("full_desktop", true)

    /** Taskbar melayang di atas app lain (butuh izin overlay). */
    val overlayBar = flowB("overlay_bar", false)

    /** Id preset aksen (lihat SukiAccents). */
    val accent = flowS("accent", "steel")

    /** Id wallpaper (lihat SukiWalls). */
    val wallpaper = flowS("wallpaper", "charcoal")

    /** Sudah melewati layar persiapan. */
    val setupDone = flowB("setup_done", false)

    /** Batas jendela tambahan dari pengguna (0 = pakai nilai mesin). */
    val windowLimit = flowI("window_limit", 0)

    /** Aplikasi yang disematkan di desktop (pkg dipisah koma, urut). */
    val pinned = flowS("pinned", "")

    /** Aplikasi terakhir dibuka (pkg dipisah koma, terbaru di depan). */
    val recent = flowS("recent", "")

    fun setLockLandscape(v: Boolean) = put("lock_landscape", v)
    fun setFullDesktop(v: Boolean) = put("full_desktop", v)
    fun setOverlayBar(v: Boolean) = put("overlay_bar", v)
    fun setAccent(v: String) = put("accent", v)
    fun setWallpaper(v: String) = put("wallpaper", v)
    fun setSetupDone(v: Boolean) = put("setup_done", v)
    fun setWindowLimit(v: Int) = put("window_limit", v)

    fun setPinned(list: List<String>) = put("pinned", list.joinToString(","))

    fun togglePinned(pkg: String) {
        val cur = pinned.value.split(",").filter { it.isNotBlank() }.toMutableList()
        if (!cur.remove(pkg)) cur.add(0, pkg)
        setPinned(cur)
    }

    fun pushRecent(pkg: String) {
        val cur = recent.value.split(",").filter { it.isNotBlank() }.toMutableList()
        cur.remove(pkg)
        cur.add(0, pkg)
        put("recent", cur.take(24).joinToString(","))
    }

    private fun put(key: String, v: Boolean) {
        sp.edit().putBoolean(key, v).apply()
        when (key) {
            "lock_landscape" -> lockLandscape.value = v
            "full_desktop" -> fullDesktop.value = v
            "overlay_bar" -> overlayBar.value = v
            "setup_done" -> setupDone.value = v
        }
    }

    private fun put(key: String, v: String) {
        sp.edit().putString(key, v).apply()
        when (key) {
            "accent" -> accent.value = v
            "wallpaper" -> wallpaper.value = v
            "pinned" -> pinned.value = v
            "recent" -> recent.value = v
        }
    }

    private fun put(key: String, v: Int) {
        sp.edit().putInt(key, v).apply()
        if (key == "window_limit") windowLimit.value = v
    }
}
