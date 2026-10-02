package app.sukios

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// ============================================================================
// Terminal: perintah apa adanya lewat SukiShell. Fitur pengguna mahir: SATU-SATUNYA tempat yang memanggil
// SukiShell.run langsung (tanpa daftar izin ShellPolicy) dan melaporkan hasilnya persis seperti keluarannya.
// Tanpa Shizuku, kode 127 dilaporkan apa adanya, bukan hasil palsu.
// ============================================================================

private val QUICK = listOf(
    "id",
    "wm size",
    "dumpsys display",
    "settings get global enable_freeform_support",
    "settings get global force_resizable_activities",
    "getprop ro.product.model",
)

@Composable
fun TerminalContent(app: SukiApp) {
    val log = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    val shell by SukiShell.state.collectAsState()

    WinPanel(
        "Terminal",
        if (shell.ready) "identitas: ${if (shell.uid == 0) "root" else "shell"} (uid ${shell.uid})" else "SukiShell belum siap",
        actions = { IconBtn(GlyphKind.TRASH, "Bersihkan riwayat", size = 34) { log.clear() } },
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                QUICK.forEach { cmd -> Chip(cmd) { runCommand(scope, cmd.split(" ").toTypedArray(), log) } }
            }
            if (!shell.ready) {
                Spacer(Modifier.height(6.dp))
                Txt("Tanpa Shizuku, terminal tidak punya hak istimewa: perintah akan melaporkan kode 127, bukan hasil palsu.", 11, SWarning, maxLines = 3)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SearchField(
                    input, { input = it }, "perintah, dipisah spasi tanpa tanda kutip", Modifier.weight(1f),
                    icon = GlyphKind.TERMINAL, mono = true,
                    onDone = { submit(scope, input, log) { input = "" } },
                )
                Btn("Jalankan") { submit(scope, input, log) { input = "" } }
            }
            Spacer(Modifier.height(8.dp))
            val shape = RoundedCornerShape(RADIUS_SM.dp)
            Box(Modifier.fillMaxWidth().weight(1f).clip(shape).background(SBg).border(1.dp, SLineSoft, shape).padding(10.dp)) {
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    if (log.isEmpty()) {
                        Txt("Riwayat kosong.", 11, SFaint, font = SukiMono)
                    } else {
                        log.forEach { line -> Txt(line, 11, SDim, maxLines = 400, font = SukiMono) }
                    }
                }
            }
        }
    }
}

private fun submit(scope: CoroutineScope, text: String, log: MutableList<String>, clear: () -> Unit) {
    if (text.isBlank()) return
    runCommand(scope, text.trim().split(Regex("\\s+")).toTypedArray(), log)
    clear()
}

private fun runCommand(scope: CoroutineScope, argv: Array<String>, log: MutableList<String>) {
    scope.launch {
        val r = SukiShell.io { this.run(*argv) }
        val stamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        log.add(0, "[$stamp] $ ${argv.joinToString(" ")}\n${r.full().take(2000)}")
        while (log.size > 30) log.removeAt(log.size - 1)
    }
}
