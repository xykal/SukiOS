package app.sukios

/** Cara memeriksa bahwa satu langkah persiapan sudah terpenuhi, tanpa menjalankan shell. */
enum class CheckKind { GLOBAL_SETTING, OVERLAY, SECURE_SETTINGS, HOME }

/**
 * Satu langkah persiapan otomatis. [commands] dicoba berurutan sampai [check] terpenuhi; langkah
 * [optional] yang gagal hanya diberi peringatan dan tidak membatalkan persiapan.
 */
data class PlanStep(
    val id: String,
    val label: String,
    val check: CheckKind,
    val key: String = "",
    val expect: String = "",
    val commands: List<List<String>>,
    val optional: Boolean = false,
)

/**
 * SetupPlan — daftar langkah yang dijalankan SukiOS lewat Shizuku supaya aplikasi bisa dibuka sebagai
 * jendela. Murni dan tanpa Android: isi daftar dan kebolehan tiap perintah dibuktikan di CI
 * (SetupPlanTest), pemeriksaan hasilnya dilakukan SukiAuto dengan API Android (bukan dengan menebak).
 *
 * Kunci setelan nomor tinggi (Android 12L ke atas) baru dikirim di versi itu; di versi lama kuncinya
 * tidak dikenal sistem dan hanya akan menyisakan nilai yatim.
 */
object SetupPlan {

    const val FREEFORM = "enable_freeform_support"
    const val RESIZABLE = "force_resizable_activities"
    const val NON_RESIZABLE = "enable_non_resizable_multi_window"
    const val SIZECOMPAT = "enable_sizecompat_freeform"

    /** Android 12L (API 32): setelan multi-window untuk aplikasi tidak resizable dikenal sejak sini. */
    const val SDK_MULTI_WINDOW_KEYS = 32

    /** Kunci yang harus menyala agar mode jendela mungkin. Dipakai juga oleh pemeriksa status. */
    val CORE_KEYS = listOf(FREEFORM, RESIZABLE)

    fun build(sdk: Int, pkg: String, homeComponent: String): List<PlanStep> {
        val steps = ArrayList<PlanStep>()
        steps += setting("freeform", "Aktifkan jendela mengambang", FREEFORM, optional = false)
        steps += setting("resizable", "Izinkan semua aplikasi diubah ukurannya", RESIZABLE, optional = false)
        if (sdk >= SDK_MULTI_WINDOW_KEYS) {
            steps += setting("nonresizable", "Izinkan aplikasi tetap-ukuran masuk jendela", NON_RESIZABLE, optional = true)
            steps += setting("sizecompat", "Izinkan mode kompatibilitas ukuran mengambang", SIZECOMPAT, optional = true)
        }
        steps += PlanStep(
            id = "secure",
            label = "Izin tulis setelan sistem untuk SukiOS",
            check = CheckKind.SECURE_SETTINGS,
            commands = listOf(listOf("pm", "grant", pkg, ShellArgs.PERM_SECURE_SETTINGS)),
            optional = true,
        )
        steps += PlanStep(
            id = "overlay",
            label = "Izin tampil di atas aplikasi lain",
            check = CheckKind.OVERLAY,
            commands = listOf(listOf("appops", "set", pkg, ShellArgs.OP_OVERLAY, "allow")),
            optional = true,
        )
        steps += PlanStep(
            id = "home",
            label = "Jadikan SukiOS launcher",
            check = CheckKind.HOME,
            commands = listOf(
                listOf("cmd", "role", "add-role-holder", ShellArgs.ROLE_HOME, pkg),
                listOf("cmd", "package", "set-home-activity", homeComponent),
            ),
            optional = true,
        )
        return steps
    }

    private fun setting(id: String, label: String, key: String, optional: Boolean) = PlanStep(
        id = id,
        label = label,
        check = CheckKind.GLOBAL_SETTING,
        key = key,
        expect = "1",
        commands = listOf(listOf("settings", "put", "global", key, "1")),
        optional = optional,
    )
}
