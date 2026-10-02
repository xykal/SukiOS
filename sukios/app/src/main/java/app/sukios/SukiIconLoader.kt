package app.sukios

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import kotlin.math.roundToInt

/**
 * IconLoader — menyeragamkan ikon aplikasi jadi kotak penuh yang bisa dipotong SukiOS sendiri.
 *
 * Ikon adaptif digambar dari dua lapisannya (108 dp, area aman 72 dp di tengah) lalu diskalakan supaya
 * area aman memenuhi bitmap; bentuk topeng bawaan perangkat (lingkaran, squircle, dll.) tidak ikut.
 * Ikon lama yang tidak adaptif diberi plat gelap seragam dan digambar 78% di tengah.
 */
object IconLoader {

    const val PX = 128
    private const val SAFE_ZONE = 72f
    private const val LAYER = 108f
    private val PLATE = AndroidColor.parseColor("#242A38")

    fun render(d: Drawable, px: Int = PX): Bitmap {
        val bmp = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val c = AndroidCanvas(bmp)
        if (d is AdaptiveIconDrawable) {
            val full = (px * LAYER / SAFE_ZONE).roundToInt()
            val off = -((full - px) / 2)
            d.background?.let {
                it.setBounds(off, off, off + full, off + full)
                it.draw(c)
            }
            d.foreground?.let {
                it.setBounds(off, off, off + full, off + full)
                it.draw(c)
            }
        } else {
            c.drawColor(PLATE)
            val inset = (px * 0.11f).roundToInt()
            d.setBounds(inset, inset, px - inset, px - inset)
            d.draw(c)
        }
        return bmp
    }
}
