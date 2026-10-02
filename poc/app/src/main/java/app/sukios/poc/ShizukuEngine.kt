package app.sukios.poc

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

// ============================================================================
// Engine akses lanjutan lewat Shizuku (identitas shell / uid 2000).
//
// Kenapa ini penting untuk SukiOS:
//  - Perangkat Android Go / RAM rendah diblokir platform dari multi-window.
//  - Freeform window untuk app pihak ketiga juga ditolak untuk app biasa.
//  - Dengan identitas shell, beberapa pintu terbuka: force-resizable,
//    appops, peluncuran app ke display tertentu, dan diagnostik.
//
// CATATAN VERSI (dari pemeriksaan bytecode AAR, bukan tebakan):
//  - api 13.1.5: Shizuku.newProcess SUDAH TIDAK ADA (upstream menyiapkan
//    penghapusan; penggantinya UserService).
//  - api 12.2.0: newProcess masih ada, bersama API izin modern.
//  Karena itu proyek ini memakai pasangan 12.2.0 (api + provider).
//  Bila kelak pindah ke 13.x, newProcess harus diganti UserService;
//  pemanggilan lewat reflection di bawah sengaja disiapkan untuk masa transisi.
// ============================================================================

data class ShellResult(val code: Int, val out: String, val err: String) {
    val ok: Boolean get() = code == 0

    /** Ringkasan singkat untuk ditampilkan di UI/log. */
    fun brief(limit: Int = 220): String {
        val body = if (ok) out else err.ifBlank { out }
        val joined = body.lineSequence()
            .filter { it.isNotBlank() }
            .take(4)
            .joinToString(" | ")
        return joined.take(limit).ifBlank { "exit=$code" }
    }
}

object ShizukuEngine {

    const val REQ_CODE = 4201
    const val PKG_SHIZUKU = "moe.shizuku.privileged.api"

    fun installed(ctx: Context): Boolean = try {
        ctx.packageManager.getPackageInfo(PKG_SHIZUKU, 0)
        true
    } catch (_: Throwable) {
        false
    }

    fun binderAlive(): Boolean = try {
        Shizuku.pingBinder()
    } catch (_: Throwable) {
        false
    }

    fun version(): Int = try {
        Shizuku.getVersion()
    } catch (_: Throwable) {
        -1
    }

    fun uid(): Int = try {
        Shizuku.getUid()
    } catch (_: Throwable) {
        -1
    }

    fun granted(): Boolean = try {
        binderAlive() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (_: Throwable) {
        false
    }

    fun requestPermission() {
        try {
            Shizuku.requestPermission(REQ_CODE)
        } catch (_: Throwable) {
            // Shizuku tidak aktif: UI akan menjelaskan lewat status, bukan crash.
        }
    }

    /** Membungkus callback Shizuku jadi lambda sederhana untuk Compose. */
    fun newResultListener(onResult: (code: Int, granted: Boolean) -> Unit): Shizuku.OnRequestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { code, grantResult ->
            onResult(code, grantResult == PackageManager.PERMISSION_GRANTED)
        }

    fun addListener(listener: Shizuku.OnRequestPermissionResultListener) {
        try {
            Shizuku.addRequestPermissionResultListener(listener)
        } catch (_: Throwable) {
        }
    }

    fun removeListener(listener: Shizuku.OnRequestPermissionResultListener) {
        try {
            Shizuku.removeRequestPermissionResultListener(listener)
        } catch (_: Throwable) {
        }
    }

    fun openShizukuApp(ctx: Context): Boolean = try {
        val intent = ctx.packageManager.getLaunchIntentForPackage(PKG_SHIZUKU)
        if (intent == null) {
            false
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
            true
        }
    } catch (_: Throwable) {
        false
    }

    /**
     * Menjalankan perintah shell dengan identitas shell (uid 2000).
     * Utama: Shizuku.newProcess. Cadangan: reflection, untuk masa transisi
     * ketika kelas API berubah antar versi.
     */
    fun shell(command: String): ShellResult {
        if (!binderAlive()) return ShellResult(-1, "", "Shizuku binder belum aktif")
        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val out = process.inputStream.bufferedReader().readText()
            val err = process.errorStream.bufferedReader().readText()
            ShellResult(process.waitFor(), out.trim(), err.trim())
        } catch (first: Throwable) {
            shellViaReflection(command, first)
        }
    }

    private fun shellViaReflection(command: String, firstError: Throwable): ShellResult = try {
        val cls = Class.forName("rikka.shizuku.Shizuku")
        val method = cls.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        )
        method.isAccessible = true
        val args: Any = arrayOf("sh", "-c", command)
        val process = method.invoke(null, args, null, null) as? Process
        if (process == null) {
            ShellResult(-1, "", "newProcess tidak mengembalikan Process")
        } else {
            val out = process.inputStream.bufferedReader().readText()
            val err = process.errorStream.bufferedReader().readText()
            ShellResult(process.waitFor(), out.trim(), err.trim())
        }
    } catch (second: Throwable) {
        ShellResult(
            -1, "",
            "GAGAL: ${firstError.javaClass.simpleName} lalu ${second.javaClass.simpleName}: " +
                (second.message ?: "-")
        )
    }

    // ------------------------------------------------------------------ status
    /** Ringkasan status untuk ditampilkan di UI. */
    fun statusLine(ctx: Context): String = when {
        !installed(ctx) -> "Shizuku belum terpasang di perangkat ini"
        !binderAlive() -> "Shizuku terpasang, tetapi servis belum aktif (buka app Shizuku)"
        version() < 11 -> "Shizuku versi lama (API ${version()}); minimal API 11"
        granted() -> "Siap: izin diberikan, uid shell = ${uid()}"
        else -> "Menunggu izin (uid shell belum diberikan)"
    }
}
