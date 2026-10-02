package app.sukios

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================================================
// Menu aksi aplikasi (tekan lama) dan kartu "jendela butuh akses lanjutan".
// ============================================================================

@Composable
private fun CenterSheet(onDismiss: () -> Unit, width: Int, content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(SScrim)
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
        contentAlignment = Alignment.Center,
    ) {
        Glass(Modifier.width(width.dp).pointerInput(Unit) { detectTapGestures { } }) { content() }
    }
}

@Composable
fun AppMenu(app: SukiApp, e: AppEntry) {
    val pinnedCsv by app.prefs.pinned.collectAsState()
    val ext by SukiTasks.windows.collectAsState()
    val scope = rememberCoroutineScope()
    val pinned = pinnedCsv.split(",").contains(e.pkg)
    val running = ext.any { it.pkg == e.pkg }

    CenterSheet(onDismiss = { SukiRuntime.menuApp = null }, width = 400) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(e, 44)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Txt(e.label, 15, SText, FontWeight.SemiBold)
                    Txt(e.pkg, 11, SFaint)
                }
            }
            Spacer(Modifier.height(14.dp))
            Btn("Buka sebagai jendela", icon = GlyphKind.EXTERNAL, modifier = Modifier.fillMaxWidth()) {
                SukiRuntime.menuApp = null
                launchApp(app, e)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BtnGhost("Layar penuh", icon = GlyphKind.FULLSCREEN, modifier = Modifier.weight(1f)) {
                    SukiRuntime.menuApp = null
                    app.prefs.pushRecent(e.pkg)
                    app.index.launch(e)
                }
                BtnGhost(if (pinned) "Lepas sematan" else "Sematkan", icon = GlyphKind.PIN, modifier = Modifier.weight(1f)) {
                    app.prefs.togglePinned(e.pkg)
                    SukiRuntime.menuApp = null
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BtnGhost("Info aplikasi", icon = GlyphKind.INFO, modifier = Modifier.weight(1f)) {
                    SukiRuntime.menuApp = null
                    app.index.openInfo(e)
                }
                if (running) {
                    Btn("Hentikan", primary = false, danger = true, icon = GlyphKind.CLOSE, modifier = Modifier.weight(1f)) {
                        SukiRuntime.menuApp = null
                        scope.launch {
                            val r = SukiShell.io { forceStop(e.pkg) }
                            SukiTasks.refresh(app.packageName)
                            SukiRuntime.say(if (r.ok) "${e.label} dihentikan." else "Gagal menghentikan: ${r.short(80)}", if (r.ok) Tone.OK else Tone.ERR)
                        }
                    }
                } else {
                    Btn("Copot pemasangan", primary = false, danger = true, icon = GlyphKind.TRASH, modifier = Modifier.weight(1f)) {
                        SukiRuntime.menuApp = null
                        app.index.uninstall(e)
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedCard(app: SukiApp, e: AppEntry) {
    val shell by SukiShell.state.collectAsState()
    CenterSheet(onDismiss = { SukiRuntime.blockedApp = null }, width = 440) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(GlyphKind.SHIELD, SWarning, 42)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Txt("Jendela butuh akses lanjutan", 16, SText, FontWeight.Bold, font = SukiBrand)
                    Txt("${e.label} dibuka sebagai jendela lewat Shizuku.", 12, SDim, maxLines = 2)
                }
            }
            Spacer(Modifier.height(10.dp))
            Txt(shell.note, 12, SWarning, maxLines = 3)
            Spacer(Modifier.height(14.dp))
            Btn("Siapkan sekarang", icon = GlyphKind.SHIELD_CHECK, modifier = Modifier.fillMaxWidth()) {
                SukiRuntime.blockedApp = null
                SukiRuntime.setupOpen = true
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BtnGhost("Layar penuh sekali ini", modifier = Modifier.weight(1f)) {
                    SukiRuntime.blockedApp = null
                    app.index.launch(e)
                }
                BtnGhost("Batal", modifier = Modifier.weight(1f)) { SukiRuntime.blockedApp = null }
            }
        }
    }
}

/**
 * Aplikasi sudah berjalan layar penuh. Android tidak memindahkan tugas yang sudah ada ke mode jendela,
 * jadi satu-satunya jalan adalah menghentikannya dulu. Tidak pernah dilakukan tanpa persetujuan.
 */
@Composable
fun RestartCard(app: SukiApp, e: AppEntry) {
    CenterSheet(onDismiss = { SukiRuntime.restartApp = null }, width = 440) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(e, 44)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Txt("${e.label} sudah berjalan layar penuh", 15, SText, FontWeight.Bold, font = SukiBrand, maxLines = 2)
                    Txt("Android tidak memindahkan aplikasi yang sudah terbuka ke jendela.", 12, SDim, maxLines = 3)
                }
            }
            Spacer(Modifier.height(12.dp))
            Txt("Hentikan dulu, lalu buka lagi sebagai jendela. Data yang belum disimpan di aplikasi itu bisa hilang.", 12, SWarning, maxLines = 3)
            Spacer(Modifier.height(14.dp))
            Btn("Hentikan lalu buka sebagai jendela", icon = GlyphKind.REFRESH, modifier = Modifier.fillMaxWidth()) {
                SukiRuntime.restartApp = null
                app.scope.launch {
                    SukiShell.io { forceStop(e.pkg) }
                    delay(700)
                    launchApp(app, e)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BtnGhost("Bawa ke depan", modifier = Modifier.weight(1f)) {
                    SukiRuntime.restartApp = null
                    app.index.launch(e)
                }
                BtnGhost("Batal", modifier = Modifier.weight(1f)) { SukiRuntime.restartApp = null }
            }
        }
    }
}
