package app.sukios

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Bingkai jendela SukiOS.
 *
 * Sepenuhnya digambar sendiri: bilah judul, tombol kecilkan/perbesar/tutup,
 * delapan titik ubah ukuran, dan geser yang menempel ke tepi (snap).
 *
 * Catatan yang jujur soal batasnya:
 *  - Ini jendela untuk ISI milik SukiOS (Setelan, Laboratorium, Terminal,
 *    Aplikasi, Tentang, Diagnostik). Aplikasi pihak ketiga tidak bisa
 *    dimasukkan ke dalam kotak ini; mereka dibuka Android di jendela sistem
 *    (layar penuh, atau mengambang kalau perangkat mengizinkan).
 *  - Mengubah ukuran dari tepi atas/kiri juga menggeser jendela, supaya
 *    sisi yang berlawanan terasa diam di tempat.
 */
@Composable
fun WinFrame(app: SukiApp, w: Win, size: IntSize) {
    val wins = app.wins
    val density = LocalDensity.current.density
    val focused = wins.focusedId == w.id
    val accent = accentById(SukiRuntime.accentId)

    val wDp = with(LocalDensity.current) { w.w.toDp() }
    val hDp = with(LocalDensity.current) { w.h.toDp() }

    Box(
        Modifier
            .offset { IntOffset(w.x.roundToInt(), w.y.roundToInt()) }
            .size(wDp, hDp)
            .clip(RoundedCornerShape(12.dp))
            .background(SChrome)
            .border(
                1.dp,
                if (focused) accent.copy(alpha = 0.42f) else SLine,
                RoundedCornerShape(12.dp),
            )
            .pointerInput(w.id) { detectTapGestures { wins.focus(w.id) } },
    ) {
        Column(Modifier.fillMaxSize()) {
            TitleBar(app, w, size, density)
            Box(Modifier.weight(1f).fillMaxWidth()) { WinContent(app, w) }
        }
        ResizeHandles(app, w, size)
    }
}

@Composable
private fun TitleBar(app: SukiApp, w: Win, size: IntSize, density: Float) {
    val wins = app.wins
    val accent = accentById(SukiRuntime.accentId)
    val sw = size.width.toFloat()
    val sh = size.height.toFloat()

    Row(
        Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(SOverlay)
            .pointerInput(w.id) {
                detectDragGestures(
                    onDragStart = { wins.focus(w.id) },
                    onDrag = { change, delta ->
                        change.consume()
                        wins.moveBy(w.id, delta.x, delta.y, sw, sh)
                    },
                    onDragEnd = { wins.snapFromPosition(w.id, sw, sh, density) },
                )
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(11.dp))
        Dot(accent.copy(alpha = 0.85f), 7)
        Spacer(Modifier.width(9.dp))
        Txt(
            w.title, 12, SText, FontWeight.SemiBold,
            maxLines = 1, modifier = Modifier.weight(1f)
        )
        WinBtn(GlyphKind.MIN) { wins.toggleMinimize(w.id) }
        WinBtn(if (w.maximized) GlyphKind.RESTORE else GlyphKind.MAX) { wins.toggleMax(w.id, sw, sh) }
        WinBtn(GlyphKind.CLOSE, danger = true) { wins.close(w.id) }
        Spacer(Modifier.width(7.dp))
    }
}

@Composable
private fun WinBtn(glyph: GlyphKind, danger: Boolean = false, onClick: () -> Unit) {
    val tone = if (danger) SDanger else SDim
    Box(
        Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(7.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) { Glyph(glyph, 13, tone) }
}

@Composable
private fun BoxScope.ResizeHandles(app: SukiApp, w: Win, size: IntSize) {
    val wins = app.wins
    val sw = size.width.toFloat()
    val sh = size.height.toFloat()

    fun handle(modifier: Modifier, dirX: Int, dirY: Int) {
        Box(
            modifier.pointerInput(w.id, dirX, dirY) {
                detectDragGestures { change, delta ->
                    change.consume()
                    if (dirX < 0) wins.moveBy(w.id, delta.x, 0f, sw, sh)
                    if (dirY < 0) wins.moveBy(w.id, 0f, delta.y, sw, sh)
                    wins.resizeBy(w.id, delta.x * dirX, delta.y * dirY, sw, sh)
                }
            },
        )
    }

    handle(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(6.dp), 0, -1)
    handle(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(6.dp), 0, 1)
    handle(Modifier.align(Alignment.CenterStart).fillMaxHeight().width(6.dp), -1, 0)
    handle(Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(6.dp), 1, 0)
    handle(Modifier.align(Alignment.TopStart).size(16.dp), -1, -1)
    handle(Modifier.align(Alignment.TopEnd).size(16.dp), 1, -1)
    handle(Modifier.align(Alignment.BottomStart).size(16.dp), -1, 1)
    handle(Modifier.align(Alignment.BottomEnd).size(16.dp), 1, 1)
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
        WinKind.APP -> ExternalNote(app, w)
    }
}

/** Jaring pengaman: jendela jenis APP tidak dibuat oleh SukiOS sendiri. */
@Composable
private fun ExternalNote(app: SukiApp, w: Win) {
    WinPanel("Aplikasi luar", "dibuka oleh Android, bukan di dalam jendela SukiOS") {
        ScrollArea {
            Txt(
                "Aplikasi pihak ketiga tidak dapat dimasukkan ke dalam kotak ini tanpa " +
                    "dukungan sistem (activity embedding) atau izin force-resizable lewat Shizuku. " +
                    "Gunakan tombol di jendela Aplikasi untuk mencoba membukanya sebagai jendela.",
                11, SDim, maxLines = 6
            )
            ColSpacer(10)
            Btn("Buka") {
                val pkg = w.pkg ?: return@Btn
                val e = app.index.byPkg(pkg) ?: return@Btn
                launchApp(app, e)
            }
        }
    }
}
