# SukiOS — Product Requirements Document (PRD)

| | |
|---|---|
| **Produk** | SukiOS — Desktop Environment untuk Android |
| **Versi Dokumen** | 1.0 (Draft) |
| **Tanggal** | 1 Oktober 2026 |
| **Platform** | Android 10 (API 29) → Android 16 (API 36) |
| **Bahasa UI** | Indonesia (default), English (opsional) |
| **Status** | Discovery & Design — belum masuk development |

---

## 0. Ringkasan Eksekutif

**SukiOS** adalah *launcher / desktop environment* untuk Android yang mengubah smartphone atau tablet menjadi pengalaman komputer desktop: taskbar, start menu, jendela aplikasi mengambang yang bisa digeser & di-resize, multitasking, widget, file explorer, dan settings lengkap.

Berbeda dari "skin Windows" yang sering beredar, SukiOS **tidak meniru merek pihak lain**. Ia adalah sistem operasi desktop dengan **identitas, bahasa desain, dan penamaan sendiri** — sehingga:

1. **Aman secara hukum** — tidak ada merek dagang, logo, nama produk, atau ikon milik pihak lain yang dipakai.
2. **Punya karakter** — bukan "Windows palsu", tapi OS dengan kepribadian sendiri.
3. **Siap dikomersialkan** — bisa dipublikasikan, dimonetisasi, atau dikembangkan menjadi produk komersial.

> **Nama kerja internal proyek:** SukiOS. Bahasa desain visualnya: **"Suki Glass"**.

---

## 1. Latar Belakang & Masalah

### 1.1 Konteks
Smartphone modern punya CPU 8-core, RAM 8–16 GB, dan layar 6.7" — lebih kuat dari laptop 10 tahun lalu. Tapi antarmuka yang dipakai masih murni *mobile*: satu aplikasi penuh layar, satu waktu, tanpa jendela.

Padahal ada momen ketika pengguna butuh **produktivitas desktop**:
- Kerja sambil pakai keyboard eksternal + mouse.
- Cast ke TV / monitor lewat HDMI atau DeX-like mode.
- Buka 2–3 aplikasi bersamaan (chat + browser + catatan).
- Tablet yang seharusnya jadi perangkat kerja ringan.

### 1.2 Masalah yang Ada Sekarang
| Masalah | Detail |
|---|---|
| Split screen Android terbatas | Cuma 2 (kadang 3) aplikasi, tidak bisa resize bebas, tidak ada jendela mengambang |
| Launcher desktop yang ada jelek/berbayar | Banyak "PC launcher" di Play Store cuma ubah ikon + wallpaper, tanpa window management asli |
| Skin Windows = risiko hukum | Menggunakan nama, logo, ikon, dan suara start-up sistem lain → rawan takedown & DMCA |
| Android 16 desktop windowing belum ada "rumahnya" | Platform sudah mendukung freeform window, tapi UI-nya generic; belum ada desktop environment yang matang di atasnya |

### 1.3 Peluang
Android 16 (dan seterusnya) membawa **desktop windowing** sebagai kemampuan native — Google sendiri membangunnya di atas fondasi Samsung DeX, dengan jendela freeform, header custom, taskbar bawaan sistem, dan **ukuran jendela minimum 386 × 352 dp**. Artinya: panggungnya sudah dibangun, SukiOS tinggal jadi **pemain utamanya**.

---

## 2. Visi & Positioning

> **Visi:** Menjadikan setiap Android sebagai komputer desktop yang bisa kamu bawa di kantong.

**Positioning one-liner:**
> *"SukiOS — bukan sekadar launcher. Ini desktop OS-nya Android."*

### 2.1 Prinsip Produk (Product Pillars)
1. **Desktop-first, bukan phone-first** — semua keputusan desain mengasumsikan ada kursor, jendela, dan multitasking.
2. **Familiar, bukan tiruan** — nyaman bagi pengguna yang pernah pakai komputer, tapi identitas visual 100% original.
3. **Ringan & mulus** — 60 fps saat drag window, cold start < 800 ms, hemat RAM (target < 150 MB).
4. **Bisa dipakai tanpa baca manual** — setiap fitur self-explanatory; discoverability lewat interaksi.

---

## 3. Target Pengguna

### Persona 1 — "Rian, Mahasiswa Teknik" (Primary)
- HP Android mid-range, sering pakai keyboard Bluetooth untuk nulis tugas.
- Frustrasi: tidak bisa buka PDF + Word + browser bersamaan.
- Butuh: multitasking nyata, file explorer, folder di desktop.

### Persona 2 — "Dina, Freelancer Desain" (Secondary)
- Punya tablet Android + stylus, ingin tablet jadi laptop ringan.
- Butuh: jendela bebas, drag & drop antar aplikasi, widget cepat.

### Persona 3 — "Bagas, Power User / Tinkerer" (Secondary)
- Suka customisasi ekstrem launcher, ganti tema, bikin widget.
- Butuh: kontrol penuh (taskbar position, tema, shortcut, scripting opsional).
- Peran penting: **early adopter & penyebar di komunitas** (Reddit, forum launcher).

### Persona 4 — "Pak Yusuf, Pengguna Sehari-hari" (Tertiary)
- Belum pernah pakai Linux/PC-mode, ingin HP terasa "lebih gede".
- Butuh: setup otomatis, tidak membingungkan, ada mode "Simple".

---

## 4. Bahasa Desain & Branding

### 4.1 Keputusan Branding

| Elemen | Keputusan | Alasan |
|---|---|---|
| Nama OS | **SukiOS** | Nama original, mudah diingat, terdengar ramah & modern |
| Bahasa desain | **Suki Glass** | Nama sistem desain yang bisa dipakai sebagai brand sendiri |
| Maskot | **Suki** — karakter bentuk minimalis (opsional, fase 2) | Bisa jadi kekuatan brand jangka panjang |
| Logo | Mark *squircle* dengan gerigi aurora membentuk huruf **S** | Original, bukan bendera 4 kotak, bukan jendela miring |
| Signature visual | Gradien **Aurora** (ungu → teal) | Membedakan dari biru khas vendor lain |
| Suara | Chime 3 nada (komposisi sendiri) | Tidak memakai suara sistem mana pun |

### 4.2 Daftar Hal yang DILARANG (Trademark Safety)

Bagian ini adalah **requirement wajib**, bukan saran:

** DILARANG:**
- Kata "Windows", "Microsoft", "Win 11/12", "Metro", "Fluent" sebagai nama produk, nama fitur, atau di metadata store.
- Logo 4 kotak perspektif (logo Windows), logo Microsoft, ikon-ikon bergaya Fluent Microsoft.
- Wallpaper, foto, atau aset yang berasal dari sistem operasi lain.
- Suara start-up Windows, font Segoe UI, ikon dari sistem lain.
- Menyalin *pixel-perfect* taskbar/start menu Windows sampai membingungkan pengguna (menimbulkan risiko *trade dress*).
- Menampilkan nama aplikasi pihak lain seolah berafiliasi (misal: "SukiOS for Windows").

