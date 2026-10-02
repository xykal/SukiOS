package app.sukios

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Isi jendela SukiOS.
 *
 * Semua yang ditampilkan di sini berasal dari pemeriksaan nyata: hasil perintah
 * apa adanya, fakta perangkat yang dibaca dari sistem, dan status yang
 * dilaporkan jujur — termasuk saat sebuah kemampuan tidak tersedia.
 */
@Composable
fun SettingsContent(app: SukiApp) {
    val ctx = LocalContext.current
    val prefs = app.prefs
    val shell by SukiShell.state.collectAsState()
    val lockLandscape by prefs.lockLandscape.collectAsState()
    val fullDesktop by prefs.fullDesktop.collectAsState()

    WinPanel("Setelan SukiOS", "tampilan, desktop, jendela, akses lanjutan") {
        ScrollArea {
            Label("Aksen")
            ColSpacer(6)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SukiAccents.forEach { a ->
                    Box(
                        Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(a.color)
                            .border(
                                if (a.id == SukiRuntime.accentId) 2.dp else 1.dp,
                                if (a.id == SukiRuntime.accentId) SText else SLine,
                                RoundedCornerShape(7.dp),
                            )
                            .clickable { prefs.setAccent(a.id) },
                    )
                }
            }

            ColSpacer(14)
            Label("Desktop")
            ToggleRow("Kunci mendatar (landscape)", lockLandscape) { prefs.setLockLandscape(it) }
            ToggleRow("Desktop penuh (status bar disembunyikan)", fullDesktop) { prefs.setFullDesktop(it) }
            ToggleRow("Taskbar melayang di atas aplikasi lain", SukiRuntime.overlayBarOn) {
                toggleOverlayBar(app, ctx, it)
            }

            ColSpacer(14)
            Label("Jendela")
            KeyValue("Batas jendela", "${app.wins.maxWindows} (${if (SukiRuntime.goMode) "mode Go" else "mode normal"})")
            KeyValue("Terbuka sekarang", "${app.wins.count}")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BtnGhost("Kecilkan semua") { app.wins.minimizeAll() }
                BtnGhost("Tutup semua") { app.wins.closeAll() }
            }

            ColSpacer(14)
            Label("Launcher")
            KeyValue("Status", if (SukiRuntime.isDefaultLauncher) "SukiOS adalah launcher default" else "SukiOS belum jadi launcher default")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Btn("Jadikan launcher default") { app.index.openDefaultLauncherSettings() }
                BtnGhost("Izin overlay") { app.index.openOverlaySettings() }
            }

            ColSpacer(14)
            Label("Akses lanjutan (Shizuku)")
            KeyValue("Terpasang", if (shell.installed) "ya" else "tidak")
            KeyValue("Versi", if (shell.version > 0) "${shell.version}" else "-")
            KeyValue("SukiShell", if (shell.serviceBound) "tersambung (protokol ${shell.protocol})" else "belum tersambung")
            KeyValue("Identitas", when {
                shell.uid == 2000 -> "shell (uid 2000)"
                shell.uid == 0 -> "root (uid 0)"
                else -> "-"
            })
            KeyValue("Catatan", shell.note)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Btn("Minta izin") { SukiRuntime.say(SukiShell.requestPermission()) }
                BtnGhost("Buka Shizuku") { SukiShell.openShizukuApp() }
                BtnGhost("Periksa ulang") { SukiShell.refresh() }
            }

            ColSpacer(14)
            Label("SukiOS")
            KeyValue("Versi", app.diag.appVersion())
            KeyValue("Mesin", "SukiWin / SukiShell / SukiIndex / SukiKit")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BtnGhost("Tentang SukiOS") { openInternal(app, WinKind.ABOUT) }
                BtnGhost("Laboratorium") { openInternal(app, WinKind.LAB) }
                BtnGhost("Diagnostik") { openInternal(app, WinKind.DIAG) }
            }
        }
    }
}

