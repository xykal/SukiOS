package app.sukios

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Layar persiapan.
 *
 * Muncul sekali di awal. Semua langkah di sini OPSIONAL kecuali menandai
 * selesai: SukiOS tetap bisa dipakai tanpa Shizuku dan tanpa izin overlay.
 * Langkah-langkahnya nyata — setiap tombol membuka pengaturan sistem yang
 * benar, bukan sekadar penjelasan.
 */
@Composable
fun SetupLayer(app: SukiApp) {
    val ctx = LocalContext.current
    val shell = SukiShell.state.value
    var step by remember { mutableStateOf(0) }

    Box(Modifier.fillMaxSize().background(Color(0xCC000000))) {
        Box(Modifier.align(Alignment.Center)) {
            Panel(Modifier.width(560.dp), color = SSheet, radius = 16, pad = 20) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Dot(accentById(SukiRuntime.accentId), 10)
                    Spacer(Modifier.width(10.dp))
                    Txt("Selamat datang di SukiOS", 17, SText, FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Txt("langkah ${step + 1} dari 3", 10, SFaint)
                }
                Spacer(Modifier.height(14.dp))

                when (step) {
                    0 -> {
                        Txt("1. Jadikan SukiOS launcher", 13, SText, FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Txt(
                            "Supaya tombol Beranda membawa kall ke SukiOS, bukan ke launcher bawaan. " +
                                "Android akan menanyakan konfirmasi.",
                            11, SDim, maxLines = 4
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Btn("Buka pilihan launcher") { app.index.openDefaultLauncherSettings() }
                            BtnGhost("Sudah, lanjut") { step = 1 }
                        }
                    }
                    1 -> {
                        Txt("2. Taskbar melayang (opsional)", 13, SText, FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Txt(
                            "Izin \"tampil di atas aplikasi lain\" membuat taskbar tetap terlihat " +
                                "saat kall memakai aplikasi lain. Tanpa ini, semua fitur lain tetap jalan.",
                            11, SDim, maxLines = 4
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Btn("Beri izin") { app.index.openOverlaySettings() }
                            BtnGhost("Lewati") { step = 2 }
                        }
                    }
                    else -> {
                        Txt("3. Akses lanjutan (opsional)", 13, SText, FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Txt(
                            "Shizuku memberi SukiOS identitas shell untuk membuka izin yang biasanya " +
                                "diblokir: memaksa aplikasi bisa diubah ukurannya, memberi izin overlay " +
                                "otomatis, dan membaca diagnostik display. Perlu aplikasi Shizuku " +
                                "terpasang dan dijalankan.",
                            11, SDim, maxLines = 5
                        )
                        Spacer(Modifier.height(8.dp))
                        KeyValue("Status", shell.note)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Btn("Buka aplikasi Shizuku") { SukiShell.openShizukuApp() }
                            BtnGhost("Minta izin") { SukiRuntime.say(SukiShell.requestPermission()) }
                            BtnGhost("Periksa ulang") { SukiShell.refresh() }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BtnGhost("Kembali") { if (step > 0) step-- }
                    Spacer(Modifier.width(8.dp))
                    Btn(if (step == 2) "Mulai pakai SukiOS" else "Lanjut") {
                        if (step == 2) {
                            app.prefs.setSetupDone(true)
                            SukiRuntime.say("Selamat memakai SukiOS.")
                        } else {
                            step++
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    BtnGhost("Lewati semua") {
                        app.prefs.setSetupDone(true)
                        SukiRuntime.say("Persiapan dilewati. Bisa dibuka lagi dari Setelan.")
                    }
                }
            }
        }
    }
}
