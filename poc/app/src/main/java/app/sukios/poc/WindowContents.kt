package app.sukios.poc

import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.view.View

// ============================================================================
// Isi tiap jendela SukiOS
// ============================================================================

@Composable
fun WindowContent(st: SukiState, w: WinState) {
    when (w.kind) {
        WinKind.TESTS -> TestsContent(st)
        WinKind.PICKER -> PickerContent(st, w)
        WinKind.MONITOR -> MonitorContent(st)
        WinKind.ACCESS -> AccessContent(st)
        WinKind.EMBED -> EmbedContent(st)
        WinKind.FILES -> FilesContent()
        WinKind.NOTES -> NotesContent()
    }
}

/** Menjalankan perintah shell lewat Shizuku dan mencatat hasilnya. */
private fun runShell(st: SukiState, label: String, cmd: String, onNote: (String) -> Unit) {
    val result = ShizukuEngine.shell(cmd)
    val line = "$label — ${if (result.ok) "OK" else "GAGAL"}: ${result.brief()}"
    st.log("shell", line)
    onNote(line)
}

// ------------------------------------------------------------ baris penilaian
@Composable
private fun VerdictRow(st: SukiState, lane: String, pkg: String? = null) {
    val key = if (pkg.isNullOrEmpty()) lane else "$lane:$pkg"
    Row(
        Modifier.fillMaxWidth().padding(top = 6.dp).horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Chip("Berhasil", SukiSuccess, st.verdicts[key] == "OK") { st.verdict(key, "OK") }
        Chip("Hitam/kosong", SukiWarning, st.verdicts[key] == "HITAM") { st.verdict(key, "HITAM") }
        Chip("Gagal", SukiDanger, st.verdicts[key] == "GAGAL") { st.verdict(key, "GAGAL") }
    }
}

@Composable
private fun LogList(st: SukiState, max: Int) {
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0B0D0F))
            .padding(8.dp)
    ) {
        Txt("Log (${st.logs.size})", 10, SukiTextDisabled, FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        st.logs.takeLast(max).forEach { l ->
            Txt("[${l.stamp}] ${l.tag}: ${l.msg}", 9, SukiTextDim, maxLines = 2)
        }
    }
}

