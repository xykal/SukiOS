# PROGRESS — SukiOS

Start: 2026-10-01
Zona waktu: **UTC**. Hari 1 = **2026-10-01**.
Log ini hanya mencatat pekerjaan yang benar-benar selesai. Label verifikasi mengikuti aturan di `AGENTS.md`.

---

## 2026-10-01 — hari kerja ke-1

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

## 2026-10-02 — hari kerja ke-2

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

### 2026-10-02 (Hari 2, sesi audit) — audit CONTINUE, perbaikan CRIT/HIGH, v0.3.1-alpha

Permintaan kall: lanjutkan proyek. Mode CONTINUE: audit dulu (`docs/AUDIT-2026-10-02.md`: 3 CRIT, 9 HIGH, 11 MED, 4 LOW), lalu CRIT, lalu HIGH.
MED dan LOW tetap usulan di berkas audit sampai kall bilang "gas".

**Akar masalah.** v0.3.0 lulus kompilasi tetapi UI tidak bisa menggambar ulang: `Win` berisi field biasa di `mutableStateListOf`, `StateFlow.value` dibaca
tanpa `collectAsState` (0 pemakaian), dan perintah SukiShell (binder + proses) jalan di thread utama tanpa batas waktu. Tidak ketahuan karena belum pernah diuji di perangkat.

**Done**
- Win jadi state Compose; `collectAsState` di semua composable; `SukiShell.io` (IO) untuk semua pemanggil; `ShellExec` (tanpa shell parsing, timeout 15 dtk, batas 64 KiB per aliran); `ShellArgs` (allowlist).
- Mesin jendela: tak lagi crash saat area kerja < minimum, snap mengingat ukuran bebas, taskbar memakai kepadatan sebenarnya, ukuran minimum dalam dp, `resizeEdge`.
- Laporan diagnostik jujur: RAM total, UTC sungguhan, fitur freeform dari PackageManager.
- About: lisensi Shizuku-API yang benar (MIT, bukan Apache-2.0) + atribusi `Brand.kt`; layar persiapan tidak lagi menyapa pengguna akhir "kall".
- 44 uji unit JVM (WinEngine 19, ShellExec 12, ShellArgs 5, PureHelpers 4, SourceHygiene 4) menjadi gerbang `Build APK` dan `Release APK`.
- Workflow: 7 action dipin SHA, tag rilis divalidasi lewat `env:` + regex, `tools/check_workflows.py` (W1-W4 + `--self-test`).
- Dokumen baru: `SECURITY.md`, `THIRD_PARTY_NOTICES.md` (DRAFT), `IDEAS.md`, README dua bahasa, `docs/release-notes/v0.3.1-alpha.md`. Typo `xyikal` -> `xykal` di panduan signing.
- Private vulnerability reporting dinyalakan lewat API (`PUT` = 204, `GET` = `enabled: true`).

**Rekaman CI**

| Objek | ID / commit | Hasil |
|---|---|---|
| Build APK #1 | run 37031748026 · `bc1c5ab` | **success**: workflow-check lulus, uji unit **44 dijalankan, 0 gagal, 0 error, 0 dilewati**, build debug + release. Bukti diambil dari log sebelum run dihapus pembersih |
| Build APK #2 | run 37032705523 · `ff247b9` | **success** (semua langkah, termasuk uji unit). Jumlah uji tidak terekam: pembersih menghapus run sebelum log sempat dibaca |
| Release APK | run 37033191915 · tag `v0.3.1-alpha` | **success**: tag tervalidasi, uji unit, build release, draft release dibuat |
| Pembersihan | 37032409746, 37033108222, 37033316673 | sukses; yang terakhir `purge_mode=all`: **14 entri / 1086,0 MB -> 0**, run rilis dihapus (GET = 404) |

URL run: `https://github.com/xykal/SukiOS/actions/runs/<ID>`; ketiganya sudah dihapus pembersih sesuai kebijakan, jadi tidak bisa dibuka lagi.
Jalur purge-saat-rilis kini **terbukti end-to-end lewat tag** (sebelumnya baru sampai tahap wiring).

