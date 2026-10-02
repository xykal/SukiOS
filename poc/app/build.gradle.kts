plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// ---------------------------------------------------------------------------
// Signing rilis
//
// Prioritas: environment SUKIOS_KEYSTORE_* (diisi dari GitHub Secrets saat CI).
// Kalau tidak lengkap, PoC jatuh ke debug key supaya build tetap jalan —
// workflow rilis akan menandai APK seperti itu sebagai TIDAK resmi.
// Nilai rahasia tidak pernah ditulis ke file ini, hanya dibaca dari env.
// ---------------------------------------------------------------------------
val ksFile = System.getenv("SUKIOS_KEYSTORE_FILE")
val ksStorePass = System.getenv("SUKIOS_KEYSTORE_PASSWORD")
val ksAlias = System.getenv("SUKIOS_KEY_ALIAS")
val ksKeyPass = System.getenv("SUKIOS_KEY_PASSWORD")
val hasReleaseSigning = listOf(ksFile, ksStorePass, ksAlias, ksKeyPass).all { !it.isNullOrBlank() }

android {
    namespace = "app.sukios.poc"
    compileSdk = 35

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(ksFile!!)
                storePassword = ksStorePass
                keyAlias = ksAlias
                keyPassword = ksKeyPass
            }
        }
    }

    defaultConfig {
        applicationId = "app.sukios.poc"
        minSdk = 29          // Android 10 — sama dengan target minimum SukiOS (PRD §8)
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0-poc"
        resourceConfigurations += listOf("in", "en")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Compose BOM: menyamakan versi semua library Compose.
    // Kalau mau versi terbaru, lihat developer.android.com/develop/ui/compose/bom/bom-mapping
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime")

    // Shizuku — engine akses lanjutan (identitas shell/uid 2000).
    // Versi dipin ke 12.2.0 secara SENGAJA: API 13.x sudah menghapus newProcess
    // (penggantinya UserService). Keputusan ini didasarkan pada pemeriksaan
    // bytecode AAR, bukan asumsi. Lihat ShizukuEngine.kt untuk catatan lengkap.
    implementation("dev.rikka.shizuku:api:12.2.0")
    implementation("dev.rikka.shizuku:provider:12.2.0")
}
