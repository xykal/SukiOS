package app.sukios

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ============================================================================
// Tentang SukiOS: merek, mesin, lisensi pihak ketiga, dan atribusi.
// Nama perusahaan HANYA datang dari Brand (sumber tunggal); awalan "Powered by" dari strings.xml.
// ============================================================================

@Composable
fun AboutContent(app: SukiApp) {
    WinPanel("Tentang SukiOS") {
        ScrollArea {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LogoMark(60)
                Spacer(Modifier.width(16.dp))
                Column {
                    Txt("SukiOS", 28, SText, FontWeight.ExtraBold, font = SukiBrand)
                    Txt(app.diag.appVersion(), 12, SFaint)
                }
            }
            ColSpacer(12)
            Txt(
                "Desktop environment untuk Android: taskbar, start menu, jendela mengambang, dan multitugas. " +
                    "Dibangun dari nol dengan Kotlin, dengan bahasa visual Aurora milik sendiri.",
                12, SDim, maxLines = 5,
            )
            ColSpacer(16)
            Label("Mesin")
            ColSpacer(4)
            KeyValue("SukiWin", "jendela milik SukiOS: geser, ubah ukuran, snap, tumpukan fokus")
            KeyValue("SukiWindowing", "membuka aplikasi sebagai jendela mengambang, diverifikasi dari sistem")
            KeyValue("SukiShell", "akses lanjutan lewat Shizuku + UserService sendiri")
            KeyValue("SukiIndex", "indeks aplikasi terpasang dan ikon seragam")
            KeyValue("SukiKit", "sistem tampilan Aurora: kaca, gradien, gerak, ikon vektor")
            ColSpacer(16)
            Label("Lisensi pihak ketiga")
            ColSpacer(4)
            KeyValue("Shizuku-API", "MIT, RikkaW. Hanya kurir binder; semua kemampuan inti jalan tanpa Shizuku.")
            KeyValue("Inter", "SIL OFL 1.1, The Inter Project Authors. Teks antarmuka.")
            KeyValue("Plus Jakarta Sans", "SIL OFL 1.1, The Plus Jakarta Sans Project Authors. Judul dan nama merek.")
            Txt("Teks lengkap ada di THIRD_PARTY_NOTICES.md pada repo dan di assets/licenses pada aplikasi.", 11, SFaint, maxLines = 3)
            ColSpacer(16)
            Label("Dibuat oleh")
            ColSpacer(4)
            Txt(stringResource(R.string.powered_by, Brand.COMPANY), 13, SText, FontWeight.Medium)
            Txt(Brand.COPYRIGHT, 11, SFaint, maxLines = 2)
        }
    }
}