**Artefak:** draft release `SukiOS v0.3.1-alpha`, `SukiOS-v0.3.1-alpha.apk` 1.872.719 byte, debug-signed (0 secrets keystore).

**Belum diverifikasi**
- Perilaku di perangkat nyata. Uji unit membuktikan logika di JVM dan bahwa tiap properti jendela tercatat sebagai state Compose; bukan tampilan di layar.
- Dua perbaikan yang hanya bisa dibuktikan di perangkat: titik indikator Shizuku berubah warna saat status berubah, dan `SukiShell.io` tidak menahan layar pada perintah lambat.

**Pelajaran**
- SHA action harus dari `GET /repos/<owner>/<repo>/commits/<tag>`. `git/ref/tags/<tag>` mengembalikan SHA tag-object untuk tag beranotasi: `gradle/actions@v4` terbaca `48b5f213...`, padahal commit-nya `ed408507...`. Hampir terpin salah.
- `actions/cache/usage` tertinggal sekitar 5 menit; pakai `actions/caches` untuk keadaan sebenarnya (usage menunjukkan 8 entri / 664 MB padahal daftar nyata 0).
- Jendela pengambilan bukti sebelum pembersih menghapus run itu sekitar setengah menit: satu perintah yang polling tiap 3 detik, bukan beberapa panggilan terpisah.
- `Float.coerceIn(min, max)` melempar saat min > max. Uji acak 4000 langkah dengan benih tetap kini menjaga seluruh mesin jendela dari kelas cacat ini.

### 2026-10-02 (Hari 2, sesi ketiga) — dua implementasi paralel; main diperbaiki sampai hijau

**Temuan.** Sesi ini mengerjakan permintaan yang sama (Aurora kembali, jendela mutlak, kunci mendatar)
secara lokal dari basis `6eb8a11`, sementara `origin/main` sudah berisi lima commit sesi lain
(`db65cec`..`af190c1`, 17:22-17:41 UTC) yang mengerjakan hal yang sama dan lebih luas:
52 berkas utama, 12 berkas uji (91 uji), font Inter + Plus Jakarta Sans (OFL), generator ikon
`tools/gen_glyphs.py`, `ProbeActivity` untuk uji jendela nyata, `SetupPlan` dengan kunci berjenjang SDK.
Implementasi di `main` dipakai sebagai kebenaran; hasil kerja lokal sesi ini **tidak** digabung
(tabrakan nama di SukiKit/SukiWindows/SukiTaskbar/ShellArgs/manifest/build.gradle, kelas rangkap,
dua sumber nilai warna) dan disimpan sebagai `docs/alt-aurora-5946dbd.bundle` supaya bisa dipetik
bila diperlukan.

**Keadaan CI saat ditemukan.** Build terakhir `main` **merah** dan tidak pernah diperbaiki:
run 37042179977 (`ef1a648`, 17:39 UTC) — 91 uji, 2 gagal. Push terakhir `af190c1` (17:41 UTC)
tidak pernah melewati CI sama sekali.

| Uji yang gagal | Sebab | Tindakan |
|---|---|---|
| `SourceHygieneTest:27` kata 'kall' di sumber | Sudah diperbaiki `af190c1` (KDoc `FreeformParse`), tetapi commit itu tidak pernah dibuild | Dibuktikan oleh build di bawah |
| `WinEngineTest:217` assertNull(middle.snap) | Lebar bawaan jendela internal 66% area kerja: di layar 2400 px / kepadatan 2,75 sisa ruang tiap sisi ~30 dp, lebih sempit dari tepi snap 24 dp, jadi posisi mana pun selalu menempel | `7fa0702`: kasus "di tengah" memakai jendela 360 dp yang memang muat di tengah |

**Catatan verifikasi**

