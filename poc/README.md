# SukiOS PoC — Window Engine & Capability Probe

Aplikasi Android kecil yang tugasnya menjawab **satu pertanyaan besar** sebelum kita menulis ribuan baris kode:

> **"App pihak ketiga (WhatsApp, Chrome, YouTube…) sebenernya bisa dijalankan di dalam jendela SukiOS di HP ini, atau nggak?"**

Jawabannya menentukan seluruh arsitektur SukiOS. Makanya kita ukur dulu, jangan menebak.

---

## 1. Cara Mendapatkan APK dari Repo Ini

Repo SukiOS memakai tiga workflow GitHub Actions:

| Workflow | Kapan jalan | Hasil |
|---|---|---|
| `Build APK` | setiap push ke `main` / pull request | verifikasi kompilasi (tanpa menyimpan artifact) |
| `Release APK (signed, draft)` | push tag `v*` atau manual | APK bertanda tangan, dilampirkan ke **draft release** |
| `Cleanup CI traces` | setelah build/release selesai | hapus riwayat run + artifact task itu |

Langkah mendapatkan APK:

```bash
git tag v0.1.0-poc
git push origin v0.1.0-poc
```

1. Buka tab **Releases** -> draft `SukiOS PoC v0.1.0-poc` -> unduh APK.
2. Kirim APK ke HP, lalu install (aktifkan "Install unknown apps" untuk app yang membuka file itu).
3. Workflow cleanup menghapus riwayat run + artifact setelah selesai. Draft release dan APK-nya tetap ada.

Kenapa APK tidak disimpan di artifact? Karena policy kerja: artifact CI dibersihkan setelah build selesai. Deliverable APK diletakkan di release, bukan di artifact.

Untuk mengembangkan di PC: buka folder `poc/` di Android Studio (versi terbaru); Studio akan menawarkan membuat Gradle wrapper.

## 2. Yang Diuji — 5 Jalur (Lane)

Buka app **SukiOS PoC** di HP. Kamu akan lihat "desktop" dengan ikon di kiri, taskbar di bawah, dan angka FPS di kanan taskbar.

| Lane | Yang diuji | Kenapa penting |
|---|---|---|
| **1** | **Window manager SukiOS** — geser, resize, snap | Ini yang pasti bisa. Kita ukur FPS-nya (target ≥ 55 fps) |
| **2** | **Freeform window** — app pihak ketiga jadi jendela kecil | Kalau ini jalan → SukiOS bisa jadi desktop beneran |
| **3** | **Split screen** — 2 app berdampingan | Multitasking paling andal di HP biasa |
| **4** | **Embed via ActivityView** — app lain di dalam jendela SukiOS | Jalur paling ambisius; riset kami bilang **kemungkinan besar ditolak platform** |
| **5** | **Overlay taskbar** — taskbar SukiOS mengapung di atas app fullscreen | Jalur fallback yang selalu bisa dipakai |

### Urutan pengujian yang gue sarankan

**Langkah 0 — Baca Monitor Perangkat**
Buka ikon **Monitor Perangkat** di desktop. Ini hasil probe otomatis. Screenshot saja halaman ini. Yang paling penting:
- **Feature FREEFORM** → kalau `tidak ada`, lane 2 kemungkinan besar gagal (normal, bukan berarti app-nya rusak).
- **RAM rendah** → kalau `YA`, platform memang membatasi semua mode multi-window.
- **Kelas lebar** → menentukan layout SukiOS nanti (compact/medium/expanded).

**Langkah 1 — Lane 1 (window manager)**
Geser jendela "Panduan Uji" ke tepi kiri → harus nempel separuh. Ke sudut → seperempat. Ke atas → maximize. Resize dari tepi/sudut. **Perhatikan angka FPS di taskbar saat menggeser.** Lalu tekan chip penilaian di panel LANE 1.

**Langkah 2 — Lane 3 (split screen, manual)**
Buka **Daftar Aplikasi** → pilih app (misal Chrome) → tombol **Fullscreen**. Lalu tekan tombol **Recents** (kotak di navigasi HP) → tahan app → **Split screen** → pilih app kedua. Bisa? Beri nilai di LANE 3.

**Langkah 3 — Lane 2 (freeform)**
Di **Daftar Aplikasi**, pilih app yang sama → tombol **Freeform**. Amati: app muncul **fullscreen** (→ freeform tidak didukung) atau **jendela kecil** (→ jackpot!). Beri nilai di LANE 2.

**Langkah 4 — Lane 4 (embed ActivityView)**
Buka **Uji Embed** → pilih target app dari daftar → tombol **1. Buat ActivityView** → tunggu 1 detik → tombol **2. Jalankan app**. Status-nya bakal nulis persis apa yang terjadi (misal "GAGAL (…): SecurityException …"). Screenshot + nilai LANE 4.

