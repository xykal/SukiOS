package app.sukios

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SukiHome — wajah SukiOS.
 *
 * Susunan lapisan, dari belakang ke depan:
 *   wallpaper - ikon desktop - jendela - taskbar - start menu - panel pintasan - pesan
 *
 * Semua isi jendela digambar SukiOS sendiri (Compile), jadi tidak ada
 * ketergantungan pada tampilan sistem. Aplikasi pihak ketiga dibuka oleh
 * Android seperti biasa; kalau perangkat mengizinkan, mereka bisa mengambang.
 */
@Composable
fun SukiHome(
    app: SukiApp,
    onApplyDesktop: (Boolean) -> Unit,
    onApplyOrientation: (Boolean) -> Unit,
) {
    val prefs = app.prefs
    var size by remember { mutableStateOf(IntSize.Zero) }
    val setupDone by prefs.setupDone.collectAsState()

    // Mesin jendela bekerja dalam piksel; kepadatan layar yang benar datang dari sini
    // (bukan perkiraan dari tinggi layar) dan ikut berubah saat density berubah.
    val density = LocalDensity.current.density
    SideEffect { app.wins.density = density }

    LaunchedEffect(Unit) {
        launch { prefs.fullDesktop.collect { onApplyDesktop(it) } }
        launch { prefs.lockLandscape.collect { onApplyOrientation(it) } }
        launch { prefs.accent.collect { SukiRuntime.accentId = it } }
        launch { prefs.wallpaper.collect { SukiRuntime.wallpaperId = it } }
        launch { prefs.overlayBar.collect { on -> SukiRuntime.overlayBarOn = on && app.index.hasOverlay() } }
    }

    LaunchedEffect(Unit) { app.index.load() }

    LaunchedEffect(SukiRuntime.toast) {
        if (SukiRuntime.toast != null) {
            delay(2600)
            SukiRuntime.toast = null
        }
    }

    BackHandler(enabled = SukiRuntime.startOpen || SukiRuntime.quickOpen || app.wins.count > 0) {
        when {
            SukiRuntime.startOpen || SukiRuntime.quickOpen -> SukiRuntime.closePanels()
            else -> {
                val id = app.wins.focusedId
                if (id > 0) app.wins.close(id)
            }
        }
    }

    Box(Modifier.fillMaxSize().background(SBg).onSizeChanged {
        size = it
        SukiRuntime.screenW = it.width.toFloat()
        SukiRuntime.screenH = it.height.toFloat()
    }) {
        Wallpaper(SukiRuntime.wallpaperId, Modifier.fillMaxSize())
        DesktopLayer(app, size)
        WindowLayer(app)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) { Taskbar(app, size) }
        if (SukiRuntime.startOpen) StartMenu(app, size)
        if (SukiRuntime.quickOpen) QuickPanel(app, size)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) { ToastLayer() }
        if (!setupDone) SetupLayer(app)
    }
}

