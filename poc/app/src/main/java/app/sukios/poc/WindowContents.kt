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
        WinKind.EMBED -> EmbedContent(st)
        WinKind.FILES -> FilesContent()
        WinKind.NOTES -> NotesContent()
    }
}

// ------------------------------------------------------------ baris penilaian
@Composable
private fun VerdictRow(st: SukiState, lane: String, pkg: String? = null) {
    val key = if (pkg.isNullOrEmpty()) lane else "$lane:$pkg"
    Row(
        Modifier.fillMaxWidth().padding(top = 6.dp).horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Chip("✔ Berhasil", SukiSuccess, st.verdicts[key] == "OK") { st.verdict(key, "OK") }
        Chip("⬛ Hitam/kosong", SukiWarning, st.verdicts[key] == "HITAM") { st.verdict(key, "HITAM") }
        Chip("✘ Gagal", SukiDanger, st.verdicts[key] == "GAGAL") { st.verdict(key, "GAGAL") }
    }
}

@Composable
private fun LogList(st: SukiState, max: Int) {
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0A0C11))
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
            "Uji 5 jalur windowing. Setiap jalur punya tombol penilaian — isi semuanya, " +
                "lalu buka Monitor Perangkat → Salin laporan, dan tempel hasilnya.",
            10, SukiTextDisabled
        )
        Spacer(Modifier.height(12.dp))

        // ---- LANE 1
        Panel {
            Txt("LANE 1 — Window manager SukiOS", 13, SukiAccent, FontWeight.Bold)
            Txt(
                "Geser jendela ini ke tepi kiri/kanan layar → harus menempel separuh. " +
                    "Ke sudut → seperempat. Resize dari tepi/sudut. Perhatikan angka FPS di taskbar " +
                    "saat menggeser: target ≥ 55 fps.",
                10, SukiTextDim
            )
            VerdictRow(st, "lane1-wm")
        }
        Spacer(Modifier.height(10.dp))

        // ---- LANE 2
        Panel {
            Txt("LANE 2 — App pihak ketiga jadi jendela (freeform)", 12, SukiInfo, FontWeight.Bold)
            Txt(
                "Buka 'Daftar Aplikasi' → pilih app → tekan tombol 'Freeform'. " +
                    "Kalau app muncul fullscreen, artinya HP ini tidak mengizinkan freeform untuk app biasa.",
                10, SukiTextDim
            )
            Row(Modifier.padding(top = 6.dp)) {
                Btn("Buka Daftar Aplikasi", SukiAccent2) { st.open(WinKind.PICKER, 900, 700) }
            }
            VerdictRow(st, "lane2-freeform")
        }
        Spacer(Modifier.height(10.dp))

        // ---- LANE 3
        Panel {
            Txt("LANE 3 — Split screen", 12, SukiInfo, FontWeight.Bold)
            Txt(
                "Uji manual: buka app apa saja (tombol Fullscreen di Daftar Aplikasi), " +
                    "lalu tekan tombol Recents (kotak) → tahan app → pilih 'Split screen'. " +
                    "Ini cara paling andal untuk multitasking di HP biasa.",
                10, SukiTextDim
            )
            VerdictRow(st, "lane3-split")
        }
        Spacer(Modifier.height(10.dp))

        // ---- LANE 4
        Panel {
            Txt("LANE 4 — Embed app di dalam jendela (ActivityView)", 12, SukiWarning, FontWeight.Bold)
            Txt(
                "Jalur paling ambisius: menjalankan app lain DI DALAM jendela SukiOS. " +
                    "ActivityView adalah kelas sistem (bukan API publik) dan platform modern " +
                    "membatasi embedding ke app yang mendeklarasikan allowEmbedded. " +
                    "PoC ini akan membuktikan apakah benar-benar ditolak di HP ini.",
                10, SukiTextDim
            )
            Row(Modifier.padding(top = 6.dp)) {
                Btn("Buka Uji Embed", SukiWarning) { st.open(WinKind.EMBED, 900, 700) }
            }
            VerdictRow(st, "lane4-embed")
        }
        Spacer(Modifier.height(10.dp))

        // ---- LANE 5
        Panel {
            Txt("LANE 5 — Taskbar melayang di atas app lain (overlay)", 12, SukiSuccess, FontWeight.Bold)
            Txt(
                "Jalur fallback: app tetap fullscreen, tapi taskbar SukiOS mengapung di atasnya. " +
                    "Buka Monitor Perangkat → beri izin overlay → Mulai overlay → pindah ke app lain.",
                10, SukiTextDim
            )
            Row(Modifier.padding(top = 6.dp)) {
                Btn("Buka Monitor", SukiSuccess) { st.open(WinKind.MONITOR, 900, 700) }
                Spacer(Modifier.width(6.dp))
                BtnGhost("Bagikan laporan") {
                    st.log("report", shareReport(ctx, st))
                }
            }
            VerdictRow(st, "lane5-overlay")
        }
        Spacer(Modifier.height(12.dp))

        Txt("Trik cepat", 12, SukiText, FontWeight.SemiBold)
        Txt(
            "• Double-tap judul jendela → maximize\n" +
                "• Geser jendela ke paling atas layar → maximize\n" +
                "• Klik ikon app di taskbar → minimize / restore\n" +
                "• Tekan lama taskbar → pindah posisi (belum ada di PoC)",
            10, SukiTextDim
        )
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
                        .background(if (isSel) SukiAccent.copy(alpha = 0.20f) else Color.Transparent)
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
                Btn("Fullscreen (kontrol)", SukiAccent2) {
                    note = launchPlain(ctx, sel.pkg)
                    st.log("lane2", "fullscreen ${sel.pkg}: $note")
                }
                Btn("Freeform", SukiInfo) {
                    val deskW = w.w
                    val deskH = w.h
                    note = launchWithBounds(ctx, sel.pkg, freeformMode = true, deskW = deskW, deskH = deskH)
                    st.log("lane2", "freeform ${sel.pkg}: $note")
                }
                Btn("Split screen", SukiWarning) {
                    note = launchSplit(ctx, sel.pkg)
                    st.log("lane3", "split ${sel.pkg}: $note")
                }
                BtnGhost("Embed (ActivityView)") {
                    st.open(WinKind.EMBED, w.w, w.h)
                }
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
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Btn("Probe ulang") {
                runProbe(ctx, st.act, st)
                note = "Probe dijalankan ulang."
            }
            Btn("Salin laporan", SukiAccent2) { note = copyReport(ctx, st) }
            BtnGhost("Bagikan") { note = shareReport(ctx, st) }
        }

        Spacer(Modifier.height(12.dp))
        Panel {
            Txt("Uji overlay taskbar (LANE 5)", 12, SukiSuccess, FontWeight.Bold)
            Txt(
                "Taskbar SukiOS digambar di atas app apa pun. Butuh izin 'Tampilkan di atas app lain'.",
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

// ================================================================= UJI EMBED
@Composable
private fun EmbedContent(st: SukiState) {
    val ctx = LocalContext.current
    // FrameLayout stabil (dibuat sekali) sebagai wadah ActivityView hasil reflection.
    val container = remember { FrameLayout(ctx) }
    var view by remember { mutableStateOf<Any?>(null) }
    var target by remember { mutableStateOf(st.apps.firstOrNull()?.pkg ?: "") }
    var status by remember { mutableStateOf("Tekan tombol 1, tunggu ~1 detik, lalu tombol 2.") }

    Column(Modifier.fillMaxSize().padding(10.dp)) {
        Txt("Uji Embed — jalankan app lain di dalam jendela ini", 13, SukiText, FontWeight.Bold)
        Txt(
            "ActivityView = kelas sistem (android.app.ActivityView). Kita panggil lewat reflection " +
                "dan catat persis apa yang terjadi. Hasilnya menentukan apakah SukiOS bisa " +
                "menjalankan app pihak ketiga di dalam jendela, atau harus pakai jalur lain.",
            10, SukiTextDisabled
        )
        Spacer(Modifier.height(8.dp))

        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Btn("1. Buat ActivityView") {
                val v = activityViewCreate(ctx)
                if (v == null) {
                    status = "GAGAL: ActivityView tidak bisa dibuat (kelas/API ditolak)."
                    st.verdict("lane4-embed", "GAGAL DIBUAT")
                } else {
                    view = v
                    val asView = v as? View
                    if (asView != null) {
                        container.removeAllViews()
                        container.addView(
                            asView,
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                        )
                        status = "ActivityView dibuat & dipasang. Tunggu ~1 detik, lalu tombol 2."
                    } else {
                        status = "Dibuat, tapi bukan turunan View (tidak bisa dipasang)."
                    }
                }
                st.log("embed", status)
            }
            Btn("2. Jalankan app", SukiWarning) {
                val v = view
                if (v == null) {
                    status = "Tekan tombol 1 dulu."
                } else {
                    val r = activityViewStart(ctx, v, target)
                    status = r
                    st.verdict("lane4-embed", if (r.startsWith("GAGAL")) "GAGAL" else "BERHASIL DIPANGGIL")
                    st.log("embed", r)
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
            Modifier.fillMaxWidth().height(150.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0E1118))
                .border(1.dp, SukiStroke, RoundedCornerShape(10.dp))
        ) {
            AndroidView(factory = { container }, modifier = Modifier.fillMaxSize())
        }

        Spacer(Modifier.height(8.dp))
        Txt("Target app: ${target.ifEmpty { "(belum dipilih)" }}", 10, SukiTextDim, maxLines = 1)
        LazyColumn(Modifier.height(110.dp).fillMaxWidth()) {
            items(st.apps.take(20)) { a ->
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (target == a.pkg) SukiAccent.copy(alpha = 0.22f) else Color.Transparent)
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
private data class FileRow(val icon: String, val name: String, val info: String)

@Composable
private fun FilesContent() {
    val files = listOf(
        FileRow("📁", "Dokumen", "12 item"),
        FileRow("📁", "Gambar", "248 item"),
        FileRow("📄", "Proposal-SukiOS.pdf", "2,4 MB"),
        FileRow("📊", "Roadmap-PoC.xlsx", "840 KB"),
        FileRow("🗒️", "catatan-window-engine.txt", "4 KB"),
        FileRow("🖼️", "mockup-desktop.png", "5,1 MB"),
        FileRow("🎬", "demo-drag-window.mp4", "88 MB")
    )
    Column(Modifier.fillMaxSize().padding(10.dp)) {
        Txt("Files", 14, SukiText, FontWeight.Bold)
        Txt("Data contoh — PoC ini belum menyentuh penyimpanan asli (butuh SAF, PRD §7.10)", 10, SukiTextDisabled)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
            items(files) { f ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .clickable { }
                        .padding(horizontal = 8.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Txt(f.icon, 14)
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
                "- Kalau tidak bisa: pakai overlay taskbar + split screen\n" +
                "- Ukur FPS saat drag jendela (target >= 55)\n\n" +
                "Tulis bebas di sini — ini text field beneran."
        )
    }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Txt("Notes", 14, SukiText, FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier.fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0E1118))
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
