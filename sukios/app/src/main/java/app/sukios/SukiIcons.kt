package app.sukios

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ============================================================================
// Ikon aplikasi, ubin ikon bawaan, dan logo SukiOS.
// ============================================================================

private const val LOGO_S =
    "M18,7.5c-0.8,-1.9 -2.7,-3 -5.4,-3c-3,0 -5.1,1.4 -5.1,3.5c0,2.1 1.9,3.2 5.1,4c3.2,0.8 5.4,1.9 5.4,4.3" +
        "c0,2.4 -2.4,3.8 -5.4,3.8c-3,0 -5.1,-1.1 -5.9,-3"

/** Campuran dua warna; t = 0 memberi a, t = 1 memberi b. */
fun mix(a: Color, b: Color, t: Float): Color = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
    alpha = a.alpha + (b.alpha - a.alpha) * t,
)

private val FALLBACK_TONES = listOf(C_VIOLET, C_TEAL, C_PINK, C_BLUE, C_AMBER, C_GREEN)

/** Warna tetap untuk satu nama, supaya ikon cadangan tidak berganti-ganti antar tampilan. */
fun toneFor(name: String): Color = Color(FALLBACK_TONES[(name.hashCode() and 0x7FFFFFFF) % FALLBACK_TONES.size])

fun initialOf(label: String): String = label.trim().take(1).uppercase().ifBlank { "?" }

/** Ikon aplikasi asli (sudah dinormalkan jadi kotak bulat oleh SukiIndex), atau huruf awal di atas gradien. */
@Composable
fun AppIcon(entry: AppEntry, size: Int = ICON_DP, modifier: Modifier = Modifier, lift: Boolean = false) {
    val shape = RoundedCornerShape((size * 0.21f).dp)
    val base = if (lift) modifier.shadow(6.dp, shape, clip = false, ambientColor = Color(0x66000000), spotColor = Color(0x99000000)) else modifier
    val icon = entry.icon
    if (icon != null) {
        Image(
            bitmap = icon,
            contentDescription = null,
            modifier = base.size(size.dp).clip(shape),
            contentScale = ContentScale.Fit,
        )
    } else {
        val tone = toneFor(entry.label)
        Box(
            base
                .size(size.dp)
                .clip(shape)
                .background(Brush.linearGradient(listOf(mix(tone, SText, 0.15f), mix(tone, SInk, 0.45f))))
                .border(1.dp, Color(0x33FFFFFF), shape),
            contentAlignment = Alignment.Center,
        ) { Txt(initialOf(entry.label), (size / 2.3f).toInt(), SInk, FontWeight.Bold, font = SukiBrand) }
    }
}

/** Ubin ikon bawaan SukiOS: glyph di atas plat gradien berwarna [tone]. */
@Composable
fun IconTile(kind: GlyphKind, tone: Color, size: Int = ICON_DP, modifier: Modifier = Modifier, lift: Boolean = false) {
    val shape = RoundedCornerShape((size * 0.21f).dp)
    val base = if (lift) modifier.shadow(6.dp, shape, clip = false, ambientColor = Color(0x66000000), spotColor = Color(0x99000000)) else modifier
    Box(
        base
            .size(size.dp)
            .clip(shape)
            .background(SChrome)
            .background(Brush.linearGradient(listOf(tone.copy(alpha = 0.42f), tone.copy(alpha = 0.10f))))
            .border(1.dp, tone.copy(alpha = 0.50f), shape),
        contentAlignment = Alignment.Center,
    ) { Glyph(kind, (size * 0.48f).toInt(), mix(tone, SText, 0.55f)) }
}

/** Logo SukiOS: kotak bulat bergradien Aurora dengan huruf S. Digambar sendiri, tanpa aset luar. */
@Composable
fun LogoMark(size: Int = 40, modifier: Modifier = Modifier) {
    val s = remember { runCatching { PathParser().parsePathString(LOGO_S).toPath() }.getOrNull() }
    val violet = remember { Color(C_VIOLET) }
    val teal = remember { Color(C_TEAL) }
    val pink = remember { Color(C_PINK) }
    Canvas(modifier.size(size.dp)) {
        val w = this.size.width
        val r = w * 0.30f
        drawRoundRect(
            brush = Brush.linearGradient(listOf(violet, teal), start = Offset.Zero, end = Offset(w, w)),
            cornerRadius = CornerRadius(r, r),
        )
        drawRoundRect(
            brush = Brush.radialGradient(
                0f to pink.copy(alpha = 0.40f),
                1f to pink.copy(alpha = 0f),
                center = Offset(w * 0.86f, w * 1.0f),
                radius = w * 0.75f,
            ),
            cornerRadius = CornerRadius(r, r),
        )
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x00FFFFFF))),
            cornerRadius = CornerRadius(r, r),
        )
        if (s != null) {
            val k = w / 24f
            withTransform({ scale(k, k, pivot = Offset.Zero) }) {
                drawPath(
                    s, Color.White,
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
        }
    }
}
