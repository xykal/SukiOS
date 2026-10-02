package app.sukios

/**
 * ShellArgs — validasi argumen sebelum menjadi argv untuk SukiShell.
 *
 * Allowlist ketat: yang tidak cocok ditolak, tidak dibersihkan. Tidak ada pola yang
 * boleh diawali "-", jadi nilai dari luar tidak bisa menyelinap menjadi opsi perintah
 * (argument injection). Tanpa jangkar ^ dan $: Regex.matches sudah mewajibkan seluruh
 * string cocok, sedangkan $ di Java masih menerima baris baru di ujung.
 */
object ShellArgs {

    private const val MAX_DISPLAY_ID = 999_999
    private const val MAX_TASK_ID = 9_999_999
    private const val MAX_COORD = 20_000

    /** Setelan global yang boleh disentuh SukiOS, semuanya terkait mode jendela. */
    val SETTING_KEYS = setOf(
        "enable_freeform_support",
        "force_resizable_activities",
        "enable_non_resizable_multi_window",
        "enable_sizecompat_freeform",
    )

    const val PERM_SECURE_SETTINGS = "android.permission.WRITE_SECURE_SETTINGS"
    const val ROLE_HOME = "android.app.role.HOME"
    const val OP_OVERLAY = "SYSTEM_ALERT_WINDOW"
    const val MODE_FREEFORM = 5

    private val PACKAGE = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)*")
    private val COMPONENT = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)*/\\.?[A-Za-z_][A-Za-z0-9_.\$]*")
    private val PROPERTY = Regex("[A-Za-z0-9][A-Za-z0-9_.\\-]*")
    private val INT = Regex("[0-9]{1,6}")

    fun isPackage(s: String): Boolean = PACKAGE.matches(s)

    fun isComponent(s: String): Boolean = COMPONENT.matches(s)

    fun isProperty(s: String): Boolean = PROPERTY.matches(s)

    fun isDisplayId(id: Int): Boolean = id in 0..MAX_DISPLAY_ID

    fun isTaskId(id: Int): Boolean = id in 1..MAX_TASK_ID

    /** Koordinat layar untuk `am task resize`: Android menolak nilai negatif, jadi kita pun tidak mengirimnya. */
    fun isCoord(v: Int): Boolean = v in 0..MAX_COORD

    fun isSettingKey(s: String): Boolean = s in SETTING_KEYS

    /** Teks bilangan bulat kecil, mis. nilai setelan 0/1 atau koordinat dalam argv. */
    fun isSmallInt(s: String): Boolean = INT.matches(s)
}

/**
 * ShellPolicy — daftar PERINTAH yang boleh dijalankan fungsi bertipe di SukiShell.
 *
 * Murni dan tanpa Android, jadi dibuktikan di CI (ShellPolicyTest). Tiap aturan memeriksa seluruh
 * argv: program, subperintah, dan setiap argumen. Terminal di Laboratorium sengaja TIDAK lewat sini:
 * itu fitur pengguna mahir yang memanggil SukiShell.run langsung dan melaporkan hasilnya apa adanya.
 */
object ShellPolicy {

    fun allows(a: List<String>): Boolean = when (a.firstOrNull()) {
        "settings" -> settings(a)
        "pm" -> a.size == 4 && a[1] == "grant" && ShellArgs.isPackage(a[2]) && a[3] == ShellArgs.PERM_SECURE_SETTINGS
        "appops" -> a.size == 5 && a[1] == "set" && ShellArgs.isPackage(a[2]) &&
            a[3] == ShellArgs.OP_OVERLAY && a[4] == "allow"
        "cmd" -> cmd(a)
        "am" -> am(a)
        "dumpsys" -> a == listOf("dumpsys", "activity", "activities") || a == listOf("dumpsys", "display")
        "wm" -> a == listOf("wm", "size")
        "getprop" -> a.size == 2 && ShellArgs.isProperty(a[1])
        "input" -> a == listOf("input", "keyevent", "3") || a == listOf("input", "keyevent", "4")
        "id" -> a.size == 1
        else -> false
    }

    private fun settings(a: List<String>): Boolean = when {
        a.size == 4 -> a[1] == "get" && a[2] == "global" && ShellArgs.isSettingKey(a[3])
        a.size == 5 -> a[1] == "put" && a[2] == "global" && ShellArgs.isSettingKey(a[3]) && (a[4] == "0" || a[4] == "1")
        else -> false
    }

    private fun cmd(a: List<String>): Boolean = when {
        a.size == 5 -> a[1] == "role" && a[2] == "add-role-holder" && a[3] == ShellArgs.ROLE_HOME && ShellArgs.isPackage(a[4])
        a.size == 4 -> a[1] == "package" && a[2] == "set-home-activity" && ShellArgs.isComponent(a[3])
        a.size == 3 -> a[1] == "statusbar" && a[2] == "expand-notifications"
        else -> false
    }

    private fun am(a: List<String>): Boolean = when (a.getOrNull(1)) {
        "start" -> a.size == 6 && a[2] == "--windowingMode" && a[3] == ShellArgs.MODE_FREEFORM.toString() &&
            a[4] == "-n" && ShellArgs.isComponent(a[5])
        "task" -> task(a)
        "force-stop" -> a.size == 3 && ShellArgs.isPackage(a[2])
        else -> false
    }

    /** `am task resize <id> <kiri> <atas> <kanan> <bawah>`: empat argumen terpisah (diperiksa di sumber AOSP 10 sampai 15). */
    private fun task(a: List<String>): Boolean =
        a.size == 8 && a[2] == "resize" && isTaskArg(a[3]) && a.subList(4, 8).all { c -> coordArg(c) }

    private fun isTaskArg(s: String): Boolean =
        s.length in 1..7 && s.all { it in '0'..'9' } && ShellArgs.isTaskId(s.toInt())

    private fun coordArg(s: String): Boolean = ShellArgs.isSmallInt(s) && (s.toIntOrNull()?.let { ShellArgs.isCoord(it) } == true)
}
