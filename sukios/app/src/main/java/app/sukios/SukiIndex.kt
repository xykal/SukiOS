package app.sukios

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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
    /** Aktivitas peluncur mengunci potret: dipakai untuk memilih bentuk kotak jendela awal. */
    val portraitOnly: Boolean = false,
) {
    val component: String get() = "$pkg/$activity"
}

/**
 * Penyaringan murni: cocokkan label atau nama paket tanpa membedakan huruf besar/kecil.
 * Dipisah dari SukiIndex supaya composable memberi daftar yang sudah di-collect (dan
 * ikut digambar ulang saat indeks selesai dimuat), serta supaya bisa diuji di JVM.
 */
fun filterApps(all: List<AppEntry>, query: String, limit: Int = 48): List<AppEntry> {
    val key = query.trim().lowercase()
    if (key.isEmpty()) return all.take(limit)
    return all.filter {
        it.label.lowercase().contains(key) || it.pkg.lowercase().contains(key)
    }.take(limit)
}

private val PORTRAIT_ORIENTATIONS = setOf(
    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
    ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
    ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT,
    ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT,
)

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
            val icon = runCatching { IconLoader.render(r.loadIcon(pm)).asImageBitmap() }.getOrNull()
            val portrait = r.activityInfo.screenOrientation in PORTRAIT_ORIENTATIONS
            found.add(
                AppEntry(
                    label = label,
                    pkg = ai.packageName,
                    activity = r.activityInfo.name,
                    system = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    icon = icon,
                    portraitOnly = portrait,
                )
            )
        }

        apps.value = found.distinctBy { it.pkg + "/" + it.activity }
            .sortedBy { it.label.lowercase() }
        loading.value = false
    }

    fun byPkg(pkg: String): AppEntry? = apps.value.firstOrNull { it.pkg == pkg }

    private fun baseIntent(e: AppEntry): Intent =
        Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setClassName(e.pkg, e.activity)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

    /**
     * Buka lewat startActivity biasa. Dipakai untuk "layar penuh", untuk membawa jendela yang sudah ada
     * ke depan, dan saat kebijakan jendela dimatikan. Jalur jendela ada di SukiWindowing.
     */
    fun launch(e: AppEntry): Boolean = runCatching {
        ctx.startActivity(baseIntent(e)); true
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
        ctx.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
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
