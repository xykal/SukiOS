package app.sukios

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.graphics.Color

// ============================================================================
// SukiTheme — token desain Aurora (DESIGN.md).
//
// Palet, aksen, wallpaper, ukuran, gerak. Warna mentah ditulis sebagai const Long
// (ARGB) supaya uji kontras WCAG (ThemeContrastTest) membacanya tanpa memuat
// Compose. Teks memakai font di SukiFonts.kt; ikon di SukiGlyph.kt.
// ============================================================================

// ---- Bidang (gelap kebiruan) ----
const val C_BG = 0xFF0B0D12L
const val C_SURFACE = 0xFF12151DL
const val C_ELEVATED = 0xFF1A1F2BL
const val C_OVERLAY = 0xFF242A38L
const val C_CHROME = 0xFF1F2532L
const val C_SHEET = 0xFF161B27L

// ---- Teks: semuanya lolos WCAG AA 4,5:1 di atas semua bidang di atas ----
const val C_TEXT = 0xFFF2F4F8L
const val C_DIM = 0xFFB4BCCCL
const val C_FAINT = 0xFF8D96ABL
const val C_INK = 0xFF0B0D12L

// ---- Cahaya Aurora dan warna status ----
const val C_VIOLET = 0xFF7C5CFFL
const val C_VIOLET_FILL = 0xFF6C4BF2L
const val C_VIOLET_TEXT = 0xFFA89BFFL
const val C_TEAL = 0xFF35D0BAL
const val C_PINK = 0xFFFF7AB6L
const val C_BLUE = 0xFF5B9DFFL
const val C_AMBER = 0xFFFFB44CL
const val C_GREEN = 0xFF3FD08AL
const val C_RED = 0xFFFF5F56L
const val C_RED_TEXT = 0xFFFF8A82L

// ---- Ukuran tetap (dp) ----
/** Area yang dipesan taskbar di bawah layar: tinggi bar + jarak bawah. Dipakai mesin jendela. */
const val TASKBAR_DP = 54
const val TASKBAR_BAR_DP = 48
const val ICON_DP = 48

const val RADIUS_SM = 6
const val RADIUS_MD = 9
const val RADIUS_LG = 12
const val RADIUS_WIN = 10
const val RADIUS_PILL = 999

// ---- Gerak (ms) ----
const val MOTION_INSTANT = 90
const val MOTION_FAST = 150
const val MOTION_BASE = 220
const val MOTION_SLOW = 320

/** Kurva gerak utama (dari token Aurora: cubic-bezier(.2, 0, 0, 1)). */
val EaseOut = CubicBezierEasing(0.2f, 0f, 0f, 1f)

// ---- Warna Compose ----
val SBg = Color(C_BG)
val SSurface = Color(C_SURFACE)
val SElevated = Color(C_ELEVATED)
val SOverlay = Color(C_OVERLAY)
val SChrome = Color(C_CHROME)
val SSheet = Color(C_SHEET)

val SText = Color(C_TEXT)
val SDim = Color(C_DIM)
val SFaint = Color(C_FAINT)
val SInk = Color(C_INK)

val SSuccess = Color(C_GREEN)
val SWarning = Color(C_AMBER)
val SDanger = Color(C_RED)
val SDangerText = Color(C_RED_TEXT)
val SInfo = Color(C_BLUE)

val SLine = Color(0x1FFFFFFF)
val SLineSoft = Color(0x12FFFFFF)
val SLineStrong = Color(0x33FFFFFF)
val SScrim = Color(0x8C05060A)

// ---- Aksen pilihan pengguna ----
/**
 * Satu aksen = dua cahaya (utama + kedua, dipakai untuk gradien) dan tiga turunan yang menjamin keterbacaan:
 *  - fill: isi tombol; on: teks di atas fill; text: warna untuk teks aksen di bidang gelap.
 */
data class AccentSpec(
    val id: String,
    val label: String,
    val main: Long,
    val second: Long,
    val fill: Long,
    val on: Long,
    val text: Long,
)

val ACCENT_SPECS = listOf(
    AccentSpec("aurora", "Aurora", C_VIOLET, C_TEAL, C_VIOLET_FILL, 0xFFFFFFFFL, C_VIOLET_TEXT),
    AccentSpec("laguna", "Laguna", C_TEAL, C_BLUE, C_TEAL, C_INK, C_TEAL),
    AccentSpec("mekar", "Mekar", C_PINK, C_VIOLET, C_PINK, C_INK, C_PINK),
    AccentSpec("azur", "Azur", C_BLUE, C_TEAL, C_BLUE, C_INK, C_BLUE),
    AccentSpec("bara", "Bara", C_AMBER, C_PINK, C_AMBER, C_INK, C_AMBER),
    AccentSpec("mint", "Mint", C_GREEN, C_TEAL, C_GREEN, C_INK, C_GREEN),
)

