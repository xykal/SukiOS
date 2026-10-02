package app.sukios

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================================
// SukiKit — kit tampilan SukiOS.
//
// Aturan yang mengikat (DESIGN.md 1.3): matte, tanpa neon, tanpa glow.
// Bayangan selalu netral hitam; pemisahan bidang memakai garis tipis dan
// perbedaan nada, bukan cahaya. Tidak ada emoji sebagai ikon antarmuka —
// semua ikon digambar sebagai vektor di Glyph().
//
// Catatan teknis: teks memakai Text dari material3 (parameter sederhana:
// color, fontSize, fontWeight, maxLines). Konstruktor TextStyle di Compose
// 1.7 sudah diubah bentuknya, jadi jalur itu sengaja tidak dipakai.
// ============================================================================

// ---- Palet ----
val SBg = Color(0xFF0F1113)
val SSurface = Color(0xFF17191C)
val SElevated = Color(0xFF1E2124)
val SOverlay = Color(0xFF262A2E)
val SChrome = Color(0xFF1B1E21)
val SSheet = Color(0xFF131518)

val SText = Color(0xFFE7E8EA)
val SDim = Color(0xFFA0A4A9)
val SFaint = Color(0xFF6B7076)

val SAccent = Color(0xFF6E8CA8)
val SSage = Color(0xFF7B9E8C)
val SClay = Color(0xFFA08F76)
val SSuccess = Color(0xFF7FA98A)
val SWarning = Color(0xFFC0A06A)
val SDanger = Color(0xFFB4675F)
val SInfo = Color(0xFF7E93AC)

val SLine = Color(0x14FFFFFF)
val SLineStrong = Color(0x22FFFFFF)

// ---- Ukuran tetap ----
const val TASKBAR_DP = 54
const val ICON_DP = 44

// ---- Aksen pilihan pengguna (semua saturasi rendah, sesuai aturan) ----
data class SukiAccent(val id: String, val label: String, val color: Color)

val SukiAccents = listOf(
    SukiAccent("steel", "Steel", SAccent),
    SukiAccent("sage", "Sage", SSage),
    SukiAccent("clay", "Clay", SClay),
    SukiAccent("slate", "Slate", Color(0xFF6F757C)),
    SukiAccent("moss", "Moss", Color(0xFF8C9E7B)),
    SukiAccent("mauve", "Mauve", Color(0xFF9E8AA0)),
    SukiAccent("dust", "Dust", Color(0xFF8A93A0)),
    SukiAccent("olive", "Olive", Color(0xFFA09B76)),
    SukiAccent("denim", "Denim", Color(0xFF5C6E83)),
    SukiAccent("ash", "Ash", Color(0xFF7E93AC)),
)

fun accentById(id: String): Color = SukiAccents.firstOrNull { it.id == id }?.color ?: SAccent

// ---- Wallpaper: gradien dua nada gelap, tanpa sorotan menyala ----
data class SukiWall(val id: String, val label: String, val top: Color, val bottom: Color)

val SukiWalls = listOf(
    SukiWall("charcoal", "Arang", Color(0xFF16191D), Color(0xFF0E1013)),
    SukiWall("slate", "Batu Tulis", Color(0xFF151A20), Color(0xFF0F1317)),
    SukiWall("moss", "Lumut", Color(0xFF141A17), Color(0xFF0E1310)),
    SukiWall("sand", "Pasir", Color(0xFF1A1714), Color(0xFF131110)),
    SukiWall("iron", "Besi", Color(0xFF191B1E), Color(0xFF101214)),
    SukiWall("dusk", "Senja", Color(0xFF1A1519), Color(0xFF120F13)),
)

fun wallById(id: String): SukiWall = SukiWalls.firstOrNull { it.id == id } ?: SukiWalls[0]

// ---- Teks ----
@Composable
fun Txt(
    text: String,
    size: Int = 13,
    color: Color = SText,
    weight: FontWeight = FontWeight.Normal,
    maxLines: Int = 1,
    modifier: Modifier = Modifier,
    align: TextAlign? = null,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size.sp,
        fontWeight = weight,
        fontFamily = FontFamily.SansSerif,
        textAlign = align,
        maxLines = maxLines,
    )
}

@Composable
fun Label(text: String) {
    Txt(text.uppercase(), 10, SFaint, FontWeight.SemiBold)
}