// ================================================================ PANDUAN UJI
@Composable
private fun TestsContent(st: SukiState) {
    val ctx = LocalContext.current

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)
    ) {
        Txt("Panduan Uji SukiOS PoC", 15, SukiText, FontWeight.Bold)
        Txt(
            "Uji 6 jalur windowing. Setiap jalur punya tombol penilaian — isi semuanya, " +
                "lalu buka Monitor Perangkat lalu Salin laporan, dan tempel hasilnya.",
            10, SukiTextDisabled
        )
        Spacer(Modifier.height(12.dp))

        Panel {
            Txt("Mode Desktop (baru)", 13, SukiAccent, FontWeight.Bold)
            Txt(
                "Dua tombol di taskbar kanan: Landscape (kunci orientasi) dan Desktop " +
                    "(sembunyikan status/navigation bar). Keduanya aktif secara default di build ini. " +
                    "Kalau desktop terasa penuh layar, itu memang tujuannya.",
                10, SukiTextDim
            )
            Spacer(Modifier.height(4.dp))
            Txt(
                if (st.goMode)
                    "Perangkat ini terdeteksi kelas Go/RAM rendah: batas 3 jendela dan efek dekoratif dimatikan."
                else
                    "Perangkat ini normal: batas 8 jendela, efek tetap ringan (semua matte, tanpa blur berat).",
                10, if (st.goMode) SukiWarning else SukiSuccess
            )
        }
        Spacer(Modifier.height(10.dp))

        Panel {
            Txt("LANE 1 — Window manager SukiOS", 13, SukiAccent, FontWeight.Bold)
            Txt(
                "Geser jendela ini ke tepi kiri/kanan layar lalu harus menempel separuh. Ke sudut lalu " +
                    "seperempat. Resize dari tepi/sudut. Perhatikan angka FPS di taskbar saat menggeser: " +
                    "target minimal 55 fps.",
                10, SukiTextDim
            )
            VerdictRow(st, "lane1-wm")
        }
        Spacer(Modifier.height(10.dp))

        Panel {
            Txt("LANE 2 — App pihak ketiga jadi jendela (freeform)", 12, SukiInfo, FontWeight.Bold)
            Txt(
                "Buka Daftar Aplikasi lalu pilih app lalu tekan tombol Freeform. " +
                    "Kalau app muncul fullscreen, artinya HP ini tidak mengizinkan freeform untuk app biasa. " +
                    "Setelah Shizuku aktif, coba lagi lewat LANE 6 (force-resizable).",
                10, SukiTextDim
            )
            Row(Modifier.padding(top = 6.dp)) {
                Btn("Buka Daftar Aplikasi", SukiAccent2) { st.open(WinKind.PICKER, 900, 700) }
            }
            VerdictRow(st, "lane2-freeform")
        }
        Spacer(Modifier.height(10.dp))

        Panel {
            Txt("LANE 3 — Split screen", 12, SukiInfo, FontWeight.Bold)
            Txt(
                "Buka app apa saja (tombol Fullscreen di Daftar Aplikasi), lalu tahan app dari layar Recents " +
                    "lalu pilih Split screen. Ini cara paling andal untuk multitasking di HP biasa.",
                10, SukiTextDim
            )
            VerdictRow(st, "lane3-split")
        }
        Spacer(Modifier.height(10.dp))

        Panel {
            Txt("LANE 4 — Embed app di dalam jendela (ActivityView)", 12, SukiWarning, FontWeight.Bold)
            Txt(
                "Jalur paling ambisius: menjalankan app lain di dalam jendela SukiOS. " +
                    "ActivityView adalah kelas sistem dan platform membatasi embedding ke app yang " +
                    "mendeklarasikan allowEmbedded. PoC ini membuktikan apakah benar-benar ditolak di HP ini.",
                10, SukiTextDim
            )
            Row(Modifier.padding(top = 6.dp)) {
                Btn("Buka Uji Embed", SukiWarning) { st.open(WinKind.EMBED, 900, 700) }
            }
            VerdictRow(st, "lane4-embed")
        }
        Spacer(Modifier.height(10.dp))

        Panel {
            Txt("LANE 5 — Taskbar melayang di atas app lain (overlay)", 12, SukiSuccess, FontWeight.Bold)
            Txt(
                "Jalur fallback: app tetap fullscreen, taskbar SukiOS mengapung di atasnya. " +
                    "Buka Monitor Perangkat lalu beri izin overlay lalu Mulai overlay lalu pindah ke app lain. " +
                    "Dengan Shizuku, izin overlay bisa diberikan otomatis dari LANE 6.",
                10, SukiTextDim
            )
            Row(Modifier.padding(top = 6.dp)) {
                Btn("Buka Monitor", SukiSuccess) { st.open(WinKind.MONITOR, 900, 700) }
                Spacer(Modifier.width(6.dp))
                BtnGhost("Bagikan laporan") { st.log("report", shareReport(ctx, st)) }
            }
            VerdictRow(st, "lane5-overlay")
        }
        Spacer(Modifier.height(10.dp))

        Panel {
            Txt("LANE 6 — Akses Lanjutan lewat Shizuku (baru)", 12, Color(0xFFA08F76), FontWeight.Bold)
            Txt(
                "Membuka pintu yang diblokir platform untuk app biasa: force-resizable (app pihak ketiga " +
                    "boleh di-resize), izin overlay otomatis, dan peluncuran app ke display tertentu. " +
                    "Terutama berguna di perangkat Go yang multi-window-nya dimatikan sistem.",
                10, SukiTextDim
            )
            Spacer(Modifier.height(4.dp))
            Txt(ShizukuEngine.statusLine(ctx), 10, if (st.shizukuReady) SukiSuccess else SukiWarning, maxLines = 2)
            Row(Modifier.padding(top = 6.dp)) {
                Btn("Buka Akses Lanjutan", Color(0xFFA08F76)) { st.open(WinKind.ACCESS, 900, 700) }
            }
            VerdictRow(st, "lane6-shizuku")
        }
    }
}

