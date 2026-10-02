# PROGRESS — SukiOS

Zona waktu: **UTC**. Hari 1 = **2026-10-01**.
Log ini hanya mencatat pekerjaan yang benar-benar selesai. Label verifikasi mengikuti aturan di `AGENTS.md`.

---

## 2026-10-01 (Hari 1)

### Fase 0 — Discovery & Design: selesai
- `PRD.md` v1.1 — 27 fitur berprioritas, user story + acceptance criteria, peta permission Android, roadmap 3 fase, analisis risiko. Termasuk **Addendum §16** berisi temuan riset window engine (pembatasan platform untuk embed app pihak ketiga).
- `DESIGN.md` — design system Suki Glass v1.0.
- `mockup.html` — prototipe interaktif (drag/resize/snap jendela, start menu, quick settings, settings yang benar-benar mengubah tampilan).
- `assets/` — logo SVG + set ikon bawaan.

### PoC window engine: kompilasi terverifikasi, pengujian perangkat belum
- 9 file Kotlin di `poc/` — window manager, 5 lane uji, capability probe, laporan teks.
- Kompilasi (debug + release) berhasil di CI. Pengujian di perangkat nyata **belum** dilakukan.

### Infrastruktur repo: aktif
- Repo publik `xykal/SukiOS`.
- Workflow: `Build APK`, `Release APK (signed, draft)`, `Cleanup CI traces`.
- Script: `tools/gen-keystore.sh`, `tools/cleanup_ci.py`, panduan `docs/RELEASE-SIGNING.md`.

### Catatan verifikasi

| Waktu (UTC) | Hasil | Bukti |
|---|---|---|
| 2026-10-01 23:34 | `Build APK` run **36941499802** (commit `23cbd3a`, branch `main`) — kesimpulan **success** | `CI-VERIFIED`; riwayat run + artifact dihapus otomatis setelah rekaman diambil, URL run tidak bisa dibuka lagi |
| 2026-10-01 23:41 | `Release APK (signed, draft)` run **36942422305** — draft release `v0.1.0-poc` dibuat | APK `SukiOS-PoC-v0.1.0-poc.apk` (~14 MB) terlampir di draft release; riwayat run sudah dibersihkan |

Catatan penting:
- APK pada rilis ini **debug-signed** karena 4 secrets keystore rilis belum diset. Bukan untuk distribusi publik. Lihat `docs/RELEASE-SIGNING.md`.
- Artifact build tidak disimpan (workflow build tidak mengunggah artifact) — jalur distribusi APK adalah lewat draft release.
- Cache Gradle **tidak** dihapus (sesuai kebijakan) agar build berikutnya tetap cepat.

### 2026-10-02 — kebijakan cleanup diperluas ke cache

Kall meminta jejak CI ikut dibersihkan sampai cache setelah setiap rilis. Perubahan diterapkan:

- `cleanup.yml`: `purge_mode=all` saat dipicu `Release APK`; `none` saat `Build APK` supaya build tetap cepat.
- `cleanup_ci.py`: mode cache `none|all` + verifikasi sisa entri setelah penghapusan.

| Waktu (UTC) | Hasil | Bukti |
|---|---|---|
| 2026-10-02 08:57 | `Build APK` run **36987219073** (commit `3e6f3c0`) — **success** | `CI-VERIFIED`; riwayat run dihapus otomatis. Mode cache saat itu `none` (benar untuk build) |
| 2026-10-02 09:07 | `Cleanup CI traces` run **36987929145** (dispatch manual, `purge_caches=true`) — **success** | cache 12 entri / 913,8 MB → **0 entri** ("verifikasi: sisa entri cache = 0"); riwayat run dipertahankan karena `keep_run_record=true` |

Catatan:
- Jalur purge khusus-rilis baru terverifikasi sampai tahap wiring (nama workflow dirujuk persis sama) dan tahap eksekusi purge (lewat dispatch manual). Uji end-to-end lewat tag rilis akan terjadi pada rilis berikutnya.
- Menghapus cache berarti build berikutnya mengunduh ulang Gradle + dependensi (sekitar 5-8 menit pada repo ini).

### Keadaan akhir repo (2026-10-02)
- cache: **0 entri** · artifact: **0** · run Actions tersisa: **2** (keduanya run pembersih, dipangkas otomatis ke maksimal 2)
- draft release `v0.1.0-poc` + APK (~14 MB): **utuh** (release bukan objek cleanup)
- tag `v0.1.0-poc`: ada