class SukiAccent(spec: AccentSpec) {
    val id = spec.id
    val label = spec.label
    val main = Color(spec.main)
    val second = Color(spec.second)
    val fill = Color(spec.fill)
    val on = Color(spec.on)
    val text = Color(spec.text)
}

val SukiAccents: List<SukiAccent> = ACCENT_SPECS.map { SukiAccent(it) }

fun accentById(id: String): SukiAccent = SukiAccents.firstOrNull { it.id == id } ?: SukiAccents[0]

/** Aksen yang sedang dipakai. Membaca state Compose, jadi pemanggil ikut digambar ulang saat aksen berubah. */
fun accentNow(): SukiAccent = accentById(SukiRuntime.accentId)

// ---- Wallpaper ----
/** Satu cahaya lunak: pusat (x,y) dan radius (rx, ry) sebagai pecahan lebar/tinggi layar; rot dalam radian. */
data class WallLight(val x: Float, val y: Float, val rx: Float, val ry: Float, val color: Long, val alpha: Float, val rot: Float = 0f)

data class WallSpec(val id: String, val label: String, val base0: Long, val base1: Long, val lights: List<WallLight>)

val WALL_SPECS = listOf(
    WallSpec(
        "aurora", "Aurora", 0xFF0A0C12L, 0xFF0E1220L,
        listOf(
            WallLight(0.16f, 0.10f, 0.60f, 0.95f, C_VIOLET, 0.58f),
            WallLight(0.84f, 0.22f, 0.50f, 0.75f, C_TEAL, 0.42f),
            WallLight(0.62f, 1.00f, 0.50f, 0.80f, C_PINK, 0.34f),
            WallLight(0.50f, 0.38f, 0.62f, 0.16f, C_TEAL, 0.20f, -0.30f),
            WallLight(0.40f, 0.30f, 0.50f, 0.12f, C_VIOLET, 0.22f, 0.22f),
        ),
    ),
    WallSpec(
        "bara", "Bara", 0xFF120B0BL, 0xFF1A0F0CL,
        listOf(
            WallLight(0.14f, 0.10f, 0.60f, 0.95f, C_AMBER, 0.46f),
            WallLight(0.86f, 0.24f, 0.50f, 0.85f, C_RED, 0.34f),
            WallLight(0.56f, 1.00f, 0.55f, 0.90f, C_PINK, 0.24f),
            WallLight(0.50f, 0.40f, 0.62f, 0.15f, C_AMBER, 0.16f, -0.28f),
        ),
    ),
    WallSpec(
        "mint", "Mint", 0xFF07110FL, 0xFF0A1618L,
        listOf(
            WallLight(0.18f, 0.10f, 0.60f, 0.95f, C_TEAL, 0.46f),
            WallLight(0.84f, 0.22f, 0.52f, 0.85f, C_BLUE, 0.36f),
            WallLight(0.60f, 1.00f, 0.55f, 0.90f, C_GREEN, 0.26f),
            WallLight(0.50f, 0.38f, 0.62f, 0.15f, C_TEAL, 0.16f, -0.28f),
        ),
    ),
    WallSpec(
        "senja", "Senja", 0xFF0B0A16L, 0xFF120F24L,
        listOf(
            WallLight(0.20f, 0.08f, 0.62f, 0.95f, 0xFF5B6BFFL, 0.50f),
            WallLight(0.86f, 0.30f, 0.52f, 0.85f, C_PINK, 0.32f),
            WallLight(0.50f, 1.00f, 0.60f, 0.90f, C_VIOLET, 0.34f),
            WallLight(0.50f, 0.40f, 0.62f, 0.15f, C_PINK, 0.14f, -0.28f),
        ),
    ),
    WallSpec(
        "gletser", "Gletser", 0xFF080E14L, 0xFF0C1620L,
        listOf(
            WallLight(0.16f, 0.10f, 0.60f, 0.95f, C_BLUE, 0.46f),
            WallLight(0.84f, 0.24f, 0.52f, 0.85f, C_TEAL, 0.34f),
            WallLight(0.60f, 1.00f, 0.55f, 0.90f, 0xFFB6C8FFL, 0.18f),
            WallLight(0.50f, 0.38f, 0.62f, 0.15f, C_BLUE, 0.16f, -0.28f),
        ),
    ),
    WallSpec(
        "noir", "Noir", 0xFF0A0B0EL, 0xFF0D0F14L,
        listOf(
            WallLight(0.20f, 0.10f, 0.60f, 0.95f, 0xFF9AA7C7L, 0.14f),
            WallLight(0.84f, 0.24f, 0.50f, 0.85f, C_VIOLET, 0.10f),
        ),
    ),
)

fun wallById(id: String): WallSpec = WALL_SPECS.firstOrNull { it.id == id } ?: WALL_SPECS[0]
