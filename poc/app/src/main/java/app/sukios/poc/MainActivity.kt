package app.sukios.poc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember

/**
 * SukiOS PoC — pintu masuk.
 *
 * Satu Activity yang menjadi "desktop": semua jendela SukiOS hidup di dalamnya.
 * Di produksi nanti, Activity ini yang akan mendaftarkan dirinya sebagai
 * CATEGORY_HOME (launcher default) — lihat PRD §7.1.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val st = remember { SukiState() }
            SukiRoot(st, this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Jaring pengaman: pastikan overlay taskbar ikut berhenti
        stopService(android.content.Intent(this, OverlayTaskbarService::class.java))
    }
}