### 2026-10-02 (Hari 2) — mode desktop, engine Shizuku, aturan visual matte

Permintaan kall: kunci landscape + desktop penuh, engine pembuka izin untuk Android Go, dan memastikan tidak ada nuansa neon/cyber.

**Kode PoC v0.2.0-poc**
- Mode tampilan: `lockLandscape` (SENSOR_LANDSCAPE) dan `fullDesktop` (WindowInsetsController), aktif default, bisa diubah dari taskbar dan Monitor.
- `ShizukuEngine.kt` baru: status, izin, eksekusi shell (uid 2000) dengan jalur cadangan reflection.
- Jendela **Akses Lanjutan** (LANE 6): uji identitas shell, force-resizable, izin overlay via appops, peluncuran app ke display, diagnostik display.
- Mode Go: `isLowRamDevice` lalu batas 3 jendela + efek dekoratif dimatikan.
- Palet diturunkan saturasinya; emoji dihapus dari antarmuka; batas jendela dihormati di `SukiState.open()`.

**Keputusan versi terbukti dari bytecode (bukan asumsi)**
- `dev.rikka.shizuku:api:13.1.5` — `newProcess` **tidak ada** (javap pada AAR).
- `dev.rikka.shizuku:api:12.2.0` — `newProcess` ada, bersama API izin modern lalu dipakai.
- Provider wajib: `rikka.shizuku.ShizukuProvider` + authority `${applicationId}.shizuku`.

**Dokumen & aset**
- `DESIGN.md` v1.1: §1.3 aturan wajib anti-neon, palet matte, "Suki Glow" diganti "Suki Shadow", preset aksen baru.
- `PRD.md` v1.2: F-14 (mode desktop), F-15 (akses lanjutan), F-16 (mode Go), 3 baris baru di tabel §8, addendum §16.6.
- `mockup.html`: palet matte, wallpaper rata, emoji diganti badge huruf. Uji jsdom: 8 jendela, 8 badge, 0 error.
- `assets/logo.svg`: gradien dihapus, aksen solid.

**Verifikasi**
- Uji statis Kotlin: saldo kurung, import API baru, cakupan `WinKind`, arity pemanggilan, referensi usang — bersih.
- Uji mockup (jsdom): semua interaksi inti jalan, tanpa error.
- CI build 1 (run 36988978988, commit 7952258): **gagal** — 3 error di `WindowChrome.kt` (argumen `drawLine` salah urutan). Seluruh kode Shizuku, LANE 6, dan mode desktop lolos kompilasi.
- Perbaikan commit `e2c300b`; CI build 2 (run 36989176265): **sukses**. Riwayat run dihapus otomatis oleh pembersih, kesimpulan tercatat di log pembersih run 36989402222.
- Rilis `v0.2.0-poc` (draft, debug-signed): dibuat setelah build sukses.

**Pelajaran**
- Verifikasi lewat CI menemukan bug nyata yang tidak tertangkap uji statis (kesalahan urutan argumen pada API Compose). Karena itu klaim "berfungsi" selalu menunggu CI.

### 2026-10-02 (Hari 2) — rekaman rilis v0.2.0-poc

| Objek | ID / Nilai | Hasil |
|---|---|---|
| Build APK (percobaan 1) | run 36988978988 · commit `7952258` | gagal kompilasi (3 error `drawLine`) — run dihapus setelah diagnosis |
| Perbaikan | commit `e2c300b` | argumen `drawLine(color, start, end)` |
| Build APK (percobaan 2) | run 36989176265 · commit `e2c300b` | **success** (kesimpulan tercatat di log pembersih run 36989402222) |
| Release APK | run 36991200698 · tag `v0.2.0-poc` | **success** |
| Build APK (commit dokumen) | run 36991199736 · commit `676ba4f` | success |
| Pembersihan | run 36991293763 dan 36991358091 | success, riwayat run dihapus |

**Keadaan repo setelah pembersihan:** 2 run pembersih (riwayat pembersih dipangkas otomatis ke 2), 0 artifact, cache 4 entri / 296 MB (ditinggalkan build terakhir yang selesai setelah rilis dipurge — sesuai desain build mempertahankan cache), 2 tag, 2 draft release berisi APK, 0 secret.

**Artefak:** `SukiOS-PoC-v0.2.0-poc.apk` 13,78 MB (14.113 KB), debug-signed karena secrets keystore rilis belum diset.

