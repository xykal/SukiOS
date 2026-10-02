package app.sukios

import androidx.compose.ui.graphics.Color

// Pemetaan jenis jendela dan nada pesan ke ikon dan warna, dipakai bersama oleh desktop, taskbar, dan bilah judul.

fun glyphFor(kind: WinKind): GlyphKind = when (kind) {
    WinKind.SETTINGS -> GlyphKind.SETTINGS
    WinKind.LAB -> GlyphKind.FLASK
    WinKind.TERMINAL -> GlyphKind.TERMINAL
    WinKind.APPS -> GlyphKind.APPS
    WinKind.ABOUT -> GlyphKind.INFO
    WinKind.DIAG -> GlyphKind.CHART
    WinKind.APP -> GlyphKind.WINDOW
}

fun kindTone(kind: WinKind): Color = Color(
    when (kind) {
        WinKind.SETTINGS -> C_VIOLET
        WinKind.APPS -> C_TEAL
        WinKind.TERMINAL -> C_BLUE
        WinKind.LAB -> C_PINK
        WinKind.ABOUT -> C_AMBER
        WinKind.DIAG -> C_GREEN
        WinKind.APP -> C_VIOLET
    },
)

fun toneColor(t: Tone): Color = when (t) {
    Tone.OK -> SSuccess
    Tone.WARN -> SWarning
    Tone.ERR -> SDanger
    Tone.INFO -> SInfo
}
