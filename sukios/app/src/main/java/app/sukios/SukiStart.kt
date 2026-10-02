package app.sukios

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ============================================================================
// Start menu: kaca melayang di atas taskbar. Pencarian, aplikasi tersemat, semua aplikasi.
// Ketuk = buka sebagai jendela; tekan lama = menu aksi (sematkan, info, hentikan, copot).
// ============================================================================

private const val START_W = 560
private const val START_MAX_H = 340
private const val COLS = 6

@Composable
fun StartMenu(app: SukiApp) {
    var q by remember { mutableStateOf("") }
    val allApps by app.index.apps.collectAsState()
    val loading by app.index.loading.collectAsState()
    val pinnedCsv by app.prefs.pinned.collectAsState()
    val results = remember(allApps, q) { filterApps(allApps, q, 200) }
    val pinned = remember(pinnedCsv, allApps) {
        val byPkg = allApps.associateBy { it.pkg }
        pinnedCsv.split(",").filter { it.isNotBlank() }.mapNotNull { byPkg[it] }
    }
    val searching = q.isNotBlank()

    BoxWithConstraints(Modifier.fillMaxSize().imePadding()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(SScrim)
                .pointerInput(Unit) { detectTapGestures { SukiRuntime.startOpen = false } },
        )
        val h = minOf(START_MAX_H.dp, maxHeight - (TASKBAR_DP + 14).dp)
        Glass(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = (TASKBAR_DP + 6).dp)
                .width(minOf(START_W.dp, maxWidth - 20.dp))
                .height(h)
                .pointerInput(Unit) { detectTapGestures { } },
        ) {
            Column(Modifier.fillMaxSize().padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SearchField(q, { q = it }, "Cari aplikasi", Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    IconBtn(GlyphKind.SETTINGS, "Setelan SukiOS") { openInternal(app, WinKind.SETTINGS) }
                }
                Spacer(Modifier.height(8.dp))
                when {
                    loading -> Txt("Memuat daftar aplikasi...", 12, SDim)
                    results.isEmpty() -> Txt("Tidak ada aplikasi yang cocok dengan \"$q\".", 12, SDim, maxLines = 2)
                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(COLS),
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (!searching && pinned.isNotEmpty()) {
                            item(key = "h-pinned", span = { GridItemSpan(maxLineSpan) }) { Heading("Disematkan", "${pinned.size}") }
                            items(pinned, key = { "p:" + it.pkg }) { e -> Cell(app, e) }
                        }
                        item(key = "h-all", span = { GridItemSpan(maxLineSpan) }) {
                            Heading(if (searching) "Hasil pencarian" else "Semua aplikasi", "${results.size}")
                        }
                        items(results, key = { "a:" + it.pkg + "/" + it.activity }) { e -> Cell(app, e) }
                    }
                }
                Spacer(Modifier.height(6.dp))
                HLine()
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Txt("SukiOS ${app.diag.appVersion()}", 11, SFaint, modifier = Modifier.weight(1f))
                    IconBtn(GlyphKind.SHIELD, "Persiapan jendela", size = 36) { SukiRuntime.closePanels(); SukiRuntime.setupOpen = true }
                    IconBtn(GlyphKind.CHART, "Diagnostik", size = 36) { openInternal(app, WinKind.DIAG) }
                    IconBtn(GlyphKind.INFO, "Tentang SukiOS", size = 36) { openInternal(app, WinKind.ABOUT) }
                }
            }
        }
    }
}

@Composable
private fun Heading(text: String, count: String) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Label(text)
        Spacer(Modifier.weight(1f))
        Txt(count, 11, SFaint)
    }
}

@Composable
private fun Cell(app: SukiApp, e: AppEntry) {
    Column(
        Modifier
            .fillMaxWidth()
            .tap(radius = RADIUS_MD, label = e.label, onLongPress = { SukiRuntime.menuApp = e }) { launchApp(app, e) }
            .padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppIcon(e, 40)
        Spacer(Modifier.height(4.dp))
        Txt(e.label, 11, SText, FontWeight.Medium, maxLines = 2, align = TextAlign.Center, modifier = Modifier.height(30.dp))
    }
}
