package app.sukios

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Beranda SukiOS.
 *
 * Activity ini adalah launcher: dia menangani tombol Home (kategori HOME), jadi pengguna langsung masuk
 * ke SukiOS, bukan ke launcher bawaan.
 *
 * Tanggung jawabnya sengaja tipis:
 *  - memasang mode tampilan (desktop penuh) sesuai preferensi
 *  - menyerahkan seluruh tampilan ke Compose (SukiHome)
 *  - memuat indeks aplikasi sekali di latar belakang
 *
 * Orientasi TIDAK diatur di sini: AndroidManifest mengunci semua activity ke sensorLandscape (landscape dua
 * arah), dan ManifestTest membuktikannya di CI.
 */
class SukiHomeActivity : ComponentActivity() {

    private val app: SukiApp by lazy { SukiApp.of(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = app.prefs
        // Izinkan wallpaper sistem/live wallpaper digambar di belakang Beranda. Saat mode Aurora biasa,
        // lapisan Compose tetap menutupnya; saat wallpaper sistem dipilih, lapisan itu dibuat transparan.
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)

        // Kelas perangkat menentukan batas jendela SukiOS: perangkat RAM rendah/Go dibatasi 3 jendela
        // (sistem memang membatasi multi-window di sana) dan tidak menjalankan animasi dekoratif.
        val am = getSystemService(ActivityManager::class.java)
        val go = am?.isLowRamDevice == true
        SukiRuntime.goMode = go
        SukiRuntime.deviceClass = when {
            go -> "RAM rendah / Android Go: 3 jendela"
            am != null && am.memoryClass >= 512 -> "besar: 8 jendela"
            else -> "normal: 8 jendela"
        }
        app.wins.maxWindows = if (go) 3 else 8
        app.wins.density = resources.displayMetrics.density

        SukiRuntime.accentId = prefs.accent.value
        SukiRuntime.wallpaperId = prefs.wallpaper.value
        SukiRuntime.wallMotion = prefs.wallMotion.value
        SukiRuntime.overlayBarOn = prefs.overlayBar.value

        applyDesktopMode(prefs.fullDesktop.value)

        setContent { SukiHome(app = app, onApplyDesktop = ::applyDesktopMode) }

        // Indeks aplikasi dimuat di dalam Compose (LaunchedEffect), bukan di sini:
        // supaya tidak ada coroutine yang menggantung saat activity dihancurkan.
    }

    override fun onStart() {
        super.onStart()
        SukiRuntime.visible = true
    }

    override fun onStop() {
        SukiRuntime.visible = false
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        SukiShell.refresh()
        SukiAuto.refreshCore(app)
        SukiRuntime.isDefaultLauncher = app.index.isCurrentLauncher()
        SukiRuntime.overlayBarOn = app.prefs.overlayBar.value && app.index.hasOverlay()
        applyDesktopMode(app.prefs.fullDesktop.value)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applyDesktopMode(app.prefs.fullDesktop.value)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Tombol Beranda ditekan lagi: tutup panel yang terbuka, jangan buat yang baru.
        SukiRuntime.closePanels()
        if (intent.getStringExtra(EXTRA_OPEN) == "start") {
            SukiRuntime.startOpen = true
        }
    }

    /** Desktop penuh = status bar dan navigation bar disembunyikan. */
    private fun applyDesktopMode(full: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, !full)
        val c = WindowCompat.getInsetsController(window, window.decorView)
        c.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (full) {
            c.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            c.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    companion object {
        const val EXTRA_OPEN = "open"
    }
}
