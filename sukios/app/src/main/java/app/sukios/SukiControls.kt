package app.sukios

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

// ============================================================================
// Kontrol interaktif Aurora: chip, saklar, penggeser, kolom cari, baris setelan,
// pil status. Semuanya memakai Modifier.tap dan membaca aksen aktif.
// ============================================================================

@Composable
fun Chip(
    label: String,
    active: Boolean = false,
    icon: GlyphKind? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val ac = accentNow()
    val shape = RoundedCornerShape(RADIUS_SM.dp)
    val fg = if (active) ac.text else SDim
    Row(
        modifier
            .tap(radius = RADIUS_SM, label = label, onClick = onClick)
            .heightIn(min = 32.dp)
            .background(if (active) ac.main.copy(alpha = 0.18f) else Color(0x0FFFFFFF), shape)
            .border(1.dp, if (active) ac.main.copy(alpha = 0.55f) else SLine, shape)
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Glyph(icon, 14, fg)
            Spacer(Modifier.width(6.dp))
        }
        Txt(label, 12, fg, FontWeight.Medium)
    }
}

@Composable
fun SukiSwitch(on: Boolean, onChange: (Boolean) -> Unit, label: String? = null) {
    val ac = accentNow()
    val x by animateDpAsState(if (on) 20.dp else 2.dp, tween(MOTION_FAST), label = "thumb")
    val shape = RoundedCornerShape(13.dp)
    val track: Brush = if (on) Brush.horizontalGradient(listOf(ac.main, ac.second)) else SolidColor(Color(0x29FFFFFF))
    Box(
        Modifier
            .tap(radius = RADIUS_PILL, label = label, onClick = { onChange(!on) })
            .size(width = 44.dp, height = 26.dp)
            .background(track, shape)
            .border(1.dp, SLine, shape),
    ) {
        Box(
            Modifier
                .offset(x = x, y = 2.dp)
                .size(22.dp)
                .shadow(3.dp, CircleShape, clip = false)
                .background(Color.White, CircleShape),
        )
    }
}

/** Baris setelan: judul + keterangan di kiri, kontrol di kanan. */
@Composable
fun SettingRow(
    title: String,
    desc: String? = null,
    trailing: @Composable () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Txt(title, 13, SText, FontWeight.Medium, maxLines = 2)
            if (desc != null) Txt(desc, 12, SFaint, maxLines = 3)
        }
        trailing()
    }
}

@Composable
fun ToggleRow(label: String, on: Boolean, desc: String? = null, onChange: (Boolean) -> Unit) {
    SettingRow(label, desc) { SukiSwitch(on, onChange, label) }
}

/** Penggeser 0..1 (volume dan sejenisnya). Sentuh atau geser di mana saja pada batang. */
@Composable
fun SukiSlider(value: Float, onChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    val ac = accentNow()
    val v = value.coerceIn(0f, 1f)
    val change by rememberUpdatedState(onChange)
    var width by remember { mutableStateOf(1f) }
    Box(
        modifier
            .height(28.dp)
            .onSizeChanged { width = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    change((down.position.x / width).coerceIn(0f, 1f))
                    drag(down.id) { c ->
                        change((c.position.x / width).coerceIn(0f, 1f))
                        c.consume()
                    }
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Color(0x29FFFFFF)))
        Box(
            Modifier
                .fillMaxWidth(v)
                .height(6.dp)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(ac.main, ac.second))),
        )
        Box(
            Modifier
                .offset { IntOffset(((width - 18.dp.toPx()) * v).roundToInt(), 0) }
                .size(18.dp)
                .shadow(3.dp, CircleShape, clip = false)
                .background(Color.White, CircleShape),
        )
    }
}

/** Kolom cari/ketik satu baris dengan ikon, hint, dan kursor beraksen. */
@Composable
fun SearchField(
    value: String,
    onChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    icon: GlyphKind? = GlyphKind.SEARCH,
    mono: Boolean = false,
    onDone: (() -> Unit)? = null,
) {
    val ac = accentNow()
    val shape = RoundedCornerShape(RADIUS_MD.dp)
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = fieldStyle(13, mono),
        cursorBrush = SolidColor(ac.main),
        keyboardOptions = KeyboardOptions(imeAction = if (onDone != null) ImeAction.Done else ImeAction.Search),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }, onSearch = { onDone?.invoke() }),
        modifier = modifier,
        decorationBox = { inner ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(shape)
                    .background(SBg.copy(alpha = 0.7f))
                    .border(1.dp, SLine, shape)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Glyph(icon, 16, SFaint)
                    Spacer(Modifier.width(8.dp))
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) Txt(hint, 13, SFaint)
                    inner()
                }
            }
        },
    )
}

/** Pil status kecil: titik berwarna + teks. */
@Composable
fun StatusPill(text: String, tone: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(RADIUS_PILL.dp)
    Row(
        modifier
            .clip(shape)
            .background(tone.copy(alpha = 0.14f))
            .border(1.dp, tone.copy(alpha = 0.40f), shape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Dot(tone, 6)
        Spacer(Modifier.width(6.dp))
        Txt(text, 11, mix(tone, SText, 0.35f), FontWeight.Medium)
    }
}

/** Satu item sidebar (Setelan): ikon + label, menyala bila aktif. */
@Composable
fun SideItem(kind: GlyphKind, label: String, active: Boolean, onClick: () -> Unit) {
    val ac = accentNow()
    val shape = RoundedCornerShape(RADIUS_SM.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .tap(radius = RADIUS_SM, label = label, onClick = onClick)
            .heightIn(min = 40.dp)
            .then(if (active) Modifier.background(ac.main.copy(alpha = 0.16f), shape) else Modifier)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Glyph(kind, 18, if (active) ac.text else SDim)
        Spacer(Modifier.width(10.dp))
        Txt(label, 13, if (active) SText else SDim, if (active) FontWeight.SemiBold else FontWeight.Medium)
    }
}
