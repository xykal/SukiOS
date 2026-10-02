package app.sukios.poc

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

// ============================================================================
// Shell SukiOS PoC: desktop + ikon + taskbar + start menu
// Termasuk mode tampilan: kunci landscape + desktop penuh (imersif).
// ============================================================================

@Composable
fun SukiRoot(st: SukiState, act: Activity) {
    val ctx = LocalContext.current
    val fps = rememberFps()

    LaunchedEffect(Unit) {
        st.act = act
        st.log("boot", "SukiOS PoC 0.2.0-poc — window manager, probe, akses lanjutan")

        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        st.goMode = am.isLowRamDevice
        st.maxWindows = if (st.goMode) 3 else 8
        st.log("mode", if (st.goMode) {
            "Perangkat kelas Go/RAM rendah: batas 3 jendela, efek dekoratif dikurangi"
        } else {
            "Perangkat normal: batas 8 jendela"
        })

        runProbe(ctx, act, st)
        refreshShizuku(ctx, st)

        val list = loadApps(ctx)
        st.apps.clear()
        st.apps.addAll(list)
        st.appsLoaded = true
        st.log("boot", "${list.size} aplikasi terdeteksi")
    }

    // Kunci orientasi: phone dipegang tegak pun tetap landscape (mode desktop).
    LaunchedEffect(st.act, st.lockLandscape) {
        val a = st.act ?: return@LaunchedEffect
        a.requestedOrientation = if (st.lockLandscape) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        st.log("tampilan", "Kunci landscape: ${if (st.lockLandscape) "AKTIF" else "nonaktif"}")
    }

    // Desktop penuh: sembunyikan status bar + navigation bar (imersif, geser untuk muncul).
    LaunchedEffect(st.act, st.fullDesktop) {
        val a = st.act ?: return@LaunchedEffect
        WindowCompat.setDecorFitsSystemWindows(a.window, !st.fullDesktop)
        val controller = WindowCompat.getInsetsController(a.window, a.window.decorView)
        if (st.fullDesktop) {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
        st.log("tampilan", "Desktop penuh: ${if (st.fullDesktop) "AKTIF" else "nonaktif"}")
    }

    // Hasil permintaan izin Shizuku datang lewat callback, bukan polling.
    DisposableEffect(Unit) {
        val listener = ShizukuEngine.newResultListener { _, granted ->
            st.shizukuReady = granted
            st.verdict("shizuku-izin", if (granted) "OK" else "DITOLAK")
            st.log("shizuku", if (granted) "Izin diberikan." else "Izin ditolak pengguna.")
            refreshShizuku(ctx, st)
        }
        ShizukuEngine.addListener(listener)
        onDispose { ShizukuEngine.removeListener(listener) }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(SukiBg)) {
        val density = LocalDensity.current
        val fullW = with(density) { maxWidth.roundToPx() }
        val fullH = with(density) { maxHeight.roundToPx() }
        val taskbarPx = with(density) { 48.dp.roundToPx() }
        val deskW = fullW
        val deskH = (fullH - taskbarPx).coerceAtLeast(240)

        SukiWallpaper(flat = st.goMode)

        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                DesktopIcons(st, deskW, deskH)
                WindowLayer(st, deskW, deskH)
                SnapPreview(st, deskW, deskH)
            }
            SukiTaskbar(st, fps, deskW, deskH)
        }

        if (st.startOpen) SukiStartMenu(st, deskW, deskH)
    }
}

/**
 * Wallpaper matte: dua nada gelap, tanpa sorotan radial/glow.
 * Di perangkat Go efek dekoratif dimatikan total (hemat RAM & GPU).
 */
@Composable
private fun SukiWallpaper(flat: Boolean) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF14171B), Color(0xFF0E1013)))
        )
    )
    if (!flat) {
        // Satu panel tipis sebagai pemisah visual — tetap tanpa warna menyala.
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0x0AFFFFFF), Color(0x00000000)))
            )
        )
    }
}

