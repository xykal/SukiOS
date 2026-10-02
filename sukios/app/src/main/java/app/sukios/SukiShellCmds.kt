package app.sukios

// ============================================================================
// Perintah bertipe untuk SukiShell. Semuanya lewat runChecked: argumen divalidasi
// dan seluruh argv harus ada di ShellPolicy, jadi tidak ada string bebas yang
// sampai ke sisi shell. Hasil dikembalikan apa adanya (SukiResult).
// ============================================================================

private fun bad(msg: String) = SukiResult(ShellExec.CODE_BAD_ARGS, "", msg)

fun SukiShell.identity(): SukiResult = runChecked("id")

fun SukiShell.deviceSummary(): SukiResult = deviceSummaryRaw()

fun SukiShell.forceResizable(on: Boolean): SukiResult = putSetting("force_resizable_activities", on)

fun SukiShell.readForceResizable(): SukiResult = runChecked("settings", "get", "global", "force_resizable_activities")

fun SukiShell.putSetting(key: String, on: Boolean): SukiResult {
    if (!ShellArgs.isSettingKey(key)) return bad("Kunci setelan tidak diizinkan")
    return runChecked("settings", "put", "global", key, if (on) "1" else "0")
}

fun SukiShell.allowOverlay(pkg: String): SukiResult {
    if (!ShellArgs.isPackage(pkg)) return bad("Nama paket tidak valid")
    return runChecked("appops", "set", pkg, ShellArgs.OP_OVERLAY, "allow")
}

fun SukiShell.grantSecureSettings(pkg: String): SukiResult {
    if (!ShellArgs.isPackage(pkg)) return bad("Nama paket tidak valid")
    return runChecked("pm", "grant", pkg, ShellArgs.PERM_SECURE_SETTINGS)
}

fun SukiShell.setHomeRole(pkg: String): SukiResult {
    if (!ShellArgs.isPackage(pkg)) return bad("Nama paket tidak valid")
    return runChecked("cmd", "role", "add-role-holder", ShellArgs.ROLE_HOME, pkg)
}

fun SukiShell.setHomeActivity(component: String): SukiResult {
    if (!ShellArgs.isComponent(component)) return bad("Komponen tidak valid")
    return runChecked("cmd", "package", "set-home-activity", component)
}

/** Buka aktivitas sebagai jendela mengambang (windowing mode 5). Inilah satu-satunya jalur jendela pihak ketiga. */
fun SukiShell.launchFreeform(component: String): SukiResult {
    if (!ShellArgs.isComponent(component)) return bad("Komponen tidak valid")
    return runChecked("am", "start", "--windowingMode", ShellArgs.MODE_FREEFORM.toString(), "-n", component)
}

/** Ubah kotak jendela: `am task resize <id> kiri atas kanan bawah` (Android menolak koordinat negatif atau kanan/bawah nol). */
fun SukiShell.taskResize(taskId: Int, b: PxRect): SukiResult {
    if (!ShellArgs.isTaskId(taskId)) return bad("Id tugas tidak valid")
    if (!listOf(b.l, b.t, b.r, b.b).all { ShellArgs.isCoord(it) } || b.r <= 0 || b.b <= 0) return bad("Koordinat di luar batas")
    return runChecked("am", "task", "resize", taskId.toString(), b.l.toString(), b.t.toString(), b.r.toString(), b.b.toString())
}

/** Daftar tugas dan aktivitas dari sudut pandang sistem; diurai oleh FreeformParse. */
fun SukiShell.dumpTasks(): SukiResult = runChecked("dumpsys", "activity", "activities")

fun SukiShell.displays(): SukiResult = runChecked("dumpsys", "display")

fun SukiShell.windowSize(): SukiResult = runChecked("wm", "size")

fun SukiShell.forceStop(pkg: String): SukiResult {
    if (!ShellArgs.isPackage(pkg)) return bad("Nama paket tidak valid")
    return runChecked("am", "force-stop", pkg)
}

/** Tombol kembali/beranda global: berguna saat aplikasi pihak ketiga menutupi layar. */
fun SukiShell.globalBack(): SukiResult = runChecked("input", "keyevent", "4")

fun SukiShell.globalHome(): SukiResult = runChecked("input", "keyevent", "3")

fun SukiShell.systemProperty(key: String): SukiResult {
    if (!ShellArgs.isProperty(key)) return bad("Nama properti tidak valid")
    return runChecked("getprop", key)
}

fun SukiShell.expandStatusBar(): SukiResult = runChecked("cmd", "statusbar", "expand-notifications")
