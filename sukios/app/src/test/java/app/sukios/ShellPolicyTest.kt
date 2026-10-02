package app.sukios

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Bukti: hanya perintah yang dikenal dan berargumen sah yang diizinkan fungsi bertipe di SukiShell. */
class ShellPolicyTest {

    private fun ok(vararg a: String) = assertTrue("harus diizinkan: ${a.toList()}", ShellPolicy.allows(a.toList()))
    private fun no(vararg a: String) = assertFalse("harus ditolak: ${a.toList()}", ShellPolicy.allows(a.toList()))

    @Test
    fun window_mode_settings_are_allowed_with_zero_or_one_only() {
        ok("settings", "put", "global", "enable_freeform_support", "1")
        ok("settings", "put", "global", "force_resizable_activities", "0")
        ok("settings", "get", "global", "force_resizable_activities")
        no("settings", "put", "global", "enable_freeform_support", "2")
        no("settings", "put", "global", "enable_freeform_support", "1;id")
        no("settings", "put", "global", "adb_enabled", "1")
        no("settings", "put", "secure", "enable_freeform_support", "1")
        no("settings", "put", "global", "enable_freeform_support")
        no("settings", "delete", "global", "enable_freeform_support")
    }

    @Test
    fun only_the_two_known_grants_are_allowed() {
        ok("pm", "grant", "app.sukios", "android.permission.WRITE_SECURE_SETTINGS")
        no("pm", "grant", "app.sukios", "android.permission.READ_SMS")
        no("pm", "grant", "app.sukios;id", "android.permission.WRITE_SECURE_SETTINGS")
        no("pm", "revoke", "app.sukios", "android.permission.WRITE_SECURE_SETTINGS")
        ok("appops", "set", "app.sukios", "SYSTEM_ALERT_WINDOW", "allow")
        no("appops", "set", "app.sukios", "SYSTEM_ALERT_WINDOW", "ignore")
        no("appops", "set", "app.sukios", "CAMERA", "allow")
    }

    @Test
    fun launcher_role_and_home_activity_need_valid_names() {
        ok("cmd", "role", "add-role-holder", "android.app.role.HOME", "app.sukios")
        ok("cmd", "package", "set-home-activity", "app.sukios/.SukiHomeActivity")
        no("cmd", "role", "add-role-holder", "android.app.role.SMS", "app.sukios")
        no("cmd", "role", "add-role-holder", "android.app.role.HOME", "app.sukios;id")
        no("cmd", "package", "set-home-activity", "-h")
        no("cmd", "package", "uninstall", "app.sukios")
    }

    @Test
    fun freeform_start_only_accepts_mode_five_and_a_valid_component() {
        ok("am", "start", "--windowingMode", "5", "-n", "com.android.chrome/com.google.android.apps.chrome.Main")
        no("am", "start", "--windowingMode", "3", "-n", "com.a/.B")
        no("am", "start", "--windowingMode", "5", "-n", "com.a/.B;id")
        no("am", "start", "--windowingMode", "5", "-n", "com.a/.B", "--es", "x", "y")
        no("am", "start", "-n", "com.a/.B")
        no("am", "broadcast", "-a", "x")
    }

    @Test
    fun task_resize_takes_four_separate_non_negative_integers() {
        ok("am", "task", "resize", "42", "100", "60", "1500", "900")
        ok("am", "task", "resize", "42", "0", "0", "1", "1")
        no("am", "task", "resize", "42", "100,60,1500,900")
        no("am", "task", "resize", "0", "100", "60", "1500", "900")
        no("am", "task", "resize", "42", "-1", "60", "1500", "900")
        no("am", "task", "resize", "42", "100", "60", "1500", "x")
        no("am", "task", "resize", "42", "100", "60", "99999", "900")
        no("am", "task", "resize", "4 2", "100", "60", "1500", "900")
        no("am", "task", "resize", "+42", "100", "60", "1500", "900")
        no("am", "task", "lock", "42")
        no("am", "set-task-windowing-mode", "--toTop", "42", "5")
    }

    @Test
    fun read_only_helpers_and_input_keys_are_exact() {
        ok("dumpsys", "activity", "activities")
        ok("dumpsys", "display")
        ok("wm", "size")
        ok("id")
        ok("getprop", "ro.product.model")
        ok("input", "keyevent", "4")
        ok("input", "keyevent", "3")
        ok("am", "force-stop", "com.example.app")
        no("dumpsys", "activity", "service")
        no("dumpsys")
        no("input", "keyevent", "26")
        no("input", "text", "hi")
        no("getprop", "-h")
        no("id", "-a")
        no("rm", "-rf", "/")
        no("sh", "-c", "id")
        no("")
    }

    @Test
    fun an_empty_argv_is_never_allowed() {
        assertFalse(ShellPolicy.allows(emptyList()))
    }
}