@Composable
fun LabContent(app: SukiApp) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val shell by SukiShell.state.collectAsState()
    val recentCsv by app.prefs.recent.collectAsState()
    val allApps by app.index.apps.collectAsState()
    var output by remember { mutableStateOf("") }
    val facts = remember(SukiRuntime.isDefaultLauncher, SukiRuntime.deviceClass) { app.diag.deviceFacts() }
    val recents = remember(recentCsv, allApps) {
        recentCsv.split(",").filter { it.isNotBlank() }
            .mapNotNull { pkg -> allApps.firstOrNull { it.pkg == pkg } }.take(4)
    }

    // Perintah shell memanggil binder dan membuat proses: dijalankan di thread IO supaya
    // layar tidak menunggu perintah yang lambat.
    fun runShell(limit: Int = 6000, block: SukiShell.() -> SukiResult) {
        output = "menjalankan..."
        scope.launch { output = SukiShell.io(block).full().take(limit) }
    }

    WinPanel("Laboratorium", "uji nyata di perangkat ini, hasil apa adanya") {
        ScrollArea {
            Label("Perangkat")
            facts.forEach { KeyValue(it.key, it.value) }

            ColSpacer(14)
            Label("Uji jendela aplikasi pihak ketiga")
            Txt(
                "Tombol di bawah mengirim perintah buka dengan kotak jendela (bounds). " +
                    "Kalau aplikasi tetap memenuhi layar, artinya perangkat ini tidak " +
                    "menghormati bounds — jalur jendela pihak ketiga TERTUTUP di sini.",
                10, SFaint, maxLines = 4
            )
            ColSpacer(6)
            if (recents.isEmpty()) {
                Txt("Belum ada aplikasi yang pernah dibuka. Buka satu dulu dari desktop atau start menu.", 10, SFaint, maxLines = 2)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    recents.forEach { e ->
                        BtnGhost("Uji: ${e.label.take(14)}") {
                            val sw = SukiRuntime.screenW
                            val sh = SukiRuntime.screenH
                            val bounds = Rect(
                                60, 60,
                                (60 + sw * 0.55f).toInt().coerceAtLeast(400),
                                (60 + sh * 0.60f).toInt().coerceAtLeast(320),
                            )
                            output = app.diag.windowProbe(e, bounds)
                        }
                    }
                }
            }

            ColSpacer(14)
            Label("Perintah SukiShell")
            if (!shell.ready) {
                Txt(
                    "SukiShell belum siap: ${shell.note}. " +
                        "Perintah di bawah akan melaporkan kegagalan tanpa menyembunyikannya.",
                    10, SWarning, maxLines = 3
                )
                ColSpacer(6)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BtnGhost("Identitas") { runShell { identity() } }
                BtnGhost("Baca force-resizable") { runShell { readForceResizable() } }
            }
            ColSpacer(6)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BtnGhost("Nyalakan force-resizable") { runShell { forceResizable(true) } }
                BtnGhost("Matikan") { runShell { forceResizable(false) } }
            }
            ColSpacer(6)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BtnGhost("Daftar display") { runShell(limit = 4000) { displays() } }
                BtnGhost("Ukuran jendela") { runShell { windowSize() } }
                BtnGhost("Ringkas perangkat") { runShell { deviceSummary() } }
            }
            ColSpacer(10)
            ResultBox(output, tone = if (output.startsWith("kode=0")) SSuccess else SDim, maxHeight = 240)
            ColSpacer(8)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BtnGhost("Putuskan SukiShell") {
                    SukiShell.unbind()
                    output = "SukiShell diputus. Binder akan diikat ulang saat dibutuhkan."
                }
                BtnGhost("Salin laporan diagnostik") {
                    scope.launch {
                        copyText(ctx, withContext(Dispatchers.IO) { app.diag.buildReport() })
                        SukiRuntime.say("Laporan diagnostik disalin.")
                    }
                }
                BtnGhost("Bagikan") {
                    scope.launch { shareText(ctx, withContext(Dispatchers.IO) { app.diag.buildReport() }) }
                }
            }
        }
    }
}

