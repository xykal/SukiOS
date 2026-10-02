# AGENTS.md — SukiOS

Panduan kerja untuk agen/developer yang melanjutkan repo ini. Baca ini sebelum mengubah apa pun.

## Identitas proyek
- **SukiOS** — desktop environment untuk Android (launcher + window manager). Bahasa desain: **Suki Glass**.
- Dokumen penting: `PRD.md` (§16 = temuan riset pembatasan platform), `DESIGN.md`, `mockup.html`, `sukios/` (aplikasi Android), `docs/AUDIT-*.md` (audit terbaru dulu).
- Aturan merek: **jangan** memakai nama, logo, ikon, suara, atau aset sistem operasi lain. Lihat PRD §4.2.

## Struktur
```
sukios/                    Gradle root project (modul :app)
  app/src/main/java/app/sukios/   kode aplikasi:
    SukiWin/SukiWinModel      mesin + model jendela internal
    SukiWindowing             jalur tunggal membuka app pihak ketiga sebagai jendela
    FreeformParse/WindowBounds  pengurai dumpsys + geometri kotak app luar (murni)
    SetupPlan/SukiAuto        rencana penyiapan Shizuku + pelaksana & verifikasinya
    ProbeActivity/WindowStatus  jendela uji milik sendiri + kalimat status jujur
    SukiShell/SukiShellCmds/ShellExec/ShellArgs  akses lanjutan + pagar perintah
    SukiIndex/SukiIconLoader  indeks aplikasi + penyeragaman ikon
    SukiTheme/SukiFonts/SukiGlyph(+Data)  token warna/ukuran/gerak, font, ikon vektor
    SukiKit/SukiKitLayout/SukiControls  komponen tampilan
    SukiHome/SukiDesktop/SukiWindows/SukiTaskbar/SukiStart/SukiQuick/SukiTray  lapisan UI
    SukiSettings/SukiLab/SukiTerminal/SukiAppList/SukiAbout/SukiDiag(+View)  isi jendela
  app/src/test/java/app/sukios/   uji unit JVM (gerbang CI)
.github/workflows/         build.yml · release.yml · cleanup.yml (semua action dipin SHA)
tools/                     check_workflows.py · cleanup_ci.py · gen-keystore.sh
docs/                      audit, panduan rilis & signing, release-notes/<tag>.md
PRD.md · DESIGN.md · mockup.html · PROGRESS.md · IDEAS.md · SECURITY.md · THIRD_PARTY_NOTICES.md
```

## Aturan wajib

1. **Rahasia tidak pernah masuk repo.** Kredensial disimpan di luar repo (folder `uploads/` pada workspace); di CI memakai GitHub Secrets. Periksa `git status` sebelum commit; `.gitignore` sudah menutup pola umum.
2. **Signing rilis** memakai 4 secrets: `SUKIOS_KEYSTORE_BASE64`, `SUKIOS_KEYSTORE_PASSWORD`, `SUKIOS_KEY_ALIAS`, `SUKIOS_KEY_PASSWORD`. Tanpa itu, workflow rilis jatuh ke debug key dan menandainya sebagai peringatan di catatan rilis.
3. **Kebijakan cleanup (milik kall):** setelah setiap build/release selesai, workflow `Cleanup CI traces` menghapus riwayat run + artifact yang dibuat task itu.
   - Setelah **rilis** (`Release APK`): jejak dibersihkan **termasuk cache** (`purge_mode=all`).
   - Setelah **build biasa** (`Build APK`): cache **dibiarkan** agar build berikutnya cepat. Mode bisa dipaksa manual lewat `purge_caches: true` saat dispatch.
   - **Release/deliverable tidak dihapus**, tag tidak dihapus, run yang gagal tidak dihapus (log error dibiarkan untuk diagnosis).
4. **Jangan hapus** release, tag, branch, atau riwayat run milik orang lain. **Jangan** bersih-bersih untuk menyembunyikan aktivitas.
5. **Gagal build tidak dibersihkan otomatis** — log error dibiarkan agar bisa didiagnosis dulu (naikkan `include_failed: true` bila memang ingin dibersihkan).

## Aturan kode (dibuktikan oleh uji; jangan dilonggarkan tanpa alasan tertulis)

