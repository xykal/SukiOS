package app.sukios.poc

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

// ============================================================================
// Jendela SukiOS — chrome, drag, resize 8 arah, snap (DESIGN.md §10.3, §8.2)
// ============================================================================

@Composable
fun SukiWindow(st: SukiState, w: WinState, deskW: Int, deskH: Int) {
    val density = LocalDensity.current
    val minW = with(density) { 260.dp.roundToPx() }
    val minH = with(density) { 200.dp.roundToPx() }
    val active = st.activeId == w.id && !w.minimized

    Box(
        Modifier
            .offset { IntOffset(w.x, w.y) }
            .size(with(density) { w.w.toDp() }, with(density) { w.h.toDp() })
            .zIndex(w.z.toFloat())
            .shadow(if (active) 18.dp else 8.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(SukiElevated)
            .border(
                1.dp,
                if (active) SukiAccent.copy(alpha = 0.55f) else SukiStroke,
                RoundedCornerShape(14.dp)
            )
            .pointerInput(w.id) { detectTapGestures { st.focus(w) } }
    ) {
        Column(Modifier.fillMaxSize()) {
            TitleBar(st, w, deskW, deskH)
            Box(Modifier.fillMaxSize()) { WindowContent(st, w) }
        }

        if (!w.maximized) ResizeHandles(w, minW, minH)
    }
}

@Composable
private fun TitleBar(st: SukiState, w: WinState, deskW: Int, deskH: Int) {
    val density = LocalDensity.current
    val edge = with(density) { 26.dp.roundToPx() }

    Row(
        Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(SukiChrome)
            .pointerInput(w.id) {
                detectDragGestures(
                    onDragStart = { st.focus(w) },
                    onDrag = { change, drag ->
                        change.consume()
                        // Drag dari kondisi maximize = restore dulu (seperti desktop OS)
                        if (w.maximized) st.toggleMax(w, deskW, deskH)
                        w.x += drag.x.toInt()
                        w.y += drag.y.toInt()
                        st.snapHint = snapZoneOf(w, deskW, deskH, edge)
                    },
                    onDragEnd = {
                        val zone = st.snapHint
                        st.snapHint = null
                        if (zone != null) st.snap(w, zone, deskW, deskH) else clampWindow(w, deskW, deskH)
                    }
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(12.dp))
        Dot(accentFor(w.kind), 12)
        Spacer(Modifier.width(8.dp))
        Txt(
            w.kind.title, 13, SukiText, FontWeight.SemiBold,
            maxLines = 1, modifier = Modifier.weight(1f)
        )
        WinBtn("—") { st.toggleMinimize(w) }
        WinBtn(if (w.maximized) "❐" else "▢") { st.toggleMax(w, deskW, deskH) }
        WinBtn("✕", danger = true) { st.close(w) }
        Spacer(Modifier.width(6.dp))
    }
}

@Composable
private fun WinBtn(label: String, danger: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier
            .padding(end = 4.dp)
            .size(28.dp)
            .clip(RoundedCornerShape(7.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Txt(label, 12, if (danger) SukiDanger else SukiTextDim, FontWeight.Bold)
    }
}

@Composable
private fun BoxScope.ResizeHandles(w: WinState, minW: Int, minH: Int) {
    Handle(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(7.dp), "N", w, minW, minH)
    Handle(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(7.dp), "S", w, minW, minH)
    Handle(Modifier.align(Alignment.CenterStart).fillMaxHeight().width(7.dp), "W", w, minW, minH)
    Handle(Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(7.dp), "E", w, minW, minH)
    Handle(Modifier.align(Alignment.TopStart).size(18.dp), "NW", w, minW, minH)
    Handle(Modifier.align(Alignment.TopEnd).size(18.dp), "NE", w, minW, minH)
    Handle(Modifier.align(Alignment.BottomStart).size(18.dp), "SW", w, minW, minH)
    Handle(Modifier.align(Alignment.BottomEnd).size(18.dp), "SE", w, minW, minH)
}

@Composable
private fun Handle(mod: Modifier, dir: String, w: WinState, minW: Int, minH: Int) {
    Box(
        mod.pointerInput(dir) {
            detectDragGestures { change, drag ->
                change.consume()
                val dx = drag.x.toInt()
                val dy = drag.y.toInt()

                if (dir.contains("E")) w.w = maxOf(minW, w.w + dx)
                if (dir.contains("S")) w.h = maxOf(minH, w.h + dy)
                if (dir.contains("W")) {
                    val nw = maxOf(minW, w.w - dx)
                    w.x += (w.w - nw)
                    w.w = nw
                }
                if (dir.contains("N")) {
                    val nh = maxOf(minH, w.h - dy)
                    w.y += (w.h - nh)
                    w.h = nh
                }
                w.maximized = false
            }
        }
    )
}

// ------------------------------------------------------------------ geometri

/** Menjaga jendela tetap bisa dijangkau (tidak hilang ke luar layar). */
fun clampWindow(w: WinState, deskW: Int, deskH: Int) {
    w.w = ci(w.w, 220, deskW)
    w.h = ci(w.h, 160, deskH)
    w.x = ci(w.x, -w.w + 140, maxOf(0, deskW - 140))
    w.y = ci(w.y, 0, maxOf(0, deskH - 60))
}

/**
 * Zona snap berdasarkan posisi jendela saat digeser:
 * tepi atas = maximize · tepi kiri/kanan = separuh · sudut = seperempat.
 */
fun snapZoneOf(w: WinState, deskW: Int, deskH: Int, edge: Int): String? {
    val nearTop = w.y < edge
    val nearBottom = w.y + w.h > deskH - edge

    return when {
        nearTop && w.x < edge -> "TL"
        nearTop && w.x + w.w > deskW - edge -> "TR"
        nearTop -> "MAX"
        w.x < edge -> if (nearBottom) "BL" else "L"
        w.x + w.w > deskW - edge -> if (nearBottom) "BR" else "R"
        else -> null
    }
}