**Belum diuji di perangkat.** Kompilasi terbukti di CI; perilaku runtime (Shizuku, force-resizable, `am start --display`, mode desktop pada berbagai OEM) menunggu laporan dari perangkat nyata.

### 2026-10-02 (Hari 2, lanjutan) — v0.3.0-alpha: jadi launcher + mesin sendiri

Permintaan kall: UI/UX yang lebih bagus dipakai apa adanya, jadikan launcher OS sungguhan, tanam Shizuku langsung, dan bangun engine/lib sendiri.

**Arsitektur berubah**
- `poc/` menjadi `sukios/`; paket `app.sukios.poc` menjadi `app.sukios`. Aplikasi bukan lagi alat ukur: manifes memuat kategori HOME + DEFAULT + LAUNCHER.
- Mesin ditulis sendiri, bukan menambal API pihak lain:
  - **SukiShell** — kontrak AIDL sendiri (`ISukiShell`, `destroy() = 16777114`) + `SukiShellService` yang berjalan sebagai uid 2000. Shizuku 13.1.5 hanya kurir binder; `newProcess` tidak dipakai karena sudah dihapus di 13.x.
  - **SukiWin** — mesin jendela: geser, ukur ulang 8 arah, snap ke tepi, maximize, minimize, tumpukan fokus, area kerja dikurangi taskbar.
  - **SukiIndex** — indeks aplikasi terpasang + peluncuran biasa/berjendela/info/hapus.
  - **SukiKit** — sistem tampilan matte, ikon vektor (tanpa emoji).
  - **SukiDiag** — diagnostik perangkat + laporan siap dibagikan.
- UI: Beranda, taskbar, start menu, panel pintasan, jendela Setelan/Laboratorium/Terminal/Aplikasi/Tentang/Diagnostik, taskbar melayang, layar persiapan.

**Rekaman CI**

| Objek | ID / commit | Hasil |
|---|---|---|
| Build APK (percobaan 1) | run 37026575743 · `b0f04a0` | **gagal** — 134 error, akar: `TextStyle(color=...)` tidak ada lagi di Compose 1.7.4 |
| Perbaikan | commit `4bd59d4` | teks memakai `Text` material3; kolom ketik memakai `TextStyle.Default.copy()`; SukiKit ditulis ulang utuh |
| Build APK (percobaan 2) | run 37027127027 · `4bd59d4` | **sukses** |
| Release APK | run 37027612561 · tag `v0.3.0-alpha` | **sukses** — draft release + `SukiOS-v0.3.0-alpha.apk` 1,77 MB |
| Pembersihan | 37026830807, 37027582151, 37027781065 | sukses |

**Pemeriksaan isi APK (bukan asumsi)**
- Dex memuat: `SukiShellService`, `ISukiShell$Stub`, `ISukiShell$Stub$Proxy`, `ShizukuProvider`, `Shizuku`, `SukiHomeActivity`, `SukiOverlayBar`.
- Manifest memuat: `android.intent.category.HOME` (+DEFAULT+LAUNCHER), `SYSTEM_ALERT_WINDOW`, `QUERY_ALL_PACKAGES`, provider `rikka.shizuku.ShizukuProvider` dengan authority `app.sukios.shizuku`, izin `API_V23`, metadata `V3_SUPPORT`.
- APK turun dari 13,78 MB (PoC) ke 1,77 MB karena R8 + shrinkResources kini menyala (sebelumnya minify dimatikan).

**Pelajaran teknis**
- Menulis kode Compose tanpa kompilator di tangan berbahaya. Verifikasi bytecode AAR sebelum menulis kode adalah cara termurah menghindari satu putaran CI penuh.
- Dua suntingan paralel ke berkas yang sama bisa saling menimpa. Untuk berkas besar, tulis ulang utuh, jangan sunting paralel.

**Belum diverifikasi**
- Perilaku di perangkat: launcher, jendela, taskbar melayang, SukiShell, force-resizable. Semua menunggu laporan uji.

### Langkah berikutnya
1. Uji 5 lane di perangkat nyata, kirim laporan lewat tombol "Salin laporan" di app.
2. Set 4 secrets keystore supaya rilis berikutnya benar-benar signed (lihat `docs/RELEASE-SIGNING.md`).
3. Setelah hasil uji masuk: kunci arsitektur window (PRD §16.2) lalu mulai Fase 1.