| Waktu (UTC) | Hasil | Bukti |
|---|---|---|
| 2026-10-02 20:17 | `Build APK` run **37059399904** (commit `7fa0702`) — **success** | `CI-VERIFIED 37059399904`; kesimpulan dibaca langsung dari API saat polling. Run dihapus pembersih (run `Cleanup CI traces` 37059574454, 20:17:04) sebelum log sempat ditarik, jadi jumlah uji build ini tidak terekam |
| 2026-10-02 20:22 | `Build APK` run **37059892157** (commit `4ad1062`, dokumen saja) — **success** | `CI-VERIFIED 37059892157`; URL job terekam di `ci-evidence/jobs.txt` sebelum run dihapus. Log gagal ditarik (404) |
| 2026-10-02 20:26 | `Build APK` run **37060453979** (commit `eb14c46`, dokumen + bundel) — **success** | `CI-VERIFIED 37060453979`; **log tersimpan** di `ci-evidence/run37060453979.log`: `uji unit: 91 dijalankan, 0 gagal, 0 error, 0 dilewati`; self-test workflow lulus (5 contoh buruk ditolak); `assembleDebug` + `assembleRelease` BUILD SUCCESSFUL; APK debug 22 MB, release 2,2 MB |
| 2026-10-02 20:40 | `Build APK` run **37061983778** (commit `c1ef801`, dokumen saja) — **success** | `CI-VERIFIED 37061983778`; log di `ci-evidence/run37061983778.log`: `uji unit: 91 dijalankan, 0 gagal`; `3 workflow bersih (W1-W4)` setelah nama langkah uji diubah; debug + release BUILD SUCCESSFUL |
| 2026-10-02 20:42 | `Build APK` run **37062204334** (commit `e91f695`) — **success** | `CI-VERIFIED 37062204334`; log di `ci-evidence/run37062204334.log`. Commit inilah yang di-tag `v0.4.0-alpha` |
| 2026-10-02 20:51 | `Build APK` run **37062663145** (commit `0f93217`, dokumen saja) — **success** | `CI-VERIFIED 37062663145`; log di `ci-evidence/run37062663145.log`: `uji unit: 91 dijalankan, 0 gagal, 0 error, 0 dilewati`. Build ini lambat (5,5 menit) karena cache Gradle baru dihapus oleh purge pasca-rilis — sesuai kebijakan `AGENTS.md` |
| 2026-10-02 17:39 | run 37042179977 (`ef1a648`) — **failure**, 91 uji / 2 gagal | Run gagal tidak dihapus otomatis; log lengkap tersimpan di `ci-evidence/run37042179977-unit.log` |

**Rilis**

| Waktu (UTC) | Hasil | Bukti |
|---|---|---|
| 2026-10-02 20:44 | `Release APK (signed, draft)` run **37062363916** (tag `v0.4.0-alpha` -> commit `e91f695`) — **success** | `CI-VERIFIED 37062363916`; log di `ci-evidence/rel37062363916.log`: 11 langkah success termasuk "Uji unit sebelum rilis" dan "Buat draft release"; peringatan workflow `Secrets keystore belum ada; APK akan memakai debug key` |

**Artefak:** draft release `SukiOS v0.4.0-alpha`, `SukiOS-v0.4.0-alpha.apk` **2.279.003 byte**,
debug-signed (0 secrets keystore), catatan rilis diambil dari `docs/release-notes/v0.4.0-alpha.md`
+ kaki otomatis (commit `e91f695`, versionName `0.4.0`, peringatan debug key, atribusi merek).

Perbaikan kecil ikut di commit ini: judul `# SukiOS v0.4.0-alpha` dihapus dari berkas catatan rilis
karena workflow sudah menulis `## SukiOS <tag>` sendiri (rilis kali ini punya dua judul).

**Artefak baru
- `docs/release-notes/v0.4.0-alpha.md` — ditulis dari kode yang benar-benar ada di `main`, bukan dari rencana:
  Aurora v2 (6 aksen dengan turunan `fill`/`on`/`text`, 6 wallpaper, font OFL, ikon hasil generator),
  jendela mutlak (satu jalur `am start --windowingMode 5`, koreksi kotak hanya bila tidak masuk akal,
  enam kesimpulan `OutcomeKind`, potongan dump mentah bisa disalin), penyiapan otomatis
  (`SetupPlan` idempoten + verifikasi lewat API Android + `ProbeActivity`), kunci `sensorLandscape`,
  `minSdk` 29, enam pemeriksaan untuk uji perangkat, dan batas yang diketahui.

