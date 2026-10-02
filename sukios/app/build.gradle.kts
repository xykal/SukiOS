plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// ---------------------------------------------------------------------------
// Signing rilis
//
// Prioritas: environment SUKIOS_KEYSTORE_* (diisi dari GitHub Secrets saat CI).
// Kalau tidak lengkap, build jatuh ke debug key supaya tetap jalan — workflow
// rilis menandai APK seperti itu sebagai TIDAK resmi.
// Nilai rahasia tidak pernah ditulis ke file ini, hanya dibaca dari env.
// ---------------------------------------------------------------------------
val ksFile = System.getenv("SUKIOS_KEYSTORE_FILE")
val ksStorePass = System.getenv("SUKIOS_KEYSTORE_PASSWORD")
val ksAlias = System.getenv("SUKIOS_KEY_ALIAS")
val ksKeyPass = System.getenv("SUKIOS_KEY_PASSWORD")
val hasReleaseSigning = listOf(ksFile, ksStorePass, ksAlias, ksKeyPass).all { !it.isNullOrBlank() }

android {
    namespace = "app.sukios"
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
        applicationId = "app.sukios"
        minSdk = 29          // Android 10 — target minimum SukiOS (PRD 8)
        targetSdk = 35
        versionCode = 3
        versionName = "0.3.0"
        resourceConfigurations += listOf("in", "en")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            // R8 menyala: AIDL Stub dan kelas UserService wajib disimpan,
            // aturannya ada di proguard-rules.pro.
            isMinifyEnabled = true
            isShrinkResources = true
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
        // AIDL menyala karena SukiShell memakai kontrak sendiri (ISukiShell),
        // bukan API yang sudah ditinggalkan.
        aidl = true
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
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime")
    // material3 dipakai HANYA untuk komposisi teks (Text). Tidak memakai
    // tema Material: seluruh warna dan bentuk berasal dari SukiKit.
    implementation("androidx.compose.material3:material3")

    // Shizuku 13.1.5 — dipakai sebagai KURIR binder saja.
    // Semua eksekusi berjalan di UserService milik SukiOS (ISukiShell),
    // karena Shizuku#newProcess sudah dihapus sejak 13.x.
    // Dipastikan lewat pemeriksaan bytecode AAR: newProcess = 0 hasil,
    // bindUserService + UserServiceArgs tersedia penuh.
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
}
