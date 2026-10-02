package app.sukios

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// ============================================================================
// Jendela Aplikasi: semua aplikasi terpasang. Ketuk baris = buka sebagai jendela; tekan lama atau tombol
// titik tiga = menu aksi. Pin menyematkan ke desktop dan taskbar.
// ============================================================================

@Composable
fun AppsContent(app: SukiApp) {
    var q by remember { mutableStateOf("") }
    val allApps by app.index.apps.collectAsState()
    val pinnedCsv by app.prefs.pinned.collectAsState()
    val apps = remember(allApps, q) { filterApps(allApps, q, 300) }
    val pinned = remember(pinnedCsv) { pinnedCsv.split(",").filter { it.isNotBlank() }.toSet() }
    val scope = rememberCoroutineScope()

    WinPanel(
        "Aplikasi", "${allApps.size} aplikasi terpasang",
        actions = {
            IconBtn(GlyphKind.REFRESH, "Muat ulang daftar", size = 34) {
                scope.launch { app.index.load() }
                SukiRuntime.say("Menyegarkan daftar aplikasi...")
            }
        },
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)) {
            SearchField(q, { q = it }, "Cari aplikasi atau nama paket", Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            LazyColumn(Modifier.weight(1f)) {
                items(apps, key = { it.pkg + "/" + it.activity }) { e ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .tap(radius = RADIUS_MD, label = "Buka ${e.label}", onLongPress = { SukiRuntime.menuApp = e }) { launchApp(app, e) }
                            .padding(horizontal = 6.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppIcon(e, 36)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Txt(e.label, 13, SText, FontWeight.Medium)
                            Txt(e.pkg + if (e.system) "  -  sistem" else "", 11, SFaint)
                        }
                        IconBtn(GlyphKind.PIN, if (e.pkg in pinned) "Lepas sematan ${e.label}" else "Sematkan ${e.label}", size = 36, active = e.pkg in pinned) {
                            app.prefs.togglePinned(e.pkg)
                        }
                        IconBtn(GlyphKind.MORE, "Menu ${e.label}", size = 36) { SukiRuntime.menuApp = e }
                    }
                }
            }
        }
    }
}
