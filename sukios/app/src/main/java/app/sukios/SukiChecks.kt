package app.sukios

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ============================================================================
// Daftar periksa: baris status (ikon + label + keterangan) yang dipakai layar Persiapan dan Setelan > Jendela.
// ============================================================================

data class CheckRow(val label: String, val status: StepStatus, val detail: String = "")

/** Empat prasyarat Shizuku, dibaca dari status yang diukur, bukan dari asumsi. */
fun shizukuRows(s: SukiShellState): List<CheckRow> = listOf(
    CheckRow(
        "Shizuku terpasang",
        if (s.installed) StepStatus.OK else StepStatus.FAIL,
        if (s.installed) "" else "pasang aplikasi Shizuku",
    ),
    CheckRow(
        "Shizuku berjalan",
        if (s.binderAlive) StepStatus.OK else if (s.installed) StepStatus.WARN else StepStatus.WAIT,
        if (s.binderAlive) "versi ${s.version}" else "mulai lewat Debugging nirkabel",
    ),
    CheckRow(
        "Izin untuk SukiOS",
        if (s.granted) StepStatus.OK else if (s.binderAlive) StepStatus.WARN else StepStatus.WAIT,
        if (s.granted) "diberikan" else "setujui dialog dari Shizuku",
    ),
    CheckRow(
        "SukiShell tersambung",
        if (s.serviceBound) StepStatus.OK else if (s.granted) StepStatus.RUN else StepStatus.WAIT,
        if (s.serviceBound) "uid ${s.uid}" else "",
    ),
)

fun stepRows(steps: List<StepState>): List<CheckRow> = steps.map { CheckRow(it.label, it.status, it.detail) }

@Composable
fun Checklist(rows: List<CheckRow>) {
    Column(Modifier.fillMaxWidth()) {
        rows.forEach { r ->
            val (glyph, tone) = when (r.status) {
                StepStatus.OK -> Pair(GlyphKind.OK_CIRCLE, SSuccess)
                StepStatus.FAIL -> Pair(GlyphKind.FAIL_CIRCLE, SDanger)
                StepStatus.WARN -> Pair(GlyphKind.ALERT, SWarning)
                StepStatus.RUN -> Pair(GlyphKind.REFRESH, accentNow().main)
                StepStatus.WAIT -> Pair(GlyphKind.WAIT_CIRCLE, SFaint)
            }
            Row(Modifier.fillMaxWidth().heightIn(min = 30.dp).padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Glyph(glyph, 17, tone)
                Spacer(Modifier.width(10.dp))
                Txt(r.label, 13, SText, FontWeight.Medium, maxLines = 2, modifier = Modifier.weight(1f))
                if (r.detail.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Txt(r.detail, 12, if (r.status == StepStatus.FAIL) SDangerText else SFaint, maxLines = 2, align = TextAlign.End, modifier = Modifier.widthIn(max = 190.dp))
                }
            }
        }
    }
}

/** Ringkasan hasil uji jendela terakhir untuk ditampilkan; null bila belum pernah diuji. */
fun probeLine(raw: String): String? {
    val p = WindowStatusLogic.parseProbe(raw) ?: return null
    val verdict = when (p.verdict) {
        ProbeRecord.VERDICT_OK -> "LULUS"
        ProbeRecord.VERDICT_FULLSCREEN -> "DITOLAK PERANGKAT (layar penuh)"
        else -> "TIDAK TERMULAI"
    }
    return "Uji jendela terakhir: $verdict ${Fmt.utcStamp(p.at)} UTC. ${p.note}".trim()
}