/**
 * Gaya untuk kolom ketik (BasicTextField).
 *
 * Dibangun dari TextStyle.Default lewat copy() — bukan konstruktor TextStyle,
 * karena konstruktor bergaya lama sudah disembunyikan di Compose 1.7.
 * Sengaja dipusatkan di sini supaya hanya ada SATU tempat yang bergantung
 * pada bentuk API itu.
 */
fun fieldStyle(size: Int = 12, mono: Boolean = false): TextStyle =
    TextStyle.Default.copy(
        color = SText,
        fontSize = size.sp,
        fontFamily = if (mono) FontFamily.Monospace else FontFamily.SansSerif,
    )

// ---- Wadah ----
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    color: Color = SElevated,
    radius: Int = 14,
    pad: Int = 12,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(radius.dp))
            .background(color)
            .border(1.dp, SLine, RoundedCornerShape(radius.dp))
            .padding(pad.dp),
        content = content,
    )
}

@Composable
fun Dot(color: Color, size: Int = 8, modifier: Modifier = Modifier) {
    Box(modifier.size(size.dp).clip(CircleShape).background(color))
}

// ---- Kendali ----
@Composable
fun Btn(label: String, primary: Boolean = true, enabled: Boolean = true, onClick: () -> Unit) {
    val accent = accentById(SukiRuntime.accentId)
    val bg = when {
        !enabled -> SElevated
        primary -> accent
        else -> SOverlay
    }
    val fg = when {
        !enabled -> SFaint
        primary -> Color(0xFF0F1113)
        else -> SText
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(bg)
            .then(if (primary && enabled) Modifier else Modifier.border(1.dp, SLine, RoundedCornerShape(9.dp)))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Txt(label, 12, fg, FontWeight.SemiBold)
    }
}

@Composable
fun BtnGhost(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(9.dp))
            .border(1.dp, SLine, RoundedCornerShape(9.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Txt(label, 12, if (enabled) SDim else SFaint, FontWeight.Medium)
    }
}

@Composable
fun Chip(label: String, active: Boolean = false, onClick: () -> Unit) {
    val accent = accentById(SukiRuntime.accentId)
    val bg = if (active) accent.copy(alpha = 0.22f) else SOverlay
    val fg = if (active) accent else SDim
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, SLine, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Txt(label, 11, fg, FontWeight.Medium)
    }
}

@Composable
fun KeyValue(key: String, value: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Txt(key, 11, SFaint, modifier = Modifier.width(132.dp))
        Txt(value, 11, SText, maxLines = 3, modifier = Modifier.weight(1f))
    }
}

@Composable
fun ColSpacer(h: Int = 8) = Spacer(Modifier.height(h.dp))

// ---- Wallpaper ----
@Composable
fun Wallpaper(id: String, modifier: Modifier = Modifier) {
    val w = wallById(id)
    Box(modifier.background(Brush.verticalGradient(listOf(w.top, w.bottom))))
}

// ============================================================================
// Glyph — ikon vektor. Tidak ada emoji, tidak ada font ikon eksternal.
// ============================================================================
enum class GlyphKind {
    HOME, APPS, SEARCH, SETTINGS, TERMINAL, LAB, INFO, PLUS, CHECK, REFRESH,
    POWER, ROTATE, DESKTOP, OVERLAY, SHELL, PIN, STAR, FOLDER, BACK,
    CLOSE, MIN, MAX, RESTORE, WINDOW, CHART
}

