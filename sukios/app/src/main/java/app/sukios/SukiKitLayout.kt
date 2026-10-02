package app.sukios

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Kerangka isi jendela: baris data, WinPanel, area gulir, kotak hasil.

// ---- Baris data ----
@Composable
fun KeyValue(key: String, value: String, mono: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Txt(key, 12, SFaint, modifier = Modifier.width(140.dp))
        Txt(
            value, 12, SText, maxLines = 4, modifier = Modifier.weight(1f),
            font = if (mono) SukiMono else SukiSans,
        )
    }
}

// ---- Kerangka isi jendela ----
/**
 * Kerangka isi jendela. Judul jendela sudah ada di bilah judul, jadi di sini hanya ada
 * baris alat tipis (keterangan + aksi) bila diperlukan.
 */
@Composable
fun WinPanel(
    title: String,
    subtitle: String? = null,
    actions: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(SSurface)) {
        if (subtitle != null || actions != null) {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Txt(subtitle ?: title, 12, SFaint, modifier = Modifier.weight(1f))
                if (actions != null) actions()
            }
            HLine()
        }
        Box(Modifier.weight(1f).fillMaxWidth()) { content() }
    }
}

/** Kolom yang bisa digulir: dasar hampir semua isi jendela. */
@Composable
fun ScrollArea(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        content = content,
    )
}

/** Kotak hasil: keluaran perintah apa adanya, huruf monospasi. */
@Composable
fun ResultBox(text: String, tone: Color = SDim, maxHeight: Int = 220) {
    val shape = RoundedCornerShape(RADIUS_SM.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp, max = maxHeight.dp)
            .clip(shape)
            .background(SBg)
            .border(1.dp, SLineSoft, shape)
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
    ) {
        Txt(text.ifBlank { "(belum ada hasil)" }, 11, tone, maxLines = 400, font = SukiMono)
    }
}
