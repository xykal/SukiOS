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

    /** Sembunyikan status bar + navigation bar. */
    val fullDesktop = flowB("full_desktop", true)

    /** Taskbar melayang di atas app lain (butuh izin overlay). */
    val overlayBar = flowB("overlay_bar", false)

    /** Id preset aksen (lihat SukiAccents). Id lama dari versi sebelum Aurora jatuh ke aksen bawaan. */
    val accent = flowS("accent", "aurora")

    /** Id wallpaper (lihat WALL_SPECS). */
    val wallpaper = flowS("wallpaper", "aurora")

    /** Cahaya wallpaper bergeser pelan. Dimatikan otomatis di perangkat RAM rendah. */
    val wallMotion = flowB("wall_motion", true)

    /** Aplikasi selalu dibuka sebagai jendela. Bila Shizuku belum siap: tahan dan tawarkan persiapan, jangan diam-diam layar penuh. */
    val strictWindows = flowB("strict_windows", true)

    /** Hasil uji jendela terakhir: "OK|FULLSCREEN|NOT_STARTED|<epoch>|<catatan>" (kosong = belum pernah diuji). */
    val probe = flowS("probe", "")

    /** Sudah melewati layar persiapan. */
    val setupDone = flowB("setup_done", false)

    /** Aplikasi yang disematkan di desktop (pkg dipisah koma, urut). */
    val pinned = flowS("pinned", "")

    /** Aplikasi terakhir dibuka (pkg dipisah koma, terbaru di depan). */
    val recent = flowS("recent", "")

    fun setFullDesktop(v: Boolean) = put("full_desktop", v)
    fun setOverlayBar(v: Boolean) = put("overlay_bar", v)
    fun setAccent(v: String) = put("accent", v)
    fun setWallpaper(v: String) = put("wallpaper", v)
    fun setSetupDone(v: Boolean) = put("setup_done", v)
    fun setWallMotion(v: Boolean) = put("wall_motion", v)
    fun setStrictWindows(v: Boolean) = put("strict_windows", v)
    fun setProbe(v: String) = put("probe", v)

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
            "full_desktop" -> fullDesktop.value = v
            "overlay_bar" -> overlayBar.value = v
            "setup_done" -> setupDone.value = v
            "wall_motion" -> wallMotion.value = v
            "strict_windows" -> strictWindows.value = v
        }
    }

    private fun put(key: String, v: String) {
        sp.edit().putString(key, v).apply()
        when (key) {
            "accent" -> accent.value = v
            "wallpaper" -> wallpaper.value = v
            "probe" -> probe.value = v
            "pinned" -> pinned.value = v
            "recent" -> recent.value = v
        }
    }
}