**Pelajaran**
- Ambil log job **di putaran polling yang sama** dengan terbacanya `completed`, dan langsung lewat
  `/actions/jobs/<id>/logs` dengan `curl -L`. Jendela bukti kali ini lebih sempit dari setengah menit:
  dua run sukses terhapus sebelum lognya sempat ditarik.
- Naikkan versi **setelah** build hijau (aturan `AGENTS.md`). `versionName 0.4.0` sudah dinaikkan di
  commit yang build-nya merah; tag baru boleh didorong setelah build hijau, yaitu sekarang.
- Sebelum menulis kode, `git fetch` dulu. Satu permintaan yang dikerjakan dua sesi menghasilkan dua
  implementasi penuh; yang satu harus dibuang.

### 2026-10-02 (Hari 2, sesi ketiga, lanjutan) — dokumen disinkronkan dengan kode `main`

Kode Aurora v2 dan jendela mutlak sudah ada di `main` sejak `db65cec`, tetapi empat dokumen masih
menjelaskan versi matte v1.1. Disamakan dengan apa yang benar-benar ada di kode (bukan rencana):

| Berkas | Perubahan |
|---|---|
| `DESIGN.md` | v1.1 -> **v2.0 Aurora**: §1.3 aturan matte dicabut + aturan jujur (tanpa klaim blur), §1.4 catatan implementasi (token ARGB, ikon hasil generator, font dipaket), §4.1 palet nyata dari `SukiTheme.kt` + konstanta + rasio, §4.2 enam preset aksen dengan `fill`/`on`/`text`, §4.3 material yang benar-benar dipakai (`Glass`, `Panel`, bayangan 30/12 dp, aurora veil), §5 font, §16.1 peta token -> berkas, §16.3 checklist baru, §17 cheat sheet angka nyata, §18 riwayat revisi. §10-14 ditandai **spesifikasi target**, bukan keadaan kode |
| `PRD.md` | 1.0 -> **1.4**: §16.6 c ditulis ulang (matte dicabut), catatan baru d (jendela mutlak) dan e (kunci mendatar), §7.19 `F-19` + §7.20 `F-20` dengan acceptance criteria yang dicentang sesuai kode dan dua butir yang jujur masih menunggu uji perangkat |
| `AGENTS.md` | struktur paket diperinci per mesin; aturan kode ditambah: warna hanya dari `SukiTheme.kt` + wajib daftar di `ThemeContrastTest`, ikon tidak disunting tangan, app luar hanya lewat `SukiWindowing` dengan enam `OutcomeKind`, penyiapan diverifikasi API Android bukan kode keluar, shell lewat `runChecked`, orientasi terkunci, batas berkas 300 baris |
| `README.md` | status jujur diperbarui (uji perangkat v0.3.1-alpha -> apa yang ditindaklanjuti, `main` hijau, tag 0.4.0 menunggu), mesin disebutkan benar, daftar uji CI diperinci |
| `.github/workflows/build.yml` | nama langkah uji diperbarui (hanya teks; `tools/check_workflows.py --self-test` dan pemeriksaan 3 workflow tetap lulus lokal) |

Belum disinkronkan (dicatat supaya tidak dianggap selesai): `mockup.html` masih prototipe matte,
`assets/logo.svg` belum diperiksa terhadap ikon launcher gradien, PRD §10-14/§7 lama masih berisi
angka target yang belum cocok dengan kode.

### Langkah berikutnya
1. **Uji di perangkat (kall):** pasang draft `v0.4.0-alpha` (2.279.003 byte, debug key), jalankan
   6 pemeriksaan di `docs/release-notes/v0.4.0-alpha.md`, kirim hasil "Salin laporan diagnostik".
   Yang paling menentukan: apakah aplikasi pihak ketiga benar-benar jadi jendela di perangkat itu,
   dan apa bunyi `OutcomeKind`-nya bila tidak.
2. **Keputusan kall:** lisensi; `gradle/actions` v6 (komponen cache proprietari) boleh/tidak;
   4 secrets keystore; pencabutan token GitHub (scope terlalu lebar, lihat audit "Risiko proses").
