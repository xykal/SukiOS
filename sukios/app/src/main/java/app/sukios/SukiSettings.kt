package app.sukios

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// ============================================================================
// Setelan SukiOS: sidebar kiri, isi kanan. Tampilan, Jendela (Shizuku + uji nyata), Desktop, Launcher.
// ============================================================================

private enum class SettingsTab(val label: String, val glyph: GlyphKind) {
    LOOK("Tampilan", GlyphKind.SPARK),
    WINDOWS("Jendela", GlyphKind.WINDOW),
    DESKTOP("Desktop", GlyphKind.DESKTOP),
    LAUNCHER("Launcher", GlyphKind.HOME),
}

@Composable
fun SettingsContent(app: SukiApp) {
    var tab by remember { mutableStateOf(SettingsTab.LOOK) }
    Row(Modifier.fillMaxSize().background(SSurface)) {
        Column(Modifier.width(158.dp).fillMaxHeight().background(SBg.copy(alpha = 0.45f)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SettingsTab.values().forEach { t -> SideItem(t.glyph, t.label, tab == t) { tab = t } }
            Spacer(Modifier.weight(1f))
            SideItem(GlyphKind.INFO, "Tentang", false) { openInternal(app, WinKind.ABOUT) }
        }
        Box(Modifier.width(1.dp).fillMaxHeight().background(SLineSoft))
        Box(Modifier.weight(1f).fillMaxHeight()) {
            when (tab) {
                SettingsTab.LOOK -> LookTab(app)
                SettingsTab.WINDOWS -> WindowsTab(app)
                SettingsTab.DESKTOP -> DesktopTab(app)
                SettingsTab.LAUNCHER -> LauncherTab(app)
            }
        }
    }
}

@Composable
private fun LookTab(app: SukiApp) {
    val prefs = app.prefs
    val motion by prefs.wallMotion.collectAsState()
    ScrollArea {
        Label("Aksen")
        ColSpacer(8)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SukiAccents.forEach { a ->
                val sel = a.id == SukiRuntime.accentId
                Column(
                    Modifier.tap(radius = RADIUS_MD, label = "Aksen ${a.label}") { prefs.setAccent(a.id) }.padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(a.main, a.second)))
                            .border(if (sel) 2.dp else 1.dp, if (sel) SText else SLine, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { if (sel) Glyph(GlyphKind.CHECK, 16, a.on) }
                    Spacer(Modifier.height(4.dp))
                    Txt(a.label, 11, if (sel) SText else SDim, FontWeight.Medium)
                }
            }
        }
        ColSpacer(16)
        Label("Wallpaper")
        ColSpacer(8)
        WALL_SPECS.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { w ->
                    val sel = w.id == SukiRuntime.wallpaperId
                    val shape = RoundedCornerShape(RADIUS_MD.dp)
                    Column(Modifier.tap(radius = RADIUS_MD, label = "Wallpaper ${w.label}") { prefs.setWallpaper(w.id) }.padding(3.dp)) {
                        Box(
                            Modifier
                                .size(width = 104.dp, height = 60.dp)
                                .clip(shape)
                                .border(if (sel) 2.dp else 1.dp, if (sel) accentNow().main else SLine, shape),
                        ) { Wallpaper(w.id, Modifier.fillMaxSize(), motion = false) }
                        Spacer(Modifier.height(4.dp))
                        Txt(w.label, 11, if (sel) SText else SDim, FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        ColSpacer(10)
        ToggleRow(
            "Wallpaper bergerak", motion && !SukiRuntime.goMode,
            if (SukiRuntime.goMode) "Dimatikan di perangkat RAM rendah (mode Go)." else "Cahaya aurora bergeser pelan. Mematikannya menghemat baterai.",
        ) { if (!SukiRuntime.goMode) prefs.setWallMotion(it) }
    }
}

@Composable
private fun WindowsTab(app: SukiApp) {
    val prefs = app.prefs
    val scope = rememberCoroutineScope()
    val shell by SukiShell.state.collectAsState()
    val auto by SukiAuto.state.collectAsState()
    val probeRaw by prefs.probe.collectAsState()
    val strict by prefs.strictWindows.collectAsState()
    val status = WindowStatusLogic.of(
        installed = shell.installed, alive = shell.binderAlive, granted = shell.granted, bound = shell.serviceBound,
        coreOn = auto.coreOn, setupRunning = auto.running, probe = WindowStatusLogic.parseProbe(probeRaw),
    )
    ScrollArea {
        Panel(Modifier.fillMaxWidth(), pad = 14) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Txt(status.label, 15, SText, FontWeight.Bold, font = SukiBrand)
                    Txt(status.detail, 12, SDim, maxLines = 3)
                }
                StatusPill(if (status.tone == Tone.OK) "terbukti" else "belum terbukti", toneColor(status.tone))
            }
            ColSpacer(12)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Btn("Siapkan otomatis", icon = GlyphKind.SHIELD_CHECK, enabled = shell.ready && !auto.running) {
                    scope.launch { SukiAuto.ensure(app, force = true) }
                }
                BtnGhost("Uji jendela", icon = GlyphKind.PLAY, enabled = shell.ready && !auto.probing) {
                    scope.launch {
                        val r = SukiAuto.probe(app)
                        SukiRuntime.say("Uji jendela: ${r.verdict}. ${r.note}", if (r.ok) Tone.OK else Tone.WARN)
                    }
                }
            }
            probeLine(probeRaw)?.let {
                ColSpacer(10)
                Txt(it, 11, SFaint, maxLines = 4)
            }
        }
        ColSpacer(14)
        Label("Akses lanjutan (Shizuku)")
        ColSpacer(4)
        Checklist(shizukuRows(shell))
        if (!shell.ready) {
            ColSpacer(8)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Btn("Minta izin", enabled = shell.binderAlive && !shell.granted) { SukiRuntime.say(SukiShell.requestPermission()) }
                BtnGhost("Buka Shizuku") { SukiShell.openShizukuApp() }
                BtnGhost("Periksa ulang") { SukiShell.refresh() }
            }
        }
        ColSpacer(14)
        Label("Persiapan otomatis")
        ColSpacer(4)
        if (auto.steps.isEmpty()) Txt("Belum dijalankan. Perlu Shizuku siap.", 12, SFaint) else Checklist(stepRows(auto.steps))
        ColSpacer(14)
        ToggleRow(
            "Selalu buka sebagai jendela", strict,
            "Bila Shizuku belum siap, aplikasi tidak dibuka layar penuh diam-diam: SukiOS menawarkan persiapan dulu.",
        ) { prefs.setStrictWindows(it) }
        ColSpacer(8)
        Txt(
            "Mode jendela memakai fitur freeform Android lewat Shizuku. Hasilnya bergantung pada pabrikan: " +
                "sebagian ROM menolak atau baru menerapkannya setelah HP dimulai ulang. Karena itu keadaan \"aktif\" " +
                "hanya ditampilkan setelah uji jendela lulus di perangkat ini.",
            11, SFaint, maxLines = 6,
        )
    }
}