@Composable
fun TerminalContent(app: SukiApp) {
    val log = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    val shell by SukiShell.state.collectAsState()

    WinPanel("Terminal", if (shell.ready) "identitas: ${if (shell.uid == 0) "root" else "shell"} (uid ${shell.uid})" else "SukiShell belum siap") {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "id",
                    "wm size",
                    "dumpsys display",
                    "settings get global force_resizable_activities",
                    "getprop ro.product.model",
                ).forEach { cmd ->
                    Chip(cmd, active = false) {
                        runCommand(scope, cmd.split(" ").toTypedArray(), log)
                    }
                }
            }
            if (!shell.ready) {
                ColSpacer(8)
                Txt(
                    "Tanpa Shizuku, terminal tidak punya hak istimewa: perintah di atas akan " +
                        "melaporkan kode 127 (belum siap) — bukan hasil palsu.",
                    10, SWarning, maxLines = 3
                )
            }
            ColSpacer(10)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(SBg)
                        .border(1.dp, SLine, RoundedCornerShape(9.dp))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (input.isEmpty()) Txt("perintah (dipisah spasi, tanpa tanda kutip)", 11, SFaint, maxLines = 1)
                    BasicTextField(
                        value = input,
                        onValueChange = { input = it },
                        singleLine = true,
                        textStyle = fieldStyle(12, mono = true),
                        cursorBrush = SolidColor(accentById(SukiRuntime.accentId)),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Btn("Jalankan") {
                    if (input.isNotBlank()) {
                        runCommand(scope, input.trim().split(Regex("\\s+")).toTypedArray(), log)
                        input = ""
                    }
                }
            }
            ColSpacer(10)
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(SBg)
                    .border(1.dp, SLine, RoundedCornerShape(9.dp))
                    .padding(10.dp),
            ) {
                ScrollAreaInner {
                    if (log.isEmpty()) {
                        Txt("Riwayat kosong.", 10, SFaint)
                    } else {
                        log.forEach { line -> Txt(line, 10, SDim, maxLines = 400) }
                    }
                }
            }
        }
    }
}

private fun runCommand(scope: CoroutineScope, argv: Array<String>, log: MutableList<String>) {
    scope.launch {
        val r = SukiShell.io { this.run(*argv) }
        val stamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
            .format(java.util.Date())
        log.add(0, "[$stamp] $ ${argv.joinToString(" ")}\n${r.full().take(2000)}")
        while (log.size > 30) log.removeAt(log.size - 1)
    }
}

