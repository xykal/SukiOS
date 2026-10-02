package app.sukios

import android.text.format.DateFormat
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
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

// ============================================================================
// Desktop: ikon di kiri (disusun per kolom, mengikuti tinggi layar), jam besar dan
// status jendela di kanan atas. Ikon bawaan SukiOS dan aplikasi yang disematkan atau
// baru dibuka. Tekan lama pada ikon aplikasi membuka menu aksinya.
// ============================================================================

private val CELL_W = 84.dp
private val CELL_H = 88.dp
private val WIDGET_W = 300.dp
private const val DESK_ICON = 46

private class Builtin(val kind: WinKind, val label: String)

private val BUILTINS = listOf(
    Builtin(WinKind.SETTINGS, "Setelan"),
    Builtin(WinKind.APPS, "Aplikasi"),
    Builtin(WinKind.TERMINAL, "Terminal"),
    Builtin(WinKind.LAB, "Laboratorium"),
)

/** Satu sel di desktop: ikon bawaan (kind != null) atau aplikasi pihak ketiga. */
private class DeskItem(val builtin: Builtin?, val entry: AppEntry?)

@Composable
fun DesktopLayer(app: SukiApp) {
    val prefs = app.prefs
    val apps by app.index.apps.collectAsState()
    val loading by app.index.loading.collectAsState()
    val pinnedCsv by prefs.pinned.collectAsState()
    val recentCsv by prefs.recent.collectAsState()
    val shell by SukiShell.state.collectAsState()
    val auto by SukiAuto.state.collectAsState()
    val probeRaw by prefs.probe.collectAsState()

    val order = remember(pinnedCsv, recentCsv) {
        (pinnedCsv.split(",") + recentCsv.split(",")).filter { it.isNotBlank() }.distinct()
    }
    val items = remember(apps, order) {
        val byPkg = apps.associateBy { it.pkg }
        BUILTINS.map { DeskItem(it, null) } + order.mapNotNull { byPkg[it] }.take(24).map { DeskItem(null, it) }
    }
    val status = WindowStatusLogic.of(
        installed = shell.installed, alive = shell.binderAlive, granted = shell.granted, bound = shell.serviceBound,
        coreOn = auto.coreOn, setupRunning = auto.running, probe = WindowStatusLogic.parseProbe(probeRaw),
    )

    BoxWithConstraints(Modifier.fillMaxSize().padding(bottom = TASKBAR_DP.dp)) {
        val rows = ((maxHeight - 20.dp) / CELL_H).toInt().coerceAtLeast(1)
        val cols = ((maxWidth - WIDGET_W - 24.dp) / CELL_W).toInt().coerceAtLeast(1)
        Row(Modifier.padding(start = 12.dp, top = 10.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            items.take(rows * cols).chunked(rows).forEach { col ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) { col.forEach { DeskTile(app, it) } }
            }
        }
        Column(
            Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 22.dp),
            horizontalAlignment = Alignment.End,
        ) {
            ClockWidget()
            Spacer(Modifier.height(10.dp))
            Box(Modifier.tap(radius = RADIUS_PILL, label = "Status jendela: ${status.label}") { SukiRuntime.setupOpen = true }) {
                StatusPill(status.label, toneColor(status.tone))
            }
            if (loading) {
                Spacer(Modifier.height(8.dp))
                Txt("Memuat daftar aplikasi...", 11, SDim, lift = true)
            }
        }
    }
}

@Composable
private fun DeskTile(app: SukiApp, item: DeskItem) {
    val b = item.builtin
    val e = item.entry
    val label = b?.label ?: e?.label ?: ""
    Column(
        Modifier
            .width(CELL_W)
            .tap(
                radius = RADIUS_MD,
                label = label,
                onLongPress = if (e != null) ({ SukiRuntime.menuApp = e }) else null,
                onClick = {
                    if (b != null) openInternal(app, b.kind) else if (e != null) launchApp(app, e)
                },
            )
            .padding(top = 6.dp, bottom = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (b != null) {
            IconTile(glyphFor(b.kind), kindTone(b.kind), DESK_ICON, lift = true)
        } else if (e != null) {
            AppIcon(e, DESK_ICON, lift = true)
        }
        Spacer(Modifier.height(4.dp))
        Txt(
            label, 11, SText, FontWeight.Medium, maxLines = 2, align = TextAlign.Center, lift = true,
            modifier = Modifier.height(30.dp).padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun ClockWidget() {
    val ctx = LocalContext.current
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(60_000L - now % 60_000L + 30L)
        }
    }
    val is24 = remember { DateFormat.is24HourFormat(ctx) }
    val timeFmt = remember(is24) { SimpleDateFormat(if (is24) "HH:mm" else "h:mm", Locale.getDefault()) }
    val dateFmt = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()) }
    Txt(
        timeFmt.format(Date(now)), 58, SText, FontWeight.Light,
        font = SukiClockFont, lift = true, spacing = -1.5f,
    )
    Txt(dateFmt.format(Date(now)), 14, SDim, FontWeight.Medium, lift = true)
}
