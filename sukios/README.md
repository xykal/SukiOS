# SukiOS — aplikasi Android

Launcher sekaligus desktop environment untuk Android. Tombol Home membawa pengguna
ke SukiOS: desktop, taskbar, start menu, panel pintasan, dan jendela mengambang
untuk isi milik SukiOS sendiri.

Modul ini (`sukios/`) adalah aplikasi Android-nya. Dokumen produk ada di akar repo:
`PRD.md` (apa dan untuk siapa) dan `DESIGN.md` (aturan tampilan).

---

## Status jujur

| Hal | Keadaan |
|---|---|
| Kompilasi | **terverifikasi di CI** (Build APK run 37027127027, commit `4bd59d4`) |
| Rilis | `v0.3.0-alpha`, APK 1,77 MB di draft release |
| Isi APK | **diperiksa**: kelas `SukiShellService`, `ISukiShell$Stub`, `ShizukuProvider`, `SukiHomeActivity`, `SukiOverlayBar` ada di dex; manifest memuat kategori `HOME` |
| Tanda tangan | **debug key** — secrets keystore rilis belum diset. Jangan dipakai untuk distribusi publik |
| Uji di perangkat | **BELUM** — tidak ada yang boleh mengklaim "berfungsi" sebelum ada laporan dari perangkat nyata |

---

## Yang sudah jadi

**Launcher (Beranda SukiOS)**
- Kategori `HOME` + `DEFAULT` + `LAUNCHER`: tombol Home masuk ke SukiOS, bukan launcher bawaan.
- Desktop dengan ikon aplikasi tersemat, baris ikon sistem, dan penanda jendela yang sedang terbuka.
- Taskbar: tombol mulai, daftar jendela terbuka, indikator Shizuku, saklar cepat, jam, baterai.
- Start menu: pencarian aplikasi, tombol sematkan, jumlah aplikasi terpasang.
- Panel pintasan: saklar desktop, pemilih aksen (10 preset) dan wallpaper (6 preset), status Shizuku, pintasan setelan.
- Layar persiapan tiga langkah (jadikan launcher, izin overlay, akses lanjutan) — semuanya bisa dilewati.
- Mode Go: perangkat RAM rendah otomatis dibatasi 3 jendela dan efek dekoratif dimatikan.

**Jendela (SukiWin)**
- Geser dari bilah judul, ubah ukuran 8 arah, snap ke tepi (kiri/kanan/maksimal), maximize, minimize ke taskbar.
- Tumpukan fokus, siklus fokus, "kecilkan semua" dan "tutup semua".
- Isi jendela: Setelan, Laboratorium, Terminal, Aplikasi, Tentang, Diagnostik.

**Akses lanjutan (SukiShell) — engine sendiri**
- Kontrak AIDL sendiri (`app/sukios/shell/ISukiShell.aidl`) dengan `destroy() = 16777114`.
- `SukiShellService` berjalan di proses terpisah sebagai **uid 2000** (shell) atau **uid 0** (root/Sui).
- Shizuku 13.1.5 dipakai **hanya sebagai kurir binder**. Tidak ada `newProcess` di mana pun: jalur itu sudah dihapus dari API 13.x.
- Perintah selalu berbentuk daftar argumen (tanpa shell parsing), jadi tidak ada celah injeksi; nama paket dan id display divalidasi sebelum dipakai.
- Hasil selalu dilaporkan apa adanya: kode keluar, stdout, stderr.
- Kemampuan: identitas, force-resizable aplikasi pihak ketiga, izin overlay otomatis (appops), peluncuran ke display lain, diagnostik display, tombol kembali/home global.
- **Tanpa Shizuku semuanya tetap jalan**; perintah akan melaporkan kode 127 dan alasannya, bukan hasil palsu.

---

## Memasang

1. Unduh APK dari draft release `v0.3.0-alpha` di halaman Releases.
2. Pasang (izinkan "instal dari sumber tidak dikenal" bila diminta).
3. Buka SukiOS, lalu **Setelan → Jadikan launcher default** (atau tekan Home dan pilih SukiOS).
4. Opsional: beri izin "tampil di atas aplikasi lain" untuk taskbar melayang.
5. Opsional: pasang aplikasi Shizuku, jalankan servisnya, lalu **Setelan → Akses lanjutan → Minta izin**.

## Menguji sendiri di perangkat

| Yang diuji | Caranya | Hasil yang diharapkan |
|---|---|---|
| Launcher | Tekan tombol Home | Masuk ke Beranda SukiOS (muncul pilihan launcher saat pertama) |
| Jendela internal | Ketuk ikon Laboratorium di desktop | Jendela mengambang; coba geser, ubah ukuran, snap ke tepi |
| Taskbar melayang | Taskbar → ikon overlay → beri izin | Bar "Kembali / Beranda / Menu" muncul di atas aplikasi lain |
| SukiShell | Laboratorium → Identitas | `uid=2000` (atau 0 bila root) — kode keluar 0 |
| Jendela app pihak ketiga | Jendela Aplikasi → tombol jendela pada satu aplikasi | Kalau aplikasi tetap penuh layar, perangkat menolak bounds (freeform mati) |
| Diagnostik | Setelan → Diagnostik → Bagikan | Berkas teks berisi fakta perangkat, status Shizuku, dan hasil dari sisi shell |

## Membangun

Build dijalankan CI (`.github/workflows/`), bukan di perangkat:

- `Build APK` — setiap dorongan ke `main`: debug + release, ringkasan APK.
- `Release APK (signed, draft)` — setiap tag `v*`: membuat draft release dengan APK bertanda tangan.
- `Cleanup CI traces` — membersihkan riwayat run/artifact/cache sesuai kebijakan (release memurge, build tidak).

Kunci rilis dibaca dari environment (`SUKIOS_KEYSTORE_*`), tidak pernah ditulis ke repo.
Panduan: `docs/RELEASE-SIGNING.md`.

## Catatan teknis yang perlu diingat

- **Compose 1.7.4 tidak punya lagi fungsi pabrik `TextStyle(color = ...)`.** Diperiksa lewat bytecode `ui-text-android-1.7.4.aar`: yang tersisa hanya konstruktor `(SpanStyle, ParagraphStyle, PlatformTextStyle)` dan `copy()` versi stabil. Karena itu teks memakai `Text` dari material3 dan kolom ketik memakai `TextStyle.Default.copy()` lewat satu fungsi `fieldStyle()` di `SukiKit.kt`.
- **AIDL wajib disimpan dari R8**: aturannya ada di `proguard-rules.pro`. Tanpa itu, `ISukiShell` dan `SukiShellService` akan dihapus/rename dan SukiShell mati di build rilis.
- **Batas yang tidak bisa ditembus**: aplikasi pihak ketiga tidak bisa dimasukkan ke jendela SukiOS. Yang bisa dilakukan: membuka mereka layar penuh dengan taskbar mengapung, atau mencoba mengambang lewat bounds bila perangkat mengizinkan.
