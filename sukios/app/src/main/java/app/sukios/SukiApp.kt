package app.sukios

import android.app.Application
import android.content.Context

/**
 * Container proses SukiOS.
 *
 * Semua engine dirakit di sini sekali, lalu dipakai bersama oleh Beranda,
 * taskbar melayang, dan jendela. Tidak ada singleton global di luar ini,
 * supaya urutan inisialisasi jelas dan mudah dilacak kalau ada masalah.
 *
 * Engine yang ditanam:
 *  - SukiPrefs  : preferensi pengguna (kunci nilai)
 *  - SukiIndex  : indeks aplikasi terpasang
 *  - WinEngine  : mesin jendela (pindah, ubah ukuran, snap, tumpukan fokus)
 *  - SukiDiag   : diagnostik perangkat + ekspor laporan
 *  - SukiShell  : akses lanjutan lewat Shizuku + UserService milik SukiOS
 */
class SukiApp : Application() {

    lateinit var prefs: SukiPrefs
        private set
    lateinit var index: SukiIndex
        private set
    lateinit var wins: WinEngine
        private set
    lateinit var diag: SukiDiag
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = SukiPrefs(this)
        index = SukiIndex(this)
        wins = WinEngine()
        diag = SukiDiag(this)
        SukiShell.init(this)
    }

    companion object {
        fun of(ctx: Context): SukiApp = ctx.applicationContext as SukiApp
    }
}
