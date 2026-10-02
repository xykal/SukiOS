# IDEAS — SukiOS

Backlog ide. D = dampak, U = usaha (S kurang dari 1 hari, M 1-3 hari, L lebih dari 3 hari). Dipangkas berkala.
Sumber: audit, friksi alur pengguna, celah keamanan, hasil uji perangkat (belum ada).

| # | Ide | Kenapa penting bagi pengguna | D | U |
|---|---|---|---|---|
| 1 | Matriks uji perangkat: 5 perangkat, tiap laporan dari "Salin laporan" | gerbang keputusan PRD 16.5 butuh data nyata, bukan satu perangkat | tinggi | S |
| 2 | Task View (PRD F-07): grid semua jendela terbuka | milestone M6; cara tercepat berpindah jendela | tinggi | M |
| 3 | Pintasan keyboard: Alt+Tab = `cycle()`, Win+panah = `snap()` | pengguna mouse + keyboard; mesin sudah punya fungsinya | tinggi | S |
| 4 | Pulihkan tata letak jendela setelah proses dimatikan OEM (risiko R7) | jendela hilang saat sistem membunuh proses di latar | sedang | M |
| 5 | Kontras `SFaint` jadi `#8B9197` (4,54:1 terendah) dan target sentuh 44 dp | terbaca di bawah matahari, mudah ditekan | sedang | S |
| 6 | i18n: default Inggris, `values-in` Indonesia, migrasi bertahap | pasar global tanpa menyentuh logika | sedang | L |
| 7 | Layar "Lisensi sumber terbuka" di APK (teks Apache-2.0 dan MIT) | memenuhi syarat distribusi lisensi pihak ketiga | sedang | S |
| 8 | Pemeriksaan UID pemanggil di `SukiShellService` (konstruktor `Context`, perlu uji runtime) | lapisan kedua pada kemampuan paling berbahaya | sedang | M |
| 9 | Lint, detekt/ktlint, CodeQL, OSV, lockfile Gradle, SBOM CycloneDX di CI | menangkap kelas cacat yang baru ketahuan lewat audit manual | sedang | M |
| 10 | Dependabot mingguan dan dikelompokkan (Gradle + Actions) | BOM Compose sudah dua tahun tertinggal | sedang | S |
| 11 | ~~Pecah `SukiApps.kt` (543 baris) per jendela~~ **selesai 2026-10-02** (`db65cec`: jadi SukiSettings/SukiLab/SukiTerminal/SukiAppList/SukiAbout/SukiDiagView) | tiap jendela bisa dibaca dan diuji sendiri | rendah | M |
| 12 | GIF demo dari perangkat nyata untuk README | bukti paling meyakinkan bagi calon pengguna | tinggi | S (setelah uji) |
| 13 | Pembuat keystore sekali pakai lewat workflow, bila kall tanpa komputer | `gen-keystore.sh` butuh JDK lokal; kunci yang dibuat di CI tidak bisa dicadangkan | sedang | M |
| 14 | Pengecualian layar penuh per aplikasi + titik penanda di desktop | satu aplikasi rusak dalam jendela tidak boleh memaksa mode jendela dimatikan untuk semua | tinggi | S |
| 15 | Tepi snap ikut sisa ruang: `min(24 dp, 20% ruang bebas per sisi)` | jendela internal bawaan 66% area kerja hampir selalu menempel saat digeser di layar lebar | sedang | S |
| 16 | `ProbeActivity` jadi `exported="false"` | aplikasi lain tidak perlu bisa memicu jendela uji; `am start` uid 2000 tetap bisa membuka komponen tertutup | sedang | S |
