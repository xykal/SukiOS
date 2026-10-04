# SukiOS — aplikasi Android

Launcher sekaligus desktop environment untuk Android. Tombol Home membawa pengguna
ke SukiOS: desktop, taskbar, start menu, panel pintasan, jendela internal milik SukiOS,
dan aplikasi pihak ketiga yang dicoba dibuka sebagai jendela freeform Android.

Modul ini (`sukios/`) adalah aplikasi Android-nya. Dokumen produk ada di akar repo:
`PRD.md` (apa dan untuk siapa), `DESIGN.md` (Aurora v2), dan `../PROGRESS.md` (bukti kerja).

---

## Status jujur

| Hal | Keadaan |
|---|---|
| Kompilasi + uji unit | **terverifikasi di CI**; run dan bukti log dicatat di `../PROGRESS.md` |
| Rilis uji terbaru | `v0.4.0-alpha`, draft release, APK `SukiOS-v0.4.0-alpha.apk` sekitar 2,28 MB |
| Tanda tangan | **debug key** bila 4 secrets keystore rilis belum diset. Jangan dipakai untuk distribusi publik |
| Uji di perangkat | `v0.4.0-alpha` **belum diuji di perangkat nyata**. Uji unit membuktikan logika JVM/kompilasi, bukan perilaku OEM/ROM |
| Checklist uji | Ikuti `../docs/release-notes/v0.4.0-alpha.md`, bagian “Yang harus diuji di perangkat” |

---

## Yang sudah jadi

**Launcher (Beranda SukiOS)**
- Kategori `HOME` + `DEFAULT` + `LAUNCHER`: tombol Home masuk ke SukiOS, bukan launcher bawaan.
- Desktop dengan ikon aplikasi tersemat, alat SukiOS, aplikasi terbaru, dan indikator jendela.
- Taskbar Aurora 54 dp: tombol mulai, jendela internal, aplikasi tersemat/berjalan, tray jaringan-baterai-jam, status akses lanjutan.
- Start menu: pencarian aplikasi, grid sematan, daftar aplikasi, dan aksi cepat.
- Panel pintasan: status jendela, status Shizuku, saklar desktop penuh, taskbar melayang, pemilih 6 aksen dan 6 wallpaper.
- Setelan Tampilan bisa memakai Aurora bawaan atau live wallpaper sistem Android di belakang desktop SukiOS.
- Mode Go/RAM rendah: batas jendela internal turun ke 3 dan gerak wallpaper dimatikan.

**Tampilan Aurora v2**
- Palet gelap kebiruan + cahaya aurora, 6 preset aksen, 6 wallpaper, Inter + Plus Jakarta Sans.
- Kontras token teks/aksen dibuktikan oleh `ThemeContrastTest`.
- Ikon SukiOS digambar sendiri sebagai path vektor; `SukiGlyphData.kt` dihasilkan `tools/gen_glyphs.py` dan dijaga `GlyphDataTest`.

**Jendela internal (SukiWin)**
- Geser dari bilah judul, ubah ukuran 8 arah, snap ke kiri/kanan/atas, maximize, minimize ke taskbar.
- Tumpukan fokus, siklus fokus, “kecilkan semua”, “tutup semua”, dan geometri bebas yang dipulihkan setelah snap/maximize.
- Isi jendela: Setelan, Laboratorium, Terminal, Aplikasi, Tentang, Diagnostik.

**Jendela aplikasi pihak ketiga (SukiWindowing)**
- Satu jalur peluncuran: `am start --windowingMode 5 -n <pkg>/<activity>` lewat SukiShell.
- Hasil diverifikasi dari `dumpsys activity activities`, bukan diasumsikan.
- Enam outcome jujur: `WINDOWED`, `UNVERIFIED`, `FULLSCREEN`, `RUNNING_FULLSCREEN`, `BLOCKED`, `FAILED`.
- Bila aplikasi sudah berjalan layar penuh, pengguna ditawari menghentikannya dulu; SukiOS tidak mematikan aplikasi diam-diam.

