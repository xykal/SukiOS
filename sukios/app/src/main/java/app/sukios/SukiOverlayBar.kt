package app.sukios

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * SukiOverlayBar — taskbar melayang di atas aplikasi lain.
 *
 * Ini jalur Lapis B: saat aplikasi pihak ketiga memenuhi layar, bar milik
 * SukiOS tetap bisa dipanggil. Dipakai untuk kembali, ke Beranda, atau
 * membuka start menu tanpa keluar dari aplikasi.
 *
 * Digambar dengan View biasa, bukan Compose: lebih ringan, tidak perlu
 * memasang pemilik siklus hidup di dalam jendela sistem, dan tidak ada
 * risiko crash saat proses aplikasi dibersihkan.
 */
class SukiOverlayBar : Service() {

    private lateinit var wm: WindowManager
    private var bar: View? = null

    // Tombol Kembali memanggil SukiShell (binder + proses): di luar thread utama,
    // hasilnya dikembalikan ke thread utama hanya untuk menampilkan pesan.
    private val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val ui = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        running.value = true
        show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (bar == null) show()
        return START_STICKY
    }

    override fun onDestroy() {
        hide()
        io.cancel()
        running.value = false
        super.onDestroy()
    }

    private fun show() {
        if (bar != null) return
        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        val strip = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#EB161B27"))
                cornerRadius = dp(16).toFloat()
                setStroke(dp(1), Color.parseColor("#33FFFFFF"))
            }
            setPadding(dp(6), dp(4), dp(6), dp(4))
            elevation = dp(6).toFloat()
        }

        fun button(label: String, onClick: () -> Unit): TextView = TextView(this).apply {
            text = label
            setTextColor(Color.parseColor("#F2F4F8"))
            typeface = ResourcesCompat.getFont(this@SukiOverlayBar, R.font.inter_medium)
            textSize = 13f
            setPadding(dp(14), dp(9), dp(14), dp(9))
            setOnClickListener { onClick() }
        }

        strip.addView(button("Kembali") {
            io.launch {
                val r = SukiShell.globalBack()
                if (r.code == ShellExec.CODE_NOT_RUNNABLE) ui.post { toast("Tombol kembali butuh Shizuku aktif.") }
            }
        })
        strip.addView(button("Beranda") { openHome(null) })
        strip.addView(button("Menu") { openHome("start") })

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(10)
            y = dp(10)
        }

        runCatching { wm.addView(strip, lp) }
            .onFailure {
                toast("Gagal menampilkan bar: ${it.message}")
                stopSelf()
                return
            }
        bar = strip
    }

    private fun hide() {
        bar?.let { runCatching { wm.removeView(it) } }
        bar = null
    }

    private fun openHome(extra: String?) {
        val i = Intent(this, SukiHomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (extra != null) putExtra(SukiHomeActivity.EXTRA_OPEN, extra)
        }
        runCatching { startActivity(i) }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    companion object {
        val running = MutableStateFlow(false)

        fun start(ctx: Context) {
            // startService dipakai dengan sengaja: bar ini BUKAN foreground service,
            // jadi memakai startForegroundService akan berakhir dengan ANR.
            runCatching { ctx.startService(Intent(ctx, SukiOverlayBar::class.java)) }
        }

        fun stop(ctx: Context) {
            runCatching { ctx.stopService(Intent(ctx, SukiOverlayBar::class.java)) }
        }

        fun toggle(ctx: Context, want: Boolean) {
            if (want) start(ctx) else stop(ctx)
        }
    }
}
