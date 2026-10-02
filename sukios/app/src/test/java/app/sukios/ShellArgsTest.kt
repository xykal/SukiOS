package app.sukios

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Muatan injeksi harus ditolak oleh allowlist; nilai yang sah harus lolos. */
class ShellArgsTest {

    private val injection = listOf(
        "", " ", "com.a b", "com.a;id", "com.a\nid", "com.a\n", "\$(id)", "`id`", "com.a&&id",
        "com.a|id", "com.a>/sdcard/x", "com.a'", "com.a\"", "-h", "--help", "-n", "../etc/passwd",
        "com.a/../b", "com.a\u0000", "com.\u0661", "com.", ".com", "com..a", "1com.a", "com.1a",
    )

    @Test
    fun package_names_reject_every_injection_payload() {
        injection.forEach { assertFalse("harus ditolak: [$it]", ShellArgs.isPackage(it)) }
    }

    @Test
    fun real_package_names_pass() {
        listOf("app.sukios", "com.android.chrome", "moe.shizuku.privileged.api", "org.example_app1.Main2", "android")
            .forEach { assertTrue(it, ShellArgs.isPackage(it)) }
    }

    @Test
    fun components_need_package_slash_class_and_nothing_else() {
        listOf("app.sukios/app.sukios.SukiHomeActivity", "com.foo/.Main", "com.foo/com.foo.Outer\$Inner")
            .forEach { assertTrue(it, ShellArgs.isComponent(it)) }
        listOf(
            "com.foo", "com.foo/", "com.foo/Main;id", "com.foo/Main\nid", "-n com.foo/Main", "com.foo/-x",
            "com.foo/Main space", "com.foo//Main", "com.foo/../x", "com.foo/..", "com.foo/Main\n",
        ).forEach { assertFalse("harus ditolak: [$it]", ShellArgs.isComponent(it)) }
    }

    @Test
    fun property_keys_cannot_start_with_a_dash() {
        listOf("ro.build.version.sdk", "persist.sys.timezone", "dalvik.vm.heapsize", "a-b.c")
            .forEach { assertTrue(it, ShellArgs.isProperty(it)) }
        listOf("", "-h", "-T", "ro.build;id", "ro build", "ro.build\n", "\$(id)", "ro.build.\u0661")
            .forEach { assertFalse("harus ditolak: [$it]", ShellArgs.isProperty(it)) }
    }

    @Test
    fun display_ids_are_small_non_negative_integers() {
        listOf(0, 5, 999_999).forEach { assertTrue("$it", ShellArgs.isDisplayId(it)) }
        listOf(-1, Int.MIN_VALUE, 1_000_000, Int.MAX_VALUE).forEach { assertFalse("$it", ShellArgs.isDisplayId(it)) }
    }
}