3. **[MED] Geometri snap.** Jendela internal bawaan 66% area kerja menyisakan ruang geser ~30 dp per sisi
   di layar 2400 px, hampir sama dengan tepi snap 24 dp. Usul: tepi snap ikut sisa ruang
   (`min(24dp, 20% ruang bebas)`), atau lebar bawaan dibatasi maksimum dp.
4. **[MED] `ProbeActivity` `exported="true"`.** Aplikasi lain bisa memicunya; isinya tidak berbahaya
   (melaporkan lalu menutup diri), tetapi `am start` dari uid 2000 bisa membuka komponen yang tidak
   diekspor. Usul: `exported="false"` lalu uji lagi. Masih menunggu uji runtime sebelum diubah.
5. **[MED] Pengecualian layar penuh per aplikasi.** Belum ada; `strictWindows` masih global.
   Usul: satu toggle di menu aplikasi + titik penanda di desktop.
6. **[LOW] Biaya runtime belum diukur:** polling `dumpsys` 3 detik + wallpaper bergerak.
7. **[LOW] Pemecahan berkas >300 baris** ditunda; `SourceHygieneTest` di `main` memakai batas 300.
8. MED/LOW lain di `docs/AUDIT-2026-10-02.md` tetap menunggu "gas".

## 2026-10-04 — audit follow-up: sinkronisasi dokumen status + lisensi APK

Permintaan: lanjut dari audit 2026-10-04 (“gasken”). Dikerjakan lokal tanpa push dan tanpa memakai token GitHub.

**Done**
- `docs/AUDIT-2026-10-04.md` ditambahkan sebagai audit status `322b579`.
- `README.md` disinkronkan ke keadaan sekarang: tag terbaru `v0.4.0-alpha`, bukti uji unit CI terakhir, draft release debug-signed, build baru belum diuji perangkat, dan `mockup.html` ditandai stale.
- `sukios/README.md` ditulis ulang supaya tidak lagi menyuruh install `v0.3.0-alpha`; status rilis, checklist perangkat, Aurora v2, SukiWindowing, SukiAuto, dan batas platform diperbarui.
- `THIRD_PARTY_NOTICES.md` disinkronkan dengan kondisi nyata: font Inter/Plus Jakarta Sans memang ikut APK, ikon SukiOS digambar sendiri, dan salinan lisensi APK dicatat.
- Asset lisensi APK ditambah:
  - `sukios/app/src/main/assets/licenses/MIT-Shizuku-API.txt`
  - `sukios/app/src/main/assets/licenses/Apache-2.0.txt`
- `mockup.html` diberi banner visible bahwa file itu mockup lama matte v1.1 dan bukan sumber visual Aurora v2.

**Verifikasi lokal**
- `python3 tools/check_workflows.py` — `LOCAL-VERIFIED`.
- `python3 tools/check_workflows.py --self-test` — `LOCAL-VERIFIED`.
- `python3 tools/gen_glyphs.py --check` — `LOCAL-VERIFIED`.
- Pemeriksaan teks: README modul tidak lagi menyebut instruksi install `v0.3.0-alpha`; asset lisensi MIT/Apache ada.

**Belum dilakukan**
- Build/unit test Gradle lokal: `BLOCKED` karena workspace ini tidak punya Gradle/Android SDK/JDK 17.
- Push/CI: belum dilakukan. Kalau patch ini dipush, commit dokumen+asset lisensi boleh memakai `[skip ci]` kecuali ingin bukti CI baru.
- Perangkat nyata: `UNVERIFIED` untuk `v0.4.0-alpha` sampai laporan diagnostik dikirim.

## 2026-10-04 — cleanup kecil: bounds null dibuat eksplisit

Permintaan lanjutan: “bebas”. Dipilih perubahan paling aman dari audit: hapus cabang mati tanpa mengubah perilaku runtime yang dimaksud.

**Done**
- `SukiWindowing.openLocked` tidak lagi punya cabang `WindowBounds.initial(...)` saat `task.bounds == null`, karena `WindowBounds.needsFix(null, ...)` memang mengembalikan `false` dan test mengunci aturan itu.
- Aturan kini eksplisit: **bounds null = jangan menebak kotak jendela**; SukiOS hanya mengirim `am task resize` bila dump memberi bounds valid yang memang perlu dikoreksi.
- `docs/AUDIT-2026-10-04.md` diperbarui: L-02 menjadi `LOCAL-FIXED`.