@Composable
fun Glyph(kind: GlyphKind, dim: Int = 18, color: Color = SText) {
    Canvas(Modifier.size(dim.dp)) {
        // Catatan penting: di dalam lambda ini, kata "size" milik DrawScope,
        // bukan parameter fungsi. Karena itu lebarnya diambil dari this.size.
        val w = this.size.width
        val h = this.size.height
        val sw = (w * 0.095f).coerceAtLeast(1.15f)
        val st = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)

        fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
            drawLine(
                color = color,
                start = Offset(w * x1, h * y1),
                end = Offset(w * x2, h * y2),
                strokeWidth = sw,
                cap = StrokeCap.Round,
            )

        fun rect(x: Float, y: Float, ww: Float, hh: Float, solid: Boolean = false) =
            drawRect(
                color = color,
                topLeft = Offset(w * x, h * y),
                size = Size(w * ww, h * hh),
                style = if (solid) Fill else st,
            )

        fun font(x: Float, y: Float, radius: Float, solid: Boolean = false) =
            drawCircle(
                color = color,
                radius = w * radius,
                center = Offset(w * x, h * y),
                style = if (solid) Fill else st,
            )

        fun poly(vararg pts: Float, close: Boolean = false, solid: Boolean = false) {
            val p = Path()
            p.moveTo(w * pts[0], h * pts[1])
            var i = 2
            while (i + 1 < pts.size) {
                p.lineTo(w * pts[i], h * pts[i + 1])
                i += 2
            }
            if (close) p.close()
            drawPath(path = p, color = color, style = if (solid) Fill else st)
        }

        when (kind) {
            GlyphKind.HOME -> {
                poly(0.10f, 0.50f, 0.50f, 0.14f, 0.90f, 0.50f)
                poly(0.22f, 0.48f, 0.22f, 0.88f, 0.78f, 0.88f, 0.78f, 0.48f)
            }
            GlyphKind.APPS -> {
                rect(0.12f, 0.12f, 0.32f, 0.32f, solid = true)
                rect(0.56f, 0.12f, 0.32f, 0.32f, solid = true)
                rect(0.12f, 0.56f, 0.32f, 0.32f, solid = true)
                rect(0.56f, 0.56f, 0.32f, 0.32f, solid = true)
            }
            GlyphKind.SEARCH -> {
                font(0.44f, 0.44f, 0.26f)
                line(0.64f, 0.64f, 0.86f, 0.86f)
            }
            GlyphKind.SETTINGS -> {
                font(0.50f, 0.50f, 0.30f)
                font(0.50f, 0.50f, 0.09f, solid = true)
                line(0.50f, 0.05f, 0.50f, 0.20f)
                line(0.50f, 0.80f, 0.50f, 0.95f)
                line(0.05f, 0.50f, 0.20f, 0.50f)
                line(0.80f, 0.50f, 0.95f, 0.50f)
            }
            GlyphKind.TERMINAL -> {
                rect(0.08f, 0.16f, 0.84f, 0.68f)
                line(0.26f, 0.38f, 0.42f, 0.50f)
                line(0.42f, 0.50f, 0.26f, 0.62f)
                line(0.52f, 0.64f, 0.72f, 0.64f)
            }
            GlyphKind.LAB -> {
                poly(0.36f, 0.12f, 0.64f, 0.12f)
                poly(0.42f, 0.14f, 0.42f, 0.44f, 0.18f, 0.84f, 0.82f, 0.84f, 0.58f, 0.44f, 0.58f, 0.14f)
                line(0.30f, 0.66f, 0.70f, 0.66f)
            }
            GlyphKind.INFO -> {
                font(0.50f, 0.50f, 0.40f)
                font(0.50f, 0.30f, 0.055f, solid = true)
                line(0.50f, 0.46f, 0.50f, 0.72f)
            }
            GlyphKind.PLUS -> {
                line(0.50f, 0.16f, 0.50f, 0.84f)
                line(0.16f, 0.50f, 0.84f, 0.50f)
            }
            GlyphKind.CHECK -> poly(0.16f, 0.54f, 0.40f, 0.78f, 0.84f, 0.24f)
            GlyphKind.REFRESH -> {
                poly(0.80f, 0.34f, 0.62f, 0.16f, 0.46f, 0.24f, 0.36f, 0.42f)
                poly(0.20f, 0.66f, 0.38f, 0.84f, 0.54f, 0.76f, 0.64f, 0.58f)
            }
            GlyphKind.POWER -> {
                font(0.50f, 0.56f, 0.34f)
                line(0.50f, 0.10f, 0.50f, 0.42f)
            }
            GlyphKind.ROTATE -> {
                rect(0.18f, 0.28f, 0.64f, 0.44f)
                poly(0.62f, 0.14f, 0.86f, 0.24f, 0.66f, 0.40f)
            }
            GlyphKind.DESKTOP -> {
                rect(0.10f, 0.18f, 0.80f, 0.50f)
                line(0.34f, 0.84f, 0.66f, 0.84f)
                line(0.50f, 0.68f, 0.50f, 0.84f)
            }
            GlyphKind.OVERLAY -> {
                rect(0.10f, 0.24f, 0.80f, 0.40f)
                rect(0.16f, 0.70f, 0.68f, 0.16f, solid = true)
            }
            GlyphKind.SHELL -> {
                poly(0.20f, 0.30f, 0.44f, 0.50f, 0.20f, 0.70f)
                line(0.54f, 0.72f, 0.80f, 0.72f)
            }
            GlyphKind.PIN -> {
                font(0.50f, 0.36f, 0.14f, solid = true)
                line(0.50f, 0.50f, 0.50f, 0.86f)
            }
            GlyphKind.STAR -> poly(
                0.50f, 0.12f, 0.62f, 0.40f, 0.90f, 0.42f, 0.68f, 0.60f, 0.76f, 0.88f,
                0.50f, 0.72f, 0.24f, 0.88f, 0.32f, 0.60f, 0.10f, 0.42f, 0.38f, 0.40f,
                close = true,
            )
            GlyphKind.FOLDER -> poly(
                0.10f, 0.76f, 0.10f, 0.26f, 0.42f, 0.26f, 0.48f, 0.36f, 0.90f, 0.36f, 0.90f, 0.76f,
                close = true,
            )
            GlyphKind.BACK -> poly(0.56f, 0.22f, 0.30f, 0.50f, 0.56f, 0.78f)
            GlyphKind.CLOSE -> {
                line(0.24f, 0.24f, 0.76f, 0.76f)
                line(0.76f, 0.24f, 0.24f, 0.76f)
            }
            GlyphKind.MIN -> line(0.18f, 0.50f, 0.82f, 0.50f)
            GlyphKind.MAX -> rect(0.18f, 0.20f, 0.64f, 0.60f)
            GlyphKind.RESTORE -> {
                rect(0.34f, 0.14f, 0.50f, 0.46f)
                rect(0.16f, 0.38f, 0.50f, 0.46f)
            }
            GlyphKind.WINDOW -> {
                rect(0.12f, 0.18f, 0.76f, 0.64f)
                line(0.12f, 0.34f, 0.88f, 0.34f)
                rect(0.20f, 0.42f, 0.24f, 0.28f, solid = true)
            }
            GlyphKind.CHART -> {
                rect(0.16f, 0.56f, 0.14f, 0.28f, solid = true)
                rect(0.42f, 0.34f, 0.14f, 0.50f, solid = true)
                rect(0.68f, 0.18f, 0.14f, 0.66f, solid = true)
            }
        }
    }
}