** DIIZINKAN & DIANJURKAN:**
- Menggunakan **paradigma** yang sama (taskbar, start menu, jendela) — konsep UI seperti ini sudah menjadi standar industri (Linux KDE/GNOME/XFCE, ChromeOS, macOS punya padanannya). Paradigma bukan merek.
- Font **Inter** (lisensi SIL OFL 1.1 — bebas dipakai komersial).
- Ikon sendiri, atau Material Symbols (Apache 2.0) yang di-restyle.
- Menyebut kompatibilitas secara deskriptif: *"desktop-style launcher"*, bukan *"Windows 12 clone"*.

**Catatan hukum:** dokumen ini bukan nasihat hukum. Sebelum rilis komersial, lakukan *trademark clearance search* di basis data DJKI (Indonesia) dan USPTO/EUIPO jika mau rilis global.

### 4.3 Referensi Visual
Arah rasa: *glassmorphism modern* + *Mica/acrylic* + sudut membulat besar + gradien aurora.
**Bukan** referensi: menyalin tema visual vendor tertentu.

---

## 5. Analisis Kompetitor

| Produk | Kelebihan | Kelemahan | Pelajaran untuk SukiOS |
|---|---|---|---|
| **Computer Launcher** (Play Store) | Populer, banyak fitur desktop | UI dated, iklan, berat, window palsu (cuma overlay) | Jangan pakai iklan mengganggu; buat window yang benar-benar jalan |
| **Win 11 Launcher / skin sejenis** | Familiar instan | Risiko hukum tinggi, tidak menambah produktivitas nyata | Familiar boleh, tiruan jangan |
| **Samsung DeX** | Sempurna di Samsung + monitor | Terkunci ke perangkat Samsung, butuh hardware khusus | SukiOS harus jalan di HP apapun, tanpa monitor tambahan |
| **Sentient OS / Linux-on-Android** | Kaya fitur desktop | Berat, butuh root/VNC, tidak untuk pengguna awam | Ringan, native, tanpa root |
| **ChromeOS / Android desktop windowing** | Fondasi platform kuat | UI minimalis, terbatas, OEM-dependent | **Manfaatkan sebagai fondasi, bukan lawan** |

**Celah pasar yang diisi SukiOS:** desktop environment *native*, *ringan*, *tanpa root*, *beridentitas sendiri*, dan **jalan mulus di HP biasa** — bukan hanya tablet atau perangkat flagship.

---

## 6. Ruang Lingkup

Legenda prioritas: **P0** = wajib MVP · **P1** = wajib V1 · **P2** = nice-to-have · **P3** = masa depan

### 6.1 MVP (Fase 1) — "Bisa jadi launcher beneran"

> **Status 2 Okt 2026:** v0.3.0-alpha sudah berbentuk launcher (kategori HOME aktif) dengan taskbar, start menu, jendela, snap, dan mesin akses lanjutan sendiri. Kompilasi terverifikasi di CI; uji perangkat belum ada, jadi belum ada klaim "berfungsi". v0.3.1-alpha (2 Okt 2026, audit `docs/AUDIT-2026-10-02.md`) memperbaiki cacat yang membuat UI tidak menggambar ulang dan menambah 44 uji unit; uji perangkat tetap belum ada.
| # | Fitur | Prioritas |
|---|---|---|
| 1 | Home screen pengganti (bisa diset sebagai launcher default) | P0 |
| 2 | Desktop: wallpaper, grid ikon, drag & susun, rename, uninstall | P0 |
| 3 | Taskbar: start, pinned apps, running apps indicator, jam, tray | P0 |
| 4 | Start Menu: search, pinned grid, daftar semua app, power menu | P0 |
| 5 | Window Manager: buka app dalam jendela mengambang (drag, resize, min, max, close) | P0 |
| 6 | Task View: daftar semua jendela terbuka | P0 |
| 7 | Quick Settings: wifi, bluetooth, senter, rotasi, brightness, volume | P1 |
| 8 | Settings app (personalization, taskbar, window, sistem) | P1 |
| 9 | Context menu (klik kanan / long-press) desktop & ikon | P1 |
| 10 | Lock screen bergaya SukiOS | P1 |

### 6.2 V1 (Fase 2) — "Produktivitas nyata"
| # | Fitur | Prioritas |
|---|---|---|
| 11 | Snap Layouts (drag ke tepi → jendela menempel setengah/kuartal) | P1 |
| 12 | Snap Assist (pilih app untuk mengisi sisa ruang) | P1 |
| 13 | Widgets di desktop (jam, cuaca, kalender, baterai, catatan) | P1 |
| 14 | File Explorer internal (navigasi dokumen, gambar, unduhan) | P1 |
| 15 | Notification Center (panel notifikasi custom + badge di taskbar) | P1 |
| 16 | Universal Search (apps + settings + file + web, satu kotak) | P1 |
| 17 | Dukungan mouse & keyboard eksternal penuh (hover, klik kanan, shortcut) | P1 |
| 18 | Tema & accent color, dark/light, wallpaper library | P1 |
| 19 | Multi-desktop / "Desk" (2–4 ruang kerja terpisah) | P2 |
| 20 | Mode Simple (UI disederhanakan untuk pengguna awam) | P2 |

### 6.3 V2 (Fase 3) — "Ekosistem"
| # | Fitur | Prioritas |
|---|---|---|
| 21 | Suki Store (kurasi app + tema, monetisasi) | P2 |
| 22 | Desktop mode penuh saat tersambung ke monitor eksternal (HDMI/USB-C) | P2 |
| 23 | Integrasi Android 16+ desktop windowing native (fallback otomatis) | P2 |
| 24 | Suki Terminal (shell sederhana, perintah dasar untuk power user) | P2 |
| 25 | Sinkronisasi layout desktop antar perangkat | P3 |
| 26 | Dukungan stylus: gesture pintasan (coret = potong, dll.) | P3 |
| 27 | AI Assistant terintegrasi di search bar | P3 |

### 6.4 Di Luar Cakupan (Out of Scope) — sengaja TIDAK dikerjakan
- Mengganti sistem operasi Android (SukiOS = *desktop environment*, bukan ROM/custom OS).
- Root, flashing, bootloader (harus jalan tanpa root — **batasan non-negotiable**).
- Menjalankan aplikasi Windows (.exe) — bisa jadi proyek terpisah di masa depan via Wine/Box64, tapi bukan bagian dari SukiOS.
- Emulasi produk pihak lain atau penggunaan merek pihak lain.

---

## 7. Spesifikasi Fitur (User Story + Acceptance Criteria)

### 7.1 Shell & Desktop — `F-01`

**User Story:** Sebagai pengguna, aku ingin layar utama HP-ku terlihat dan terasa seperti desktop komputer, supaya aku bisa mengatur aplikasi seperti ikon di PC.

**Acceptance Criteria:**
- [ ] Aplikasi bisa diset sebagai launcher default lewat Settings → Home app (via intent `CATEGORY_HOME`).
- [ ] Menekan tombol Home selalu membawa ke Desktop SukiOS, bukan ke launcher lama.
- [ ] Desktop mendukung minimal 5 baris × 6 kolom ikon (menyesuaikan orientasi & ukuran layar).
- [ ] Ikon bisa: dipindah (drag), disusun ulang (auto-align), di-rename, disembunyikan, dan di-uninstall (dengan konfirmasi).
- [ ] Menekan ikon membuka aplikasi; long-press/klik kanan membuka context menu.
- [ ] Wallpaper bisa diganti dari galeri atau koleksi bawaan SukiOS.
- [ ] Layout desktop tersimpan persisten (bertahan setelah HP restart).
- [ ] Mendukung mode gelap & terang.

