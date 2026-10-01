package app.sukios.poc

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

// ============================================================================
// Shell SukiOS PoC: desktop + ikon + taskbar + start menu
// ============================================================================

@Composable
fun SukiRoot(st: SukiState, act: Activity) {
    val ctx = LocalContext.current
    val fps = rememberFps()

    LaunchedEffect(Unit) {
        st.act = act
        st.log("boot", "SukiOS PoC 0.1.0-poc — window manager + capability probe")
        runProbe(ctx, act, st)
        val list = loadApps(ctx)
        st.apps.clear()
        st.apps.addAll(list)
        st.appsLoaded = true
        st.log("boot", "${list.size} aplikasi terdeteksi")
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(SukiBg)) {
        val density = LocalDensity.current
        val fullW = with(density) { maxWidth.roundToPx() }
        val fullH = with(density) { maxHeight.roundToPx() }
        val taskbarPx = with(density) { 48.dp.roundToPx() }
        val deskW = fullW
        val deskH = (fullH - taskbarPx).coerceAtLeast(240)

        SukiWallpaper()

        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                DesktopIcons(st, deskW, deskH)
                WindowLayer(st, deskW, deskH)
                SnapPreview(st, deskW, deskH)
            }
            SukiTaskbar(st, fps)
        }

        if (st.startOpen) SukiStartMenu(st, deskW, deskH)
    }
}

@Composable
private fun SukiWallpaper() {
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(
                listOf(Color(0xFF0A0C12), Color(0xFF141A2C))
            )
        )
    ) {
        // Aurora: dua sorotan radial — identitas visual SukiOS (DESIGN.md §4.1)
        // Catatan: center & radius di Brush memakai satuan PIXEL (bukan persen),
        // jadi angkanya dituning untuk layar ~1080×2400 dengan glow yang lembut.
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(Color(0x887C5CFF), Color(0x00000000)),
                    center = Offset(280f, 260f),
                    radius = 1100f
                )
            )
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(Color(0x6635D0BA), Color(0x00000000)),
                    center = Offset(900f, 380f),
                    radius = 1000f
                )
            )
        )
    }
}

@Composable
private fun DesktopIcons(st: SukiState, deskW: Int, deskH: Int) {
    val items = listOf(
        WinKind.TESTS, WinKind.PICKER, WinKind.MONITOR,
        WinKind.EMBED, WinKind.FILES, WinKind.NOTES
    )
    Column(
        Modifier
            .padding(12.dp)
            .width(86.dp)
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
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(accentFor(k), accentFor(k).copy(alpha = 0.55f))
                            )
                        )
                )
                Spacer(Modifier.height(6.dp))
                Txt(k.title, 9, Color.White, FontWeight.SemiBold, maxLines = 2)
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
                .background(SukiAccent.copy(alpha = 0.18f))
                .border(2.dp, SukiAccent, RoundedCornerShape(14.dp))
        )
    }
}

@Composable
private fun SukiTaskbar(st: SukiState, fps: Float) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(SukiSurface)
            .border(1.dp, SukiStrokeSoft)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tombol Start
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Brush.linearGradient(listOf(SukiAccent, SukiAccent2)))
                .clickable { st.startOpen = !st.startOpen },
            contentAlignment = Alignment.Center
        ) {
            Txt("S", 15, Color.White, FontWeight.Bold)
        }

        Spacer(Modifier.width(8.dp))

        // Chip jendela yang terbuka
        st.windows.forEach { w ->
            val active = st.activeId == w.id && !w.minimized
            Row(
                Modifier
                    .padding(end = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) SukiAccent.copy(alpha = 0.22f) else Color.Transparent)
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

        val fpsColor = when {
            fps >= 55f -> SukiSuccess
            fps >= 40f -> SukiWarning
            else -> SukiDanger
        }
        Txt("FPS ${fps.toInt()}", 11, fpsColor, FontWeight.Bold)
        Spacer(Modifier.width(12.dp))
        Txt("SukiOS PoC", 10, SukiTextDisabled)
        Spacer(Modifier.width(6.dp))
    }
}

@Composable
private fun SukiStartMenu(st: SukiState, deskW: Int, deskH: Int) {
    val items = listOf(
        WinKind.TESTS, WinKind.PICKER, WinKind.MONITOR,
        WinKind.EMBED, WinKind.FILES, WinKind.NOTES
    )
    Box(Modifier.fillMaxSize()) {
        // Scrim: tekan di luar untuk menutup (pakai tap detector — tanpa ripple)
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
                .width(290.dp)
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
        }
    }
}
