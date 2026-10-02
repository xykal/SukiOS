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
- Kompilasi di CI: menunggu run berikutnya (dicatat setelah selesai).

### Langkah berikutnya
1. Uji 5 lane di perangkat nyata, kirim laporan lewat tombol "Salin laporan" di app.
2. Set 4 secrets keystore supaya rilis berikutnya benar-benar signed (lihat `docs/RELEASE-SIGNING.md`).
3. Setelah hasil uji masuk: kunci arsitektur window (PRD §16.2) lalu mulai Fase 1.
