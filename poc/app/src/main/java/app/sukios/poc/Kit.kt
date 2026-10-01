package app.sukios.poc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================================
// Suki Glass — token desain (port dari DESIGN.md §4, §5, §7)
// Semua warna di sini nanti pindah ke :core:designsystem saat project dipecah.
// ============================================================================

val SukiBg = Color(0xFF0B0D12)
val SukiSurface = Color(0xFF12151D)
val SukiElevated = Color(0xFF1A1F2B)
val SukiOverlay = Color(0xFF242A38)
val SukiChrome = Color(0xFF1F2532)

val SukiStroke = Color(0x1FFFFFFF)       // putih 12%
val SukiStrokeSoft = Color(0x12FFFFFF)   // putih 7%

val SukiText = Color(0xFFF2F4F8)
val SukiTextDim = Color(0xFFA8B0C0)
val SukiTextDisabled = Color(0xFF5A6274)

val SukiAccent = Color(0xFF7C5CFF)       // Aurora Violet
val SukiAccent2 = Color(0xFF35D0BA)      // Aurora Teal
val SukiDanger = Color(0xFFFF5F56)
val SukiSuccess = Color(0xFF3FD08A)
val SukiWarning = Color(0xFFFFB44C)
val SukiInfo = Color(0xFF5B9DFF)

fun accentFor(k: WinKind): Color = when (k) {
    WinKind.TESTS -> SukiAccent
    WinKind.PICKER -> SukiAccent2
    WinKind.MONITOR -> SukiInfo
    WinKind.EMBED -> SukiWarning
    WinKind.FILES -> Color(0xFFFFB44C)
    WinKind.NOTES -> Color(0xFFFF7AB6)
}

/** coerceIn yang tidak meledak kalau max < min. */
fun ci(v: Int, lo: Int, hi: Int): Int = v.coerceIn(lo, maxOf(lo, hi))

/**
 * Geometri zona snap: [x, y, w, h] dalam px.
 * Satu-satunya sumber kebenaran untuk snap — dipakai oleh SukiState.snap()
 * DAN oleh pratinjau visual, supaya tidak pernah beda.
 */
fun zoneRect(zone: String, deskW: Int, deskH: Int): IntArray {
    val hw = deskW / 2
    val hh = deskH / 2
    return when (zone) {
        "MAX" -> intArrayOf(0, 0, deskW, deskH)
        "L" -> intArrayOf(0, 0, hw, deskH)
        "R" -> intArrayOf(hw, 0, deskW - hw, deskH)
        "TL" -> intArrayOf(0, 0, hw, hh)
        "TR" -> intArrayOf(hw, 0, deskW - hw, hh)
        "BL" -> intArrayOf(0, hh, hw, deskH - hh)
        "BR" -> intArrayOf(hw, hh, deskW - hw, deskH - hh)
        else -> intArrayOf(0, 0, deskW, deskH)
    }
}

// ============================================================================
// Primitif UI
// ============================================================================

@Composable
fun Txt(
    text: String,
    size: Int = 13,
    color: Color = SukiText,
    fw: FontWeight = FontWeight.Normal,
    maxLines: Int = Int.MAX_VALUE,
    modifier: Modifier = Modifier
) {
    BasicText(
        text = text,
        modifier = modifier,
        maxLines = maxLines,
        style = TextStyle(color = color, fontSize = size.sp, fontWeight = fw)
    )
}

@Composable
fun Btn(
    label: String,
    accent: Color = SukiAccent,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) accent else SukiStrokeSoft)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Txt(label, 12, if (enabled) Color.White else SukiTextDisabled, FontWeight.SemiBold)
    }
}

@Composable
fun BtnGhost(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, SukiStroke, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Txt(label, 12, SukiText, FontWeight.Medium)
    }
}

@Composable
fun Chip(
    label: String,
    color: Color = SukiAccent,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) color.copy(alpha = 0.28f) else SukiStrokeSoft)
            .border(1.dp, if (selected) color else SukiStrokeSoft, RoundedCornerShape(999.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Txt(
            label, 11,
            if (selected) color else SukiTextDim,
            if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun Panel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SukiElevated)
            .border(1.dp, SukiStrokeSoft, RoundedCornerShape(12.dp))
            .padding(12.dp),
        content = content
    )
}

/** Satu baris "label: value" untuk daftar hasil probe. */
@Composable
fun KV(key: String, value: String, ok: Boolean? = null) {
    val color = when (ok) {
        true -> SukiSuccess
        false -> SukiDanger
        null -> SukiTextDim
    }
    Box(Modifier.padding(vertical = 3.dp)) {
        Txt("$key: $value", 11, color, FontWeight.Normal)
    }
}

/** Kotak kecil berwarna — dipakai sebagai "ikon" aplikasi di taskbar/jendela. */
@Composable
fun Dot(color: Color, size: Int = 12) {
    Box(Modifier.size(size.dp).clip(RoundedCornerShape(4.dp)).background(color))
}
