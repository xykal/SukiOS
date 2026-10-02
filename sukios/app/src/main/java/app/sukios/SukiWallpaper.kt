package app.sukios

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Shader
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

// ============================================================================
// Wallpaper Aurora.
//
// Lapisan dari belakang ke depan: gradien dasar gelap, beberapa cahaya lunak
// berbentuk elips (ungu, teal, merah muda, plus pita tipis yang miring), vinyet,
// dan butiran halus. Posisi dan radius berupa pecahan ukuran layar, jadi hasilnya
// sama di layar apa pun. Cahaya bergeser sangat pelan; dimatikan di perangkat
// RAM rendah (mode Go) atau lewat Setelan.
//
// Gradien radial berakhir di warna yang SAMA dengan alpha 0 (bukan Transparent
// hitam), kalau tidak tepinya menggelap kelabu.
// ============================================================================

private const val RAD_TO_DEG = 57.29578f
private const val DRIFT_MS = 36_000

private fun grainBrush(): Brush? = runCatching {
    val n = 96
    val px = IntArray(n * n)
    val rnd = java.util.Random(7)
    for (i in px.indices) {
        val v = rnd.nextInt(160)
        px[i] = (0xFF shl 24) or (v shl 16) or (v shl 8) or v
    }
    val bmp = Bitmap.createBitmap(px, n, n, Bitmap.Config.ARGB_8888)
    ShaderBrush(BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT))
}.getOrNull()

@Composable
fun Wallpaper(id: String, modifier: Modifier = Modifier, motion: Boolean = true) {
    val spec = remember(id) { wallById(id) }
    val colors = remember(spec) { spec.lights.map { Color(it.color) } }
    val base0 = remember(spec) { Color(spec.base0) }
    val base1 = remember(spec) { Color(spec.base1) }
    val grain = remember { grainBrush() }
    val clear = remember { Color(0x00000000) }
    val shade = remember { Color(0x73000000) }

    val phase: State<Float>? = if (motion) {
        rememberInfiniteTransition(label = "aurora").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(DRIFT_MS, easing = LinearEasing), RepeatMode.Restart),
            label = "drift",
        )
    } else {
        null
    }

    Canvas(modifier.fillMaxSize()) {
        val w = this.size.width
        val h = this.size.height
        drawRect(Brush.linearGradient(listOf(base0, base1), start = Offset.Zero, end = Offset(w, h)))

        val t = (phase?.value ?: 0f) * 2f * PI.toFloat()
        spec.lights.forEachIndexed { i, l ->
            val c = colors[i]
            val dx = if (phase != null) sin(t + i * 1.7f) * w * 0.025f else 0f
            val dy = if (phase != null) cos(t + i * 2.3f) * h * 0.03f else 0f
            val rx = w * l.rx
            val ry = h * l.ry
            withTransform({
                translate(w * l.x + dx, h * l.y + dy)
                rotate(l.rot * RAD_TO_DEG, pivot = Offset.Zero)
                scale(1f, ry / rx, pivot = Offset.Zero)
            }) {
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to c.copy(alpha = l.alpha),
                        0.45f to c.copy(alpha = l.alpha * 0.5f),
                        1f to c.copy(alpha = 0f),
                        center = Offset.Zero,
                        radius = rx,
                    ),
                    radius = rx,
                    center = Offset.Zero,
                )
            }
        }

        drawRect(
            Brush.radialGradient(
                0f to clear,
                0.6f to clear,
                1f to shade,
                center = Offset(w / 2f, h / 2f),
                radius = hypot(w, h) * 0.5f,
            ),
        )
        grain?.let { drawRect(it, alpha = 0.022f) }
    }
}