**Verifikasi lokal**
- `python3 tools/check_workflows.py` — `LOCAL-VERIFIED`.
- `python3 tools/check_workflows.py --self-test` — `LOCAL-VERIFIED`.
- `python3 tools/gen_glyphs.py --check` — `LOCAL-VERIFIED`.
- Pemeriksaan XML manifest via Python — `LOCAL-VERIFIED` (Home+Probe masih exported sesuai kode saat ini; perubahan exported Probe sengaja belum dilakukan karena butuh uji runtime).

**Belum dilakukan**
- Build/unit test Gradle lokal: `BLOCKED` karena workspace ini tidak punya Gradle/Android SDK/JDK 17.
- Push/CI: belum dilakukan.

## 2026-10-04 — live wallpaper sistem untuk desktop SukiOS

Permintaan: “Tambah juga agar bisa pasang live wallpaper”. Audit singkat dilakukan dulu di `docs/AUDIT-2026-10-04.md`, lalu implementasi lokal.

**Done**
- Mode wallpaper baru `WALLPAPER_SYSTEM_LIVE = "system-live"` ditambahkan. Id ini sengaja bukan bagian dari `WALL_SPECS` agar tidak dianggap preset Aurora biasa.
- `SukiHomeActivity` menyalakan `FLAG_SHOW_WALLPAPER`; theme `SukiTheme` memakai `windowShowWallpaper=true` dan background window transparan.
- `SukiHome` membuat root background transparan saat mode live wallpaper aktif.
- `Wallpaper()` menampilkan `SystemWallpaperVeil`: lapisan gelap/transparan + cahaya Aurora tipis di atas wallpaper sistem agar teks/taskbar tetap terbaca.
- `SukiWallpaperPicker.kt` membuka pemilih live wallpaper Android (`ACTION_LIVE_WALLPAPER_CHOOSER`) dengan fallback ke picker wallpaper umum.
- Setelan > Tampilan mendapat kartu **Live wallpaper sistem**: tombol `Pakai` dan `Pilih live wallpaper`.
- `sukios/README.md` diperbarui; `PureHelpersTest` ditambah untuk menjaga id live wallpaper tidak bentrok dengan preset Aurora.

**Verifikasi lokal**
- `python3 tools/check_workflows.py` — `LOCAL-VERIFIED`.
- `python3 tools/check_workflows.py --self-test` — `LOCAL-VERIFIED`.
- `python3 tools/gen_glyphs.py --check` — `LOCAL-VERIFIED`.
- Hitung baris: semua sumber Kotlin tulisan tangan tetap <300 baris (`SukiSettings.kt` 286).

**Belum dilakukan**
- Build/unit test Gradle lokal: `BLOCKED` karena workspace ini tidak punya Gradle/Android SDK/JDK 17.
- Uji perangkat: `UNVERIFIED`. Yang perlu dicek: live wallpaper benar-benar terlihat di belakang desktop pada ROM target, dan picker Android muncul dari tombol Setelan.

### 2026-10-04 — CI setelah live wallpaper

Perubahan live wallpaper + sinkronisasi dokumen dipush ke `main`.

| Waktu (UTC) | Hasil | Bukti |
|---|---|---|
| 2026-10-04 00:54 | `Build APK` run **37166215942** (commit `b4e5a48`) — **success** | `CI-VERIFIED 37166215942`; kesimpulan success terekam dari API saat polling. Log build gagal disalin karena run dihapus pembersih sebelum endpoint logs masih bisa diakses |
| 2026-10-04 00:54 | `Cleanup CI traces` run **37166399385** — **success** | log cleanup mencatat target run `37166215942`, commit `b4e5a48`, kesimpulan **success**, riwayat run dihapus (GET 404), `purge_mode=none`, artifact 0, cache 0 |

Catatan: karena ini build biasa, cache tidak dipurge sesuai kebijakan. Perilaku live wallpaper di perangkat nyata tetap `UNVERIFIED` sampai diuji dari Setelan > Tampilan > Live wallpaper sistem.