---

### 7.2 Taskbar — `F-02`

**User Story:** Sebagai pengguna, aku ingin ada taskbar di bawah layar yang menunjukkan aplikasi favorit dan yang sedang berjalan, supaya aku bisa berpindah aplikasi dalam satu ketukan.

**Acceptance Criteria:**
- [ ] Taskbar selalu terlihat di Desktop (dapat di-auto-hide untuk mode imersif).
- [ ] Elemen kiri→kanan: tombol Start · Search bar · Task View · app yang di-pin · app berjalan · System Tray (wifi, volume, baterai, jam+tanggal) · Notification center.
- [ ] Aplikasi berjalan menampilkan indikator (garis/underline) di bawah ikonnya.
- [ ] Klik ikon app yang sedang aktif → minimize (perilaku toggle).
- [ ] Klik ikon app yang tidak aktif → fokus ke jendelanya (bring to front).
- [ ] Bisa diatur: posisi (bawah/atas/kiri), ukuran ikon, label judul, auto-hide, hanya di desktop vs selalu.
- [ ] Jam & tanggal ter-update real-time dan mengikuti format lokal pengguna.
- [ ] Tinggi taskbar default 48 dp (dapat diperbesar hingga 64 dp).

---

### 7.3 Start Menu — `F-03`

**User Story:** Sebagai pengguna, aku ingin satu tempat untuk mencari dan membuka semua aplikasi, supaya tidak perlu scroll beranda.

**Acceptance Criteria:**
- [ ] Terbuka dengan klik tombol Start, atau shortcut keyboard (Win/Super key atau `Ctrl+Space`).
- [ ] Berisi: Search field, grid "Disematkan" (pinned), daftar "Semua Aplikasi" (alfabetis, dengan header huruf), bagian "Terakhir Dipakai".
- [ ] Bagian bawah: profil pengguna + tombol Power (Sleep, Restart UI, Power Off sistem, Lock).
- [ ] Animasi buka/tutup ≤ 220 ms, dengan efek glass blur.
- [ ] Bisa dipin/unpin aplikasi langsung dari sini.
- [ ] Menekan `Esc` atau klik di luar menutup menu.
- [ ] Ukuran default 640 × 640 dp, adaptif terhadap layar (tablet: lebih besar & multi-kolom).

---

### 7.4 Universal Search — `F-04`

**User Story:** Sebagai pengguna, aku ingin mengetik satu kata dan langsung ketemu apa pun — aplikasi, file, atau pengaturan.

**Acceptance Criteria:**
- [ ] Mencari secara instan (debounce ≤ 120 ms) di: aplikasi terinstal, pengaturan SukiOS, file di folder yang diizinkan, kontak (opsional).
- [ ] Hasil dikelompokkan per kategori dengan ikon dan label yang jelas.
- [ ] Baris pertama ter-highlight otomatis; `Enter` membuka hasil teratas; panah atas/bawah untuk navigasi.
- [ ] Mendukung pencarian web sebagai hasil terakhir ("Cari '<query>' di web").
- [ ] Riwayat pencarian tersimpan lokal & bisa dibersihkan.
- [ ] Bisa dibuka kapan saja dengan gesture (mis. swipe dari taskbar) atau shortcut.

---

### 7.5 Window Manager — `F-05` ⭐ (Fitur Inti)

**User Story:** Sebagai pengguna, aku ingin membuka aplikasi dalam jendela yang bisa aku geser dan atur ukurannya, supaya aku bisa multitasking seperti di PC.

**Acceptance Criteria:**
- [ ] Aplikasi terbuka di dalam jendela dengan **title bar** SukiOS (ikon app, judul, dan kontrol jendela).
- [ ] Kontrol jendela: **Minimize · Maximize/Restore · Close** (posisi kanan atas, gaya SukiOS — bukan tiruan control button sistem lain).
- [ ] Jendela bisa **digeser** dari title bar dan **di-resize** dari sisi/sudut (8 arah).
- [ ] Ukuran minimum jendela 320 × 240 dp; maksimum = area desktop.
- [ ] Z-order benar: jendela yang disentuh naik ke depan; jendela aktif punya shadow & border lebih tegas.
- [ ] Double-tap title bar = maximize/restore.
- [ ] Jendela bisa di-minimize ke taskbar dan dipulihkan dengan animasi.
- [ ] Menutup jendela menutup aplikasi yang berjalan di dalamnya (dengan konfirmasi untuk perubahan belum tersimpan bila aplikasi mendukung).
- [ ] Mendukung multi-instance: app yang sama bisa dibuka di dua jendela.
- [ ] Maksimal 8 jendela aktif bersamaan (di atas itu → peringatan/prompt menutup).
- [ ] Drag & resize stabil di 60 fps pada perangkat mid-range.
- [ ] Jendela "snap" dengan indikator visual saat didekatkan ke tepi layar.

---

### 7.6 Snap & Multitasking — `F-06`

**User Story:** Sebagai pengguna, aku ingin jendela langsung menempel rapi di separuh atau seperempat layar, supaya aku bisa menyusun kerjaan dengan cepat.

**Acceptance Criteria:**
- [ ] Drag jendela ke **tepi kiri/kanan** → menempel setengah layar (50%).
- [ ] Drag ke **sudut** → menempel seperempat layar (25%).
- [ ] Drag ke **atas** → maximize; drag menjauh dari atas → restore.
- [ ] **Snap Layouts:** hover pada tombol maximize → muncul popup berisi pilihan tata letak (2 kolom, 3 kolom, 2×2, 1 besar + 2 kecil).
- [ ] **Snap Assist:** setelah satu jendela menempel, sisa ruang menampilkan pilihan jendela lain untuk mengisi.
- [ ] Split screen Android juga didukung sebagai fallback (2 app berdampingan) pada perangkat yang tidak mendukung window mengambang.
- [ ] Animasi snap halus (≤ 180 ms, easing *decelerate*).

---

### 7.7 Task View — `F-07`

**User Story:** Sebagai pengguna, aku ingin melihat semua jendela yang terbuka dalam satu tampilan, supaya bisa cepat berpindah.

**Acceptance Criteria:**
- [ ] Terbuka via tombol Task View, gesture swipe-up-hold, atau `Alt+Tab`.
- [ ] Menampilkan kartu *live preview* tiap jendela (atau *snapshot* terakhir bila live preview tidak memungkinkan).
- [ ] Klik kartu → fokus ke jendela tersebut & keluar dari Task View.
- [ ] Tiap kartu punya tombol tutup (×).
- [ ] Menampilkan juga "Desk" (ruang kerja) bila fitur multi-desktop aktif.
- [ ] Animasi zoom-out dari desktop (200 ms, *ease-in-out*).

---

### 7.8 Quick Settings & Notification Center — `F-08`

**User Story:** Sebagai pengguna, aku ingin panel cepat untuk wifi, bluetooth, senter, dan notifikasi — seperti panel sistem di PC.