**Akses lanjutan (SukiShell) — engine sendiri**
- Kontrak AIDL sendiri (`app/sukios/shell/ISukiShell.aidl`) dengan `destroy() = 16777114`.
- `SukiShellService` berjalan di proses terpisah sebagai **uid 2000** (shell) atau **uid 0** (root/Sui).
- Shizuku 13.1.5 dipakai **hanya sebagai kurir binder**. Tidak ada `newProcess` di mana pun: jalur itu sudah dihapus dari API 13.x.
- Perintah bertipe memakai daftar argumen, divalidasi `ShellPolicy`, tanpa shell parsing.
- Terminal adalah satu-satunya tempat yang sengaja bisa menjalankan perintah bebas; hasil tetap dilaporkan apa adanya.
- `ShellExec` memberi timeout 15 detik dan batas keluaran 64 KiB per stream supaya UI/binder tidak tertahan.

**Penyiapan otomatis (SukiAuto / SetupPlan)**
- Menyalakan setelan freeform/resizable yang relevan, memberi izin secure settings/overlay bila memungkinkan, dan mencoba menjadikan SukiOS launcher.
- Idempoten: hanya langkah yang belum terpenuhi yang dijalankan.
- Keberhasilan diperiksa dengan API Android (`Settings`, `canDrawOverlays`, launcher role), bukan kode keluar perintah.
- Uji jendela memakai `ProbeActivity` milik SukiOS sendiri dan menyimpan hasilnya untuk status “Jendela aktif”.

---

## Memasang build uji

1. Unduh APK dari draft release `v0.4.0-alpha` di halaman Releases.
2. Pasang APK (izinkan “instal dari sumber tidak dikenal” bila diminta).
3. Buka SukiOS, lalu jadikan launcher default dari pilihan Home atau Setelan Android.
4. Opsional: beri izin “tampil di atas aplikasi lain” untuk taskbar melayang.
5. Opsional: pasang aplikasi Shizuku, jalankan servisnya, lalu buka SukiOS → Setelan/Laboratorium → jalankan persiapan dan uji jendela.

## Menguji sendiri di perangkat

Checklist utama ada di `../docs/release-notes/v0.4.0-alpha.md`. Ringkasnya:

| Yang diuji | Caranya | Hasil yang diharapkan |
|---|---|---|
| Launcher | Tekan tombol Home | Masuk ke Beranda SukiOS; pilihan launcher muncul saat pertama kali |
| Orientasi | Putar perangkat | Tampilan tetap mendatar (`sensorLandscape`) |
| Persiapan jendela | Setelan/Lab → Persiapan → Uji jendela | Status “Jendela aktif” hanya muncul bila probe benar-benar lulus |
| App pihak ketiga | Buka dari desktop/Start sebagai jendela | Outcome jujur: windowed, fullscreen, blocked, failed, atau already-running fullscreen |
| App sudah fullscreen | Buka ulang dari SukiOS | Muncul tawaran “hentikan dulu”, tidak force-stop tanpa persetujuan |
| Diagnostik | Setelan → Diagnostik → Salin/Bagikan | Laporan berisi fakta perangkat, Shizuku, window status, dan potongan dump bila ada |

## Membangun

Build utama dijalankan CI (`.github/workflows/`):

- `Build APK` — setiap dorongan ke `main`: workflow-check, generator glyph check, unit test, debug build, release build.
- `Release APK (signed, draft)` — setiap tag `v*`: unit test, release build, draft release dengan APK.
- `Cleanup CI traces` — membersihkan riwayat run/artifact/cache sesuai kebijakan (release memurge cache, build biasa tidak).

Kunci rilis dibaca dari environment (`SUKIOS_KEYSTORE_*`), tidak pernah ditulis ke repo.
Panduan: `../docs/RELEASE-SIGNING.md`.

## Catatan teknis yang perlu diingat

- **Properti yang dibaca composable harus state Compose.** `mutableStateListOf<Win>` hanya memantau isi daftar; semua field `Win` yang dibaca UI memakai `mutableStateOf`.
- **StateFlow di UI dibaca lewat `collectAsState()`.** `.value` hanya boleh untuk event handler/kode non-UI.
- **Pemanggilan SukiShell dari UI lewat `SukiShell.io { ... }`.** Binder + pembuatan proses tidak boleh jalan di thread utama.
- **AIDL wajib disimpan dari R8**: aturannya ada di `proguard-rules.pro`. Tanpa itu, `ISukiShell` dan `SukiShellService` akan dihapus/rename dan SukiShell mati di build rilis.
- **Batas platform:** aplikasi pihak ketiga tidak bisa dimasukkan ke kotak Compose milik SukiOS. Yang bisa dilakukan adalah meminta Android membukanya sebagai freeform window dan memverifikasi hasilnya.
