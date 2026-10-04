package app.sukios

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SukiHome — wajah SukiOS.
 *
 * Susunan lapisan, dari belakang ke depan:
 *   wallpaper - ikon desktop - jendela - taskbar - start menu / panel pintasan - menu aplikasi - pesan - persiapan
 *
 * Isi jendela SukiOS digambar sendiri (Compose). Aplikasi pihak ketiga dibuka sebagai jendela mengambang
 * oleh Android atas perintah SukiOS lewat Shizuku (SukiWindowing); mereka tampil DI ATAS lapisan ini.
 */
@Composable
fun SukiHome(app: SukiApp, onApplyDesktop: (Boolean) -> Unit) {
    val prefs = app.prefs
    val setupDone by prefs.setupDone.collectAsState()
    val shell by SukiShell.state.collectAsState()

    // Mesin jendela bekerja dalam piksel; kepadatan layar yang benar datang dari sini
    // (bukan perkiraan dari tinggi layar) dan ikut berubah saat density berubah.
    val density = LocalDensity.current.density
    SideEffect { app.wins.density = density }

    LaunchedEffect(Unit) {
        launch { prefs.fullDesktop.collect { onApplyDesktop(it) } }
        launch { prefs.accent.collect { SukiRuntime.accentId = it } }
        launch { prefs.wallpaper.collect { SukiRuntime.wallpaperId = it } }
        launch { prefs.wallMotion.collect { SukiRuntime.wallMotion = it } }
        launch { prefs.overlayBar.collect { on -> SukiRuntime.overlayBarOn = on && app.index.hasOverlay() } }
    }

    LaunchedEffect(Unit) { app.index.load() }
    LaunchedEffect(Unit) { SukiTasks.poll(app) }

    // Persiapan otomatis hidup di lingkup proses (bukan lingkup composable): kalau status Shizuku berubah
    // di tengah jalan, langkahnya tidak terpotong.
    LaunchedEffect(shell.binderAlive, shell.granted, shell.serviceBound) {
        app.scope.launch { SukiAuto.onShellChanged(app, SukiShell.state.value) }
    }

    LaunchedEffect(SukiRuntime.toast) {
        if (SukiRuntime.toast != null) {
            delay(TOAST_MS)
            SukiRuntime.toast = null
        }
    }

    val overlayOpen = SukiRuntime.startOpen || SukiRuntime.quickOpen || SukiRuntime.menuApp != null ||
        SukiRuntime.blockedApp != null || SukiRuntime.restartApp != null || SukiRuntime.setupOpen
    BackHandler(enabled = overlayOpen || app.wins.count > 0) {
        when {
            SukiRuntime.restartApp != null -> SukiRuntime.restartApp = null
            SukiRuntime.blockedApp != null -> SukiRuntime.blockedApp = null
            SukiRuntime.menuApp != null -> SukiRuntime.menuApp = null
            SukiRuntime.setupOpen -> SukiRuntime.setupOpen = false
            SukiRuntime.startOpen || SukiRuntime.quickOpen -> SukiRuntime.closePanels()
            else -> {
                val id = app.wins.focusedId
                if (id > 0) app.wins.close(id)
            }
        }
    }

    val systemWallpaper = isSystemLiveWallpaper(SukiRuntime.wallpaperId)
    Box(
        Modifier.fillMaxSize().background(if (systemWallpaper) SBg.copy(alpha = 0f) else SBg).onSizeChanged {
            SukiRuntime.screenW = it.width.toFloat()
            SukiRuntime.screenH = it.height.toFloat()
        },
    ) {
        Wallpaper(SukiRuntime.wallpaperId, Modifier.fillMaxSize(), motion = SukiRuntime.wallMotion && !SukiRuntime.goMode)
        DesktopLayer(app)
        WindowLayer(app)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) { Taskbar(app) }
        Appear(visible = SukiRuntime.startOpen) { StartMenu(app) }
        Appear(visible = SukiRuntime.quickOpen) { QuickPanel(app) }
        SukiRuntime.menuApp?.let { AppMenu(app, it) }
        SukiRuntime.blockedApp?.let { BlockedCard(app, it) }
        SukiRuntime.restartApp?.let { RestartCard(app, it) }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) { ToastLayer() }
        if (!setupDone || SukiRuntime.setupOpen) SetupLayer(app)
    }
}

/** Panel yang muncul dengan memudar dan sedikit membesar; keluar dengan memudar. */
@Composable
private fun Appear(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(MOTION_FAST)) + scaleIn(tween(MOTION_FAST), initialScale = 0.96f),
        exit = fadeOut(tween(MOTION_INSTANT)),
    ) { content() }
}

