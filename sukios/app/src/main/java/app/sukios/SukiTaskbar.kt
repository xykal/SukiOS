package app.sukios

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// ============================================================================
// Taskbar Aurora: bar kaca melayang di bawah layar.
//   [logo] | jendela SukiOS yang terbuka | aplikasi tersemat + aplikasi yang sedang berjalan sebagai jendela | tray
// Penanda di bawah ikon: pil bergradien = jendela aktif, pil kecil = terbuka, titik = diperkecil.
// ============================================================================

private enum class BarState { NONE, OPEN, FOCUSED, MINIMIZED }

@Composable
fun Taskbar(app: SukiApp) {
    val wins = app.wins
    val ext by SukiTasks.windows.collectAsState()
    val apps by app.index.apps.collectAsState()
    val pinnedCsv by app.prefs.pinned.collectAsState()
    val pinned = remember(pinnedCsv, apps) {
        val byPkg = apps.associateBy { it.pkg }
        pinnedCsv.split(",").filter { it.isNotBlank() }.mapNotNull { byPkg[it] }.take(8)
    }
    val pinnedPkgs = remember(pinned) { pinned.map { it.pkg }.toSet() }
    val loose = remember(ext, apps, pinnedPkgs) {
        val byPkg = apps.associateBy { it.pkg }
        ext.filter { it.pkg !in pinnedPkgs }.mapNotNull { byPkg[it.pkg] }.distinctBy { it.pkg }
    }
    val topPkg = ext.firstOrNull()?.pkg

    Glass(
        Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, bottom = 6.dp).height(TASKBAR_BAR_DP.dp),
        radius = RADIUS_LG, tint = SSheet, alpha = 0.76f, lift = 18,
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            BarItem(
                "Menu mulai",
                if (SukiRuntime.startOpen) BarState.FOCUSED else BarState.NONE,
                onClick = {
                    val wasOpen = SukiRuntime.startOpen
                    SukiRuntime.closePanels()
                    SukiRuntime.startOpen = !wasOpen
                },
            ) { LogoMark(30) }

            Gap()

            Row(
                Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                wins.list.sortedBy { it.id }.forEach { w -> WinItem(app, w) }
                if (wins.list.isNotEmpty() && (pinned.isNotEmpty() || loose.isNotEmpty())) Gap()
                pinned.forEach { e ->
                    val running = ext.any { it.pkg == e.pkg }
                    AppItem(app, e, if (!running) BarState.NONE else if (e.pkg == topPkg) BarState.FOCUSED else BarState.OPEN, running)
                }
                loose.forEach { e -> AppItem(app, e, if (e.pkg == topPkg) BarState.FOCUSED else BarState.OPEN, true) }
            }

            Tray(app)
        }
    }
}

@Composable
private fun Gap() {
    Box(Modifier.padding(horizontal = 6.dp).width(1.dp).height(24.dp).background(SLine))
}

@Composable
private fun WinItem(app: SukiApp, win: Win) {
    val wins = app.wins
    val focused = wins.focusedId == win.id && !win.minimized
    BarItem(
        win.title,
        if (win.minimized) BarState.MINIMIZED else if (focused) BarState.FOCUSED else BarState.OPEN,
        onClick = {
            if (win.minimized || wins.focusedId == win.id) wins.toggleMinimize(win.id) else wins.focus(win.id)
        },
    ) { IconTile(glyphFor(win.kind), kindTone(win.kind), 32) }
}

@Composable
private fun AppItem(app: SukiApp, entry: AppEntry, state: BarState, running: Boolean) {
    BarItem(
        entry.label, state,
        onLongPress = { SukiRuntime.menuApp = entry },
        onClick = {
            // Aplikasi yang sudah berjalan sebagai jendela cukup dibawa ke depan.
            if (running) app.index.launch(entry) else launchApp(app, entry)
        },
    ) { AppIcon(entry, 32) }
}

@Composable
private fun BarItem(
    label: String,
    state: BarState,
    onLongPress: (() -> Unit)? = null,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    val ac = accentNow()
    Box(
        Modifier
            .size(42.dp)
            .tap(radius = RADIUS_MD, label = label, onLongPress = onLongPress, onClick = onClick)
            .then(if (state == BarState.FOCUSED) Modifier.background(Color(0x1AFFFFFF)) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        icon()
        if (state != BarState.NONE) {
            val w = when (state) {
                BarState.FOCUSED -> 16.dp
                BarState.OPEN -> 6.dp
                else -> 4.dp
            }
            val fill: Brush = when (state) {
                BarState.FOCUSED -> Brush.horizontalGradient(listOf(ac.main, ac.second))
                BarState.OPEN -> SolidColor(SDim)
                else -> SolidColor(SFaint)
            }
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
                    .width(w)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(fill),
            )
        }
    }
}