**Langkah 5 — Lane 5 (overlay taskbar)**
**Monitor Perangkat** → **Izin overlay** → di pengaturan Android aktifkan "Tampilkan di atas app lain" → balik ke app → **Mulai overlay** → tekan tombol Home → buka app lain (misal Chrome). **Apakah bar SukiOS masih kelihatan di bawah layar?** Beri nilai LANE 5.

**Langkah 6 — Kirim hasil**
Buka **Monitor Perangkat** → **Salin laporan** → tempel di chat kita. Atau **Bagikan** ke WhatsApp/email diri sendiri.

---

## 3. Setelah Hasilnya Masuk — Keputusan Arsitektur

Ini tabel keputusan yang sudah gue siapkan. Begitu laporanmu masuk, kita tinggal pilih kolomnya:

| Kondisi hasil uji | Arsitektur SukiOS yang dipilih |
|---|---|
| **Lane 2 = jendela** (freeform jalan) |  **Mode Desktop Penuh.** App pihak ketiga jadi jendela beneran. Kita optimalisasi window manager + snap layouts. |
| **Lane 5 jalan, Lane 2 gagal** |  **Mode Shell + Overlay** (jalur utama untuk HP biasa). App tetap fullscreen, SukiOS memberi: taskbar overlay, title bar overlay, split screen handoff, jendela untuk **app SukiOS sendiri** (Files, Notes, Calculator, Terminal, Settings). Ini yang dipakai launcher PC-style populer. |
| **Lane 3 jalan** | Tambahan: SukiOS jadi *pengendali* split screen (pintasan cepat "buka 2 app berdampingan"). |
| **Lane 4 jalan** (sangat tidak mungkin) |  Kita bisa menjalankan app apa pun di dalam jendela — produk jadi jauh lebih kuat dari rencana awal. |

**Catatan penting (temuan riset):** permintaan menjalankan app pihak ketiga di virtual display milik app biasa dibatasi platform sejak Android 9 (AOSP CL bug 63094482) — hanya activity yang mendeklarasikan `allowEmbedded="true"` yang boleh, dan pemanggilnya idealnya punya permission `ACTIVITY_EMBEDDING` (hanya app sistem). Karena itu **Lane 4 kami perlakukan sebagai eksperimen pembuktian, bukan tulang punggung produk.** Detail lengkap + rujukannya ada di `../PRD.md` §16.

---

## 4. Isi Kode (Buat Yang Mau Ngoprek)

```
app/src/main/java/app/sukios/poc/
├── MainActivity.kt          ← satu Activity = "desktop"
├── Kit.kt                   ← token desain Suki Glass (port dari DESIGN.md) + primitif UI
├── State.kt                 ← model jendela + state shell (master kebenaran)
├── Desktop.kt               ← wallpaper, ikon desktop, taskbar, start menu
├── WindowChrome.kt          ← title bar, drag, resize 8 arah, snap, geometri zona
├── WindowContents.kt        ← isi 6 jendela: panduan uji, daftar app, monitor, embed, files, notes
├── Probes.kt                ← capability probe + 5 lane peluncuran app + laporan
├── Fps.kt                   ← pengukur FPS (Choreographer)
└── OverlayTaskbarService.kt ← LANE 5: taskbar melayang (TYPE_APPLICATION_OVERLAY)
```

**Keputusan teknis yang sengaja diambil:**

- **Versi library dipin** (AGP 8.7.3 · Gradle 8.11.1 · Kotlin 2.0.21 · Compose BOM 2024.10.01) — kombinasi yang teruji, biar build pertama langsung hijau. Upgrade ke AGP 9.x + Compose BOM 2026.09.x **setelah** PoC lolos.
- **`minSdk 29`** — sama dengan target minimum SukiOS (Android 10).
- **ActivityView diakses lewat reflection** — karena itu bukan API publik (kelas `@hide`), jadi kalau ditulis langsung, build-nya gagal. Dengan reflection, app tetap ke-build dan kegagalannya justru jadi **data** yang kita catat.
- **Q2: izin `QUERY_ALL_PACKAGES`** dipasang (Play mengizinkan untuk launcher) + blok `<queries>` sebagai jalur hemat izin.
- **Signing release pakai debug key** — khusus PoC supaya APK dari CI bisa langsung diinstall. Di produksi nanti: keystore asli di GitHub Secrets.
- **configChanges lengkap** di manifest — wajib supaya Activity tidak di-recreate saat rotasi/embedding.

---

## 5. Batasan PoC (Jujur di Awal)

- Ini **bukan** SukiOS versi cantik. Ini alat ukur. Belum ada blur kaca, animasi halus, atau widget.
- Jendela **tidak menyimpan posisi** setelah app ditutup.
- File Explorer masih data contoh (belum menyentuh penyimpanan asli — itu butuh SAF).
- Belum jadi launcher default (belum `CATEGORY_HOME`) — itu Fase 1 PRD.
- FPS di taskbar mengukur frame Compose, bukan performa app pihak ketiga di dalam jendela.

---

*Setelah laporanmu masuk, langkah berikutnya: kunci arsitektur → bangun `:core:designsystem` → migrasi mockup ke Compose.*
