package app.sukios

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================================
// SukiKit — komponen dasar tampilan Aurora: teks, permukaan kaca, tombol, kotak
// hasil, kerangka isi jendela. Kontrol lain ada di SukiControls.kt, ikon aplikasi
// dan logo di SukiIcons.kt. Semua komponen yang bisa disentuh memakai Modifier.tap.
//
// Catatan teknis: teks memakai Text dari material3 (parameter sederhana). Konstruktor
// TextStyle gaya lama disembunyikan di Compose 1.7, jadi gaya dibangun lewat
// TextStyle.Default.copy(...).
// ============================================================================

private val LIFT_TEXT = TextStyle.Default.copy(
    shadow = Shadow(color = Color(0xB3000000), offset = Offset(0f, 2f), blurRadius = 6f),
)

val SHEEN: Brush = Brush.verticalGradient(listOf(Color(0x14FFFFFF), Color(0x00FFFFFF)))
val EDGE: Brush = Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x0FFFFFFF)))
private val SHADOW_AMBIENT = Color(0x66000000)
private val SHADOW_SPOT = Color(0xB3000000)

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
    font: FontFamily = SukiSans,
    spacing: Float = 0f,
    lift: Boolean = false,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size.sp,
        fontWeight = weight,
        fontFamily = font,
        letterSpacing = spacing.sp,
        textAlign = align,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = if (lift) LIFT_TEXT else LocalTextStyle.current,
    )
}

@Composable
fun Label(text: String, modifier: Modifier = Modifier) {
    Txt(text.uppercase(), 11, SFaint, FontWeight.SemiBold, modifier = modifier, spacing = 0.9f)
}

/** Gaya untuk kolom ketik (BasicTextField); satu-satunya tempat yang bergantung pada bentuk API TextStyle. */
fun fieldStyle(size: Int = 13, mono: Boolean = false): TextStyle =
    TextStyle.Default.copy(
        color = SText,
        fontSize = size.sp,
        fontFamily = if (mono) SukiMono else SukiSans,
    )

// ---- Permukaan ----
/** Permukaan kaca: bidang gelap hampir buram, kilau tipis di atas, garis tepi gradien, bayangan netral. */
@Composable
fun Glass(
    modifier: Modifier = Modifier,
    radius: Int = RADIUS_LG,
    tint: Color = SSheet,
    alpha: Float = 0.94f,
    lift: Int = 20,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius.dp)
    Box(
        modifier
            .shadow(lift.dp, shape, clip = false, ambientColor = SHADOW_AMBIENT, spotColor = SHADOW_SPOT)
            .clip(shape)
            .background(tint.copy(alpha = alpha))
            .background(SHEEN)
            .border(1.dp, EDGE, shape),
        content = content,
    )
}

/** Kartu di dalam jendela atau panel. */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    color: Color = SElevated,
    radius: Int = RADIUS_MD,
    pad: Int = 12,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius.dp)
    Column(
        modifier
            .clip(shape)
            .background(color.copy(alpha = 0.72f))
            .border(1.dp, SLineSoft, shape)
            .padding(pad.dp),
        content = content,
    )
}

@Composable
fun HLine(modifier: Modifier = Modifier) = Box(modifier.fillMaxWidth().height(1.dp).background(SLineSoft))

@Composable
fun Dot(color: Color, size: Int = 8, modifier: Modifier = Modifier) {
    Box(modifier.size(size.dp).clip(CircleShape).background(color))
}

@Composable
fun ColSpacer(h: Int = 8) = Spacer(Modifier.height(h.dp))

// ---- Tombol ----
@Composable
fun Btn(
    label: String,
    primary: Boolean = true,
    enabled: Boolean = true,
    icon: GlyphKind? = null,
    danger: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val ac = accentNow()
    val shape = RoundedCornerShape(RADIUS_MD.dp)
    val fg = when {
        !enabled -> SFaint
        danger -> SDangerText
        primary -> ac.on
        else -> SText
    }
    val skin = when {
        !enabled -> Modifier.background(SElevated, shape).border(1.dp, SLineSoft, shape)
        danger -> Modifier.background(SDanger.copy(alpha = 0.16f), shape).border(1.dp, SDanger.copy(alpha = 0.45f), shape)
        primary -> Modifier.background(ac.fill, shape).background(SHEEN, shape).border(1.dp, EDGE, shape)
        else -> Modifier.background(Color(0x14FFFFFF), shape).border(1.dp, SLine, shape)
    }
    Row(
        modifier
            .tap(enabled = enabled, radius = RADIUS_MD, label = label, onClick = onClick)
            .heightIn(min = 40.dp)
            .then(skin)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Glyph(icon, 16, fg)
            Spacer(Modifier.width(7.dp))
        }
        Txt(label, 13, fg, FontWeight.SemiBold)
    }
}

@Composable
fun BtnGhost(
    label: String,
    enabled: Boolean = true,
    icon: GlyphKind? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) = Btn(label, primary = false, enabled = enabled, icon = icon, modifier = modifier, onClick = onClick)

/** Tombol ikon persegi bulat. [label] wajib: dibaca pembaca layar. */
@Composable
fun IconBtn(
    kind: GlyphKind,
    label: String,
    modifier: Modifier = Modifier,
    size: Int = 40,
    dim: Int = 18,
    active: Boolean = false,
    tint: Color = SDim,
    onLongPress: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val ac = accentNow()
    val shape = RoundedCornerShape(RADIUS_SM.dp)
    Box(
        modifier
            .tap(radius = RADIUS_SM, label = label, onLongPress = onLongPress, onClick = onClick)
            .size(size.dp)
            .then(if (active) Modifier.background(ac.main.copy(alpha = 0.18f), shape) else Modifier),
        contentAlignment = Alignment.Center,
    ) { Glyph(kind, dim, if (active) ac.text else tint) }
}
