package app.sukios.poc

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================================
// PoC: mengukur apa yang BENAR-BENAR bisa dilakukan di HP ini.
//
// 5 jalur (lane) yang diuji:
//   1. Window manager milik SukiOS sendiri          -> selalu bisa
//   2. Freeform window (app pihak ketiga jadi jendela) -> tergantung perangkat
//   3. Split screen (2 app berdampingan)            -> tergantung perangkat
//   4. Embed via ActivityView (reflection)          -> kemungkinan besar ditolak
//   5. Overlay taskbar di atas app fullscreen       -> butuh izin overlay
//
// Hasilnya dikumpulkan jadi satu laporan teks yang bisa disalin/dibagikan.
// ============================================================================

// ---------------------------------------------------------------- daftar app
fun loadApps(ctx: Context): List<AppEntry> {
    val pm = ctx.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    @Suppress("DEPRECATION")
    val resolved = pm.queryIntentActivities(intent, 0)
    return resolved.mapNotNull { ri ->
        val pkg = ri.activityInfo?.packageName ?: return@mapNotNull null
        if (pkg == ctx.packageName) return@mapNotNull null
        val label = try {
            ri.loadLabel(pm).toString()
        } catch (_: Throwable) {
            pkg
        }
        AppEntry(label, pkg)
    }.distinctBy { it.pkg }.sortedBy { it.label.lowercase(Locale.US) }
}

/** Menyegarkan status Shizuku ke state (dipanggil saat boot, setelah izin, dan manual). */
fun refreshShizuku(ctx: Context, st: SukiState) {
    st.shizukuInstalled = ShizukuEngine.installed(ctx)
    st.shizukuReady = ShizukuEngine.granted()
    st.shizukuVersion = ShizukuEngine.version()
    st.shizukuUid = ShizukuEngine.uid()
}

