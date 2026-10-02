package app.sukios

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * SukiRuntime — keadaan UI yang hidup hanya selama proses berjalan.
 *
 * Sengaja dipisah dari SukiPrefs: yang di sini tidak disimpan. Nilai aksen dan
 * wallpaper tetap dicerminkan dari preferensi supaya komponen di SukiKit bisa
 * membacanya tanpa harus mengoper parameter ke mana-mana.
 */
object SukiRuntime {

    var accentId by mutableStateOf("steel")
    var wallpaperId by mutableStateOf("charcoal")

    /** Panel yang sedang terbuka. Hanya satu yang boleh terbuka sekaligus. */
    var startOpen by mutableStateOf(false)
    var quickOpen by mutableStateOf(false)

    /** Pesan singkat di atas taskbar. */
    var toast by mutableStateOf<String?>(null)

    /** Status sistem yang memengaruhi batas jendela. */
    var goMode by mutableStateOf(false)
    var isDefaultLauncher by mutableStateOf(false)
    var overlayBarOn by mutableStateOf(false)

    /** Apakah perangkat ini terdeteksi sebagai perangkat RAM rendah. */
    var deviceClass by mutableStateOf("Belum diperiksa")

    /** Ukuran layar terakhir yang diketahui (dipakai mesin jendela). */
    var screenW by mutableStateOf(0f)
    var screenH by mutableStateOf(0f)

    fun say(message: String) {
        toast = message
    }

    fun closePanels() {
        startOpen = false
        quickOpen = false
    }
}