@Composable
fun AppsContent(app: SukiApp) {
    var q by remember { mutableStateOf("") }
    val allApps by app.index.apps.collectAsState()
    val pinnedCsv by app.prefs.pinned.collectAsState()
    val shell by SukiShell.state.collectAsState()
    val apps = remember(allApps, q) { filterApps(allApps, q, 200) }
    val pinned = remember(pinnedCsv) { pinnedCsv.split(",").filter { it.isNotBlank() } }
    val scope = rememberCoroutineScope()

    WinPanel("Aplikasi", "${allApps.size} aplikasi terpasang") {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(SBg)
                    .border(1.dp, SLine, RoundedCornerShape(9.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (q.isEmpty()) Txt("Cari aplikasi atau nama paket...", 11, SFaint)
                BasicTextField(
                    value = q,
                    onValueChange = { q = it },
                    singleLine = true,
                    textStyle = fieldStyle(12),
                    cursorBrush = SolidColor(accentById(SukiRuntime.accentId)),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            ColSpacer(8)
            Txt(
                "Sematkan = muncul di desktop. \"Jendela\" mencoba membuka mengambang " +
                    "(tidak dijamin; lihat Laboratorium untuk hasil uji).",
                10, SFaint, maxLines = 3
            )
            ColSpacer(8)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BtnGhost("Muat ulang daftar") { scope.launch { app.index.load() }; SukiRuntime.say("Menyegarkan daftar aplikasi...") }
                if (shell.ready) {
                    BtnGhost("Nyalakan force-resizable") {
                        scope.launch {
                            val r = SukiShell.io { forceResizable(true) }
                            SukiRuntime.say(if (r.ok) "force_resizable_activities menyala" else "Gagal: ${r.err.take(80)}")
                        }
                    }
                }
            }
            ColSpacer(10)
            LazyColumn(Modifier.weight(1f)) {
                items(apps, key = { it.pkg + "/" + it.activity }) { entry ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(9.dp))
                            .padding(horizontal = 6.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val icon = entry.icon
                        if (icon != null) {
                            Image(
                                bitmap = icon, contentDescription = entry.label,
                                modifier = Modifier.size(26.dp), contentScale = ContentScale.Fit,
                            )
                        } else {
                            AppGlyph(entry.label, 26)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f).clickable { launchApp(app, entry) }) {
                            Txt(entry.label, 12, SText, maxLines = 1)
                            Txt(entry.pkg + if (entry.system) " · sistem" else "", 9, SFaint, maxLines = 1)
                        }
                        IconBtn(GlyphKind.PIN, pinned.contains(entry.pkg)) {
                            app.prefs.togglePinned(entry.pkg)
                        }
                        IconBtn(GlyphKind.WINDOW, false) {
                            val sw = SukiRuntime.screenW
                            val sh = SukiRuntime.screenH
                            val bounds = Rect(
                                80, 70,
                                (80 + sw * 0.52f).toInt().coerceAtLeast(400),
                                (70 + sh * 0.58f).toInt().coerceAtLeast(320),
                            )
                            val ok = app.index.launchWindowed(entry, bounds)
                            SukiRuntime.say(
                                if (ok) "Perintah jendela dikirim untuk ${entry.label}. Kalau tetap penuh, perangkat menolak."
                                else "Gagal mengirim perintah jendela."
                            )
                        }
                        IconBtn(GlyphKind.INFO, false) { app.index.openInfo(entry) }
                        IconBtn(GlyphKind.CLOSE, false) { app.index.uninstall(entry) }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconBtn(glyph: GlyphKind, active: Boolean, onClick: () -> Unit) {
    val accent = accentById(SukiRuntime.accentId)
    Box(
        Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(7.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) { Glyph(glyph, 13, if (active) accent else SFaint) }
}

@Composable
fun AboutContent(app: SukiApp) {
    WinPanel("Tentang SukiOS", "desktop-mu, di kantongmu") {
        ScrollArea {
            Txt("SukiOS", 22, SText, FontWeight.Bold)
            Txt(app.diag.appVersion(), 11, SFaint)
            ColSpacer(12)
            Txt(
                "Desktop environment untuk Android: taskbar, start menu, jendela mengambang, " +
                    "dan multi-tugas. Dibangun dari nol dengan Kotlin — tanpa merek pihak lain.",
                11, SDim, maxLines = 5
            )
            ColSpacer(14)
            Label("Mesin")
            KeyValue("SukiWin", "jendela: geser, ukur ulang, snap, tumpukan fokus")
            KeyValue("SukiShell", "akses lanjutan lewat Shizuku + UserService sendiri")
            KeyValue("SukiIndex", "indeks aplikasi terpasang")
            KeyValue("SukiKit", "sistem tampilan matte")
            KeyValue("SukiDiag", "diagnostik perangkat dan ekspor laporan")
            ColSpacer(14)
            Label("Aturan tampilan")
            Txt(
                "Matte, tanpa neon, tanpa glow. Bayangan netral. Ikon digambar sebagai " +
                    "vektor, bukan emoji. Aksen hanya untuk elemen aktif.",
                11, SDim, maxLines = 4
            )
            ColSpacer(14)
            Label("Catatan")
            Txt(
                "Akses lanjutan memakai pustaka Shizuku-API milik pihak ketiga (lisensi MIT; " +
                    "teks lengkap ada di THIRD_PARTY_NOTICES.md pada repo). " +
                    "SukiOS menyediakan mesin sendiri di atasnya; Shizuku hanya kurir binder. " +
                    "Semua kemampuan inti tetap berjalan tanpa Shizuku.",
                11, SDim, maxLines = 6
            )
            ColSpacer(14)
            Label("Dibuat oleh")
            Txt(stringResource(R.string.powered_by, Brand.COMPANY), 11, SText)
            Txt(Brand.COPYRIGHT, 10, SFaint, maxLines = 2)
        }
    }
}

@Composable
fun DiagContent(app: SukiApp) {
    val ctx = LocalContext.current
    var tick by remember { mutableStateOf(0) }
    // buildReport memanggil SukiShell (binder + proses): tidak boleh jalan di thread utama.
    val report by produceState("Menyusun laporan...", tick) {
        value = "Menyusun laporan..."
        value = withContext(Dispatchers.IO) { app.diag.buildReport() }
    }
    WinPanel("Diagnostik", "laporan mentah, siap dibagikan") {
        ScrollArea {
            ResultBox(report, tone = SDim, maxHeight = 420)
            ColSpacer(10)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Btn("Salin") {
                    copyText(ctx, report)
                    SukiRuntime.say("Laporan disalin.")
                }
                BtnGhost("Bagikan") { shareText(ctx, report) }
                BtnGhost("Periksa ulang") {
                    SukiShell.refresh()
                    tick++
                }
            }
        }
    }
}

@Composable
private fun ScrollAreaInner(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) { content() }
}

private fun copyText(ctx: Context, text: String) {
    runCatching {
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("SukiOS", text))
    }
}

private fun shareText(ctx: Context, text: String) {
    runCatching {
        val i = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Laporan SukiOS")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        ctx.startActivity(Intent.createChooser(i, "Bagikan laporan").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