// ------------------------------------------------------------ probe perangkat
fun runProbe(ctx: Context, act: Activity?, st: SukiState) {
    st.probes.clear()

    val pm = ctx.packageManager
    val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val dm = ctx.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    val cfg = ctx.resources.configuration
    val metrics = ctx.resources.displayMetrics

    fun add(label: String, value: String, ok: Boolean? = null) = st.probe(label, value, ok)

    // --- perangkat & layar ---
    add("Perangkat", "${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
    add("Android", "${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}")
    add("ABI", Build.SUPPORTED_ABIS.firstOrNull() ?: "?")
    add("Layar (dp)", "${cfg.screenWidthDp} × ${cfg.screenHeightDp} dp · ${metrics.densityDpi} dpi")
    add("smallestWidth", "${cfg.smallestScreenWidthDp} dp")
    add(
        "Kelas lebar (PRD §11.4)",
        when {
            cfg.screenWidthDp >= 840 -> "Expanded (≥840dp) — desktop penuh"
            cfg.screenWidthDp >= 600 -> "Medium (600–839dp)"
            else -> "Compact (<600dp) — target utama window manager"
        }
    )

    // --- sumber daya ---
    val lowRam = am.isLowRamDevice
    add(
        "RAM rendah",
        if (lowRam) "YA — platform membatasi semua mode multi-window!" else "tidak",
        !lowRam
    )
    add("memoryClass", "${am.memoryClass} MB (large: ${am.largeMemoryClass} MB)")
    add("Heap maks", "${Runtime.getRuntime().maxMemory() / (1024 * 1024)} MB")

    // --- kapabilitas windowing ---
    val freeform = pm.hasSystemFeature("android.software.freeform_window_management")
    val pip = pm.hasSystemFeature("android.software.picture_in_picture")
    val secondary = pm.hasSystemFeature("android.software.activities_on_secondary_displays")

    add("Feature FREEFORM", if (freeform) "ADA — app pihak ketiga bisa jadi jendela" else "tidak ada", freeform)
    add("Feature PiP", pip.toString(), pip)
    add("Feature activities on secondary displays", secondary.toString())
    add("Display terpasang", dm.displays.size.toString())
    dm.displays.firstOrNull()?.let { d ->
        @Suppress("DEPRECATION")
        add("Display utama", "${d.name} ${d.width}×${d.height} @ ${d.refreshRate}Hz")
    }
    add(
        "Sekarang in multi-window?",
        (act?.isInMultiWindowMode?.toString() ?: "tidak diketahui")
    )

    // --- izin khusus ---
    val overlay = Settings.canDrawOverlays(ctx)
    add("Izin SYSTEM_ALERT_WINDOW", if (overlay) "diberikan" else "belum (diminta dari Monitor)", overlay)

    // --- visibilitas daftar app ---
    val count = loadApps(ctx).size
    add(
        "App terlihat oleh launcher",
        "$count app",
        count > 5
    )

    // --- mode tampilan & perangkat Go ---
    add(
        "Kelas perangkat",
        if (lowRam) "Android Go / RAM rendah — multi-window dibatasi platform (mode Go: 3 jendela)"
        else "normal (batas 8 jendela)",
        !lowRam
    )

    // --- Shizuku (akses lanjutan) ---
    add("Shizuku terpasang", if (ShizukuEngine.installed(ctx)) "ya" else "tidak")
    add("Shizuku binder", if (ShizukuEngine.binderAlive()) "aktif" else "tidak aktif", ShizukuEngine.binderAlive())
    add("Shizuku versi API", ShizukuEngine.version().toString(), ShizukuEngine.version() >= 11)
    add(
        "Shizuku izin",
        if (ShizukuEngine.granted()) "diberikan (uid ${ShizukuEngine.uid()})" else "belum diberikan",
        ShizukuEngine.granted()
    )

    // --- bisakah ActivityView diakses? (reflection, bukan API publik) ---
    val cls = activityViewClass()
    if (cls == null) {
        add("Kelas android.app.ActivityView", "TIDAK ditemukan di runtime", false)
    } else {
        add("Kelas android.app.ActivityView", "ditemukan (${cls.name})")
        val inst = activityViewCreate(ctx)
        if (inst == null) {
            add("Instansiasi ActivityView", "GAGAL — konstruktor tidak bisa diakses", false)
        } else {
            add("Instansiasi ActivityView", "BERHASIL (objek dibuat)", true)
            activityViewRelease(inst)
        }
    }

    st.log("probe", "Probe selesai: ${st.probes.size} item")
}

// ------------------------------------------------------- LANE 2: freeform window
/**
 * Meluncurkan app pihak ketiga dengan permintaan "batas jendela" (bounds).
 * - Di perangkat yang mendukung freeform / desktop windowing (tablet, Android 16+,
 *   DeX, atau dev-option "force resizable"), app muncul sebagai jendela.
 * - Di HP biasa, umumnya diabaikan -> app muncul fullscreen.
 */
fun launchWithBounds(ctx: Context, pkg: String, freeformMode: Boolean, deskW: Int, deskH: Int): String {
    val intent = ctx.packageManager.getLaunchIntentForPackage(pkg)
        ?: return "GAGAL: $pkg tidak punya launch intent"

    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

    val l = (deskW * 0.10f).toInt()
    val t = (deskH * 0.12f).toInt()
    val r = (deskW * 0.82f).toInt()
    val b = (deskH * 0.80f).toInt()

    val opts = ActivityOptions.makeBasic().setLaunchBounds(Rect(l, t, r, b))

    var modeNote = ""
    if (freeformMode) {
        // WINDOWING_MODE_FREEFORM = 5 (WindowConfiguration). Bukan API publik -> reflection.
        try {
            val m = ActivityOptions::class.java
                .getMethod("setLaunchWindowingMode", Int::class.javaPrimitiveType)
            m.invoke(opts, 5)
            modeNote = " [mode freeform dipaksa]"
        } catch (t2: Throwable) {
            modeNote = " [setLaunchWindowingMode tidak tersedia: ${t2.javaClass.simpleName}]"
        }
    }

    return try {
        ctx.startActivity(intent, opts.toBundle())
        "Perintah dikirim$modeNote. Amati: app muncul sebagai JENDELA kecil atau FULLSCREEN?"
    } catch (t: Throwable) {
        "GAGAL: ${t.javaClass.simpleName}: ${t.message}"
    }
}

// --------------------------------------------------- LANE 2b: kontrol fullscreen
/** Kontrol: buka app seperti biasa (tanpa bounds) — pembanding untuk uji freeform. */
fun launchPlain(ctx: Context, pkg: String): String {
    val intent = ctx.packageManager.getLaunchIntentForPackage(pkg)
        ?: return "GAGAL: $pkg tidak punya launch intent"
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return try {
        ctx.startActivity(intent)
        "App dibuka normal (fullscreen). Ini pembanding: kalau ini jalan tapi 'Freeform' tidak, berarti HP tidak mengizinkan freeform."
    } catch (t: Throwable) {
        "GAGAL: ${t.javaClass.simpleName}: ${t.message}"
    }
}

// ---------------------------------------------------------- LANE 3: split screen
fun launchSplit(ctx: Context, pkg: String): String {
    val intent = ctx.packageManager.getLaunchIntentForPackage(pkg)
        ?: return "GAGAL: $pkg tidak punya launch intent"

    intent.addFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
            Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
    )

    val opts = ActivityOptions.makeBasic()
    var note = ""

    // WINDOWING_MODE_SPLIT_SCREEN_SECONDARY = 4 (reflection, bukan API publik)
    try {
        val m = ActivityOptions::class.java
            .getMethod("setLaunchWindowingMode", Int::class.javaPrimitiveType)
        m.invoke(opts, 4)
        note = " [mode split-secondary diminta]"
    } catch (t: Throwable) {
        note = " [setLaunchWindowingMode tidak tersedia: ${t.javaClass.simpleName}]"
    }

    return try {
        ctx.startActivity(intent, opts.toBundle())
        "Perintah dikirim$note. Kalau muncul fullscreen: split programmatic TIDAK didukung di HP ini (pakai cara manual dari Recents)."
    } catch (t: Throwable) {
        "GAGAL: ${t.javaClass.simpleName}: ${t.message}"
    }
}