**Acceptance Criteria:**
- [ ] Terbuka dari System Tray (klik area jam/ikon) atau swipe dari tepi kanan atas.
- [ ] Toggle cepat: Wi-Fi, Bluetooth, Pesawat, Sentera (flashlight), Rotasi, Jangan Ganggu, Airplane, Night Light, Cast/Screen Mirroring, Hotspot.
- [ ] Slider: kecerahan layar & volume media — **berfungsi nyata**, bukan visual.
- [ ] Notification Center menampilkan notifikasi aplikasi (via `NotificationListenerService`), dikelompokkan per aplikasi, bisa di-dismiss & di-reply (jika app mendukung).
- [ ] Notifikasi muncul sebagai toast di sudut kanan bawah desktop.
- [ ] Badge jumlah notifikasi di taskbar.
- [ ] Dark mode otomatis mengikuti sistem atau bisa di-override.

---

### 7.9 Widgets — `F-09`

**Acceptance Criteria:**
- [ ] Desktop mendukung widget dengan grid snapping.
- [ ] Widget bawaan: Jam analog/digital, Kalender, Cuaca, Baterai & Storage, Catatan Cepat, Now Playing (media), Statistik app usage.
- [ ] Mendukung **widget aplikasi Android pihak ketiga** (via `AppWidgetHost`).
- [ ] Widget bisa di-resize (minimal 2 ukuran) dan diletakkan bebas di grid desktop.
- [ ] Mode "Widget kecil" untuk layar HP (bukan hanya tablet).

---

### 7.10 File Explorer — `F-10`

**User Story:** Sebagai pengguna, aku ingin menjelajah file di HP-ku seperti di Windows Explorer.

**Acceptance Criteria:**
- [ ] Panel kiri: pohon navigasi (Home, Documents, Downloads, Pictures, Music, Videos, Internal, SD Card, USB OTG).
- [ ] Panel kanan: daftar file dengan mode *list* dan *grid*.
- [ ] Operasi: buka, copy, cut, paste, rename, delete (dengan konfirmasi), share, "Open with".
- [ ] Preview gambar, video, audio, dan teks langsung di jendela.
- [ ] Pencarian file di dalam folder.
- [ ] Membuka file via Storage Access Framework (aman & patuh kebijakan Play — tanpa akses berlebihan).
- [ ] Drag & drop file antar jendela.

---

### 7.11 Settings SukiOS — `F-11`

**Acceptance Criteria:**
- [ ] Kategori: Personalisasi, Taskbar, Jendela & Multitasking, Aplikasi, Widget, Sistem, Privasi, Tentang SukiOS.
- [ ] **Personalisasi:** wallpaper (galeri + koleksi), accent color (12 preset + color picker), tema (Terang/Gelap/Otomatis), efek transparansi (on/off untuk performa).
- [ ] **Taskbar:** posisi, ukuran, pinned apps, auto-hide, tampilkan label.
- [ ] **Jendela:** ukuran default, aksi tombol close, cheat sheet shortcut, snap on/off.
- [ ] **Sistem:** mode performa (Hemat Seimbang/Turbo), restart UI, reset layout, backup & restore layout (export/import JSON).
- [ ] **Tentang:** versi, changelog, kredit, open-source license, tombol "Lapor Bug".
- [ ] Pencarian di dalam Settings.

---

### 7.12 Lock Screen — `F-12`

**Acceptance Criteria:**
- [ ] Jam besar, tanggal, cuaca (opsional), notifikasi ringkas.
- [ ] Unlock via PIN/pola/biometrik bawaan sistem (tidak menyimpan kredensial sendiri).
- [ ] Bisa dimatikan (pakai lock screen bawaan Android).

---

### 7.13 Dukungan Mouse, Keyboard & Layar Besar — `F-13`

**Acceptance Criteria:**
- [ ] Pointer: hover state, klik kanan (context menu), scroll wheel, drag-select di desktop.
- [ ] Keyboard: `Win`/`Super` = Start, `Alt+Tab` = Task View, `Ctrl+Space` = Search, `Win+D` = tampilkan desktop, `Alt+F4` = tutup jendela aktif.
- [ ] Kursor bisa tampil sebagai kursor SukiOS (opsional).
- [ ] Responsif di layar 5" hingga 13"+ (tablet, foldable, DeX-mode).
- [ ] Saat konek ke monitor eksternal (perangkat yang mendukung): desktop diperluas, bukan sekadar mirroring.

---

### 7.14 Mode Desktop: Kunci Landscape & Desktop Penuh — `F-14`

**User Story:** Sebagai pengguna, aku ingin layar terkunci mendatar dan bar sistem disembunyikan, supaya rasanya benar-benar memakai komputer, bukan HP yang diputar.

**Acceptance Criteria:**
- [ ] Mode **Kunci Landscape**: orientasi dikunci ke mendatar (dua arah, tidak pernah portrait) lewat `SCREEN_ORIENTATION_SENSOR_LANDSCAPE`.
- [ ] Mode **Desktop Penuh**: status bar dan navigation bar disembunyikan; muncul sementara dengan geser dari tepi (`BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`).
- [ ] Kedua mode bisa diubah dari taskbar dan dari Settings; status tersimpan setelah restart.
- [ ] Desktop penuh tidak mengganggu keamanan: lock screen dan izin sistem tetap berfungsi normal.
- [ ] Di perangkat lipat/tablet, mode landscape otomatis memakai layout lebar (PRD §11.4).

### 7.15 Engine Akses Lanjutan (Shizuku) — `F-15`

**User Story:** Sebagai pengguna tingkat lanjut, aku ingin membuka izin yang biasanya diblokir sistem, supaya desktop ini bisa melakukan hal yang tidak bisa dilakukan launcher biasa.

**Acceptance Criteria:**
- [ ] Integrasi **Shizuku** (opsional): bila app Shizuku terpasang dan diizinkan, SukiOS mendapat identitas shell (uid 2000).
- [ ] Aksi yang didukung: **force-resizable** app pihak ketiga (`settings put global force_resizable_activities`), **izin overlay otomatis** (appops `SYSTEM_ALERT_WINDOW`), **peluncuran app ke display tertentu** (`am start --display`), dan pembacaan diagnostik (`dumpsys display`, `wm size`).
- [ ] Setiap aksi menampilkan hasil apa adanya (kode keluar + keluaran/kesalahan), tanpa menyembunyikan kegagalan.
- [ ] **Tanpa Shizuku, seluruh fitur inti tetap berjalan.** Akses lanjutan tidak pernah menjadi syarat.
- [ ] Tidak ada penyimpanan kredensial; tidak ada perintah shell yang berasal dari input pengguna bebas (hanya perintah tetap + parameter yang divalidasi: id display numerik, nama paket dari PackageManager).
- [ ] Status Shizuku (terpasang/binder/versi/uid/izin) ditampilkan jelas di UI dan di laporan diagnostik.

### 7.16 Mode Android Go (Perangkat RAM Rendah) — `F-16`

**User Story:** Sebagai pengguna HP murah/Android Go, aku ingin SukiOS tetap jalan lancar walaupun sistem membatasi multitasking.