// ---- Ikon aplikasi: kotak satu nada + inisial ----
@Composable
fun AppGlyph(label: String, size: Int = ICON_DP, accent: Color? = null) {
    val initial = label.trim().take(1).uppercase().ifBlank { "?" }
    Box(
        Modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 3.2f).dp))
            .background(accent?.copy(alpha = 0.20f) ?: SOverlay)
            .border(1.dp, SLine, RoundedCornerShape((size / 3.2f).dp)),
        contentAlignment = Alignment.Center,
    ) {
        Txt(initial, (size / 2.4f).toInt(), accent ?: SDim, FontWeight.SemiBold)
    }
}

// ---- Ikon huruf untuk berkas (menggantikan emoji) ----
@Composable
fun FileBadge(code: String, tone: Color = SAccent) {
    Box(
        Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(tone.copy(alpha = 0.18f))
            .border(1.dp, tone.copy(alpha = 0.38f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Txt(code, 9, tone, FontWeight.Bold)
    }
}

// ---- Kerangka jendela: dipakai semua isi jendela SukiOS ----
@Composable
fun WinPanel(
    title: String,
    subtitle: String? = null,
    actions: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(SSurface)) {
        Row(
            Modifier
                .padding(start = 14.dp, top = 10.dp, bottom = 9.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Txt(title, 14, SText, FontWeight.SemiBold)
                if (subtitle != null) Txt(subtitle, 10, SFaint)
            }
            if (actions != null) actions()
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(SLine))
        Box(Modifier.weight(1f).fillMaxWidth()) { content() }
    }
}

/** Kolom yang bisa digulir — dipakai hampir semua isi jendela. */
@Composable
fun ScrollArea(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        content = content,
    )
}

/** Kotak hasil: menampilkan keluaran perintah apa adanya. */
@Composable
fun ResultBox(text: String, tone: Color = SDim, maxHeight: Int = 220) {
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp, max = maxHeight.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(SBg)
            .border(1.dp, SLine, RoundedCornerShape(9.dp))
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
    ) {
        Txt(text.ifBlank { "(belum ada hasil)" }, 10, tone, maxLines = 400)
    }
}
