package app.sukios

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

/**
 * SukiIndex — indeks aplikasi terpasang.
 *
 * Dipakai bersama oleh desktop, start menu, daftar aplikasi, dan peluncuran
 * ke dalam jendela. Indeks dibangun sekali di latar belakang, lalu diperiksa
 * ulang saat pengguna menekan "Muat ulang".
 */
data class AppEntry(
    val label: String,
    val pkg: String,
    val activity: String,
    val system: Boolean,
    val icon: ImageBitmap?,
) {
    val component: String get() = "$pkg/$activity"
}

class SukiIndex(private val ctx: Context) {

    val apps = MutableStateFlow<List<AppEntry>>(emptyList())
    val loading = MutableStateFlow(false)

    private val pm: PackageManager get() = ctx.packageManager

    suspend fun load() = withContext(Dispatchers.IO) {
        loading.value = true
        val found = ArrayList<AppEntry>(256)
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolve: List<ResolveInfo> = runCatching {
            if (Build.VERSION.SDK_INT >= 33) {
                pm.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(intent, 0)
            }
        }.getOrDefault(emptyList())

        for (r in resolve) {
            val ai: ApplicationInfo = r.activityInfo?.applicationInfo ?: continue
            val label = runCatching { r.loadLabel(pm).toString() }.getOrDefault(ai.packageName)
            val icon = runCatching {
                val d = r.loadIcon(pm)
                d.toBitmap(96, 96).asImageBitmap()
            }.getOrNull()
            found.add(
                AppEntry(
                    label = label,
                    pkg = ai.packageName,
                    activity = r.activityInfo.name,
                    system = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    icon = icon,
                )
            )
        }

        apps.value = found.distinctBy { it.pkg + "/" + it.activity }
            .sortedBy { it.label.lowercase() }
        loading.value = false
    }

    fun byPkg(pkg: String): AppEntry? = apps.value.firstOrNull { it.pkg == pkg }

    fun search(q: String, limit: Int = 48): List<AppEntry> {
        val key = q.trim().lowercase()
        if (key.isEmpty()) return apps.value.take(limit)
        return apps.value.filter {
            it.label.lowercase().contains(key) || it.pkg.lowercase().contains(key)
        }.take(limit)
    }

    private fun baseIntent(e: AppEntry): Intent =
        Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setClassName(e.pkg, e.activity)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

    /** Buka seperti biasa: layar penuh. Selalu bisa. */
    fun launch(e: AppEntry): Boolean = runCatching {
        ctx.startActivity(baseIntent(e)); true
    }.getOrDefault(false)

    /**
     * Coba buka sebagai jendela mengambang.
     *
     * INI BUKAN JAMINAN. App pihak ketiga hanya bisa mengambang kalau:
     *  - perangkat mendukung freeform/multi-window, DAN
     *  - app itu resizable (atau dipaksa lewat SukiShell: force_resizable_activities).
     * Kalau tidak, sistem mengabaikan bounds dan app tetap layar penuh.
     * Pemanggil wajib memeriksa hasil dan memberi tahu pengguna apa adanya.
     */
    fun launchWindowed(e: AppEntry, bounds: Rect): Boolean = runCatching {
        val opts = android.app.ActivityOptions.makeBasic().apply {
            setLaunchBounds(bounds)
        }
        ctx.startActivity(baseIntent(e), opts.toBundle())
        true
    }.getOrDefault(false)

    /** Coba buka di display lain (dipakai jalur SukiShell / eksperimen Lab). */
    fun launchOnDisplay(e: AppEntry, displayId: Int): Boolean = runCatching {
        val i = baseIntent(e)
        val opts = android.app.ActivityOptions.makeBasic().apply {
            setLaunchDisplayId(displayId)
        }
        ctx.startActivity(i, opts.toBundle())
        true
    }.getOrDefault(false)

    fun openInfo(e: AppEntry) = runCatching {
        ctx.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", e.pkg, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun uninstall(e: AppEntry) = runCatching {
        ctx.startActivity(
            Intent(Intent.ACTION_DELETE, Uri.fromParts("package", e.pkg, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun isCurrentLauncher(): Boolean {
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val r = if (Build.VERSION.SDK_INT >= 33) {
            pm.resolveActivity(i, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.resolveActivity(i, 0)
        }
        return r?.activityInfo?.packageName == ctx.packageName
    }

    fun openDefaultLauncherSettings() = runCatching {
        val i = if (Build.VERSION.SDK_INT >= 29) {
            Intent(Settings.ACTION_HOME_SETTINGS)
        } else {
            Intent(Settings.ACTION_HOME_SETTINGS)
        }
        ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun hasOverlay(): Boolean = Settings.canDrawOverlays(ctx)

    fun openOverlaySettings() = runCatching {
        ctx.startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.fromParts("package", ctx.packageName, null)
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
