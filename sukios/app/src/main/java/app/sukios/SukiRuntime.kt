package app.sukios

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Nada pesan singkat; menentukan warna penanda di Toast. */
enum class Tone { INFO, OK, WARN, ERR }

data class ToastData(val message: String, val tone: Tone)

/** Hasil pengamatan ProbeActivity dari DALAM jendelanya sendiri (bukan dari dumpsys). */
data class ProbeSeen(val inMultiWindow: Boolean, val widthDp: Int, val heightDp: Int, val at: Long)

/**
 * SukiRuntime — keadaan UI yang hidup hanya selama proses berjalan.
 *
 * Sengaja dipisah dari SukiPrefs: yang di sini tidak disimpan. Nilai aksen dan
 * wallpaper tetap dicerminkan dari preferensi supaya komponen di SukiKit bisa
 * membacanya tanpa harus mengoper parameter ke mana-mana.
 */
object SukiRuntime {

    var accentId by mutableStateOf("aurora")
    var wallpaperId by mutableStateOf("aurora")
    var wallMotion by mutableStateOf(true)

    /** Panel yang sedang terbuka. Hanya satu yang boleh terbuka sekaligus. */
    var startOpen by mutableStateOf(false)
    var quickOpen by mutableStateOf(false)

    /** Layar Persiapan dibuka dari Setelan/tray (selain saat pertama kali). */
    var setupOpen by mutableStateOf(false)

    /** Aplikasi yang menu aksinya sedang terbuka (tekan lama), dan aplikasi yang peluncurannya tertahan. */
    var menuApp by mutableStateOf<AppEntry?>(null)
    var blockedApp by mutableStateOf<AppEntry?>(null)

    /** Aplikasi yang sudah berjalan layar penuh dan perlu dihentikan dulu agar bisa dibuka sebagai jendela. */
    var restartApp by mutableStateOf<AppEntry?>(null)

    /** Pratinjau zona snap saat bilah judul digeser. */
    var snapPreview by mutableStateOf<Bounds?>(null)

    /** Pesan singkat di atas taskbar. */
    var toast by mutableStateOf<ToastData?>(null)

    /** Status sistem yang memengaruhi batas jendela. */
    var goMode by mutableStateOf(false)
    var isDefaultLauncher by mutableStateOf(false)
    var overlayBarOn by mutableStateOf(false)

    /** Apakah perangkat ini terdeteksi sebagai perangkat RAM rendah. */
    var deviceClass by mutableStateOf("Belum diperiksa")

    /** Ukuran layar terakhir yang diketahui (dipakai mesin jendela). */
    var screenW by mutableStateOf(0f)
    var screenH by mutableStateOf(0f)

    /** Beranda sedang terlihat (Activity started). Pemeriksaan latar berhenti saat tidak. Bukan state UI. */
    @Volatile var visible: Boolean = false

    /** Pengamatan terakhir dari ProbeActivity. Bukan state UI: dibaca pemeriksa di thread IO. */
    @Volatile var probeSeen: ProbeSeen? = null

    fun say(message: String, tone: Tone = Tone.INFO) {
        toast = ToastData(message, tone)
    }

    fun closePanels() {
        startOpen = false
        quickOpen = false
        menuApp = null
    }
}
