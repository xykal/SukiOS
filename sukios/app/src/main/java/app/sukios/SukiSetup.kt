package app.sukios

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// ============================================================================
// Persiapan: layar yang muncul saat pertama kali dan bisa dibuka lagi dari Setelan, tray, atau desktop.
//
// Tidak ada wizard "lanjut-lanjut": semua langkah berjalan otomatis begitu Shizuku siap, dan layar ini
// menampilkan keadaannya secara langsung. Yang perlu dilakukan pengguna hanya menyalakan Shizuku dan
// menyetujui izinnya. Aplikasi tetap bisa dipakai tanpa itu; hanya jalur jendela pihak ketiga yang tertutup.
// ============================================================================

@Composable
fun SetupLayer(app: SukiApp) {
    val prefs = app.prefs
    val shell by SukiShell.state.collectAsState()
    val auto by SukiAuto.state.collectAsState()
    val probeRaw by prefs.probe.collectAsState()
    val setupDone by prefs.setupDone.collectAsState()
    var probedOnce by remember { mutableStateOf(false) }
    val status = WindowStatusLogic.of(
        installed = shell.installed, alive = shell.binderAlive, granted = shell.granted, bound = shell.serviceBound,
        coreOn = auto.coreOn, setupRunning = auto.running, probe = WindowStatusLogic.parseProbe(probeRaw),
    )

    // Setelah setelan jendela menyala dan belum pernah diuji, buktikan sekali secara otomatis.
    LaunchedEffect(shell.ready, auto.coreOn, auto.running) {
        if (shell.ready && auto.coreOn && !auto.running && !auto.probing && probeRaw.isBlank() && !probedOnce) {
            probedOnce = true
            app.scope.launch { SukiAuto.probe(app) }
        }
    }

    fun finish() {
        prefs.setSetupDone(true)
        SukiRuntime.setupOpen = false
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(SScrim).pointerInput(Unit) { detectTapGestures { } }) {
        Glass(
            Modifier
                .align(Alignment.Center)
                .width(minOf(700.dp, maxWidth - 24.dp))
                .height(minOf(344.dp, maxHeight - 20.dp)),
            radius = 20, lift = 28,
        ) {
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.width(214.dp).fillMaxHeight().background(SBg.copy(alpha = 0.40f)).padding(18.dp)) {
                    LogoMark(52)
                    Spacer(Modifier.height(12.dp))
                    Txt(if (setupDone) "Persiapan" else "Selamat datang", 20, SText, FontWeight.ExtraBold, font = SukiBrand)
                    Spacer(Modifier.height(6.dp))
                    Txt(
                        "SukiOS membuka aplikasi sebagai jendela lewat Shizuku. Semua langkah berjalan otomatis " +
                            "begitu Shizuku siap; kamu hanya perlu menyalakannya dan menyetujui izinnya.",
                        12, SDim, maxLines = 8,
                    )
                    Spacer(Modifier.weight(1f))
                    StatusPill(status.label, toneColor(status.tone))
                }
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 18.dp, end = 18.dp, top = 14.dp)) {
                        Label("Shizuku")
                        Spacer(Modifier.height(2.dp))
                        Checklist(shizukuRows(shell))
                        if (!shell.ready) {
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Btn(
                                    if (shell.installed) "Buka Shizuku" else "Unduh Shizuku",
                                    icon = GlyphKind.EXTERNAL,
                                ) { SukiShell.openShizukuApp() }
                                BtnGhost("Minta izin", enabled = shell.binderAlive && !shell.granted) {
                                    SukiRuntime.say(SukiShell.requestPermission())
                                }
                                BtnGhost("Periksa ulang") { SukiShell.refresh() }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Label("Mode jendela")
                        Spacer(Modifier.height(2.dp))
                        if (auto.steps.isEmpty()) {
                            Txt("Menunggu Shizuku siap.", 12, SFaint, modifier = Modifier.padding(vertical = 6.dp))
                        } else {
                            Checklist(stepRows(auto.steps))
                        }
                        probeLine(probeRaw)?.let {
                            Spacer(Modifier.height(6.dp))
                            Txt(it, 11, SFaint, maxLines = 4)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    HLine()
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BtnGhost("Uji jendela", icon = GlyphKind.PLAY, enabled = shell.ready && !auto.probing) {
                            app.scope.launch {
                                val r = SukiAuto.probe(app)
                                SukiRuntime.say("Uji jendela: ${r.verdict}. ${r.note}", if (r.ok) Tone.OK else Tone.WARN)
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        if (!setupDone) BtnGhost("Lewati") { finish() }
                        Btn(if (setupDone) "Tutup" else "Mulai pakai SukiOS") { finish() }
                    }
                }
            }
        }
    }
}
