// ============================================================================
// SukiOS PoC — root build file
//
// CATATAN VERSI (sengaja dipin, jangan asal naikkan):
// Kombinasi di bawah ini adalah kombinasi yang saling kompatibel & stabil.
// Kalau mau upgrade, cek dulu tabel kompatibilitas AGP <-> Gradle <-> Kotlin
// di developer.android.com/build/releases/gradle-plugin
// ============================================================================

plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