// ============================================================ DAFTAR APLIKASI
@Composable
private fun PickerContent(st: SukiState, w: WinState) {
    val ctx = LocalContext.current
    var selected by remember { mutableStateOf<AppEntry?>(st.apps.firstOrNull()) }
    var note by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(10.dp)) {
        Txt("Daftar Aplikasi (${st.apps.size})", 14, SukiText, FontWeight.Bold)
        Txt("Pilih satu app, lalu tekan tombol uji. Catat hasilnya.", 10, SukiTextDisabled)
        Spacer(Modifier.height(8.dp))

        LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
            items(st.apps) { a ->
                val isSel = selected?.pkg == a.pkg
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) SukiAccent.copy(alpha = 0.18f) else Color.Transparent)
                        .clickable { selected = a }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Txt(a.label, 12, SukiText, FontWeight.Medium, maxLines = 1)
                    Spacer(Modifier.width(8.dp))
                    Txt(a.pkg, 9, SukiTextDisabled, maxLines = 1, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        val sel = selected
        if (sel == null) {
            Txt("Belum ada app terpilih.", 11, SukiWarning)
        } else {
            Txt("Terpilih: ${sel.label}", 11, SukiAccent2, FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Btn("Fullscreen", SukiAccent2) {
                    note = launchPlain(ctx, sel.pkg)
                    st.log("lane2", "fullscreen ${sel.pkg}: $note")
                }
                Btn("Freeform", SukiInfo) {
                    note = launchWithBounds(ctx, sel.pkg, freeformMode = true, deskW = w.w, deskH = w.h)
                    st.log("lane2", "freeform ${sel.pkg}: $note")
                }
                Btn("Split screen", SukiWarning) {
                    note = launchSplit(ctx, sel.pkg)
                    st.log("lane3", "split ${sel.pkg}: $note")
                }
                BtnGhost("Embed") { st.open(WinKind.EMBED, w.w, w.h) }
            }
            Spacer(Modifier.height(8.dp))
            Txt("Penilaian untuk: ${sel.label}", 10, SukiTextDim)
            VerdictRow(st, "app-freeform", sel.pkg)
            VerdictRow(st, "app-split", sel.pkg)
        }

        Spacer(Modifier.height(8.dp))
        Txt(note, 10, SukiAccent2, maxLines = 3)
        Spacer(Modifier.height(6.dp))
        LogList(st, 6)
    }
}

// =========================================================== MONITOR PERANGKAT
@Composable
private fun MonitorContent(st: SukiState) {
    val ctx = LocalContext.current
    var note by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(10.dp)
    ) {
        Txt("Monitor Perangkat", 14, SukiText, FontWeight.Bold)
        Txt("Hasil probe kapabilitas — dibaca otomatis saat app dibuka.", 10, SukiTextDisabled)
        Spacer(Modifier.height(8.dp))

        st.probes.forEach { p -> KV(p.label, p.value, p.ok) }

        Spacer(Modifier.height(12.dp))
        Panel {
            Txt("Mode tampilan", 12, SukiAccent, FontWeight.Bold)
            Txt("Sama dengan dua tombol di taskbar kanan.", 10, SukiTextDim)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ToggleChip("Landscape terkunci", st.lockLandscape) { st.lockLandscape = !st.lockLandscape }
                ToggleChip("Desktop penuh", st.fullDesktop) { st.fullDesktop = !st.fullDesktop }
            }
            Spacer(Modifier.height(6.dp))
            Txt(
                "Landscape memakai SCREEN_ORIENTATION_SENSOR_LANDSCAPE (boleh dua arah, tidak pernah portrait). " +
                    "Desktop penuh memakai WindowInsetsController (bar sistem tersembunyi, geser dari tepi untuk memunculkan).",
                9, SukiTextDisabled
            )
        }

        Spacer(Modifier.height(10.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Btn("Probe ulang") {
                runProbe(ctx, st.act, st)
                refreshShizuku(ctx, st)
                note = "Probe dijalankan ulang."
            }
            Btn("Salin laporan", SukiAccent2) { note = copyReport(ctx, st) }
            BtnGhost("Bagikan") { note = shareReport(ctx, st) }
        }

        Spacer(Modifier.height(12.dp))
        Panel {
            Txt("Uji overlay taskbar (LANE 5)", 12, SukiSuccess, FontWeight.Bold)
            Txt(
                "Taskbar SukiOS digambar di atas app apa pun. Butuh izin Tampilkan di atas app lain " +
                    "(bisa diberikan otomatis lewat Akses Lanjutan bila Shizuku aktif).",
                10, SukiTextDim
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Btn("Mulai overlay", SukiSuccess) { note = startOverlay(ctx) }
                BtnGhost("Izin overlay") { openOverlaySettings(ctx); note = "Buka pengaturan izin overlay." }
                BtnGhost("Hentikan") { note = stopOverlay(ctx) }
            }
            VerdictRow(st, "lane5-overlay")
        }

        Spacer(Modifier.height(10.dp))
        Txt(note, 10, SukiAccent2, maxLines = 3)
        Spacer(Modifier.height(10.dp))
        LogList(st, 14)
    }
}

