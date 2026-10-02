package app.sukios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Jendela Diagnostik: laporan mentah, siap disalin atau dibagikan.

@Composable
fun DiagContent(app: SukiApp) {
    val ctx = LocalContext.current
    var tick by remember { mutableStateOf(0) }
    // buildReport memanggil SukiShell (binder + proses): tidak boleh jalan di thread utama.
    val report by produceState("Menyusun laporan...", tick) {
        value = "Menyusun laporan..."
        value = withContext(Dispatchers.IO) { app.diag.buildReport() }
    }
    WinPanel(
        "Diagnostik", "laporan mentah, siap dibagikan",
        actions = {
            IconBtn(GlyphKind.REFRESH, "Periksa ulang", size = 34) {
                SukiShell.refresh()
                tick++
            }
        },
    ) {
        ScrollArea {
            ResultBox(report, tone = SDim, maxHeight = 460)
            ColSpacer(10)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Btn("Salin", icon = GlyphKind.COPY) {
                    copyText(ctx, report)
                    SukiRuntime.say("Laporan disalin.", Tone.OK)
                }
                BtnGhost("Bagikan") { shareText(ctx, report) }
            }
        }
    }
}