**Acceptance Criteria:**
- [ ] Mendeteksi `ActivityManager.isLowRamDevice` saat boot dan menandai perangkat sebagai **Mode Go**.
- [ ] Mode Go: batas **3 jendela** (dari 8), efek dekoratif dimatikan (wallpaper rata, tanpa lapisan tambahan).
- [ ] Mode Go menampilkan jalur alternatif yang konkret: split screen, overlay taskbar, dan (bila tersedia) Shizuku untuk force-resizable.
- [ ] Tidak ada fitur yang disembunyikan; hanya dibatasi dengan penjelasan yang terlihat pengguna.
- [ ] Platform Android tetap bisa memblokir multi-window di perangkat Go — SukiOS **tidak** menjanjikan hal yang tidak bisa dilakukan sistem.

### 7.17 Launcher Penuh (SukiOS Home) — `F-17`

**User Story:** Sebagai pengguna, aku ingin tombol Home membawa aku ke SukiOS, bukan ke launcher bawaan, supaya desktop ini benar-benar rumah di ponselku.

**Acceptance Criteria:**
- [ ] SukiOS terdaftar sebagai launcher (`HOME` + `DEFAULT` + `LAUNCHER`) dan bisa dijadikan default dari layar persiapan maupun Setelan.
- [ ] Desktop menampilkan aplikasi tersemat, ikon sistem, dan penanda jendela yang sedang terbuka.
- [ ] Taskbar memuat tombol mulai, daftar jendela, indikator status, jam, dan baterai.
- [ ] Start menu dengan pencarian dan sematkan; panel pintasan dengan saklar desktop, aksen, wallpaper, dan status akses lanjutan.
- [ ] Perangkat RAM rendah otomatis dibatasi 3 jendela dengan penjelasan yang terlihat.
- [ ] Semua kemampuan tetap berjalan tanpa Shizuku dan tanpa izin overlay; keduanya opsional.

### 7.18 Mesin Sendiri, Bukan Tambalan — `F-18`

**User Story:** Sebagai pemilik produk, aku ingin SukiOS memakai engine dan kontrak miliknya sendiri, supaya aku tidak bergantung pada API pihak lain yang bisa dihapus sewaktu-waktu.

**Acceptance Criteria:**
- [ ] Akses lanjutan memakai kontrak AIDL milik SukiOS (`ISukiShell`) dengan `destroy() = 16777114` dan UserService sendiri yang berjalan sebagai uid 2000/0.
- [ ] Tidak ada pemakaian API yang sudah dinyatakan akan dihapus (`Shizuku#newProcess`).
- [ ] Perintah selalu berbentuk daftar argumen tanpa shell parsing; nama paket dan id display divalidasi sebelum dikirim.
- [ ] Hasil perintah dilaporkan apa adanya (kode keluar, stdout, stderr) — tidak ada kegagalan yang disembunyikan.
- [ ] Aturan penyimpanan kelas untuk R8 tertulis di repo, agar kontrak ini tidak terhapus di build rilis.

## 8. Batasan Platform Android (Realitas Teknis)

Bagian ini penting supaya ekspektasi realistis. SukiOS **bukan** ROM — ia berjalan di atas Android sebagai aplikasi biasa (non-root).

| # | Fitur | Mekanisme Teknis | Permission / Akses | Risiko / Catatan |
|---|---|---|---|---|
| 1 | Jadi launcher/home | `<intent-filter>` `CATEGORY_HOME` + `DEFAULT`; minta role `ROLE_HOME` via `RoleManager` | Tidak ada (user memilih di Settings) | Wajib: user harus set manual sebagai home app |
| 2 | Daftar semua aplikasi | `PackageManager.queryIntentActivities()` / `getInstalledApplications()` | `QUERY_ALL_PACKAGES` | **Diizinkan Play untuk launcher**, tapi wajib isi *Permissions Declaration Form* + justifikasi. Alternatif: `<queries>` spesifik |
| 3 | Membuka aplikasi | `Intent` + `FLAG_ACTIVITY_NEW_TASK` | — | Normal |
| 4 | **Jendela mengambang app lain** | `DisplayManager.createVirtualDisplay()` → tampilkan via `ActivityView` (androidx) / `Presentation`, lalu launch app dengan `ActivityOptions.setLaunchDisplayId()` | — (beberapa device perlu `INTERNAL_SYSTEM_WINDOW` bila jadi system app) | **Paling kompleks.** Sebagian app (DRM seperti Netflix, game berat, app dengan `FLAG_SECURE`) akan tampil hitam atau menolak. Perlu fallback ke split-screen. |
| 5 | Desktop windowing native (Android 16+) | Delegasi ke API desktop windowing platform (`config_isDesktopModeSupported`, freeform) | — | Hanya aktif di perangkat/OEM yang mengaktifkan; **SukiOS harus punya fallback sendiri** |
| 6 | Widget di desktop | `AppWidgetHost` + `AppWidgetHostView` | `BIND_APPWIDGET` (diberikan saat app jadi launcher default) | Perlu jadi home app dulu; sebagian widget vendor tidak kompatibel |
| 7 | Notifikasi | `NotificationListenerService` | Special access (user grant manual via Settings) | Harus minta izin eksplisit; tidak bisa baca notifikasi sebelum diizinkan |
| 8 | Panel notifikasi sistem | `AccessibilityService` (expandStatusBar) **atau** render panel custom sendiri | Aksesibilitas (special) | Play ketat soal Accessibility API; **lebih aman pakai panel custom** + opsi aksesibilitas (opt-in) |
| 9 | Ganti wallpaper | `WallpaperManager.setBitmap()` | `SET_WALLPAPER` | Normal |
| 10 | Recent apps | `UsageStatsManager.queryUsageStats()` | `PACKAGE_USAGE_STATS` (special) | Data terbatas ~7 hari; user harus grant manual |
| 11 | Uninstall app | `Intent(ACTION_DELETE)` / `ACTION_UNINSTALL_PACKAGE` | `REQUEST_DELETE_PACKAGES` | Selalu munculkan dialog konfirmasi sistem |
| 12 | File explorer | `MediaStore` + Storage Access Framework (`ACTION_OPEN_DOCUMENT_TREE`) | `READ_MEDIA_*`, `MANAGE_EXTERNAL_STORAGE` (hindari — Play sangat ketat) | Gunakan SAF; jangan minta MANAGE_EXTERNAL_STORAGE kecuali benar-benar perlu |
| 13 | Shortcut ke app | `ShortcutManager` (static + dynamic) | — | Bisa pin shortcut ke desktop |
| 14 | Wallpaper hidup / video | `WallpaperService` | `SET_WALLPAPER` | Berat di baterai → opsional |
| 15 | Baca status sistem (baterai, sinyal) | `BatteryManager`, `ConnectivityManager`, `TelephonyManager` | Beberapa perlu `READ_PHONE_STATE` | Batasi agar tidak minta permission berlebihan |
| 16 | Kunci landscape + desktop penuh | `Activity.setRequestedOrientation()`, `WindowInsetsControllerCompat` | Tidak ada | Aman; bar sistem muncul sementara lewat geser tepi |
| 17 | Akses lanjutan (Shizuku) | `Shizuku.requestPermission()` + eksekusi shell (uid 2000) | Izin dari app Shizuku (user grant) | Opsional. Upstream menyiapkan penghapusan `newProcess` (ganti: UserService) — lihat §16.6 |
| 18 | Mode Go (RAM rendah) | `ActivityManager.isLowRamDevice` | Tidak ada | Platform Android **memblokir** multi-window di perangkat Go; SukiOS hanya menyediakan jalur alternatif |