@Composable
private fun DesktopTab(app: SukiApp) {
    val ctx = LocalContext.current
    val prefs = app.prefs
    val fullDesktop by prefs.fullDesktop.collectAsState()
    ScrollArea {
        ToggleRow("Desktop penuh", fullDesktop, "Sembunyikan status bar dan navigation bar; geser dari tepi untuk memunculkannya sebentar.") {
            prefs.setFullDesktop(it)
        }
        ToggleRow("Taskbar melayang", SukiRuntime.overlayBarOn, "Bar kecil di atas aplikasi layar penuh (butuh izin tampil di atas aplikasi lain).") {
            toggleOverlayBar(app, ctx, it)
        }
        SettingRow("Orientasi", "SukiOS selalu mendatar (landscape) dan tidak bisa dibuka kuncinya.") {
            StatusPill("terkunci mendatar", SInfo)
        }
        HLine(Modifier.padding(vertical = 8.dp))
        KeyValue("Batas jendela", "${app.wins.maxWindows} (${if (SukiRuntime.goMode) "mode Go" else "mode normal"})")
        KeyValue("Terbuka sekarang", "${app.wins.count}")
        ColSpacer(8)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BtnGhost("Kecilkan semua") { app.wins.minimizeAll() }
            BtnGhost("Tutup semua") { app.wins.closeAll() }
        }
    }
}

@Composable
private fun LauncherTab(app: SukiApp) {
    val scope = rememberCoroutineScope()
    val shell by SukiShell.state.collectAsState()
    ScrollArea {
        SettingRow(
            "Launcher bawaan",
            if (SukiRuntime.isDefaultLauncher) "SukiOS adalah launcher default: tombol Beranda membawamu ke sini." else "SukiOS belum jadi launcher default.",
        ) { StatusPill(if (SukiRuntime.isDefaultLauncher) "aktif" else "belum", if (SukiRuntime.isDefaultLauncher) SSuccess else SWarning) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Btn("Jadikan launcher default", enabled = !SukiRuntime.isDefaultLauncher) {
                scope.launch {
                    if (shell.ready) SukiAuto.ensure(app, force = true)
                    SukiRuntime.isDefaultLauncher = app.index.isCurrentLauncher()
                    if (!SukiRuntime.isDefaultLauncher) app.index.openDefaultLauncherSettings()
                }
            }
            BtnGhost("Izin tampil di atas aplikasi") { app.index.openOverlaySettings() }
        }
        ColSpacer(10)
        Txt(
            "Dengan Shizuku siap, SukiOS menjadikan dirinya launcher otomatis. Tanpa Shizuku, Android akan menanyakan pilihan launcher.",
            11, SFaint, maxLines = 4,
        )
    }
}
