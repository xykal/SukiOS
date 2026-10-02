package app.sukios

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupPlanTest {

    private val pkg = "app.sukios"
    private val home = "app.sukios/.SukiHomeActivity"

    @Test
    fun android_ten_gets_the_core_steps_and_no_android_12l_keys() {
        val ids = SetupPlan.build(29, pkg, home).map { it.id }
        assertEquals(listOf("freeform", "resizable", "secure", "overlay", "home"), ids)
    }

    @Test
    fun android_12l_and_up_add_the_two_multi_window_keys() {
        val ids = SetupPlan.build(32, pkg, home).map { it.id }
        assertTrue(ids.containsAll(listOf("nonresizable", "sizecompat")))
        assertFalse(SetupPlan.build(31, pkg, home).any { it.id == "nonresizable" })
        assertTrue(SetupPlan.build(35, pkg, home).any { it.id == "sizecompat" })
    }

    @Test
    fun only_the_two_core_window_settings_are_mandatory() {
        val mandatory = SetupPlan.build(35, pkg, home).filter { !it.optional }.map { it.key }
        assertEquals(SetupPlan.CORE_KEYS, mandatory)
    }

    @Test
    fun every_command_in_the_plan_is_allowed_by_the_shell_policy() {
        for (sdk in listOf(29, 30, 31, 32, 33, 34, 35)) {
            SetupPlan.build(sdk, pkg, home).forEach { step ->
                assertTrue("langkah ${step.id} tanpa perintah", step.commands.isNotEmpty())
                step.commands.forEach { assertTrue("ditolak kebijakan: $it", ShellPolicy.allows(it)) }
            }
        }
    }

    @Test
    fun setting_steps_target_known_keys_and_expect_one() {
        SetupPlan.build(35, pkg, home).filter { it.check == CheckKind.GLOBAL_SETTING }.forEach {
            assertTrue(it.key, ShellArgs.isSettingKey(it.key))
            assertEquals("1", it.expect)
        }
    }

    @Test
    fun hostile_package_or_component_names_yield_commands_the_policy_refuses() {
        val bad = SetupPlan.build(35, "app.sukios;id", "app.sukios;id/.A")
        val touched = bad.flatMap { it.commands }.filter { cmd -> cmd.any { it.contains(";") } }
        assertTrue(touched.isNotEmpty())
        touched.forEach { assertFalse("harus ditolak: $it", ShellPolicy.allows(it)) }
    }
}