@Composable
private fun DesktopLayer(app: SukiApp, size: IntSize) {
    val prefs = app.prefs
    val apps by app.index.apps.collectAsState()
    val loading by app.index.loading.collectAsState()
    val pinnedCsv by prefs.pinned.collectAsState()
    val recentCsv by prefs.recent.collectAsState()

    val tiles = listOf(
        Triple("Setelan", GlyphKind.SETTINGS, WinKind.SETTINGS),
        Triple("Laboratorium", GlyphKind.LAB, WinKind.LAB),
        Triple("Terminal", GlyphKind.TERMINAL, WinKind.TERMINAL),
        Triple("Aplikasi", GlyphKind.APPS, WinKind.APPS),
    )

    val order = remember(pinnedCsv, recentCsv) {
        (pinnedCsv.split(",") + recentCsv.split(",")).filter { it.isNotBlank() }.distinct()
    }
    val desktopApps = remember(apps, order) {
        val byPkg = apps.associateBy { it.pkg }
        order.mapNotNull { byPkg[it] }.take(12)
    }

    Column(Modifier.padding(start = 22.dp, top = 20.dp).width(460.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            tiles.forEach { (label, glyph, kind) ->
                TileButton(label, glyph, active = app.wins.list.any { it.kind == kind && !it.minimized }) {
                    openInternal(app, kind)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        when {
            loading -> Txt("Memuat daftar aplikasi...", 11, SFaint)
            desktopApps.isEmpty() -> Txt(
                "Belum ada aplikasi tersemat. Buka jendela Aplikasi, lalu sematkan yang sering dipakai.",
                11, SFaint, maxLines = 3, modifier = Modifier.width(320.dp)
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                desktopApps.chunked(6).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { e -> AppTile(app, e) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TileButton(label: String, glyph: GlyphKind, active: Boolean, onClick: () -> Unit) {
    val accent = accentById(SukiRuntime.accentId)
    Column(
        Modifier.width(76.dp).clip(RoundedCornerShape(10.dp)).clickable { onClick() }.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(ICON_DP.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (active) accent.copy(alpha = 0.22f) else SOverlay)
                .border(1.dp, SLine, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) { Glyph(glyph, 20, if (active) accent else SDim) }
        Spacer(Modifier.height(6.dp))
        Txt(label, 10, SDim, maxLines = 1)
    }
}

@Composable
private fun AppTile(app: SukiApp, entry: AppEntry) {
    val accent = accentById(SukiRuntime.accentId)
    Column(
        Modifier.width(76.dp).clip(RoundedCornerShape(10.dp)).clickable { launchApp(app, entry) }.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val icon = entry.icon
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = entry.label,
                modifier = Modifier.size(ICON_DP.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit,
            )
        } else {
            Box(
                Modifier
                    .size(ICON_DP.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.18f))
                    .border(1.dp, SLine, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) { Txt(entry.label.take(1).uppercase(), 16, accent, FontWeight.SemiBold) }
        }
        Spacer(Modifier.height(6.dp))
        Txt(entry.label, 10, SDim, maxLines = 1)
    }
}

@Composable
private fun WindowLayer(app: SukiApp) {
    Box(Modifier.fillMaxSize()) {
        app.wins.list.sortedBy { it.z }.forEach { w ->
            key(w.id) {
                if (!w.minimized) WinFrame(app, w)
            }
        }
    }
}

@Composable
private fun ToastLayer() {
    val msg = SukiRuntime.toast ?: return
    Box(
        Modifier
            .padding(bottom = (TASKBAR_DP + 14).dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SOverlay)
            .border(1.dp, SLine, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Txt(msg, 11, SText, maxLines = 3, modifier = Modifier.width(360.dp))
    }
}

// ---------------------------------------------------------------------------
// Aksi bersama — dipakai desktop, taskbar, start menu, dan isi jendela.
// ---------------------------------------------------------------------------

fun openInternal(app: SukiApp, kind: WinKind) {
    val sw = SukiRuntime.screenW.coerceAtLeast(1f)
    val sh = SukiRuntime.screenH.coerceAtLeast(1f)
    val w = app.wins.open(kind, kind.title, sw, sh)
    if (w == null && app.wins.lastBlockedReason.isNotBlank()) SukiRuntime.say(app.wins.lastBlockedReason)
    SukiRuntime.closePanels()
}

fun launchApp(app: SukiApp, entry: AppEntry) {
    app.prefs.pushRecent(entry.pkg)
    val ok = app.index.launch(entry)
    SukiRuntime.say(if (ok) "Membuka ${entry.label}" else "Gagal membuka ${entry.label}")
    SukiRuntime.closePanels()
}

fun toggleOverlayBar(app: SukiApp, ctx: android.content.Context, want: Boolean) {
    if (want && !app.index.hasOverlay()) {
        SukiRuntime.say("Beri izin \"tampil di atas aplikasi lain\" dulu.")
        app.index.openOverlaySettings()
        return
    }
    app.prefs.setOverlayBar(want)
    SukiOverlayBar.toggle(ctx, want)
    SukiRuntime.overlayBarOn = want
    SukiRuntime.say(if (want) "Taskbar melayang dinyalakan" else "Taskbar melayang dimatikan")
}