### 8.1 Keputusan Arsitektur Kunci
> **Strategi dua lapis (Dual-Mode Windowing):**
> 1. **Lapis A — Native Desktop Windowing (Android 16+ / DeX / OEM yang mendukung):** delegasikan window management ke sistem, SukiOS hanya menyediakan *shell* (taskbar, start menu, desktop, theming). Paling stabil & paling mulus.
> 2. **Lapis B — Suki Window Engine (fallback universal, Android 10+):** `VirtualDisplay` + `ActivityView` untuk menjalankan app di dalam jendela milik SukiOS, dengan kontrol penuh atas chrome jendela.
>
> Sistem mendeteksi otomatis kemampuan perangkat saat pertama kali dijalankan ("capability probe") dan memilih lapis yang tepat. Ini yang membuat SukiOS **jalan di HP murah sekalipun**, beda dari DeX.

---

## 9. Arsitektur Teknis

### 9.1 Stack
| Lapisan | Teknologi | Alasan |
|---|---|---|
| Bahasa | **Kotlin** | Standar Android modern |
| UI | **Jetpack Compose** + Material 3 sebagai *base*, di-restyle jadi Suki Glass | Deklaratif, animasi mudah, window drag/resize lebih presisi |
| Arsitektur | **MVVM + Clean Architecture** (multi-module) | Testable, scalable |
| DI | **Hilt** | Standar |
| Async | **Coroutines + Flow** | Reaktif untuk update taskbar/search |
| Database | **Room** (layout desktop, pinned apps, settings, riwayat) | Lokal, cepat |
| DataStore | **Proto DataStore** untuk preferensi | Type-safe |
| Image | **Coil** | Icon & wallpaper loading |
| Blur/Glass | `RenderEffect` (API 31+) dengan **fallback gradient semi-transparan** untuk API < 31 | Efek glass tanpa mengorbankan kompatibilitas |

> **Catatan performa:** `RenderEffect` blur di Compose relatif mahal. Wajib ada opsi "Efek Transparansi: Nonaktif" untuk perangkat kelas bawah, dan blur harus di-cache (bukan di-render ulang tiap frame).

### 9.2 Struktur Modul
```
:app                      → entry point, MainActivity, HomeActivity
:core:designsystem        → Suki Glass tokens, komponen, tema, ikon
:core:model               → data class lintas fitur
:core:data                → repository, Room, DataStore
:core:common              → util, ext, dispatchers
:core:windowengine        → abstraksi windowing (Lapis A & B)
:feature:desktop          → wallpaper, ikon desktop, context menu
:feature:taskbar          → taskbar, tray, jam
:feature:startmenu        → start menu, search
:feature:window           → chrome jendela, snap, snap layouts, resize handle
:feature:taskview         → task view, desk
:feature:notifications    → notification center + quick settings
:feature:widgets          → host widget, widget bawaan
:feature:explorer         → file explorer
:feature:settings         → settings, personalisasi, backup layout
:feature:lockscreen       → lock screen
:feature:terminal         → (V2) terminal
```

### 9.3 Alur Window Engine (Lapis B)
```
User tap ikon app
      │
      ▼
[SukiWindowManager] create WindowModel(id, packageName, bounds, zIndex, state)
      │
      ▼
[VirtualDisplayController] DisplayManager.createVirtualDisplay(w, h, dpi, surface)
      │
      ▼
[ActivityView] attach ke surface → host VirtualDisplay di dalam Compose (AndroidView)
      │
      ▼
[LaunchAppUseCase] ActivityOptions.makeBasic().setLaunchDisplayId(vdId)
                   + FLAG_ACTIVITY_NEW_TASK + FLAG_ACTIVITY_MULTIPLE_TASK
      │
      ├── Sukses → jendela tampil & interaktif
      └── Gagal / app menolak (FLAG_SECURE, DRM) → fallback: tampilkan
          placeholder + tombol "Buka Fullscreen" / split-screen
```
Setiap jendela punya: `VirtualDisplay` + `ActivityView` + state di `WindowModel`. Saat app di-drag, **bukan** VirtualDisplay yang di-resize (mahal), tapi *View-nya* yang di-render ulang pada ukuran baru, dan `VirtualDisplay.resize()` di-debounce 100 ms setelah drag berhenti.

### 9.4 Manajemen State
- `SukiShellState` — sumber kebenaran tunggal: daftar jendela, jendela aktif, desk aktif, state taskbar/start menu.
- Diekspos sebagai `StateFlow` ke Compose.
- Persistensi layout desktop → Room, di-restore saat boot.
- **Setiap jendela adalah entri di back stack? Tidak** — back stack Android tidak cocok untuk window manager. Kita kelola sendiri, dan tombol Back meminimize jendela atas (bukan menutup app). Home = minimize semua + fokus desktop.

---

## 10. Non-Functional Requirements

| Kategori | Target | Cara Ukur |
|---|---|---|
| **Performa** | Drag/resize jendela ≥ 55 fps (mid-range), ≥ 60 fps (flagship) | Macrobenchmark + JankStats |
| **Cold start** | Launcher siap interaktif < 800 ms | Macrobenchmark |
| **RAM (idle)** | < 150 MB | Android Studio Profiler |
| **RAM (5 jendela)** | < 450 MB | Profiler |
| **APK size** | < 25 MB (tanpa aset berat) | App Bundle analysis |
| **Baterai** | Tidak muncul di daftar "pemakai baterai tinggi" setelah 8 jam idle | Battery Historian |
| **Stabilitas** | Crash-free rate > 99.5% | Play Console / Crashlytics |
| **Kompatibilitas** | Android 10 – 16+, layar 5"–13", RAM 3 GB+ | Device matrix test |
| **Responsivitas** | Setiap interaksi punya umpan balik visual < 100 ms | Review desain |
| **Bahasa & Aksesibilitas** | TalkBack berfungsi di semua elemen shell; contrast ratio ≥ 4.5:1 (AA) | Accessibility Scanner |
| **Privasi** | Zero network permission untuk fitur inti; tidak ada telemetri tanpa opt-in | Manifest audit |

---

## 11. Roadmap & Milestone

```
FASE 0 — Discovery & Design (2 minggu)          ← KAMU DI SINI
  • PRD, design system, mockup interaktif, naming/branding
  • Riset window engine (proof-of-concept VirtualDisplay)

FASE 1 — MVP "Desktop Alive" (6–8 minggu)
  M1  Project setup, arsitektur, design system di Compose
  M2  Shell: desktop + wallpaper + ikon (drag, rename, uninstall)
  M3  Taskbar + jam + tray
  M4  Start Menu + daftar aplikasi
  M5  Window Engine PoC → jendela real (drag, resize, min/max/close)
  M6  Task View + polish animasi + tes di 5 device
   Deliverable: APK alpha, bisa diset sebagai launcher default

FASE 2 — V1 "Produktif" (6–8 minggu)
  M7  Snap layouts + snap assist
  M8  Quick settings + notification center
  M9  Settings lengkap + personalisasi (accent, tema, wallpaper)
  M10 Widgets (bawaan + AppWidgetHost)
  M11 File explorer
  M12 Universal search
   Deliverable: Beta publik (Play Store internal testing)

FASE 3 — V2 "Ekosistem" (8–12 minggu)
  M13 Dukungan monitor eksternal + integrasi desktop windowing Android 16
  M14 Multi-desktop (Desk) + Suki Terminal
  M15 Suki Store / tema
   Deliverable: Rilis publik 1.0
```