// --------------------------------------------------- LANE 4: embed via ActivityView
private fun activityViewClass(): Class<*>? = try {
    Class.forName("android.app.ActivityView")
} catch (_: Throwable) {
    null
}

fun activityViewCreate(ctx: Context): Any? {
    val cls = activityViewClass() ?: return null
    try {
        return cls.getConstructor(Context::class.java).newInstance(ctx)
    } catch (_: Throwable) {
        // beberapa versi hanya menyediakan konstruktor internal
    }
    return try {
        cls.getConstructor(
            Context::class.java,
            AttributeSet::class.java,
            Int::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType
        ).newInstance(ctx, null, 0, false)
    } catch (_: Throwable) {
        null
    }
}

fun activityViewStart(ctx: Context, view: Any, pkg: String): String {
    val intent = ctx.packageManager.getLaunchIntentForPackage(pkg)
        ?: return "GAGAL: $pkg tidak punya launch intent"
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    return try {
        val m = view.javaClass.getMethod("startActivity", Intent::class.java)
        m.invoke(view, intent)
        "Perintah terkirim ke ActivityView — lihat kotak pratinjau di bawah."
    } catch (t: Throwable) {
        val root = rootCause(t)
        val extra = when {
            root is IllegalStateException -> " (ActivityView belum siap / sudah dilepas)"
            root is SecurityException -> " (app penyedia activity tidak mengizinkan embed)"
            else -> ""
        }
        "GAGAL$extra: ${root.javaClass.simpleName}: ${root.message}"
    }
}

