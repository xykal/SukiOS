package app.sukios.poc

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

/**
 * LANE 5 — Uji "taskbar melayang di atas aplikasi lain" (TYPE_APPLICATION_OVERLAY).
 *
 * Ini jalur fallback yang dipakai SukiOS kalau app pihak ketiga tidak bisa
 * dijalankan di dalam jendela: app tetap fullscreen, tapi taskbar SukiOS
 * tetap terlihat di bawahnya — persis yang dilakukan launcher PC-style populer.
 *
 * Butuh izin SYSTEM_ALERT_WINDOW (diminta lewat Settings di Monitor Perangkat).
 */
class OverlayTaskbarService : Service() {

    private var bar: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (bar == null) {
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val dp = resources.displayMetrics.density

            val lp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                (56 * dp).toInt(),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
            lp.gravity = Gravity.BOTTOM

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding((16 * dp).toInt(), 0, (16 * dp).toInt(), 0)
                setBackgroundColor(Color.parseColor("#E612151D"))
            }

            val label = TextView(this).apply {
                text = "SukiOS Taskbar — mengapung di atas app lain"
                setTextColor(Color.parseColor("#F2F4F8"))
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val stop = TextView(this).apply {
                text = "Hentikan"
                setTextColor(Color.parseColor("#35D0BA"))
                textSize = 12f
                setPadding((12 * dp).toInt(), (8 * dp).toInt(), (12 * dp).toInt(), (8 * dp).toInt())
                setOnClickListener { stopSelf() }
            }

            row.addView(label)
            row.addView(stop)

            return try {
                wm.addView(row, lp)
                bar = row
                START_STICKY
            } catch (t: Throwable) {
                stopSelf()
                START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        bar?.let { v ->
            try {
                (getSystemService(Context.WINDOW_SERVICE) as WindowManager).removeView(v)
            } catch (_: Throwable) {
                // sudah dilepas
            }
        }
        bar = null
        super.onDestroy()
    }
}