**Estimasi realistis:** MVP dalam ~2 bulan untuk 1 developer berpengalaman (full-time). Window engine adalah item paling berisiko — jangan dijadwalkan setelah semua hal lain; buat PoC-nya **di minggu pertama Fase 1**.

---

## 12. Risiko & Mitigasi

| # | Risiko | Dampak | Probabilitas | Mitigasi |
|---|---|---|---|---|
| R1 | Window engine (VirtualDisplay) tidak jalan di sebagian HP | Tinggi | Sedang | PoC di awal; capability probe; fallback split-screen & fullscreen; uji di 8+ device berbeda |
| R2 | App sensitif (DRM/game) tampil hitam di jendela | Sedang | Tinggi | Deteksi `FLAG_SECURE`; beri pesan jelas + tombol "Buka Fullscreen" |
| R3 | Kebijakan Play menolak `QUERY_ALL_PACKAGES` | Tinggi | Rendah (launcher masuk kategori diizinkan) | Isi declaration form + video justifikasi + deskripsi store yang jelas; siapkan jalur alternatif dengan `<queries>` |
| R4 | Performa drop di HP kelas bawah | Sedang | Sedang | Mode Ringan (matikan blur, animasi disederhanakan), batas jumlah jendela, lazy loading |
| R5 | Baterai boros karena launcher selalu aktif | Sedang | Sedang | Hindari polling; pakai event-driven; batasi proses latar; audit dengan Battery Historian |
| R6 | **Klaim hukum dari pemilik merek lain** | Tinggi | Rendah (jika aturan §4.2 dipatuhi) | Patuhi trademark checklist, review aset, hindari nama/logo/ikon pihak lain, dokumentasi proses desain sendiri |
| R7 | Fragmentasi OEM (Xiaomi, Oppo, Vivo agresif mematikan proses) | Sedang | Tinggi | Panduan "agar SukiOS tidak dimatikan" per OEM; whitelist battery optimization |
| R8 | Scope creep (fitur terus ditambah) | Tinggi | Tinggi | Patuhi MoSCoW; fitur baru masuk backlog V2, bukan MVP |
| R9 | Widget pihak ketiga tidak kompatibel | Rendah | Sedang | Uji 20 widget populer; sediakan widget bawaan yang bagus |

---

## 13. Metrik Keberhasilan

| Metrik | Target 6 bulan setelah rilis |
|---|---|
| Instalasi | 50.000+ |
| Retensi D7 | > 35% |
| Retensi D30 | > 20% |
| Rasio pengguna yang set sebagai launcher default | > 60% |
| Rata-rata jendela dibuka per sesi | > 2.5 |
| Rating Play Store | ≥ 4.3 |
| Crash-free rate | > 99.5% |
| Ulasan yang menyebut "multitasking" secara positif | > 200 |

---

## 14. Pertanyaan Terbuka (perlu diputuskan)

1. **Model bisnis** — gratis + donasi? Freemium (fitur tema/window lanjutan berbayar)? Sekali bayar? Beriklan (dengan cara yang tidak mengganggu)?
2. **Target rilis pertama** — umum dari Play Store, atau closed beta dulu di komunitas (Reddit r/androidthemes, Telegram, X)?
3. **Fokus perangkat** — optimalisasi HP dulu, atau tablet dulu (di mana window management paling berguna)?
4. **Tim** — solo developer atau ada desainer/tester tambahan? Ini menentukan panjangnya roadmap.
5. **Terminal di V2** — apakah ini benar-benar dibutuhkan pengguna, atau hanya "keren tapi jarang dipakai"? (Saran: tunda sampai ada permintaan nyata.)
6. **Merek** — perlu pendaftaran merek "SukiOS" di DJKI? (Rekomendasi: ya, sebelum rilis komersial, kelas 9 & 42.)

---

## 15. Lampiran

- **A. Daftar Ikon yang Dibutuhkan (MVP):** Start, Search, Task View, Settings, Explorer, Notes, Terminal, Photos, Music, Browser, Store, Power, Wifi, Bluetooth, Battery, Volume, Bell, Minimize, Maximize, Restore, Close, Pin, Unpin, Sort, Refresh, Back, Forward, Up, Grid, List, Trash.
- **B. Cheat Sheet Shortcut:** lihat §7.13.
- **C. Peta Permission:** lihat §8.
- **D. Design System:** lihat `DESIGN.md`.
- **E. Mockup interaktif:** buka `mockup.html` di browser — start menu, jendela, snap, dan settings bisa dicoba langsung.

---

## 16. Addendum v1.1 — Temuan Riset Window Engine

**Tanggal:** 1 Oktober 2026 · **Status:** temuan riset (belum diverifikasi di device pengguna) · **Tindakan:** PoC `poc/` dibangun untuk membuktikannya

Riset ini dilakukan **sebelum** menulis kode window engine, karena satu temuan di bawah ini berpotensi membatalkan seluruh asumsi arsitektur §8.1. Ini hasilnya, apa adanya.

### 16.1 Empat temuan

**T1 — `android.app.ActivityView` bukan API publik.**
Halaman referensi resminya tidak ada di dokumentasi SDK (404), dan sumber AOSP menandainya `@hide`. Aktivitas yang boleh di-embed juga harus mendeklarasikan `android:resizeableActivity="true"` **dan** `android:allowEmbedded="true"` — dan mayoritas aplikasi umum tidak mendeklarasikan keduanya. Contoh pemakaian di AOSP (CarLauncher) berjalan karena app-nya **app sistem** (`/system/priv-app`, system uid).

**T2 — Platform membatasi peluncuran activity ke virtual display milik app.**
Commit AOSP *"Restrict launching activities on virtual displays"* (Bug 63094482):

> "If an app creates a Surface and a virtual display backed by that Surface, it can then launch activities and hijack their content. This CL restricts activities that can be launched to virtual displays created by apps only to those who set `allowEmbedded` attribute."

Dokumentasi recommended-practices AOSP melengkapinya: untuk virtual display yang tidak dimiliki sistem, **hanya activity dengan `allowEmbedded` yang diizinkan, dan pemanggil sebaiknya punya permission `ACTIVITY_EMBEDDING`** (kelas signature/privileged). Display privat yang dimiliki app lain → `SecurityException`.

**T3 — `setLaunchDisplayId()` diabaikan di perangkat tanpa feature terkait.**
Dari dokumentasi API: *"Setting launch display id will be ignored on devices that don't have `FEATURE_ACTIVITIES_ON_SECONDARY_DISPLAYS`."* Artinya jalur "buat VirtualDisplay sendiri → lempar app ke sana" tidak bisa diandalkan antar perangkat.

