package app.sukios

import kotlin.math.roundToInt

/** Kotak dalam piksel layar: kiri, atas, kanan, bawah. */
data class PxRect(val l: Int, val t: Int, val r: Int, val b: Int) {
    val w: Int get() = r - l
    val h: Int get() = b - t
}

/**
 * WindowBounds — geometri awal jendela aplikasi pihak ketiga. Murni (tanpa Android), diuji di JVM.
 *
 * Sistem memilih kotak awal sendiri saat aplikasi dibuka sebagai jendela. SukiOS hanya turun tangan
 * bila kotak itu tidak masuk akal: menutupi taskbar, keluar layar, atau terlalu kecil.
 */
object WindowBounds {

    private const val MARGIN_DP = 8
    private const val MIN_W_DP = 320
    private const val MIN_H_DP = 220
    private const val TOLERANCE_DP = 6
    private const val CASCADE_DP = 28

    /** Area kerja: layar dikurangi taskbar dan margin. Sama dengan WinEngine.workArea, dalam bilangan bulat. */
    fun workArea(sw: Int, sh: Int, density: Float): PxRect {
        val d = if (density < 0.5f) 0.5f else density
        val m = (MARGIN_DP * d).roundToInt()
        val bar = (TASKBAR_DP * d).roundToInt()
        val right = (sw - m).coerceAtLeast(m + 1)
        val bottom = (sh - bar - m).coerceAtLeast(m + 1)
        return PxRect(m, m, right, bottom)
    }

    /** Kotak awal; [index] menggeser berundak supaya jendela baru tidak menumpuk persis. */
    fun initial(work: PxRect, portraitOnly: Boolean, index: Int, density: Float): PxRect {
        val d = if (density < 0.5f) 0.5f else density
        val wFrac = if (portraitOnly) 0.40f else 0.62f
        val hFrac = if (portraitOnly) 0.94f else 0.90f
        val w = (work.w * wFrac).roundToInt().coerceAtLeast((MIN_W_DP * d).roundToInt()).coerceAtMost(work.w)
        val h = (work.h * hFrac).roundToInt().coerceAtLeast((MIN_H_DP * d).roundToInt()).coerceAtMost(work.h)
        val step = ((index.coerceAtLeast(0) % 4) * CASCADE_DP * d).roundToInt()
        val left = (work.l + (work.w - w) / 2 + step).coerceAtMost(work.r - w).coerceAtLeast(work.l)
        val top = (work.t + (work.h - h) / 2 + step / 2).coerceAtMost(work.b - h).coerceAtLeast(work.t)
        return PxRect(left, top, left + w, top + h)
    }

    /** Apakah kotak dari sistem perlu dikoreksi? Null berarti tidak terbaca: jangan mengira-ngira. */
    fun needsFix(b: PxRect?, work: PxRect, density: Float): Boolean {
        if (b == null) return false
        val d = if (density < 0.5f) 0.5f else density
        val tol = (TOLERANCE_DP * d).roundToInt()
        return b.b > work.b + tol || b.r > work.r + tol || b.l < work.l - tol || b.t < work.t - tol ||
            b.w < (MIN_W_DP * d).roundToInt() || b.h < (MIN_H_DP * d).roundToInt()
    }

    /** Masukkan kotak ke area kerja: perkecil bila terlalu besar, geser bila keluar. */
    fun clampInto(b: PxRect, work: PxRect, density: Float): PxRect {
        val d = if (density < 0.5f) 0.5f else density
        val w = b.w.coerceAtLeast((MIN_W_DP * d).roundToInt()).coerceAtMost(work.w)
        val h = b.h.coerceAtLeast((MIN_H_DP * d).roundToInt()).coerceAtMost(work.h)
        val left = b.l.coerceAtMost(work.r - w).coerceAtLeast(work.l)
        val top = b.t.coerceAtMost(work.b - h).coerceAtLeast(work.t)
        return PxRect(left, top, left + w, top + h)
    }
}
