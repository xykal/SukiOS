package app.sukios

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

// ============================================================================
// Glyph — ikon vektor SukiOS. Data path ada di SukiGlyphData.kt (dihasilkan oleh
// tools/gen_glyphs.py, kisi 24x24, goresan membulat). Tanpa emoji, tanpa font ikon.
// ============================================================================

private class ParsedGlyph(val stroke: Path?, val fill: Path?)

private object GlyphCache {
    private val map = HashMap<GlyphKind, ParsedGlyph>()

    fun of(kind: GlyphKind): ParsedGlyph = map.getOrPut(kind) {
        ParsedGlyph(parse(kind.stroke), parse(kind.fill))
    }

    private fun parse(d: String): Path? =
        if (d.isBlank()) null else runCatching { PathParser().parsePathString(d).toPath() }.getOrNull()
}

/** Goresan menebal sedikit di ukuran kecil supaya tetap terbaca (optis, bukan matematis). */
fun glyphStroke(dim: Int): Float = when {
    dim <= 16 -> 2.1f
    dim <= 22 -> 1.85f
    else -> 1.7f
}

@Composable
fun Glyph(kind: GlyphKind, dim: Int = 20, color: Color = SText, weight: Float = 0f) {
    val parsed = remember(kind) { GlyphCache.of(kind) }
    val stroke = if (weight > 0f) weight else glyphStroke(dim)
    Canvas(Modifier.size(dim.dp)) {
        val k = this.size.minDimension / 24f
        withTransform({ scale(k, k, pivot = Offset.Zero) }) {
            parsed.fill?.let { drawPath(it, color, style = Fill) }
            parsed.stroke?.let {
                drawPath(it, color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}