**T4 — Yang tetap bisa dilakukan app biasa (tanpa root, tanpa system) — inilah fondasi produk:**
1. **Menjalankan konten SukiOS sendiri di jendela nyata** — 100% bisa, tanpa batasan.
2. **Split screen** dua app berdampingan — didukung sistem.
3. **Freeform/desktop windowing** bila perangkat/OEM mengaktifkannya (tablet, desktop mode Android 16 QPR, DeX, sebagian ROM dengan dev-option) — kita bisa **delegasikan** ke platform.
4. **Overlay `TYPE_APPLICATION_OVERLAY`** — taskbar/title bar SukiOS mengapung di atas app yang berjalan fullscreen. Inilah yang dipakai launcher PC-style populer untuk memberi "rasa desktop" tanpa bisa benar-benar meletakkan app di dalam jendela.

### 16.2 Revisi strategi windowing (§8.1 versi baru)

| Lapis | Dipakai saat | Kemampuan | Status |
|---|---|---|---|
| **A — Platform Windowing** | Perangkat mendukung freeform / desktop windowing (Android 16+, tablet, DeX, OEM mode) | App pihak ketiga jadi jendela beneran, Snap, multi-instance | **Delegasi penuh ke sistem** |
| **B — SukiOS Shell + Overlay** | HP biasa (mayoritas pengguna) | App tetap fullscreen; SukiOS menyediakan taskbar overlay, title bar overlay, handoff split screen, dan jendela nyata untuk **app SukiOS sendiri** | **Jalur utama produk** |
| **C — Embed App Pihak Ketiga** | Hanya app dengan `allowEmbedded` (dan idealnya caller punya `ACTIVITY_EMBEDDING`) | App lain berjalan di dalam jendela SukiOS | **Eksperimen, bukan tulang punggung** |

> **Konsekuensi bisnis yang harus diterima:** SukiOS **tidak bisa** menjadi "Windows untuk semua app" di HP Android biasa tanpa root. Yang bisa dan tetap sangat berharga: SukiOS menjadi **desktop environment** — taskbar, start menu, penyusunan jendela, produktivitas, dan suite app-nya sendiri — yang di perangkat kelas atas otomatis "naik level" jadi jendela bebas untuk semua app.

### 16.3 Perubahan pada daftar risiko (§12)

| # | Sebelum | Sesudah |
|---|---|---|
| **R1** Window engine tidak jalan di sebagian HP — Probabilitas: **Sedang** | Probabilitas: **Tinggi (terkonfirmasi sebagian)** · Mitigasi: dual-mode A/B + overlay fallback + PoC sebagai *gate* sebelum investasi UI besar |
| **R1b (baru)** Ekspektasi pengguna: mengharapkan video review "semua app jadi jendela" | Mitigasi: posisikan di store listing & onboarding sebagai *desktop environment + your own apps in windows*, bukan *floating windows for all apps* |

### 16.4 Yang TIDAK berubah

- Visi, persona, dan bahasa desain Suki Glass.
- Seluruh spesifikasi UI/UX (DESIGN.md) — window manager, snap, task view tetap dibangun apa adanya, karena **dipakai** (Lapis A & untuk app SukiOS sendiri).
- Semua aturan trademark §4.2.
- Roadmap: Fase 1 tetap dimulai dengan window manager + shell — hanya **target konten jendela**-nya yang menyesuaikan perangkat.

### 16.5 Gerbang keputusan: PoC `poc/`

PoC berisi 5 lane uji + capability probe yang menghasilkan laporan teks dari perangkat nyata (lihat `poc/README.md` §3 untuk tabel keputusan). **Setelah laporan masuk, §8.1 dikunci** dan baru setelah itu implementasi shell dimulai.

### 16.6 Addendum v1.2 — Engine Akses Lanjutan (Shizuku) dan Mode Desktop

**Tanggal:** 2 Oktober 2026 · **Status:** terpasang di PoC v0.2.0 · **Verifikasi:** kompilasi CI (uji perangkat menyusul)

Kall meminta tiga hal: mode landscape/desktop penuh, engine pembuka izin untuk perangkat Android Go, dan penegasan aturan visual anti-neon. Dua yang pertama mengubah sebagian kalkulasi §16.2.

**a. Yang berubah dengan Shizuku (F-15)**

Dengan identitas shell (uid 2000), beberapa pintu yang tertutup untuk app biasa menjadi terbuka:

| Kemampuan | Untuk app biasa | Dengan Shizuku (uid 2000) |
|---|---|---|
| `settings put global force_resizable_activities` | Ditolak (butuh `WRITE_SECURE_SETTINGS`) | **Berhasil** — app pihak ketiga jadi boleh di-resize |
| `appops set <pkg> SYSTEM_ALERT_WINDOW allow` | Hanya lewat UI pengaturan | **Berhasil** — overlay tanpa navigasi manual |
| `am start --display N` | Dibatasi (app harus `allowEmbedded`) | **Perlu diuji** — ini yang diukur LANE 6 di PoC |
| `dumpsys display`, `wm size` | Ditolak | **Berhasil** — diagnostik lengkap |

Artinya, **Lapis C (embed app pihak ketiga)** di §16.2 naik statusnya dari "eksperimen" menjadi **"mungkin, pada perangkat yang mengaktifkan Shizuku"**. Namun tetap bukan tulang punggung produk, karena:

1. Shizuku adalah app pihak ketiga yang harus dipasang dan diaktifkan pengguna (butuh ADB atau wireless debugging). Bukan jalur untuk pengguna umum.
2. Upstream Shizuku **menyiapkan penghapusan `newProcess`**; penggantinya UserService (kode sendiri berjalan di proses shell). Migrasi ini pekerjaan nyata, bukan sekadar naik versi.
3. Di perangkat yang belum diaktifkan Shizuku, semua tetap kembali ke Lapis A/B.

**Posisi produk:** Akses Lanjutan = **mode opsional untuk pengguna tingkat lanjut**, bukan fitur inti. Semua fitur inti tetap berjalan tanpa Shizuku. Ini juga aman dari sisi kebijakan Play Store: aplikasi tidak mewajibkan Shizuku dan tidak meminta izin berlebih untuk dirinya sendiri.

**b. Mode Go (F-16)**

Android Go / perangkat RAM rendah diblokir platform dari multi-window. SukiOS tidak bisa (dan tidak boleh) menjanjikan yang sebaliknya. Yang dilakukan: mendeteksi kelas perangkat, menurunkan batas jendela ke 3, mematikan efek dekoratif, dan menampilkan jalur alternatif yang benar-benar bisa dipakai (split screen, overlay, force-resizable via Shizuku).

**c. Aturan visual: matte, tanpa neon (DESIGN.md §1.3)**

Gradien Aurora v1.0 (ungu-teal) dihapus dari kode, mockup, aset logo, dan dokumen. Penggantinya: palet turun-saturasi (steel `#6E8CA8`, sage `#7B9E8C`, clay `#A08F76`) dengan bayangan netral. Emoji tidak lagi dipakai sebagai ikon antarmuka (diganti badge huruf di File Explorer).

---

*Dokumen ini hidup — akan diperbarui setiap ada keputusan baru. Versioning: 1.0 → 1.1 (temuan riset window engine) → 1.2 (Shizuku + mode desktop) → 1.3 (setelah hasil uji perangkat masuk).*