1. **Setiap properti yang dibaca composable harus state Compose.** `mutableStateListOf` tidak memantau field elemennya; itu penyebab jendela tidak bergerak di v0.3.0 (`Win`).
2. **`StateFlow` di composable dibaca lewat `collectAsState()`**, bukan `.value`. `.value` hanya boleh di event handler dan kode non-UI.
3. **Pemanggilan SukiShell dari UI lewat `SukiShell.io { ... }`** (thread IO). Binder + proses tidak boleh jalan di thread utama. Argumen ke shell lewat `ShellArgs`, tanpa shell parsing.
4. **Merek hanya dari `Brand.kt`.** Teks `XyVerse Technology Global` tidak ditulis ulang atau diterjemahkan; `BrandTest` dan `SourceHygieneTest` menjaganya. Kata ganti pengembang tidak boleh muncul di teks produk.
5. **Workflow:** action dipin SHA penuh yang didapat dari `GET /repos/<owner>/<repo>/commits/<tag>` (bukan `git/ref`, yang mengembalikan SHA tag-object untuk tag beranotasi); input yang bisa dikendalikan pengirim masuk lewat `env:`.
   `python3 tools/check_workflows.py` (dan `--self-test`) harus lulus.
6. **Warna hanya dari `SukiTheme.kt`** sebagai `const val` ARGB (`Long`) supaya uji kontras bisa membacanya tanpa Compose. Jangan menulis `Color(0x..)` di berkas UI. Pasangan teks/latar baru wajib didaftarkan di `ThemeContrastTest` (AA 4,5:1; non-teks 3:1). Teks tidak boleh memakai warna aksen murni — pakai turunan `text` dari preset.
7. **Ikon tidak disunting tangan.** `SukiGlyphData.kt` dihasilkan `tools/gen_glyphs.py`; `GlyphDataTest` mencocokkan berkas dengan generator. Tambah ikon lewat generatornya. Font dipangkas lewat `tools/mkfonts.py`; lisensi OFL wajib ikut di `assets/licenses/`.
8. **Aplikasi pihak ketiga selalu dibuka lewat `SukiWindowing`** (PRD F-19/F-20): `am start --windowingMode 5` lewat SukiShell, lalu mode tugas **dibaca dari `dumpsys`** (`FreeformParse`). Hasilnya salah satu dari enam `OutcomeKind`; tidak boleh ada "berhasil" tanpa bukti. Jangan pernah mematikan aplikasi yang sudah berjalan tanpa persetujuan pengguna.
9. **Penyiapan otomatis idempoten dan terverifikasi**: `SetupPlan` hanya mengirim langkah yang belum terpenuhi, kunci berjenjang SDK, dan keberhasilannya diperiksa dengan API Android — bukan dengan kode keluar perintah. Klaim "jendela aktif" hanya boleh muncul bila `ProbeActivity` pernah melaporkan `isInMultiWindowMode` benar di perangkat itu.
10. **Perintah shell lewat `runChecked` (ShellPolicy)**, argv diperiksa `ShellArgs`; hanya Terminal yang boleh memanggil `SukiShell.run` langsung. Dijaga `ShellPolicyTest` + `SourceHygieneTest`.
11. **Orientasi terkunci mendatar** (manifest + `SukiHomeActivity`). Keputusan produk; jangan kembalikan jadi preferensi. `ManifestTest` menjaganya.
12. Berkas Kotlin di bawah 300 baris (batas di `SourceHygieneTest`; `SukiGlyphData.kt` dikecualikan karena dihasilkan). Yang terbesar sekarang `SukiWin.kt` 273 — pecah bila bertambah, jangan ditambah.

## Label verifikasi
`LOCAL-VERIFIED` · `CI-VERIFIED <run ID>` · `UNVERIFIED` · `BLOCKED <alasan>` — jangan pakai kata "verified" tanpa bukti yang sesuai.

## Alur kerja
- Ubah kode → push ke `main` → workflow `Build APK` memeriksa workflow, menjalankan uji unit, lalu memverifikasi kompilasi (tanpa menyimpan artifact).
- Rekam bukti dari log run SEBELUM `Cleanup CI traces` menghapusnya (jendela sekitar setengah menit setelah run selesai); URL run tidak bisa dibuka lagi sesudahnya.
- APK yang bisa dipasang: `git tag vX.Y.Z && git push origin vX.Y.Z` → **draft release** berisi APK. Unduh dari tab Releases.
- Catat pekerjaan di `PROGRESS.md` (UTC). Perubahan khusus dokumen boleh memakai penanda `[skip ci]`.

## Konvensi teknis
- Versi dipin: AGP 8.7.3 · Gradle 8.11.1 · Kotlin 2.0.21 · Compose BOM 2024.10.01 · compileSdk 35 · minSdk 29.
- Naikkan versi **setelah** ada build hijau, bukan sebaliknya. Satu perubahan versi = satu build.
- `minSdk 29` adalah keputusan produk (PRD §8), bukan nilai default yang boleh dinaikkan tanpa alasan.
