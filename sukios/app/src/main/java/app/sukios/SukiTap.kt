package app.sukios

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

// ============================================================================
// tap — satu-satunya cara SukiOS membuat sesuatu bisa disentuh.
//
// Menggantikan clickable: tanpa riak bawaan, dengan umpan balik yang konsisten:
// menyusut sedikit saat ditekan, bidang menyala tipis saat ditekan atau dilayangi
// kursor (mouse/trackpad), dan peran "tombol" untuk pembaca layar.
//
// Pasang di AWAL rantai modifier komponen supaya penyusutan mencakup seluruh
// komponen (latar, garis, isi), bukan hanya isinya.
// ============================================================================

private val PRESS_TINT = Color(0x26FFFFFF)
private val HOVER_TINT = Color(0x14FFFFFF)

fun Modifier.tap(
    enabled: Boolean = true,
    radius: Int = RADIUS_MD,
    label: String? = null,
    onLongPress: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val hover = remember { MutableInteractionSource() }
    val hovered by hover.collectIsHoveredAsState()
    val click by rememberUpdatedState(onClick)
    val long by rememberUpdatedState(onLongPress)
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.96f else 1f,
        animationSpec = tween(MOTION_INSTANT),
        label = "tap",
    )
    val hasLong = onLongPress != null

    Modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clip(RoundedCornerShape(radius.dp))
        .hoverable(hover)
        .drawWithContent {
            drawContent()
            if (enabled && (pressed || hovered)) drawRect(if (pressed) PRESS_TINT else HOVER_TINT)
        }
        .semantics(mergeDescendants = true) {
            role = Role.Button
            if (label != null) contentDescription = label
        }
        .pointerInput(enabled, hasLong) {
            if (enabled) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onLongPress = if (hasLong) {
                        { _ -> long?.invoke() }
                    } else {
                        null
                    },
                    onTap = { click() },
                )
            }
        }
}
