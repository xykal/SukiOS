package app.sukios

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Taskbar SukiOS.
 *
 * Isi dari kiri ke kanan:
 *  - tombol mulai (membuka start menu)
 *  - semua jendela internal yang terbuka (klik = fokus, klik lagi = kecilkan)
 *  - indikator Shizuku dan saklar cepat
 *  - jam dan baterai
 */
@Composable
fun Taskbar(app: SukiApp, size: IntSize) {
    val accent = accentById(SukiRuntime.accentId)
    val wins = app.wins

    Row(
        Modifier
            .fillMaxWidth()
            .height(TASKBAR_DP.dp)
            .background(SChrome)
            .border(1.dp, SLine)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (SukiRuntime.startOpen) SOverlay else Color.Transparent)
                .clickable {
                    val wasOpen = SukiRuntime.startOpen
                    SukiRuntime.closePanels()
                    SukiRuntime.startOpen = !wasOpen
                },
            contentAlignment = Alignment.Center,
        ) { Glyph(GlyphKind.APPS, 19, accent) }

        Spacer(Modifier.width(10.dp))
        Box(Modifier.width(1.dp).height(24.dp).background(SLine))
        Spacer(Modifier.width(10.dp))

        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            wins.list.sortedBy { it.z }.forEach { w -> WinChip(app, w, size) }
        }

        TrayToggles(app)
        Spacer(Modifier.width(10.dp))
        Clock()
        Spacer(Modifier.width(8.dp))
        BatteryPill()
    }
}

@Composable
private fun WinChip(app: SukiApp, win: Win, size: IntSize) {
    val wins = app.wins
    val focused = wins.focusedId == win.id && !win.minimized
    val accent = accentById(SukiRuntime.accentId)
    Row(
        Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (focused) accent.copy(alpha = 0.20f) else SOverlay)
            .border(1.dp, if (focused) accent.copy(alpha = 0.45f) else SLine, RoundedCornerShape(8.dp))
            .clickable {
                if (win.minimized) {
                    wins.toggleMinimize(win.id)
                } else if (wins.focusedId == win.id) {
                    wins.toggleMinimize(win.id)
                } else {
                    wins.focus(win.id)
                }
            }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Glyph(glyphFor(win.kind), 13, if (focused) accent else SDim)
        Spacer(Modifier.width(7.dp))
        Txt(win.title, 11, if (focused) SText else SDim, FontWeight.Medium, maxLines = 1)
        if (win.minimized) {
            Spacer(Modifier.width(6.dp))
            Dot(SFaint, 5)
        }
    }
}

private fun glyphFor(kind: WinKind): GlyphKind = when (kind) {
    WinKind.SETTINGS -> GlyphKind.SETTINGS
    WinKind.LAB -> GlyphKind.LAB
    WinKind.TERMINAL -> GlyphKind.TERMINAL
    WinKind.APPS -> GlyphKind.APPS
    WinKind.ABOUT -> GlyphKind.INFO
    WinKind.DIAG -> GlyphKind.CHART
    WinKind.APP -> GlyphKind.WINDOW
}

@Composable
private fun TrayToggles(app: SukiApp) {
    val ctx = LocalContext.current
    val shell = SukiShell.state.value
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {

        // Indikator Shizuku: titik, bukan ikon ramai. Klik membuka panel pintasan.
        Box(
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { SukiRuntime.closePanels(); SukiRuntime.quickOpen = true },
            contentAlignment = Alignment.Center,
        ) {
            Dot(
                when {
                    shell.ready -> SSuccess
                    shell.binderAlive -> SWarning
                    else -> SFaint
                },
                7,
            )
        }

        TrayIcon(
            GlyphKind.OVERLAY,
            active = SukiRuntime.overlayBarOn,
            label = "Taskbar melayang",
        ) { toggleOverlayBar(app, ctx, !SukiRuntime.overlayBarOn) }

        TrayIcon(
            GlyphKind.ROTATE,
            active = app.prefs.lockLandscape.value,
            label = "Kunci mendatar",
        ) { app.prefs.setLockLandscape(!app.prefs.lockLandscape.value) }

        TrayIcon(
            GlyphKind.DESKTOP,
            active = app.prefs.fullDesktop.value,
            label = "Desktop penuh",
        ) { app.prefs.setFullDesktop(!app.prefs.fullDesktop.value) }

        Box(
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (SukiRuntime.quickOpen) SOverlay else Color.Transparent)
                .clickable { SukiRuntime.closePanels(); SukiRuntime.quickOpen = true },
            contentAlignment = Alignment.Center,
        ) { Glyph(GlyphKind.SETTINGS, 16, SDim) }
    }
}

@Composable
private fun TrayIcon(glyph: GlyphKind, active: Boolean, label: String, onClick: () -> Unit) {
    val accent = accentById(SukiRuntime.accentId)
    Box(
        Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) accent.copy(alpha = 0.20f) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) { Glyph(glyph, 16, if (active) accent else SDim) }
}

@Composable
private fun Clock() {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val fmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Txt(fmt.format(Date(now)), 12, SDim, FontWeight.Medium)
}

@Composable
private fun BatteryPill() {
    val ctx = LocalContext.current
    val level by produceState(initialValue = -1) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, i: Intent?) {
                value = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            }
        }
        runCatching {
            ctx.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.let {
                value = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            }
        }
        awaitDispose { runCatching { ctx.unregisterReceiver(receiver) } }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        val w = 26
        val fill = ((level.coerceIn(0, 100) / 100f) * (w - 4)).coerceAtLeast(1f)
        Box(
            Modifier
                .size(w.dp, 13.dp)
                .border(1.dp, SFaint, RoundedCornerShape(4.dp))
                .padding(1.dp),
        ) {
            Box(
                Modifier
                    .width(fill.dp)
                    .height(9.dp)
                    .background(if (level in 0..20) SDanger else SSuccess, RoundedCornerShape(2.dp)),
            )
        }
        Spacer(Modifier.width(6.dp))
        Txt(if (level < 0) "--%" else "$level%", 11, SDim)
    }
}
