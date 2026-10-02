package app.sukios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ============================================================================
// Laboratorium: uji nyata di perangkat ini. Fakta perangkat, tombol persiapan dan uji jendela, perintah
// SukiShell yang diizinkan (ShellPolicy), kotak hasil apa adanya, dan ekspor laporan.
// ============================================================================

@Composable
fun LabContent(app: SukiApp) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val shell by SukiShell.state.collectAsState()
    val auto by SukiAuto.state.collectAsState()
    var output by remember { mutableStateOf("") }
    val facts = remember(SukiRuntime.isDefaultLauncher, SukiRuntime.deviceClass, auto.coreOn) { app.diag.deviceFacts() }

    // Perintah shell memanggil binder dan membuat proses: dijalankan di thread IO supaya layar tidak menunggu.
    fun runShell(limit: Int = 6000, block: SukiShell.() -> SukiResult) {
        output = "menjalankan..."
        scope.launch { output = SukiShell.io(block).full().take(limit) }
    }

    WinPanel("Laboratorium", "uji nyata di perangkat ini, hasil apa adanya") {
        ScrollArea {
            Label("Perangkat")
            ColSpacer(4)
            facts.forEach { KeyValue(it.key, it.value) }

            ColSpacer(16)
            Label("Jendela aplikasi pihak ketiga")
            ColSpacer(6)
            Txt(
                "Persiapan menyalakan setelan jendela lewat Shizuku. Uji jendela membuka ProbeActivity milik SukiOS sebagai " +
                    "jendela, lalu mengukurnya dari dalam dan dari sistem.",
                11, SFaint, maxLines = 4,
            )
            ColSpacer(8)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Btn("Persiapan", icon = GlyphKind.SHIELD_CHECK, enabled = shell.ready && !auto.running) {
                    scope.launch { SukiAuto.ensure(app, force = true) }
                }
                BtnGhost("Uji jendela", icon = GlyphKind.PLAY, enabled = shell.ready && !auto.probing) {
                    output = "menguji..."
                    scope.launch {
                        val r = SukiAuto.probe(app)
                        output = "hasil=${r.verdict}\n${r.note}\n\n${SukiAuto.lastDumpExcerpt}"
                    }
                }
            }

            ColSpacer(16)
            Label("Perintah SukiShell")
            ColSpacer(6)
            if (!shell.ready) {
                Txt("SukiShell belum siap: ${shell.note}. Perintah akan melaporkan kegagalan tanpa menyembunyikannya.", 11, SWarning, maxLines = 3)
                ColSpacer(6)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BtnGhost("Identitas") { runShell { identity() } }
                BtnGhost("Baca setelan jendela") {
                    runShell {
                        val lines = SetupPlan.CORE_KEYS.map { k ->
                            val r = runChecked("settings", "get", "global", k)
                            "$k = " + if (r.ok) r.out.trim() else "gagal (${r.code}): ${r.short(80)}"
                        }
                        SukiResult(if (lines.any { it.contains("gagal") }) 1 else 0, lines.joinToString("\n"), "")
                    }
                }
            }
            ColSpacer(6)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BtnGhost("Daftar display") { runShell(limit = 4000) { displays() } }
                BtnGhost("Ukuran layar") { runShell { windowSize() } }
                BtnGhost("Ringkas perangkat") { runShell { deviceSummary() } }
            }
            ColSpacer(10)
            ResultBox(output, tone = if (output.startsWith("kode=0") || output.startsWith("hasil=OK")) SSuccess else SDim, maxHeight = 240)
            ColSpacer(10)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BtnGhost("Putuskan SukiShell") {
                    SukiShell.unbind()
                    output = "SukiShell diputus. Binder diikat ulang saat dibutuhkan."
                }
                BtnGhost("Salin laporan", icon = GlyphKind.COPY) {
                    scope.launch {
                        copyText(ctx, withContext(Dispatchers.IO) { app.diag.buildReport() })
                        SukiRuntime.say("Laporan diagnostik disalin.", Tone.OK)
                    }
                }
                BtnGhost("Bagikan") { scope.launch { shareText(ctx, withContext(Dispatchers.IO) { app.diag.buildReport() }) } }
            }
        }
    }
}
