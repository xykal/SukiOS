package app.sukios.poc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos

/**
 * Pengukur FPS sederhana berbasis Choreographer (lewat withFrameNanos).
 *
 * Dipakai untuk NFR #1 di PRD: "drag/resize jendela >= 55 fps di perangkat mid-range".
 * Rata-rata dihitung dari 30 frame terakhir supaya angkanya stabil dibaca mata.
 */
@Composable
fun rememberFps(): Float {
    var fps by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        var last = 0L
        var acc = 0f
        var n = 0
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) {
                    val dtMs = (now - last) / 1_000_000f
                    if (dtMs > 0f && dtMs < 1000f) {
                        acc += 1000f / dtMs
                        n++
                    }
                    if (n >= 30) {
                        fps = acc / n
                        acc = 0f
                        n = 0
                    }
                }
                last = now
            }
        }
    }
    return fps
}
