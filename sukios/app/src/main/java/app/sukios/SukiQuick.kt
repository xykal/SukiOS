package app.sukios

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

// ============================================================================
// Panel pintasan: kaca melayang di atas taskbar, sisi kanan.
// Ubin cepat, volume, status jendela, dan jalan pintas ke Setelan dan Diagnostik.
// ============================================================================

@Composable
fun QuickPanel(app: SukiApp) {
    val ctx = LocalContext.current
    val prefs = app.prefs
    val fullDesktop by prefs.fullDesktop.collectAsState()
    val shell by SukiShell.state.collectAsState()
    val auto by SukiAuto.state.collectAsState()
    val probeRaw by prefs.probe.collectAsState()
    val battery by rememberBattery()
    val status = WindowStatusLogic.of(
        installed = shell.installed, alive = shell.binderAlive, granted = shell.granted, bound = shell.serviceBound,
        coreOn = auto.coreOn, setupRunning = auto.running, probe = WindowStatusLogic.parseProbe(probeRaw),
    )

    BoxWithConstraints(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { SukiRuntime.quickOpen = false } })
        Glass(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = (TASKBAR_DP + 6).dp)
                .width(minOf(372.dp, maxWidth - 20.dp))
                .pointerInput(Unit) { detectTapGestures { } },
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Txt(status.label, 14, SText, FontWeight.SemiBold)
                        Txt(status.detail, 11, SDim, maxLines = 2)
                    }
                    BatteryMeter(battery.first, battery.second)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickTile(GlyphKind.WIFI, "Wi-Fi", false, Modifier.weight(1f)) {
                        openSystem(ctx, Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                    }
                    QuickTile(GlyphKind.BLUETOOTH, "Bluetooth", false, Modifier.weight(1f)) {
                        openSystem(ctx, Settings.ACTION_BLUETOOTH_SETTINGS)
                    }
                    QuickTile(GlyphKind.FULLSCREEN, "Layar penuh", fullDesktop, Modifier.weight(1f)) {
                        prefs.setFullDesktop(!fullDesktop)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickTile(GlyphKind.OVERLAY, "Bar melayang", SukiRuntime.overlayBarOn, Modifier.weight(1f)) {
                        toggleOverlayBar(app, ctx, !SukiRuntime.overlayBarOn)
                    }
                    QuickTile(GlyphKind.SHIELD_CHECK, "Jendela", status.tone == Tone.OK, Modifier.weight(1f)) {
                        SukiRuntime.closePanels()
                        SukiRuntime.setupOpen = true
                    }
                    QuickTile(GlyphKind.SETTINGS, "Setelan HP", false, Modifier.weight(1f)) {
                        openSystem(ctx, Settings.ACTION_SETTINGS)
                    }
                }
                Spacer(Modifier.height(12.dp))
                VolumeRow()
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BtnGhost("Setelan SukiOS", icon = GlyphKind.SETTINGS, modifier = Modifier.weight(1f)) {
                        openInternal(app, WinKind.SETTINGS)
                    }
                    BtnGhost("Diagnostik", icon = GlyphKind.CHART, modifier = Modifier.weight(1f)) {
                        openInternal(app, WinKind.DIAG)
                    }
                }
            }
        }
    }
}

private fun openSystem(ctx: Context, action: String) {
    runCatching { ctx.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

@Composable
private fun QuickTile(glyph: GlyphKind, label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val ac = accentNow()
    val shape = RoundedCornerShape(RADIUS_MD.dp)
    Column(
        modifier
            .tap(radius = RADIUS_MD, label = label, onClick = onClick)
            .height(64.dp)
            .background(
                if (active) Brush.linearGradient(listOf(ac.main.copy(alpha = 0.34f), ac.second.copy(alpha = 0.18f)))
                else SolidColor(Color(0x12FFFFFF)),
                shape,
            )
            .border(1.dp, if (active) ac.main.copy(alpha = 0.55f) else SLineSoft, shape)
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Glyph(glyph, 20, if (active) SText else SDim)
        Txt(label, 12, if (active) SText else SDim, FontWeight.Medium)
    }
}

@Composable
private fun VolumeRow() {
    val ctx = LocalContext.current
    val audio = remember { ctx.getSystemService(AudioManager::class.java) }
    val max = remember { audio?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 0 }
    var level by remember { mutableStateOf(if (max > 0) (audio?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0) / max.toFloat() else 0f) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Glyph(GlyphKind.VOLUME, 18, SDim)
        Spacer(Modifier.width(10.dp))
        SukiSlider(
            value = level,
            onChange = { v ->
                level = v
                if (audio != null && max > 0) runCatching { audio.setStreamVolume(AudioManager.STREAM_MUSIC, (v * max).roundToInt(), 0) }
            },
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(10.dp))
        Txt("${(level * 100).roundToInt()}%", 11, SDim, modifier = Modifier.width(34.dp), align = TextAlign.End)
    }
}
