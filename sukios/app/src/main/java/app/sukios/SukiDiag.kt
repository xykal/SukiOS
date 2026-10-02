package app.sukios

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import java.util.Locale

/**
 * SukiDiag — diagnostik perangkat dan ekspor laporan.
 *
 * Laporan ditulis apa adanya: kalau sebuah pemeriksaan gagal, hasilnya
 * "gagal" dengan alasan, bukan nilai karangan. Dipakai untuk menjawab
 * pertanyaan yang menentukan arah produk:
 *   1. Perangkat ini kelas apa (normal / RAM rendah / Go)?
 *   2. Multi-window dan freeform tersedia atau tidak?
 *   3. Shizuku siap atau tidak?
 */
class SukiDiag(private val ctx: Context) {

    data class Fact(val key: String, val value: String)

    fun deviceFacts(): List<Fact> {
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val stat = runCatching { StatFs(Environment.getDataDirectory().path) }.getOrNull()
        val mem = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(mem)
        val totalMb = mem.totalMem / 1_048_576L

        // Setiap baris di bawah dibaca dari sistem atau fitur resmi PackageManager; tidak ada
        // yang disimpulkan dari tebakan. Hasil yang menentukan arah window engine datang dari
        // uji buka jendela di Laboratorium, bukan dari baris-baris ini saja.
        return listOf(
            Fact("Android", "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"),
            Fact("Perangkat", "${Build.MANUFACTURER} ${Build.MODEL}"),
            Fact("ABI", Build.SUPPORTED_ABIS.joinToString(", ")),
            Fact("RAM total", if (am != null && totalMb > 0) "$totalMb MB" else "tidak terbaca"),
            Fact("Batas heap aplikasi", if (am != null) "${am.memoryClass} MB" else "tidak terbaca"),
            Fact("RAM rendah (sistem)", if (am?.isLowRamDevice == true) "YA" else "tidak"),
            Fact("Kelas perangkat", SukiRuntime.deviceClass),
            Fact("Display", displayFacts()),
            Fact("Fitur freeform", featureFact("android.software.freeform_window_management")),
            Fact("Fitur layar sekunder", featureFact("android.software.activities_on_secondary_displays")),
            Fact("Fitur picture-in-picture", featureFact("android.software.picture_in_picture")),
            Fact("enable_freeform_support", globalSetting("enable_freeform_support")),
            Fact("force_resizable_activities", globalSetting("force_resizable_activities")),
            Fact("Penyimpanan data", if (stat != null) "${freeGb(stat)} GB bebas dari ${totalGb(stat)} GB" else "tidak terbaca"),
            Fact("Launcher default", if (SukiRuntime.isDefaultLauncher) "SukiOS" else "aplikasi lain"),
        )
    }

    private fun featureFact(name: String): String {
        val has = runCatching { ctx.packageManager.hasSystemFeature(name) }.getOrDefault(false)
        return if (has) "ada" else "tidak ada"
    }

    private fun globalSetting(key: String): String =
        runCatching { Settings.Global.getString(ctx.contentResolver, key) }.getOrNull() ?: "(tidak diset)"

    private fun freeGb(s: StatFs) = String.format(Locale.US, "%.1f", s.availableBytes / 1073741824.0)
    private fun totalGb(s: StatFs) = String.format(Locale.US, "%.1f", s.totalBytes / 1073741824.0)

    private fun displayFacts(): String {
        // Configuration dari resource aplikasi: sumber yang benar untuk dp.
        val c = ctx.resources.configuration
        return "kecil=${c.smallestScreenWidthDp}dp, layar=${c.screenWidthDp}x${c.screenHeightDp}dp, dpi=${c.densityDpi}"
    }

    fun shellFacts(): List<Fact> {
        val s = SukiShell.state.value
        return listOf(
            Fact("Shizuku terpasang", if (s.installed) "ya" else "tidak"),
            Fact("Binder hidup", if (s.binderAlive) "ya" else "tidak"),
            Fact("Versi Shizuku", if (s.version > 0) s.version.toString() else "-"),
            Fact("Izin diberikan", if (s.granted) "ya" else "tidak"),
            Fact("SukiShell tersambung", if (s.serviceBound) "ya (protokol ${s.protocol})" else "tidak"),
            Fact("Identitas", when {
                s.uid == 2000 -> "shell (uid 2000)"
                s.uid == 0 -> "root (uid 0)"
                else -> "-"
            }),
            Fact("Catatan", s.note),
        )
    }

    /** Laporan teks lengkap, siap dibagikan lewat menu bagikan Android. */
    fun buildReport(): String {
        val sb = StringBuilder()
        sb.append("SukiOS — laporan diagnostik\n")
        sb.append("dibuat oleh: ").append(Brand.CREDIT).append('\n')
        sb.append("waktu      : ").append(Fmt.utcStamp(System.currentTimeMillis())).append(" UTC\n")
        sb.append("versi app  : ").append(appVersion()).append('\n')
        sb.append("kelas alat : ").append(SukiRuntime.deviceClass).append('\n')
        sb.append("\n== Perangkat ==\n")
        deviceFacts().forEach { sb.append(pad(it.key)).append(": ").append(it.value).append('\n') }
        sb.append("\n== Akses lanjutan ==\n")
        shellFacts().forEach { sb.append(pad(it.key)).append(": ").append(it.value).append('\n') }

        val shell = SukiShell.state.value
        if (shell.ready) {
            sb.append("\n== Dari sisi shell ==\n")
            val summary = SukiShell.deviceSummary()
            sb.append(if (summary.ok) summary.out.trim() else "gagal: ${summary.err.trim()}").append('\n')
            sb.append("\n== Ukuran jendela sistem ==\n")
            val wmSize = SukiShell.windowSize()
            sb.append(if (wmSize.ok) wmSize.out.trim() else "gagal: ${wmSize.err.trim()}").append('\n')
        } else {
            sb.append("\n(sisi shell tidak aktif — bagian ini dilewati, bukan dikosongkan)\n")
        }
        return sb.toString()
    }

    private fun pad(s: String) = s.padEnd(24, ' ')

    fun appVersion(): String = runCatching {
        val pi = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        "v${pi.versionName} (${pi.longVersionCode})"
    }.getOrDefault("tidak terbaca")

    /** Jalankan rangkaian uji jendela. Hasilnya jujur: berhasil / gagal / belum bisa dinilai. */
    fun windowProbe(entry: AppEntry?, bounds: android.graphics.Rect): String {
        if (entry == null) return "Pilih satu aplikasi di daftar Aplikasi dulu."
        val launched = ctx.let { app ->
            runCatching {
                val i = android.content.Intent(android.content.Intent.ACTION_MAIN)
                    .addCategory(android.content.Intent.CATEGORY_LAUNCHER)
                    .setClassName(entry.pkg, entry.activity)
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                val opts = android.app.ActivityOptions.makeBasic().apply { setLaunchBounds(bounds) }
                app.startActivity(i, opts.toBundle())
                true
            }.getOrDefault(false)
        }
        return if (launched) {
            "Perintah kirim. Periksa layar: kalau app memenuhi layar penuh, perangkat ini " +
                "tidak menghormati bounds (freeform mati). Kalau mengambang sesuai kotak, " +
                "berarti jalur jendela app pihak ketiga TERBUKA di perangkat ini."
        } else {
            "Gagal mengirim perintah peluncuran jendela."
        }
    }
}
