package app.sukios

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

// ============================================================================
// Bingkai jendela milik SukiOS: bilah judul, tombol kecilkan/perbesar/tutup, delapan titik ubah
// ukuran, geser dengan pratinjau snap, dan animasi muncul. Digambar sendiri sepenuhnya.
//
// Jujur soal batasnya: ini jendela untuk isi MILIK SukiOS (Setelan, Aplikasi, Terminal, Laboratorium,
// Tentang, Diagnostik). Aplikasi pihak ketiga tidak dimasukkan ke kotak ini; Android membukanya sebagai
// jendela sistem atas perintah SukiOS (SukiWindowing) dan menampilkannya di atas lapisan ini.
// ============================================================================

private const val TITLE_H = 40

@Composable
fun WinFrame(app: SukiApp, w: Win) {
    val wins = app.wins
    val focused = wins.focusedId == w.id
    val ac = accentNow()
    val shape = RoundedCornerShape(RADIUS_WIN.dp)
    val density = LocalDensity.current
    val wDp = with(density) { w.w.toDp() }
    val hDp = with(density) { w.h.toDp() }
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, tween(MOTION_BASE, easing = EaseOut)) }
    val border: Brush = if (focused) {
        Brush.linearGradient(listOf(ac.main.copy(alpha = 0.70f), ac.second.copy(alpha = 0.40f)))
    } else {
        SolidColor(SLine)
    }

    Box(
        Modifier
            .offset { IntOffset(w.x.roundToInt(), w.y.roundToInt()) }
            .size(wDp, hDp)
            .graphicsLayer {
                val s = 0.95f + 0.05f * appear.value
                scaleX = s
                scaleY = s
                alpha = appear.value
            }
            .shadow(
                if (focused) 30.dp else 12.dp, shape, clip = false,
                ambientColor = Color(0x66000000), spotColor = Color(0xCC000000),
            )
            .clip(shape)
            .background(SSurface)
            .border(1.dp, border, shape)
            .pointerInput(w.id) { detectTapGestures { wins.focus(w.id) } },
    ) {
        Column(Modifier.fillMaxSize()) {
            TitleBar(app, w, focused)
            Box(Modifier.weight(1f).fillMaxWidth()) { WinContent(app, w) }
        }
        ResizeHandles(app, w)
    }
}

@Composable
private fun TitleBar(app: SukiApp, w: Win, focused: Boolean) {
    val wins = app.wins
    Row(
        Modifier
            .fillMaxWidth()
            .height(TITLE_H.dp)
            .background(Brush.verticalGradient(listOf(SOverlay, SChrome)))
            .pointerInput(w.id) {
                detectDragGestures(
                    onDragStart = { wins.focus(w.id) },
                    onDrag = { change, delta ->
                        change.consume()
                        wins.moveBy(w.id, delta.x, delta.y, SukiRuntime.screenW, SukiRuntime.screenH)
                        val zone = wins.previewZone(w.id, SukiRuntime.screenW, SukiRuntime.screenH)
                        SukiRuntime.snapPreview = zone?.let { wins.boundsFor(it, SukiRuntime.screenW, SukiRuntime.screenH) }
                    },
                    onDragEnd = {
                        wins.snapFromPosition(w.id, SukiRuntime.screenW, SukiRuntime.screenH)
                        SukiRuntime.snapPreview = null
                    },
                    onDragCancel = { SukiRuntime.snapPreview = null },
                )
            }
            .pointerInput(w.id) {
                detectTapGestures(onDoubleTap = { wins.toggleMax(w.id, SukiRuntime.screenW, SukiRuntime.screenH) })
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(10.dp))
        IconTile(glyphFor(w.kind), kindTone(w.kind), 24)
        Spacer(Modifier.width(9.dp))
        Txt(
            w.title, 13, if (focused) SText else SDim, FontWeight.SemiBold,
            maxLines = 1, modifier = Modifier.weight(1f),
        )
        WinBtn(GlyphKind.MIN, "Kecilkan", SDim) { wins.toggleMinimize(w.id) }
        WinBtn(if (w.maximized) GlyphKind.RESTORE else GlyphKind.MAX, if (w.maximized) "Pulihkan" else "Perbesar", SDim) {
            wins.toggleMax(w.id, SukiRuntime.screenW, SukiRuntime.screenH)
        }
        WinBtn(GlyphKind.CLOSE, "Tutup", SDangerText) { wins.close(w.id) }
        Spacer(Modifier.width(6.dp))
    }
}

@Composable
private fun WinBtn(glyph: GlyphKind, label: String, tint: Color, onClick: () -> Unit) {
    Box(
        Modifier.size(width = 36.dp, height = 32.dp).tap(radius = RADIUS_SM, label = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Glyph(glyph, 15, tint) }
}

@Composable
private fun BoxScope.ResizeHandles(app: SukiApp, w: Win) {
    val wins = app.wins

    @Composable
    fun handle(modifier: Modifier, dirX: Int, dirY: Int) {
        Box(
            modifier.pointerInput(w.id, dirX, dirY) {
                detectDragGestures { change, delta ->
                    change.consume()
                    wins.resizeEdge(w.id, delta.x, delta.y, dirX, dirY, SukiRuntime.screenW, SukiRuntime.screenH)
                }
            },
        )
    }

    handle(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(5.dp), 0, -1)
    handle(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(9.dp), 0, 1)
    handle(Modifier.align(Alignment.CenterStart).fillMaxHeight().width(9.dp), -1, 0)
    handle(Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(9.dp), 1, 0)
    handle(Modifier.align(Alignment.TopStart).size(18.dp), -1, -1)
    handle(Modifier.align(Alignment.TopEnd).size(18.dp), 1, -1)
    handle(Modifier.align(Alignment.BottomStart).size(22.dp), -1, 1)
    handle(Modifier.align(Alignment.BottomEnd).size(24.dp), 1, 1)

    // Penanda sudut kanan bawah: dua garis diagonal tipis sebagai petunjuk bahwa jendela bisa ditarik.
    Canvas(Modifier.align(Alignment.BottomEnd).padding(5.dp).size(10.dp)) {
        val c = Color(0x66FFFFFF)
        val s = this.size.width
        drawLine(c, Offset(s, 1f), Offset(1f, s), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(c, Offset(s, s * 0.55f), Offset(s * 0.55f, s), strokeWidth = 1.5f, cap = StrokeCap.Round)
    }
}

@Composable
private fun WinContent(app: SukiApp, w: Win) {
    when (w.kind) {
        WinKind.SETTINGS -> SettingsContent(app)
        WinKind.LAB -> LabContent(app)
        WinKind.TERMINAL -> TerminalContent(app)
        WinKind.APPS -> AppsContent(app)
        WinKind.ABOUT -> AboutContent(app)
        WinKind.DIAG -> DiagContent(app)
        WinKind.APP -> WinPanel("Aplikasi luar", "dibuka oleh Android, bukan di dalam jendela SukiOS") {
            ScrollArea { Txt("Aplikasi pihak ketiga tampil sebagai jendela sistem di atas desktop ini.", 12, SDim, maxLines = 4) }
        }
    }
}
