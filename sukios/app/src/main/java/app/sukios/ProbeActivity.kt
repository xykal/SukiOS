package app.sukios

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * ProbeActivity — jendela uji milik SukiOS sendiri. SukiAuto membukanya lewat `am start --windowingMode 5`,
 * lalu activity ini melaporkan dari DALAM apakah ia benar-benar berada di mode jendela
 * (isInMultiWindowMode). Menutup dirinya sendiri beberapa detik kemudian. Tidak menyentuh data apa pun.
 */
class ProbeActivity : ComponentActivity() {

    private val ui = Handler(Looper.getMainLooper())
    private var multi by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        multi = isInMultiWindowMode
        setContent { ProbeScreen(multi) }
        ui.postDelayed({ report() }, SETTLE_MS)
        ui.postDelayed({ finishAndRemoveTask() }, LIFETIME_MS)
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        multi = isInMultiWindowMode
        report()
    }

    override fun onDestroy() {
        ui.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun report() {
        val c = resources.configuration
        SukiRuntime.probeSeen = ProbeSeen(isInMultiWindowMode, c.screenWidthDp, c.screenHeightDp, System.currentTimeMillis())
    }

    private companion object {
        const val SETTLE_MS = 400L
        const val LIFETIME_MS = 3_800L
    }
}

@androidx.compose.runtime.Composable
private fun ProbeScreen(multi: Boolean) {
    Column(
        Modifier.fillMaxSize().background(SBg).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LogoMark(44)
        Spacer(Modifier.height(10.dp))
        Txt("Uji jendela SukiOS", 15, SText, FontWeight.SemiBold, font = SukiBrand)
        Spacer(Modifier.height(4.dp))
        Txt(
            if (multi) "Mode jendela terdeteksi" else "Layar penuh",
            12, if (multi) SSuccess else SWarning, FontWeight.Medium, align = TextAlign.Center,
        )
        Spacer(Modifier.height(2.dp))
        Txt("Menutup otomatis...", 11, SFaint)
    }
}
