package app.sukios

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.os.BatteryManager
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

// ============================================================================
// Tray di ujung kanan taskbar: status jendela (perisai), jaringan, baterai, jam dan tanggal.
// Seluruhnya satu area sentuh yang membuka panel pintasan.
// ============================================================================

@Composable
fun Tray(app: SukiApp) {
    val shell by SukiShell.state.collectAsState()
    val auto by SukiAuto.state.collectAsState()
    val probeRaw by app.prefs.probe.collectAsState()
    val online by rememberOnline()
    val battery by rememberBattery()
    val status = WindowStatusLogic.of(
        installed = shell.installed, alive = shell.binderAlive, granted = shell.granted, bound = shell.serviceBound,
        coreOn = auto.coreOn, setupRunning = auto.running, probe = WindowStatusLogic.parseProbe(probeRaw),
    )
    val ctx = LocalContext.current
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(60_000L - now % 60_000L + 30L)
        }
    }
    val is24 = remember { DateFormat.is24HourFormat(ctx) }
    val timeFmt = remember(is24) { SimpleDateFormat(if (is24) "HH:mm" else "h:mm a", Locale.getDefault()) }
    val dateFmt = remember { SimpleDateFormat("EEE, d MMM", Locale.getDefault()) }

    Row(
        Modifier
            .tap(radius = RADIUS_MD, label = "Pengaturan cepat") {
                val wasOpen = SukiRuntime.quickOpen
                SukiRuntime.closePanels()
                SukiRuntime.quickOpen = !wasOpen
            }
            .height(42.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Glyph(if (status.tone == Tone.OK) GlyphKind.SHIELD_CHECK else GlyphKind.SHIELD, 18, toneColor(status.tone))
        Glyph(if (online) GlyphKind.WIFI else GlyphKind.WIFI_OFF, 18, if (online) SDim else SFaint)
        BatteryMeter(battery.first, battery.second)
        Column(horizontalAlignment = Alignment.End) {
            Txt(timeFmt.format(Date(now)), 12, SText, FontWeight.SemiBold)
            Txt(dateFmt.format(Date(now)), 11, SDim)
        }
    }
}

/** Ikon baterai: badan bulat, isi sesuai persen, petir saat mengisi. */
@Composable
fun BatteryMeter(level: Int, charging: Boolean) {
    val tone = when {
        charging -> SSuccess
        level in 0..20 -> SDanger
        else -> SDim
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(width = 24.dp, height = 12.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .fillMaxSize()
                    .border(1.2.dp, tone, RoundedCornerShape(3.5.dp))
                    .padding(2.dp),
            ) {
                val f = (level.coerceIn(0, 100) / 100f).coerceAtLeast(0.06f)
                Box(Modifier.fillMaxHeight().fillMaxWidth(f).clip(RoundedCornerShape(1.5.dp)).background(tone))
            }
            if (charging) Glyph(GlyphKind.BOLT, 10, SInk)
        }
        Box(Modifier.padding(start = 1.dp).size(width = 2.dp, height = 5.dp).background(tone, RoundedCornerShape(1.dp)))
        Spacer(Modifier.width(5.dp))
        Txt(if (level < 0) "--%" else "$level%", 11, SDim, FontWeight.Medium)
    }
}

@Composable
private fun rememberOnline(): State<Boolean> {
    val ctx = LocalContext.current
    return produceState(initialValue = true) {
        val cm = ctx.getSystemService(ConnectivityManager::class.java)
        if (cm != null) {
            value = cm.activeNetwork != null
            val cb = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    value = true
                }

                override fun onLost(network: Network) {
                    value = cm.activeNetwork != null
                }
            }
            runCatching { cm.registerDefaultNetworkCallback(cb) }
            awaitDispose { runCatching { cm.unregisterNetworkCallback(cb) } }
        }
    }
}

/** (persen, sedang mengisi). Persen -1 bila tidak terbaca. */
@Composable
internal fun rememberBattery(): State<Pair<Int, Boolean>> {
    val ctx = LocalContext.current
    return produceState(initialValue = Pair(-1, false)) {
        fun read(i: Intent?): Pair<Int, Boolean> {
            val level = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = i?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            val st = i?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val pct = if (level >= 0 && scale > 0) level * 100 / scale else -1
            return Pair(pct, st == BatteryManager.BATTERY_STATUS_CHARGING || st == BatteryManager.BATTERY_STATUS_FULL)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, i: Intent?) {
                value = read(i)
            }
        }
        runCatching { ctx.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) }
            .getOrNull()?.let { value = read(it) }
        awaitDispose { runCatching { ctx.unregisterReceiver(receiver) } }
    }
}
