package app.sukios.poc

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================================
// Model & state SukiOS PoC
// ============================================================================

enum class WinKind(val title: String) {
    TESTS("Panduan Uji"),
    PICKER("Daftar Aplikasi"),
    MONITOR("Monitor Perangkat"),
    EMBED("Uji Embed"),
    FILES("Files"),
    NOTES("Notes")
}

class WinState(val kind: WinKind, x0: Int, y0: Int, w0: Int, h0: Int) {
    val id: String = "w" + (++counter)

    var x by mutableStateOf(x0)
    var y by mutableStateOf(y0)
    var w by mutableStateOf(w0)
    var h by mutableStateOf(h0)

    var maximized by mutableStateOf(false)
    var minimized by mutableStateOf(false)
    var z by mutableStateOf(0)

    private var px = x0
    private var py = y0
    private var pw = w0
    private var ph = h0

    fun saveBounds() { px = x; py = y; pw = w; ph = h }
    fun restoreBounds() { x = px; y = py; w = pw; h = ph }

    companion object {
        private var counter = 0
    }
}

data class AppEntry(val label: String, val pkg: String)

class Probe(val label: String, val value: String, val ok: Boolean?)

class LogLine(val stamp: String, val tag: String, val msg: String)

/**
 * Sumber kebenaran tunggal untuk seluruh shell.
 * Di produksi ini pindah ke ViewModel + Room (PRD §9.4).
 */
class SukiState {

    val windows = mutableStateListOf<WinState>()
    val probes = mutableStateListOf<Probe>()
    val logs = mutableStateListOf<LogLine>()
    val apps = mutableStateListOf<AppEntry>()
    val verdicts = mutableStateMapOf<String, String>()

    var zTop by mutableStateOf(0)
    var activeId by mutableStateOf<String?>(null)
    var startOpen by mutableStateOf(false)
    var appsLoaded by mutableStateOf(false)
    var snapHint by mutableStateOf<String?>(null)
    var overlayRunning by mutableStateOf(false)

    /** Activity host — dipakai probe (mis. isInMultiWindowMode). Di-set dari SukiRoot. */
    var act: Activity? by mutableStateOf(null)

    // ---------------------------------------------------------------- logging
    fun log(tag: String, msg: String) {
        logs.add(LogLine(now(), tag, msg))
        while (logs.size > 400) logs.removeAt(0)
    }

    // ---------------------------------------------------------------- windows
    fun open(kind: WinKind, deskW: Int, deskH: Int): WinState {
        windows.firstOrNull { it.kind == kind }?.let { w ->
            w.minimized = false
            focus(w)
            return w
        }

        val w0 = ci((deskW * 0.62f).toInt(), 320, maxOf(320, deskW - 16))
        val h0 = ci((deskH * 0.68f).toInt(), 260, maxOf(260, deskH - 16))
        val n = windows.size
        val x0 = ci((deskW - w0) / 2 + (n % 5) * 30 - 60, 8, maxOf(8, deskW - w0 - 8))
        val y0 = ci((deskH - h0) / 2 + (n % 5) * 24 - 40, 4, maxOf(4, deskH - h0 - 8))

        val win = WinState(kind, x0, y0, w0, h0)
        win.saveBounds()
        windows.add(win)
        focus(win)
        log("shell", "Jendela dibuka: ${kind.title}")
        return win
    }

    fun focus(w: WinState) {
        w.z = ++zTop
        activeId = w.id
    }

    fun close(w: WinState) {
        windows.remove(w)
        if (activeId == w.id) {
            val top = windows.filter { !it.minimized }.maxByOrNull { it.z }
            activeId = top?.id
        }
        log("shell", "Jendela ditutup: ${w.kind.title}")
    }

    fun toggleMinimize(w: WinState) {
        w.minimized = !w.minimized
        if (!w.minimized) focus(w) else if (activeId == w.id) activeId = null
    }

    fun toggleMax(w: WinState, deskW: Int, deskH: Int) {
        if (w.maximized) {
            w.restoreBounds()
            w.maximized = false
        } else {
            w.saveBounds()
            w.x = 0
            w.y = 0
            w.w = deskW
            w.h = deskH
            w.maximized = true
        }
        log("shell", "${w.kind.title} ${if (w.maximized) "maximize" else "restore"}")
    }

    /** Zona snap: MAX · L · R · TL · TR · BL · BR (DESIGN.md §12.1) */
    fun snap(w: WinState, zone: String, deskW: Int, deskH: Int) {
        if (w.maximized) {
            w.restoreBounds()
            w.maximized = false
        }
        w.saveBounds()

        if (zone == "MAX") {
            toggleMax(w, deskW, deskH)
            return
        }

        val r = zoneRect(zone, deskW, deskH)
        w.x = r[0]
        w.y = r[1]
        w.w = r[2]
        w.h = r[3]
        log("snap", "${w.kind.title} -> $zone")
    }

    // ---------------------------------------------------------------- results
    fun verdict(key: String, value: String) {
        verdicts[key] = value
        log("verdict", "$key = $value")
    }

    fun probe(label: String, value: String, ok: Boolean? = null) {
        probes.add(Probe(label, value, ok))
    }

    companion object {
        private val FMT = SimpleDateFormat("HH:mm:ss", Locale.US)
        fun now(): String = FMT.format(Date())
    }
}