@Composable
private fun WindowLayer(app: SukiApp) {
    Box(Modifier.fillMaxSize()) {
        SnapPreview()
        app.wins.list.sortedBy { it.z }.forEach { w ->
            key(w.id) {
                if (!w.minimized) WinFrame(app, w)
            }
        }
    }
}

@Composable
private fun SnapPreview() {
    val b = SukiRuntime.snapPreview ?: return
    val ac = accentNow()
    val shape = RoundedCornerShape(RADIUS_WIN.dp)
    Box(
        Modifier
            .offset { IntOffset(b.x.roundToInt(), b.y.roundToInt()) }
            .size(with(LocalDensity.current) { b.w.toDp() }, with(LocalDensity.current) { b.h.toDp() })
            .background(ac.main.copy(alpha = 0.16f), shape)
            .border(1.5.dp, ac.main.copy(alpha = 0.70f), shape),
    )
}

@Composable
private fun ToastLayer() {
    val t = SukiRuntime.toast ?: return
    val color = when (t.tone) {
        Tone.OK -> SSuccess
        Tone.WARN -> SWarning
        Tone.ERR -> SDanger
        Tone.INFO -> accentNow().main
    }
    val glyph = when (t.tone) {
        Tone.OK -> GlyphKind.OK_CIRCLE
        Tone.WARN -> GlyphKind.ALERT
        Tone.ERR -> GlyphKind.FAIL_CIRCLE
        Tone.INFO -> GlyphKind.INFO
    }
    Glass(
        Modifier.padding(bottom = (TASKBAR_DP + 8).dp).widthIn(max = 460.dp),
        radius = RADIUS_MD, lift = 16,
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Glyph(glyph, 18, color)
            Spacer(Modifier.width(10.dp))
            Txt(t.message, 12, SText, FontWeight.Medium, maxLines = 3)
        }
    }
}

// ---------------------------------------------------------------------------
// Aksi bersama — dipakai desktop, taskbar, start menu, dan isi jendela.
// ---------------------------------------------------------------------------

private const val TOAST_MS = 3_400L

fun openInternal(app: SukiApp, kind: WinKind) {
    val sw = SukiRuntime.screenW.coerceAtLeast(1f)
    val sh = SukiRuntime.screenH.coerceAtLeast(1f)
    val w = app.wins.open(kind, kind.title, sw, sh)
    if (w == null && app.wins.lastBlockedReason.isNotBlank()) SukiRuntime.say(app.wins.lastBlockedReason, Tone.WARN)
    SukiRuntime.closePanels()
}

/**
 * Buka aplikasi pihak ketiga. Jalurnya SELALU jendela (SukiWindowing). Bila Shizuku belum siap, peluncuran
 * ditahan dan pengguna ditawari persiapan, kecuali kebijakan "selalu jendela" dimatikan di Setelan.
 */
fun launchApp(app: SukiApp, entry: AppEntry) {
    SukiRuntime.closePanels()
    app.prefs.pushRecent(entry.pkg)
    app.scope.launch {
        val strict = app.prefs.strictWindows.value
        if (!SukiShell.state.value.ready && !strict) {
            val ok = app.index.launch(entry)
            SukiRuntime.say(
                if (ok) "${entry.label} dibuka layar penuh (Shizuku belum siap)." else "Gagal membuka ${entry.label}.",
                if (ok) Tone.WARN else Tone.ERR,
            )
            return@launch
        }
        val out = SukiWindowing.open(app, entry)
        when (out.kind) {
            OutcomeKind.BLOCKED -> SukiRuntime.blockedApp = entry
            OutcomeKind.RUNNING_FULLSCREEN -> SukiRuntime.restartApp = entry
            else -> SukiRuntime.say(out.message, out.tone)
        }
    }
}

fun toggleOverlayBar(app: SukiApp, ctx: Context, want: Boolean) {
    if (want && !app.index.hasOverlay()) {
        SukiRuntime.say("Beri izin \"tampil di atas aplikasi lain\" dulu.", Tone.WARN)
        app.index.openOverlaySettings()
        return
    }
    app.prefs.setOverlayBar(want)
    SukiOverlayBar.toggle(ctx, want)
    SukiRuntime.overlayBarOn = want
    SukiRuntime.say(if (want) "Taskbar melayang dinyalakan" else "Taskbar melayang dimatikan", Tone.OK)
}