fun activityViewRelease(view: Any): String = try {
    view.javaClass.getMethod("release").invoke(view)
    "ActivityView dilepas."
} catch (t: Throwable) {
    "Gagal melepas: ${rootCause(t).javaClass.simpleName}"
}

private fun rootCause(t: Throwable): Throwable {
    var c: Throwable = t
    var guard = 0
    while (c.cause != null && c.cause !== c && guard++ < 12) {
        c = c.cause!!
    }
    return c
}

// ------------------------------------------------------------ LANE 5: overlay
fun canOverlay(ctx: Context): Boolean = Settings.canDrawOverlays(ctx)

fun openOverlaySettings(ctx: Context) {
    try {
        val i = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${ctx.packageName}")
        )
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(i)
    } catch (_: Throwable) {
        // perangkat tanpa layar pengaturan ini
    }
}

fun startOverlay(ctx: Context): String = try {
    ctx.startService(Intent(ctx, OverlayTaskbarService::class.java))
    "Overlay dimulai. Buka app lain — apakah taskbar tetap terlihat di bawah?"
} catch (t: Throwable) {
    "GAGAL menjalankan overlay: ${t.javaClass.simpleName}: ${t.message}"
}

fun stopOverlay(ctx: Context): String = try {
    ctx.stopService(Intent(ctx, OverlayTaskbarService::class.java))
    "Overlay dihentikan."
} catch (t: Throwable) {
    "Gagal menghentikan overlay: ${t.javaClass.simpleName}"
}

// ------------------------------------------------------------------ laporan
fun buildReport(ctx: Context, st: SukiState): String {
    val sb = StringBuilder()
    val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

    sb.appendLine("=== SUKIOS POC REPORT ===")
    sb.appendLine("Waktu    : $stamp")
    sb.appendLine("Versi    : 0.1.0-poc")
    sb.appendLine()

    sb.appendLine("--- KAPABILITAS PERANGKAT ---")
    if (st.probes.isEmpty()) sb.appendLine("(probe belum dijalankan)")
    st.probes.forEach { p ->
        val mark = when (p.ok) {
            true -> "[OK]  "
            false -> "[--]  "
            null -> "      "
        }
        sb.appendLine("$mark${p.label}: ${p.value}")
    }
    sb.appendLine()

    sb.appendLine("--- HASIL UJI (dinilai pengguna) ---")
    if (st.verdicts.isEmpty()) {
        sb.appendLine("(belum ada penilaian)")
    } else {
        st.verdicts.forEach { (k, v) -> sb.appendLine("• $k = $v") }
    }
    sb.appendLine()

    sb.appendLine("--- JENDELA SUKIOS ---")
    st.windows.forEach { w ->
        sb.appendLine("• ${w.kind.title}: ${w.w}×${w.h} px @ (${w.x},${w.y})${if (w.maximized) " [max]" else ""}")
    }
    sb.appendLine()

    sb.appendLine("--- LOG (80 terakhir dari ${st.logs.size}) ---")
    st.logs.takeLast(80).forEach { l -> sb.appendLine("[${l.stamp}] ${l.tag}: ${l.msg}") }

    return sb.toString()
}

fun copyReport(ctx: Context, st: SukiState): String {
    val txt = buildReport(ctx, st)
    return try {
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("SukiOS PoC Report", txt))
        "Laporan disalin ke clipboard (${txt.length} karakter). Tempel di chat!"
    } catch (t: Throwable) {
        "Gagal menyalin: ${t.javaClass.simpleName}"
    }
}

fun shareReport(ctx: Context, st: SukiState): String {
    return try {
        val i = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, "SukiOS PoC Report")
            .putExtra(Intent.EXTRA_TEXT, buildReport(ctx, st))
        val chooser = Intent.createChooser(i, "Bagikan laporan PoC")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(chooser)
        "Membuka dialog berbagi…"
    } catch (t: Throwable) {
        "Gagal berbagi: ${t.javaClass.simpleName}"
    }
}
