package app.sukios

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// Font bawaan aplikasi (OFL, lisensi ada di assets/licenses dan THIRD_PARTY_NOTICES.md).
// Berkasnya dipangkas ke aksara Latin oleh tools/mkfonts.py (lihat DESIGN.md, bagian tipografi).

/** Teks antarmuka: Inter, optical size 14. */
val SukiSans = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

/** Judul pendek dan nama merek: Plus Jakarta Sans. */
val SukiBrand = FontFamily(
    Font(R.font.jakarta_bold, FontWeight.Bold),
    Font(R.font.jakarta_extrabold, FontWeight.ExtraBold),
)

/** Jam besar di desktop: Inter Display Light. */
val SukiClockFont = FontFamily(Font(R.font.inter_display_light, FontWeight.Light))

val SukiMono: FontFamily = FontFamily.Monospace
