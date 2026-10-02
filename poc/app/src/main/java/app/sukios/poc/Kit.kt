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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================================
// Suki Glass — token desain (port dari DESIGN.md §4, §5, §7)
//
// PALET MATTE (revisi 2026-10-02): tanpa neon, tanpa glow, tanpa gradien
// menyala. Semua aksen diturunkan saturasinya; bayangan selalu netral hitam.
// Lihat DESIGN.md §1.3 untuk aturan visual wajib.
// ============================================================================

val SukiBg = Color(0xFF0F1113)
val SukiSurface = Color(0xFF17191C)
val SukiElevated = Color(0xFF1E2124)
val SukiOverlay = Color(0xFF262A2E)
val SukiChrome = Color(0xFF1B1E21)

val SukiStroke = Color(0x1AFFFFFF)       // putih 10%
val SukiStrokeSoft = Color(0x0FFFFFFF)   // putih 6%

val SukiText = Color(0xFFE7E8EA)
val SukiTextDim = Color(0xFFA0A4A9)
val SukiTextDisabled = Color(0xFF6B7076)

val SukiAccent = Color(0xFF6E8CA8)       // steel blue (matte)
val SukiAccent2 = Color(0xFF7B9E8C)      // sage (matte)
val SukiDanger = Color(0xFFB4675F)
val SukiSuccess = Color(0xFF7FA98A)
val SukiWarning = Color(0xFFC0A06A)
val SukiInfo = Color(0xFF7E93AC)

fun accentFor(k: WinKind): Color = when (k) {
    WinKind.TESTS -> SukiAccent
    WinKind.PICKER -> SukiAccent2
    WinKind.MONITOR -> SukiInfo
    WinKind.ACCESS -> Color(0xFFA08F76)   // clay
    WinKind.EMBED -> SukiWarning
    WinKind.FILES -> Color(0xFF8A93A0)    // graphite
    WinKind.NOTES -> Color(0xFF9E8AA0)    // mauve
}

/** coerceIn yang tidak meledak kalau max < min. */
fun ci(v: Int, lo: Int, hi: Int): Int = v.coerceIn(lo, maxOf(lo, hi))

/**
 * Geometri zona snap: [x, y, w, h] dalam px.
 * Satu-satunya sumber kebenaran untuk snap — dipakai oleh SukiState.snap()
 * dan oleh pratinjau visual, supaya tidak pernah berbeda.
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
        Txt(label, 12, if (enabled) Color(0xFF11161B) else SukiTextDisabled, FontWeight.SemiBold)
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

/** Tombol kecil untuk status on/off di taskbar (Matte, tanpa efek menyala). */
@Composable
fun ToggleChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(if (active) SukiAccent.copy(alpha = 0.22f) else Color.Transparent)
            .border(1.dp, if (active) SukiAccent.copy(alpha = 0.55f) else SukiStrokeSoft, RoundedCornerShape(7.dp))
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 6.dp)
    ) {
        Txt(label, 10.5f.toInt(), if (active) SukiText else SukiTextDim, FontWeight.SemiBold, maxLines = 1)
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
            .background(if (selected) color.copy(alpha = 0.24f) else SukiStrokeSoft)
            .border(1.dp, if (selected) color.copy(alpha = 0.6f) else SukiStrokeSoft, RoundedCornerShape(999.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Txt(
            label, 11,
            if (selected) SukiText else SukiTextDim,
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

/** Badge huruf untuk daftar berkas (menggantikan emoji, sesuai aturan desain). */
@Composable
fun FileBadge(text: String, tint: Color) {
    Box(
        Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(tint.copy(alpha = 0.22f))
            .border(1.dp, tint.copy(alpha = 0.45f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Txt(text, 9, tint, FontWeight.Bold, maxLines = 1)
    }
}
