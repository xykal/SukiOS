package app.sukios

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Start menu dan panel pintasan.
 *
 * Start menu: pencarian + daftar aplikasi, dengan tombol sematkan.
 * Panel pintasan: saklar cepat, pemilih aksen dan wallpaper, status Shizuku.
 */
@Composable
fun StartMenu(app: SukiApp, size: IntSize) {
    var q by remember { mutableStateOf("") }
    val apps = app.index.search(q, 60)
    val pinned = app.prefs.pinned.value.split(",").filter { it.isNotBlank() }

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x99000000))
                .pointerInput(Unit) { detectTapGestures { SukiRuntime.startOpen = false } },
        )

        Box(Modifier.align(Alignment.BottomStart).padding(start = 10.dp, bottom = (TASKBAR_DP + 8).dp)) {
            Panel(Modifier.width(430.dp).height(392.dp), color = SSheet, radius = 14, pad = 12) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Glyph(GlyphKind.SEARCH, 14, SFaint)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(SBg)
                            .border(1.dp, SLine, RoundedCornerShape(9.dp))
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (q.isEmpty()) Txt("Cari aplikasi...", 12, SFaint)
                        BasicTextField(
                            value = q,
                            onValueChange = { q = it },
                            singleLine = true,
                            textStyle = fieldStyle(12),
                            cursorBrush = SolidColor(accentById(SukiRuntime.accentId)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Label(if (q.isBlank()) "Semua aplikasi" else "Hasil pencarian")
                    Spacer(Modifier.weight(1f))
                    Txt("${app.index.apps.value.size} terpasang", 10, SFaint)
                }
                Spacer(Modifier.height(6.dp))

                if (app.index.loading.value) {
                    Txt("Memuat daftar aplikasi...", 11, SDim)
                } else if (apps.isEmpty()) {
                    Txt("Tidak ada aplikasi yang cocok dengan \"$q\".", 11, SDim, maxLines = 2)
                } else {
                    LazyColumn(Modifier.weight(1f)) {
                        items(apps, key = { it.pkg + "/" + it.activity }) { entry ->
                            AppRow(
                                app = app,
                                entry = entry,
                                isPinned = pinned.contains(entry.pkg),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRow(app: SukiApp, entry: AppEntry, isPinned: Boolean) {
    val accent = accentById(SukiRuntime.accentId)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .clickable { launchApp(app, entry) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val icon = entry.icon
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = entry.label,
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Fit,
            )
        } else {
            AppGlyph(entry.label, 28, accent)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Txt(entry.label, 12, SText, maxLines = 1)
            Txt(entry.pkg, 9, SFaint, maxLines = 1)
        }
        Box(
            Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(7.dp))
                .clickable { app.prefs.togglePinned(entry.pkg) },
            contentAlignment = Alignment.Center,
        ) { Glyph(GlyphKind.PIN, 13, if (isPinned) accent else SFaint) }
    }
}

@Composable
fun QuickPanel(app: SukiApp, size: IntSize) {
    val ctx = LocalContext.current
    val prefs = app.prefs
    val shell = SukiShell.state.value

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures { SukiRuntime.quickOpen = false } },
        )

        Box(Modifier.align(Alignment.TopEnd).padding(top = 10.dp, end = 10.dp)) {
            Panel(Modifier.width(320.dp), color = SSheet, radius = 14, pad = 12) {
                ScrollAreaBox {
                    Label("Status")
                    KeyValue("Perangkat", SukiRuntime.deviceClass)
                    KeyValue("Jendela terbuka", "${app.wins.count} dari ${app.wins.maxWindows}")
                    KeyValue("Launcher", if (SukiRuntime.isDefaultLauncher) "SukiOS aktif" else "belum default")
                    PowerFacts()

                    ColSpacer(12)
                    Label("Desktop")
                    ToggleRow("Desktop penuh", prefs.fullDesktop.value) { prefs.setFullDesktop(it) }
                    ToggleRow("Kunci mendatar", prefs.lockLandscape.value) { prefs.setLockLandscape(it) }
                    ToggleRow("Taskbar melayang", SukiRuntime.overlayBarOn) { toggleOverlayBar(app, ctx, it) }

                    ColSpacer(12)
                    Label("Akses lanjutan")
                    KeyValue("Shizuku", shell.note)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Btn("Minta izin") { SukiRuntime.say(SukiShell.requestPermission()) }
                        BtnGhost("Buka Shizuku") { SukiShell.openShizukuApp() }
                    }
                    ColSpacer(6)
                    BtnGhost("Periksa ulang") {
                        SukiShell.refresh()
                        SukiRuntime.say(SukiShell.state.value.note)
                    }

                    ColSpacer(12)
                    Label("Aksen")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SukiAccents.forEach { a ->
                            Box(
                                Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(a.color)
                                    .border(
                                        if (a.id == SukiRuntime.accentId) 2.dp else 1.dp,
                                        if (a.id == SukiRuntime.accentId) SText else SLine,
                                        RoundedCornerShape(6.dp),
                                    )
                                    .clickable { prefs.setAccent(a.id) },
                            )
                        }
                    }

                    ColSpacer(12)
                    Label("Wallpaper")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SukiWalls.forEach { w ->
                            Box(
                                Modifier
                                    .size(width = 30.dp, height = 22.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Brush.verticalGradient(listOf(w.top, w.bottom)))
                                    .border(
                                        if (w.id == SukiRuntime.wallpaperId) 2.dp else 1.dp,
                                        if (w.id == SukiRuntime.wallpaperId) SText else SLine,
                                        RoundedCornerShape(6.dp),
                                    )
                                    .clickable { prefs.setWallpaper(w.id) },
                            )
                        }
                    }

                    ColSpacer(12)
                    Label("Pintasan sistem")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        BtnGhost("Setelan SukiOS") { openInternal(app, WinKind.SETTINGS) }
                        BtnGhost("Setelan Android") {
                            runCatching {
                                ctx.startActivity(
                                    android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
                                        .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                    }
                    if (!SukiRuntime.isDefaultLauncher) {
                        ColSpacer(6)
                        Btn("Jadikan launcher default") { app.index.openDefaultLauncherSettings() }
                    }
                }
            }
        }
    }
}

/** Kolom yang bisa digulir, dipakai di dalam panel yang tingginya bebas. */
@Composable
private fun ScrollAreaBox(content: @Composable () -> Unit) {
    Column(
        Modifier
            .heightIn(max = 460.dp)
            .verticalScroll(rememberScrollState())
            .padding(2.dp),
    ) { content() }
}

@Composable
private fun PowerFacts() {
    val ctx = LocalContext.current
    val ver = remember { SukiDiag(ctx).appVersion() }
    KeyValue("Versi SukiOS", ver)
}

@Composable
fun ToggleRow(label: String, on: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .clickable { onChange(!on) }
            .padding(vertical = 7.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Txt(label, 12, SText, maxLines = 2, modifier = Modifier.weight(1f))
        Box(
            Modifier
                .width(38.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (on) accentById(SukiRuntime.accentId).copy(alpha = 0.55f) else SOverlay)
                .border(1.dp, SLine, RoundedCornerShape(10.dp)),
        ) {
            Box(
                Modifier
                    .offset { IntOffset(if (on) 19.dp.roundToPx() else 2.dp.roundToPx(), 0) }
                    .padding(top = 2.dp)
                    .size(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (on) SText else SFaint),
            )
        }
    }
}