// ============================================================ AKSES LANJUTAN
@Composable
private fun AccessContent(st: SukiState) {
    val ctx = LocalContext.current
    var note by remember { mutableStateOf("Langkah: pasang app Shizuku, aktifkan servisnya, lalu Minta izin.") }
    var displayId by remember { mutableStateOf("0") }
    var target by remember { mutableStateOf(st.apps.firstOrNull()?.pkg ?: "") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(10.dp)) {
        Txt("Akses Lanjutan — engine Shizuku", 14, SukiText, FontWeight.Bold)
        Txt(
            "Shizuku menjalankan perintah dengan identitas shell (uid 2000). Ini membuka pintu yang " +
                "diblokir untuk app biasa: force-resizable, appops, dan peluncuran app ke display tertentu. " +
                "Opsional — tanpa Shizuku, seluruh fitur inti SukiOS tetap berjalan.",
            10, SukiTextDisabled
        )
        Spacer(Modifier.height(10.dp))

        Panel {
            Txt("Status", 12, SukiAccent, FontWeight.Bold)
            KV("Shizuku terpasang", if (st.shizukuInstalled) "ya" else "tidak", st.shizukuInstalled)
            KV("Binder aktif", if (ShizukuEngine.binderAlive()) "ya" else "tidak", ShizukuEngine.binderAlive())
            KV("Versi API", st.shizukuVersion.toString(), st.shizukuVersion >= 11)
            KV("Uid shell", st.shizukuUid.toString(), st.shizukuUid == 2000)
            KV("Izin diberikan", if (st.shizukuReady) "ya" else "belum", st.shizukuReady)
            Spacer(Modifier.height(6.dp))
            Txt(ShizukuEngine.statusLine(ctx), 10, if (st.shizukuReady) SukiSuccess else SukiWarning, maxLines = 2)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Btn("Perbarui status") {
                    refreshShizuku(ctx, st)
                    note = ShizukuEngine.statusLine(ctx)
                }
                Btn("Minta izin", SukiAccent2) {
                    if (!ShizukuEngine.binderAlive()) {
                        note = "Servis Shizuku belum aktif. Buka app Shizuku lebih dulu."
                    } else {
                        ShizukuEngine.requestPermission()
                        note = "Permintaan izin dikirim — konfirmasi muncul dari dalam app Shizuku."
                    }
                }
                BtnGhost("Buka Shizuku") {
                    note = if (ShizukuEngine.openShizukuApp(ctx)) {
                        "Membuka app Shizuku…"
                    } else {
                        "App Shizuku tidak ditemukan. Pasang dari Play Store atau GitHub."
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Panel {
            Txt("1. Uji identitas shell", 12, SukiInfo, FontWeight.Bold)
            Txt("Memastikan perintah benar-benar berjalan sebagai shell (harus uid=2000).", 10, SukiTextDim)
            Spacer(Modifier.height(6.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Btn("id", SukiInfo) { runShell(st, "id", "id") { note = it } }
                BtnGhost("getprop ro.build.version.sdk") {
                    runShell(st, "getprop", "getprop ro.build.version.sdk") { note = it }
                }
            }
            VerdictRow(st, "lane6-shell")
        }

        Spacer(Modifier.height(10.dp))
        Panel {
            Txt("2. Force-resizable (kunci untuk Android Go & freeform)", 12, SukiAccent2, FontWeight.Bold)
            Txt(
                "Menyalakan opsi developer force_resizable_activities membuat app pihak ketiga boleh " +
                    "di-resize. Di banyak perangkat ini yang membuka jalan freeform/split untuk app yang " +
                    "menolak. Bisa dimatikan lagi kapan saja.",
                10, SukiTextDim
            )
            Spacer(Modifier.height(6.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Btn("Baca nilai", SukiInfo) {
                    runShell(st, "baca", "settings get global force_resizable_activities") { note = it }
                }
                Btn("Nyalakan", SukiAccent2) {
                    runShell(st, "nyalakan", "settings put global force_resizable_activities 1") { note = it }
                }
                BtnGhost("Matikan") {
                    runShell(st, "matikan", "settings put global force_resizable_activities 0") { note = it }
                }
            }
            VerdictRow(st, "lane6-force-resize")
        }

        Spacer(Modifier.height(10.dp))
        Panel {
            Txt("3. Izin overlay otomatis (appops)", 12, SukiSuccess, FontWeight.Bold)
            Txt(
                "Memberi izin Tampilkan di atas app lain tanpa membuka pengaturan manual. " +
                    "Berguna untuk taskbar melayang (LANE 5) di perangkat yang menyembunyikan pengaturan itu.",
                10, SukiTextDim
            )
            Spacer(Modifier.height(6.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Btn("Beri izin overlay", SukiSuccess) {
                    runShell(
                        st, "appops allow",
                        "appops set ${ctx.packageName} SYSTEM_ALERT_WINDOW allow"
                    ) { note = it }
                }
                BtnGhost("Cek status overlay") {
                    note = if (canOverlay(ctx)) "Izin overlay aktif." else "Izin overlay belum aktif."
                }
            }
            VerdictRow(st, "lane6-appops")
        }

        Spacer(Modifier.height(10.dp))
        Panel {
            Txt("4. Jalankan app di display lain (uji window engine)", 12, SukiWarning, FontWeight.Bold)
            Txt(
                "Uji apakah app pihak ketiga bisa diluncurkan ke display tertentu dari identitas shell. " +
                    "Cara mencoba: aktifkan Opsi Developer lalu Simulate secondary displays, lalu tekan " +
                    "Baca display di bawah untuk melihat id yang tersedia, isi id di kolom, pilih app, lalu Luncurkan.",
                10, SukiTextDim
            )
            Spacer(Modifier.height(6.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Btn("Baca display", SukiInfo) {
                    runShell(st, "display", "dumpsys display | grep -i display") { note = it }
                }
                BtnGhost("Baca ukuran") { runShell(st, "wm size", "wm size") { note = it } }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Txt("Display id:", 11, SukiTextDim)
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier.width(70.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0B0D0F))
                        .border(1.dp, SukiStrokeSoft, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    BasicTextField(
                        value = displayId,
                        onValueChange = { displayId = it.filter { c -> c.isDigit() }.take(3) },
                        textStyle = TextStyle(color = SukiText, fontSize = 12.sp),
                        singleLine = true
                    )
                }
                Spacer(Modifier.width(8.dp))
                Btn("Luncurkan", SukiWarning) {
                    val pkg = target
                    val component = ctx.packageManager
                        .getLaunchIntentForPackage(pkg)?.component?.flattenToShortString()
                    if (component == null) {
                        note = "App tujuan tidak punya launch intent."
                    } else {
                        runShell(
                            st, "launch display $displayId",
                            "am start --display $displayId -n $component"
                        ) { note = it }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Txt("Target: ${target.ifEmpty { "(pilih di bawah)" }}", 10, SukiTextDim, maxLines = 1)
            LazyColumn(Modifier.height(96.dp).fillMaxWidth()) {
                items(st.apps.take(24)) { a ->
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (target == a.pkg) SukiAccent.copy(alpha = 0.18f) else Color.Transparent)
                            .clickable { target = a.pkg }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Txt(a.label, 11, SukiText, FontWeight.Medium, maxLines = 1)
                    }
                }
            }
            VerdictRow(st, "lane6-display")
        }

        Spacer(Modifier.height(10.dp))
        Txt(note, 10, SukiAccent2, maxLines = 4)
        Spacer(Modifier.height(10.dp))
        LogList(st, 16)
    }
}

// ================================================================= UJI EMBED
@Composable
private fun EmbedContent(st: SukiState) {
    val ctx = LocalContext.current
    val container = remember { FrameLayout(ctx) }
    var view by remember { mutableStateOf<Any?>(null) }
    var target by remember { mutableStateOf(st.apps.firstOrNull()?.pkg ?: "") }
    var status by remember { mutableStateOf("Tekan tombol 1, tunggu sekitar 1 detik, lalu tombol 2.") }

    Column(Modifier.fillMaxSize().padding(10.dp)) {
        Txt("Uji Embed — jalankan app lain di dalam jendela ini", 13, SukiText, FontWeight.Bold)
        Txt(
            "ActivityView = kelas sistem (android.app.ActivityView). Kita panggil lewat reflection " +
                "dan catat persis apa yang terjadi. Hasilnya menentukan apakah SukiOS bisa " +
                "menjalankan app pihak ketiga di dalam jendela, atau harus memakai jalur lain.",
            10, SukiTextDisabled
        )
        Spacer(Modifier.height(8.dp))

        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Btn("1. Buat ActivityView") {
                val created = activityViewCreate(ctx)
                if (created == null) {
                    status = "GAGAL: ActivityView tidak bisa dibuat (kelas/API ditolak)."
                    st.verdict("lane4-embed", "GAGAL DIBUAT")
                } else {
                    view = created
                    val asView = created as? View
                    if (asView != null) {
                        container.removeAllViews()
                        container.addView(
                            asView,
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                        )
                        status = "ActivityView dibuat & dipasang. Tunggu sekitar 1 detik, lalu tombol 2."
                    } else {
                        status = "Dibuat, tapi bukan turunan View (tidak bisa dipasang)."
                    }
                }
                st.log("embed", status)
            }
            Btn("2. Jalankan app", SukiWarning) {
                val current = view
                if (current == null) {
                    status = "Tekan tombol 1 dulu."
                } else {
                    val result = activityViewStart(ctx, current, target)
                    status = result
                    st.verdict("lane4-embed", if (result.startsWith("GAGAL")) "GAGAL" else "BERHASIL DIPANGGIL")
                    st.log("embed", result)
                }
            }
            BtnGhost("3. Lepas") {
                view?.let { activityViewRelease(it) }
                view = null
                container.removeAllViews()
                status = "ActivityView dilepas."
                st.log("embed", "dilepas")
            }
        }

        Spacer(Modifier.height(8.dp))
        Txt("Status: $status", 10, SukiAccent2, maxLines = 3)
        Spacer(Modifier.height(8.dp))

        Box(
            Modifier.fillMaxWidth().height(140.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0E1114))
                .border(1.dp, SukiStroke, RoundedCornerShape(10.dp))
        ) {
            AndroidView(factory = { container }, modifier = Modifier.fillMaxSize())
        }

        Spacer(Modifier.height(8.dp))
        Txt("Target app: ${target.ifEmpty { "(belum dipilih)" }}", 10, SukiTextDim, maxLines = 1)
        LazyColumn(Modifier.height(100.dp).fillMaxWidth()) {
            items(st.apps.take(20)) { a ->
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (target == a.pkg) SukiAccent.copy(alpha = 0.20f) else Color.Transparent)
                        .clickable { target = a.pkg }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Txt(a.label, 11, SukiText, FontWeight.Medium, maxLines = 1)
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        VerdictRow(st, "lane4-embed")
        Spacer(Modifier.height(6.dp))
        LogList(st, 6)
    }
}

// ================================================================ FILES
private data class FileRow(val badge: String, val name: String, val info: String, val tint: Color)

@Composable
private fun FilesContent() {
    val files = listOf(
        FileRow("DR", "Dokumen", "12 item", Color(0xFF8A93A0)),
        FileRow("GB", "Gambar", "248 item", Color(0xFF7B9E8C)),
        FileRow("PD", "Proposal-SukiOS.pdf", "2,4 MB", Color(0xFFB4675F)),
        FileRow("XL", "Roadmap-PoC.xlsx", "840 KB", Color(0xFF7FA98A)),
        FileRow("TX", "catatan-window-engine.txt", "4 KB", Color(0xFFA0A4A9)),
        FileRow("PN", "mockup-desktop.png", "5,1 MB", Color(0xFF9E8AA0)),
        FileRow("MP", "demo-drag-window.mp4", "88 MB", Color(0xFFC0A06A))
    )
    Column(Modifier.fillMaxSize().padding(10.dp)) {
        Txt("Files", 14, SukiText, FontWeight.Bold)
        Txt("Data contoh — PoC ini belum menyentuh penyimpanan asli (butuh SAF, PRD §7.10)", 10, SukiTextDisabled)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
            items(files) { f ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FileBadge(f.badge, f.tint)
                    Spacer(Modifier.width(10.dp))
                    Txt(f.name, 12, SukiText, FontWeight.Medium, maxLines = 1)
                    Spacer(Modifier.weight(1f))
                    Txt(f.info, 10, SukiTextDisabled)
                }
            }
        }
    }
}

// ================================================================ NOTES
@Composable
private fun NotesContent() {
    var text by remember {
        mutableStateOf(
            "Catatan ide SukiOS:\n\n" +
                "- PoC: apakah app pihak ketiga bisa masuk jendela?\n" +
                "- Kalau tidak: taskbar overlay + split screen + akses Shizuku\n" +
                "- Ukur FPS saat drag jendela (target minimal 55)\n" +
                "- Mode desktop: landscape terkunci + desktop penuh\n\n" +
                "Tulis bebas di sini — ini text field beneran."
        )
    }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Txt("Notes", 14, SukiText, FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier.fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0E1114))
                .padding(10.dp)
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(color = SukiText, fontSize = 12.sp)
            )
        }
    }
}
