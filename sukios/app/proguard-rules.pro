# ============================================================================
# Aturan R8 untuk SukiOS
#
# Rilis memakai minify + shrinkResources. Yang WAJIB disimpan:
#  1. Stub AIDL milik SukiOS (ISukiShell) — dipanggil lintas proses lewat binder,
#     jadi tidak ada rujukan langsung yang bisa dilacak R8.
#  2. SukiShellService — kelas ini dimuat oleh proses shell atas permintaan
#     Shizuku, bukan dipanggil langsung dari aplikasi.
#  3. Kelas Shizuku yang dipakai: provider, binder wrapper, dan antarmuka
#     pendengar. Sebagian dipanggil lewat refleksi oleh Shizuku sendiri.
#  4. Kelas Parcelable dari Shizuku (BinderContainer) — lihat proguard.txt
#     bawaan artifact provider.
# ============================================================================

-dontwarn org.jetbrains.annotations.**

# --- 1 & 2. Kontrak SukiShell ---
-keep class app.sukios.shell.ISukiShell { *; }
-keep class app.sukios.shell.ISukiShell$* { *; }
-keep class app.sukios.SukiShellService { *; }
-keepclassmembers class app.sukios.SukiShellService {
    <init>();
}

# --- 3. Shizuku ---
-keep class rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }
-dontwarn rikka.shizuku.**
-dontwarn moe.shizuku.**

# --- 4. Parcelable Shizuku (salinan aturan resmi artifact provider) ---
-keepnames class moe.shizuku.api.BinderContainer
-keepclassmembers class moe.shizuku.api.BinderContainer {
   public static final android.os.Parcelable$Creator CREATOR;
}

# --- Nama kelas yang muncul di laporan diagnostik tetap terbaca ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