@Composable
private fun DesktopIcons(st: SukiState, deskW: Int, deskH: Int) {
    val items = listOf(
        WinKind.TESTS, WinKind.PICKER, WinKind.MONITOR,
        WinKind.ACCESS, WinKind.EMBED, WinKind.FILES, WinKind.NOTES
    )
    Column(
        Modifier
            .padding(12.dp)
            .width(92.dp)
            .verticalScroll(rememberScrollState())
    ) {
        items.forEach { k ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { st.open(k, deskW, deskH) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(accentFor(k).copy(alpha = 0.85f))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(13.dp))
                )
                Spacer(Modifier.height(6.dp))
                Txt(k.title, 9, Color(0xFFD8DADD), FontWeight.SemiBold, maxLines = 2)
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun WindowLayer(st: SukiState, deskW: Int, deskH: Int) {
    Box(Modifier.fillMaxSize()) {
        st.windows.forEach { w ->
            key(w.id) {
                if (!w.minimized) {
                    SukiWindow(st, w, deskW, deskH)
                }
            }
        }
    }
}

/** Pratinjau zona snap — muncul saat jendela digeser ke tepi layar. */
@Composable
private fun SnapPreview(st: SukiState, deskW: Int, deskH: Int) {
    val density = LocalDensity.current
    val zone = st.snapHint
    if (zone != null) {
        val r = zoneRect(zone, deskW, deskH)
        Box(
            Modifier
                .offset { IntOffset(r[0], r[1]) }
                .size(with(density) { r[2].toDp() }, with(density) { r[3].toDp() })
                .clip(RoundedCornerShape(14.dp))
                .background(SukiAccent.copy(alpha = 0.14f))
                .border(2.dp, SukiAccent.copy(alpha = 0.75f), RoundedCornerShape(14.dp))
        )
    }
}

@Composable
private fun SukiTaskbar(st: SukiState, fps: Float, deskW: Int, deskH: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(SukiSurface)
            .border(1.dp, SukiStrokeSoft)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tombol Start: warna solid matte, tanpa gradien menyala.
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(SukiAccent)
                .clickable { st.startOpen = !st.startOpen },
            contentAlignment = Alignment.Center
        ) {
            Txt("S", 15, Color(0xFF10151A), FontWeight.Bold)
        }

        Spacer(Modifier.width(8.dp))

        // Chip jendela yang terbuka
        st.windows.forEach { w ->
            val active = st.activeId == w.id && !w.minimized
            Row(
                Modifier
                    .padding(end = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) SukiAccent.copy(alpha = 0.20f) else Color.Transparent)
                    .clickable {
                        if (active) {
                            w.minimized = true
                            if (st.activeId == w.id) st.activeId = null
                        } else {
                            w.minimized = false
                            st.focus(w)
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Dot(accentFor(w.kind), 8)
                Spacer(Modifier.width(6.dp))
                Txt(
                    w.kind.title, 11,
                    if (active) SukiText else SukiTextDim,
                    FontWeight.Medium, maxLines = 1
                )
            }
        }

        Spacer(Modifier.weight(1f))

        if (st.goMode) {
            Txt("Go", 10, SukiWarning, FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
        }

        ToggleChip("Landscape", st.lockLandscape) {
            st.lockLandscape = !st.lockLandscape
            st.open(WinKind.MONITOR, deskW, deskH)
        }
        Spacer(Modifier.width(6.dp))
        ToggleChip("Desktop", st.fullDesktop) {
            st.fullDesktop = !st.fullDesktop
            st.open(WinKind.MONITOR, deskW, deskH)
        }
        Spacer(Modifier.width(10.dp))

        val fpsColor = when {
            fps >= 55f -> SukiSuccess
            fps >= 40f -> SukiWarning
            else -> SukiDanger
        }
        Txt("FPS ${fps.toInt()}", 11, fpsColor, FontWeight.Bold)
        Spacer(Modifier.width(10.dp))
        Txt("v0.2", 10, SukiTextDisabled)
        Spacer(Modifier.width(6.dp))
    }
}

@Composable
private fun SukiStartMenu(st: SukiState, deskW: Int, deskH: Int) {
    val items = listOf(
        WinKind.TESTS, WinKind.PICKER, WinKind.MONITOR,
        WinKind.ACCESS, WinKind.EMBED, WinKind.FILES, WinKind.NOTES
    )
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x99000000))
                .pointerInput(Unit) { detectTapGestures { st.startOpen = false } }
        )

        Panel(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 58.dp)
                .width(300.dp)
        ) {
            Txt("SukiOS PoC", 15, SukiText, FontWeight.Bold)
            Txt("Pilih jendela untuk dibuka", 10, SukiTextDisabled)
            Spacer(Modifier.height(10.dp))

            items.forEach { k ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            st.startOpen = false
                            st.open(k, deskW, deskH)
                        }
                        .padding(horizontal = 6.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Dot(accentFor(k), 14)
                    Spacer(Modifier.width(10.dp))
                    Txt(k.title, 12, SukiText, FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(8.dp))
            Txt(
                "Mode: ${if (st.lockLandscape) "landscape terkunci" else "bebas"} · " +
                    if (st.fullDesktop) "desktop penuh" else "bar sistem terlihat",
                9.5f.toInt(), SukiTextDisabled, maxLines = 2
            )
        }
    }
}
