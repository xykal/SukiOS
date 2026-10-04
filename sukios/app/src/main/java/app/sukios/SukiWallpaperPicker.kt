package app.sukios

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent

/** Membuka pemilih live wallpaper bawaan Android. Tidak butuh izin khusus. */
fun openLiveWallpaperPicker(ctx: Context): Boolean {
    val live = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return runCatching {
        ctx.startActivity(live)
        true
    }.getOrElse {
        // Beberapa ROM mengganti/menyembunyikan chooser live wallpaper; fallback ke picker wallpaper umum.
        runCatching {
            ctx.startActivity(Intent(Intent.ACTION_SET_WALLPAPER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        }.getOrDefault(false)
    }
}
