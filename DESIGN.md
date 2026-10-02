# SukiOS — Design System & Konsep Visual
## "Suki Glass" v2.0 — Aurora (kembali bercahaya, tetap terkendali)

| | |
|---|---|
| **Produk** | SukiOS |
| **Nama Bahasa Desain** | Suki Glass |
| **Versi** | 2.0 |
| **Tanggal** | 2 Oktober 2026 |
| **Status** | Sesuai kode di `main` (`SukiTheme.kt`, `SukiFonts.kt`, `SukiGlyph.kt`) |

---

## 1. Filsafat Desain

### 1.1 Tiga Kata
> **Hangat · Lapang · Presisi**

- **Hangat** — SukiOS tidak dingin dan korporat. Gradien aurora, sudut membulat, dan bahasa mikro yang ramah ("Halo, selamat pagi ") membuat OS ini terasa seperti milikmu, bukan milik perusahaan.
- **Lapang** — desktop adalah ruang kerja. Elemen tidak berdesakan; ada napas, ada ruang kosong, ada hirarki.
- **Presisi** — setiap sudut, jarak, dan durasi animasi punya alasan. Tidak ada elemen "kira-kira".

### 1.2 Metafora
SukiOS adalah **meja kerja**, bukan **layar HP yang diperbesar**.
- Desktop = meja. Ada ruang kosong, ada barang yang diletakkan.
- Taskbar = laci bawah meja yang selalu bisa dijangkau.
- Start menu = rak penyimpanan semua alat.
- Jendela = lembar kerja yang bisa ditumpuk, digeser, dan disimpan.

### 1.3 Aturan Visual Wajib — Aurora yang Terkendali

Revisi 2026-10-02 (permintaan kall setelah uji perangkat v0.3.1-alpha): aturan matte v1.1
**dicabut**. Tampilan matte terbaca datar dan murah; identitas SukiOS adalah cahaya aurora di
atas bidang gelap. Yang tidak berubah: cahaya itu harus melayani keterbacaan, bukan menutupinya.

**DILARANG:**
- Emoji sebagai ikon antarmuka. Ikon hanya dari `SukiGlyph` (path vektor sendiri).
- Menyalin nama, logo, ikon, suara, atau trade dress sistem operasi lain.
- Warna aksen murni untuk teks kecil. Teks aksen memakai turunan `text` (lihat §4.2).
- Bayangan berwarna aksen, bloom, grid siber, scanline, efek holografik, kilau logam.
- Klaim efek yang tidak benar-benar dipakai (misalnya menyebut "blur" padahal tidak ada).

**DIWAJIBKAN:**
- Setiap pasangan teks/latar lolos WCAG AA 4,5:1 dan **dibuktikan di CI** (`ThemeContrastTest`
  membaca konstanta ARGB di `SukiTheme.kt`). Elemen non-teks minimal 3:1.
- Teks di atas isian aksen memakai pasangan `fill`/`on` dari preset, bukan tebakan putih/hitam.
- Cahaya aurora datang dari gradien dan cahaya radial berdiameter besar dengan alpha rendah,
  bukan dari garis menyala.
- Pemisahan bidang: garis rambut putih 7-20% alpha + perbedaan nada permukaan.
- Bayangan netral hitam (`ambient 0x66000000`, `spot 0xB3000000`), bukan warna.

**Uji cepat:** ubah screenshot menjadi grayscale. Hierarki harus tetap terbaca. Kalau hilangnya
hierarki terjadi, yang menopang desain adalah warnanya — perbaiki nada permukaannya, bukan
saturasinya.

---

### 1.4 Catatan Implementasi (v2.0)

- SukiOS **tidak memakai tema Material**. Material3 hanya dipakai untuk komposisi teks (`Text`),
  karena Compose 1.7 menyembunyikan konstruktor `TextStyle` bergaya lama. Seluruh warna, bentuk,
  jarak, dan gerak berasal dari `SukiTheme.kt` + `SukiKit.kt`.
- Nilai warna ditulis sebagai `const val` ARGB (`Long`) supaya uji kontras membacanya **tanpa
  memuat Compose** di JVM. Objek `Color` (`SBg`, `SText`, ...) hanyalah pembungkusnya.
- Tidak ada blur latar nyata. `Modifier.blur`/`RenderEffect` butuh API 31 sedangkan `minSdk` 29,
  dan biayanya besar di layar penuh. "Kaca" SukiOS = gradien + alpha + garis rambut (§4.3).
- Ikon antarmuka digambar sebagai vektor oleh `SukiGlyph`; datanya di `SukiGlyphData.kt` yang
  **dihasilkan** `tools/gen_glyphs.py` dan dicocokkan dengan generator itu oleh `GlyphDataTest`.
  Jadi ikon tidak boleh disunting tangan.
- Font dipaketkan sebagai berkas (`res/font`, dipangkas ke aksara Latin oleh `tools/mkfonts.py`),
  lisensi OFL disimpan di `app/src/main/assets/licenses/`.

---

## 2. Identitas Brand

### 2.1 Nama
**SukiOS** — dari "suki" (disukai/dicintai) + "OS". Nama ramah, mudah diucapkan dalam bahasa apa pun, dan 100% original.

Tagline kandidat:
- *"Desktop-mu, di kantongmu."*
- *"Android yang kerja seperti komputer."*
- *"Your pocket desktop."* (EN)

### 2.2 Logo (konsep)

**Mark:** Sebuah *squircle* (kotak membulat ekstrem) berwarna aksen solid, di dalamnya ada huruf **S** yang dibentuk dari dua kurva dengan ujung membulat — terasa seperti gerakan, seperti jendela yang sedang terbuka. Tanpa gradien, tanpa kilau.

```
Bentuk dasar:  squircle 1024×1024, radius = 34% (≈348)
Warna:         aksen solid #6E8CA8 (versi lama memakai gradien; diganti karena terbaca neon)
Huruf:         path kustom "S" berujung bulat (stroke 96, round cap)
Bayangan:      inner highlight 8% putih di atas (kesan kaca)
Clear space:   minimal 12% dari sisi mark di semua sisi
Ukuran min:    24 px (favicon) — jika di bawah 32 px gunakan versi tanpa gradien
```

**Versi logo:**
| Varian | Penggunaan |
|---|---|
| Full color (gradien Aurora) | Default, splash screen, store listing |
| Mono putih | Di atas foto/warna terang, di dalam taskbar |
| Mono hitam | Dokumen, print |
| App icon adaptif | Foreground = mark S putih, Background = gradien ungu-biru-teal (`res/drawable/ic_launcher_background.xml`) |

> File SVG: `assets/logo.svg`

### 2.3 Yang Tidak Boleh (Visual Brand Safety)
-  Tidak ada perspektif 4 kotak sejajar (ciri khas merek lain).
-  Tidak memakai biru korporat #0078D4 atau biru khas OS lain sebagai warna utama.
-  Tidak memakai wallpaper, ikon, suara, atau font milik sistem operasi lain.
-  Tidak menyalin bentuk tombol kontrol jendela sistem lain (kita pakai gaya sendiri: pill dengan hover bulat).
-  Boleh mengadopsi **pola interaksi** standar industri (taskbar di bawah, start di kiri taskbar, jendela dengan title bar) — ini konvensi industri, bukan merek dagang.

---

## 3. Prinsip Desain (Design Principles)

| # | Prinsip | Implikasi Praktis |
|---|---|---|
| **1** | Konten dulu, chrome kemudian | Title bar tipis (40 dp), shadow halus, tidak ada border tebal. |
| **2** | Satu aksen, satu makna | Accent color hanya untuk: elemen aktif, tombol utama, indikator fokus. Bukan untuk dekorasi sembarangan. |
| **3** | Gerak menjelaskan, bukan menghias | Setiap animasi menjelaskan hubungan ruang (dari mana, ke mana). |
| **4** | Semua bisa dijangkau satu tangan & satu kursor | Target sentuh min 44 dp; elemen penting di area jangkauan. |
| **5** | Elegan saat lambat, instan saat cepat | Animasi mudah di-skip: interaksi user memotong animasi, tidak menunggu. |
| **6** | Turun kualitas, bukan turun fungsi | Di HP lemah, cahaya wallpaper dan animasi dikurangi — fungsinya tetap sama. |

---

## 4. Sistem Warna

Sumber kebenaran: `SukiTheme.kt` (konstanta ARGB). Bagian ini salinannya; bila berbeda, kode yang menang.

### 4.1 Palet Inti

**Bidang (gelap kebiruan)**
| Token | Hex | Konstanta | Penggunaan |
|---|---|---|---|
| `bg/base` | `#0B0D12` | `C_BG` | Latar desktop paling belakang |
| `bg/surface` | `#12151D` | `C_SURFACE` | Isi jendela, panel |
| `bg/sheet` | `#161B27` | `C_SHEET` | Lembar/menu di atas panel |
| `bg/elevated` | `#1A1F2B` | `C_ELEVATED` | Kartu, ubin, baris terangkat |
| `bg/chrome` | `#1F2532` | `C_CHROME` | Bilah judul, taskbar |
| `bg/overlay` | `#242A38` | `C_OVERLAY` | Tooltip, popover, kontrol melayang |

**Teks** (semuanya lolos AA 4,5:1 di **semua** bidang di atas — dibuktikan `ThemeContrastTest`)
| Token | Hex | Konstanta | Penggunaan |
|---|---|---|---|
| `text/primary` | `#F2F4F8` | `C_TEXT` | Judul, isi utama |
| `text/secondary` | `#B4BCCC` | `C_DIM` | Deskripsi, metadata |
| `text/tertiary` | `#8D96AB` | `C_FAINT` | Keterangan halus (rasio terendah, tetap >= 4,5) |
| `text/ink` | `#0B0D12` | `C_INK` | Teks di atas isian aksen terang (teal, biru, amber, hijau, pink) |

**Cahaya Aurora dan status**
| Peran | Hex | Konstanta | Catatan |
|---|---|---|---|
| Violet | `#7C5CFF` | `C_VIOLET` | Cahaya utama; untuk grafis/isi, bukan teks kecil |
| Violet fill | `#6C4BF2` | `C_VIOLET_FILL` | Isi tombol aksen Aurora |
| Violet text | `#A89BFF` | `C_VIOLET_TEXT` | Satu-satunya bentuk ungu yang boleh jadi teks |
| Teal | `#35D0BA` | `C_TEAL` | Cahaya kedua |
| Pink | `#FF7AB6` | `C_PINK` | Cahaya ketiga (wallpaper, aksen Mekar) |
| Biru / info | `#5B9DFF` | `C_BLUE` | Info, tautan |
| Amber / warning | `#FFB44C` | `C_AMBER` | Peringatan |
| Hijau / success | `#3FD08A` | `C_GREEN` | Berhasil, aktif |
| Merah / danger | `#FF5F56` | `C_RED` | Grafis bahaya, tombol tutup |
| Merah text | `#FF8A82` | `C_RED_TEXT` | Teks bahaya (merah murni gagal AA untuk teks) |

**Garis, scrim**
| Token | Nilai | Konstanta |
|---|---|---|
| `stroke/hairline` | `#12FFFFFF` (7%) | `SLineSoft` |
| `stroke/subtle` | `#1FFFFFFF` (12%) | `SLine` |
| `stroke/strong` | `#33FFFFFF` (20%) | `SLineStrong` |
| `scrim` | `#8C05060A` (55%) | `SScrim` |

Tema terang tidak dipakai di v2.0. Aurora adalah identitas gelap; tema terang ditunda sampai
ada permintaan nyata (lihat `IDEAS.md`).

### 4.2 Accent Color

Satu aksen = **dua cahaya** (`main`, `second`, dipakai untuk gradien) + **tiga turunan keterbacaan**:

```
fill  → isi tombol/chip aktif
on    → teks di atas fill (sudah dihitung kontrasnya, jangan diganti putih/hitam sendiri)
text  → warna teks aksen di atas bidang gelap (selalu >= 4,5:1)
```

**6 preset** (semua lulus `ThemeContrastTest`):

| id | Label | main | second | on |
|---|---|---|---|---|
| `aurora` | Aurora | `#7C5CFF` | `#35D0BA` | putih |
| `laguna` | Laguna | `#35D0BA` | `#5B9DFF` | ink |
| `mekar` | Mekar | `#FF7AB6` | `#7C5CFF` | ink |
| `azur` | Azur | `#5B9DFF` | `#35D0BA` | ink |
| `bara` | Bara | `#FFB44C` | `#FF7AB6` | ink |
| `mint` | Mint | `#3FD08A` | `#35D0BA` | ink |

Aturan: aksen adalah **cahaya**, bukan bidang. Maksimal ~8% area layar sebagai isian; sisanya
muncul sebagai gradien bilah judul, garis fokus, dan cahaya wallpaper.

### 4.3 Material & Efek

| Nama | Implementasi nyata di kode | Penggunaan |
|---|---|---|
| **Suki Glass** | gradien linear `main` 70% -> `second` 40% + garis rambut putih + bayangan netral | Bilah judul jendela aktif |
| **Suki Panel** | `surface`/`elevated` alpha tinggi + `stroke/subtle` 1 dp + radius 12-16 dp | Panel, start menu, quick panel |
| **Suki Shadow** | `Modifier.shadow`: jendela fokus 30 dp, tidak fokus 12 dp; ambient `0x66000000`, spot `0xB3000000`/`0xCC000000` | Jendela, kartu, menu |
| **Aurora Veil** | cahaya radial besar (alpha 0,20-0,58) di atas gradien dasar dua nada | Wallpaper; 6 preset |
| **Scrim** | `#8C05060A` | Di belakang dialog/menu terbuka |

> **Aturan jujur:** tidak ada blur latar. Jangan menulis "acrylic/mica/blur" di dokumen atau
> materi pemasaran. Yang ada adalah gradien, alpha, dan garis rambut — dan itu cukup untuk
> kesan kaca di atas bidang gelap.

**Radius** (`SukiTheme.kt`): `RADIUS_SM` 8 · `RADIUS_MD` 12 · `RADIUS_WIN` 14 · `RADIUS_LG` 16 · `RADIUS_PILL` 999.
**Ukuran tetap:** `TASKBAR_DP` 54 (area yang dipesan taskbar, dipakai mesin jendela), `TASKBAR_BAR_DP` 48, `ICON_DP` 48.
**Gerak:** `MOTION_INSTANT` 90 · `MOTION_FAST` 150 · `MOTION_BASE` 220 · `MOTION_SLOW` 320 ms, kurva `EaseOut` = `cubic-bezier(.2,0,0,1)`.

---

## 5. Tipografi

**Font dipaketkan di aplikasi** (`res/font`, lisensi SIL OFL 1.1, salinan lisensi di
`app/src/main/assets/licenses/`; dipangkas ke aksara Latin oleh `tools/mkfonts.py`):

| Keluarga di kode | Berkas | Penggunaan |
|---|---|---|
| `SukiSans` | Inter regular/medium/semibold/bold | Seluruh teks antarmuka |
| `SukiBrand` | Plus Jakarta Sans bold/extrabold | Judul pendek, nama merek |
| `SukiClockFont` | Inter Display Light | Jam besar di desktop |
| `SukiMono` | monospace sistem | Terminal, angka teknis |

Tidak ada fallback ke font sistem untuk teks produk: bila berkas font gagal dimuat, itu cacat
yang harus terlihat, bukan diam-diam berubah tampilan.

| Token | Ukuran / Line-height / Weight | Penggunaan |
|---|---|---|
| `display` | 34 / 40 / 700 | Jam di lock screen |
| `title-1` | 24 / 30 / 650 | Judul halaman settings |
| `title-2` | 18 / 24 / 600 | Judul jendela, header start menu |
| `title-3` | 15 / 20 / 600 | Label section, nama app di grid |
| `body` | 14 / 20 / 450 | Isi teks umum |
| `body-strong` | 14 / 20 / 600 | Nama file, item terpilih |
| `caption` | 12 / 16 / 500 | Metadata, jam di taskbar |
| `micro` | 10.5 / 14 / 600 | Badge, label ikon desktop |
| `mono` | 13 / 20 / 450 | Terminal, angka di kalkulator |

**Aturan:**
- Letter-spacing negatif halus untuk display/title (-0.02em) agar terasa modern.
- Skala di atas adalah acuan; yang mengikat di kode adalah ukuran yang dipakai `SukiKit.kt`.
- Maksimal **3 level tipografi** dalam satu komponen.
- Angka di jam & kalkulator pakai *tabular figures* agar tidak bergoyang.

---

## 6. Ikonografi

| Aspek | Aturan |
|---|---|
| **Grid** | 24 × 24 dp, live area 20 × 20, padding 2 |
| **Stroke** | 1.75 dp, ujung & sambungan *round* |
| **Sudut** | Radius konsisten; tidak ada sudut tajam |
| **Gaya** | Outline (default) · Filled (state aktif) |
| **Ikon app desktop** | 48 dp, gaya *squircle* dengan gradien unik per kategori app |
| **Warna** | Mengikuti `text/primary`; berubah accent saat aktif/fokus |
| **Sumber** | Dibuat sendiri, atau Material Symbols (Apache 2.0) yang di-restyle (stroke & radius disesuaikan) |
| **Dilarang** | Meniru ikon sistem operasi lain secara identik |

**Kategori warna ikon bawaan:**
```
Files      → Aurora Violet
Notes      → Aurora Sun
Photos     → Aurora Dawn
Music      → Aurora Teal
Browser    → Sky
Settings   → Slate
Terminal   → Graphite
Store      → Emerald
```

---

## 7. Bentuk, Jarak & Elevasi

### 7.1 Radius
| Token | Nilai | Penggunaan |
|---|---|---|
| `radius/xs` | 4 dp | Chip kecil, badge |
| `radius/sm` | 8 dp | Tombol, input |
| `radius/md` | 12 dp | Kartu, menu item |
| `radius/lg` | 16 dp | Panel, jendela |
| `radius/xl` | 24 dp | Start menu, modal besar |
| `radius/full` | 999 dp | Pill search, avatar |

### 7.2 Spacing (skala 4 dp)
`4 · 8 · 12 · 16 · 20 · 24 · 32 · 40 · 48 · 64`
- Padding internal jendela: **16 dp**
- Gap antar ikon desktop: **16 dp** (horizontal), **20 dp** (vertikal)
- Padding taskbar: **8 dp** vertikal, **12 dp** horizontal

### 7.3 Elevasi
| Level | Shadow | Penggunaan |
|---|---|---|
| `e0` | none | Desktop, taskbar (flat) |
| `e1` | `0 2 8 rgba(0,0,0,.18)` | Kartu, chip |
| `e2` | `0 8 24 rgba(0,0,0,.28)` | Start menu, quick settings |
| `e3` | `0 16 40 rgba(0,0,0,.38)` | Jendela aktif |
| `e4` | `0 24 64 rgba(0,0,0,.48)` | Modal, dialog konfirmasi |

---

## 8. Gerak (Motion)

### 8.1 Durasi & Easing
| Nama | Durasi | Easing | Penggunaan |
|---|---|---|---|
| `motion/instant` | 90 ms | linear | Hover, ripple |
| `motion/fast` | 150 ms | `cubic-bezier(0.2, 0, 0, 1)` | Toggle, chip, state change |
| `motion/base` | 220 ms | `cubic-bezier(0.2, 0, 0, 1)` | Buka start menu, snap |
| `motion/slow` | 320 ms | `cubic-bezier(0.05, 0.7, 0.1, 1)` | Maximize, restore window |
| `motion/decelerate` | 400 ms | `cubic-bezier(0, 0, 0, 1)` | Task View zoom-out |

### 8.2 Pola Gerak Kunci
| Interaksi | Animasi |
|---|---|
| **Buka start menu** | Fade 0→1 + slide up 24 dp + scale 0.96→1, origin dari tombol Start |
| **Minimize jendela** | Jendela scale ke 0.2 & meluncur ke ikon taskbar (posisi ikon sebagai target) |
| **Restore** | Kebalikan minimize, 320 ms |
| **Maximize** | Bounds mengembang ke layar penuh + konten cross-fade (konten tidak di-scale agar tetap tajam) |
| **Snap kiri/kanan** | Bounds menyusut mulus 220 ms + indikator snap memudar |
| **Task View** | Desktop zoom-out 0.85 + kartu jendela naik bertahap (stagger 30 ms) |
| **Drag jendela** | Jendela mengikuti kursor 1:1 (tanpa lag), shadow bertambah, opacity 0.96 |
| **Snap hint** | Overlay muncul saat kursor masuk 24 dp dari tepi, dengan fade 90 ms |
| **Notifikasi toast** | Slide in dari kanan + fade, auto-dismiss 5 detik (pause saat hover) |

### 8.3 Gerak Lanjutan (V2)
- **Spring physics** untuk resize jendela (overshoot halus 3%).
- **Parallax wallpaper** saat drag jendela (geser 2% berlawanan arah).
- **Window flip** (Alt+Tab) dengan kartu melengkung.

---

## 9. Suara (Sound Design)

| Event | Suara | Catatan |
|---|---|---|
| Boot / splash selesai | Chime 3 nada naik (C–E–G), 1.2 detik | **Komposisi original**, bukan suara sistem lain |
| Buka start menu | "Tik" lembut (sine 1200 Hz, 40 ms) | Halus, tidak mengganggu |
| Snap jendela | "Klik" rendah (200 Hz, 60 ms) | Umpan balik mekanis |
| Notifikasi | Dua nada lembut | Volume mengikuti sistem |
| Error / gagal | Nada turun pendek (F→C) | Tidak pernah kasar/distorsi |
| Kunci layar | Nada memudar | |

Prinsip: **semua suara bisa dimatikan**, default hanya aktif untuk notifikasi & boot.

---

## 10. Komponen Inti (Spesifikasi Ukuran)

> **Catatan v2.0:** bagian 10-14 adalah **spesifikasi target**, bukan keadaan kode. Yang sudah
> benar-benar ada di `main` dicatat di §17 (cheat sheet). Bila angka di bawah dan di kode
> berbeda, kode yang menang dan bagian ini yang harus diperbarui.


### 10.1 Taskbar ("Suki Bar")
```
Tinggi              48 dp (default) · 40 dp (compact) · 64 dp (besar)
Padding horizontal  12 dp
Radius (floating)   16 dp  [mode floating: taskbar terpisah dari tepi, margin 8 dp]
Background          Suki Panel (chrome) + garis rambut stroke/subtle
```
| Zona | Isi | Lebar |
|---|---|---|
| Kiri | Tombol Start (40×40) | auto |
| Kiri | Search pill ("Cari aplikasi, file, pengaturan…") | 220 dp (collapse jadi ikon di layar < 360 dp) |
| Kiri | Task View (40×40) | auto |
| Tengah | App pinned + app berjalan (ikon 32 dp, gap 4 dp) | fleksibel |
| Kanan | Tray: wifi · volume · baterai · jam/tanggal · bell | auto |

**Indikator app berjalan:** pill 16 × 3 dp, accent, 4 dp di bawah ikon. Saat app aktif → pill melebar jadi 24 dp.
**Hover:** background `stroke/subtle` radius 8 dp (sama besar di semua ikon).

### 10.2 Start Menu ("Suki Start")
```
Ukuran            640 × 640 dp (HP: 100% lebar - 16 dp, tinggi maks 80% layar)
Posisi            center horizontal, 12 dp di atas taskbar
Radius            24 dp
Background        Suki Panel (sheet/surface + garis rambut putih 12%)
Shadow            e2
Animasi           motion/base, origin dari tombol Start
```
| Bagian | Tinggi | Isi |
|---|---|---|
| Search field | 44 dp | Input pencarian + ikon |
| Tab | 36 dp | Disematkan · Semua Aplikasi · Terakhir |
| Grid pinned | fleksibel | 6 × 4 (HP), 8 × 5 (tablet), ikon 32 dp + label 12 dp |
| Baris footer | 60 dp | Avatar + nama · tombol Power (40×40) |

### 10.3 Jendela (Window)
```
Title bar          40 dp
Radius             16 dp (maximized: 0)
Border             1 dp stroke/strong (tidak aktif) · 1 px accent 40% (aktif)
Shadow             e3 (aktif) · e1 (tidak aktif, opacity lebih rendah)
Padding konten     16 dp
Min size           320 × 240 dp
Resize handle      8 dp di 4 sisi + 16×16 dp di 4 sudut (zona tak terlihat, kursor berubah)
```
| Elemen title bar | Spesifikasi |
|---|---|
| Ikon app | 16 dp, margin kiri 12 dp |
| Judul | `title-3`, maks 1 baris, ellipsis |
| Zona drag | seluruh title bar kecuali tombol |
| **Tombol kontrol** | 32 × 32, radius 8, gap 4, di kanan. Ikon 14 dp. |
| Hover minimize/maximize | bg `stroke/subtle` |
| Hover close | bg `danger` + ikon putih |
| Snap Layouts trigger | hover di tombol maximize selama 300 ms → popup 4 tata letak |

**Desain khas SukiOS:** tombol kontrol berbentuk *pill terpisah* dengan jarak antar tombol, bukan blok menyatu di sudut seperti sistem operasi lain.

### 10.4 Kartu & Tombol
| Komponen | Ukuran | Detail |
|---|---|---|
| Tombol primer | tinggi 36 dp, radius 8, padding 16 | Background accent, teks on-accent |
| Tombol sekunder | tinggi 36 dp, radius 8 | Border 1px stroke/strong, teks primary |
| Tombol ikon | 40 × 40, radius 8 | Hover bg subtle |
| Kartu | radius 12, padding 16 | e1, hover naik ke e2 |
| Chip | tinggi 28, radius full, padding 12 | 12 dp teks |
| Input text | tinggi 36, radius 8 | Border focus 2px accent |
| Toggle switch | 44 × 24 | Thumb 20, animasi motion/fast |
| Slider | track 4 dp, thumb 16 dp | Track terisi accent |
| Toast | radius 12, padding 16, lebar maks 360 | Kanan bawah, e4 |

---

## 11. Layout & Grid

### 11.1 Zona Desktop
```
┌──────────────────────────────────────────────┐
│  [Area Desktop]                              │  ← ikon grid, widget
│                                              │
│              (jendela mengambang)            │
├──────────────────────────────────────────────┤
│  Start  Search  ▢    [app][app]       12:30│  ← Suki Bar 48 dp
└──────────────────────────────────────────────┘
```

### 11.2 Grid Ikon Desktop
| Perangkat | Kolom × Baris | Ukuran sel | Ikon |
|---|---|---|---|
| HP portrait | 5 × 6 | 80 × 100 dp | 48 dp |
| HP landscape | 8 × 4 | 80 × 100 dp | 48 dp |
| Tablet 10" | 8 × 6 | 96 × 112 dp | 56 dp |
| Tablet landscape | 10 × 6 | 96 × 112 dp | 56 dp |

### 11.3 Area Kerja Jendela
- Area desktop untuk jendela = ukuran layar **dikurangi** tinggi taskbar (48 dp) dan status bar (24 dp).
- **Inset aman:** jendela tidak boleh menutupi taskbar atau punch-hole kamera.

### 11.4 Breakpoint
| Kelas lebar | Rentang | Perilaku |
|---|---|---|
| Compact | < 600 dp | Start menu full width, taskbar ikon lebih sedikit, window default maximize, snap 2 zona |
| Medium | 600–839 dp | Start menu multi-kolom, snap 3 zona |
| Expanded | ≥ 840 dp | Taskbar lebih tinggi, start menu 8 kolom, snap layout 4 zona, multi-desktop aktif |

---

## 12. Pola Interaksi

### 12.1 Mouse & Kursor
| Aksi | Perilaku |
|---|---|
| Hover ikon desktop | Border glow + label muncul jika tersembunyi |
| Hover taskbar 500 ms | Tooltip nama app + preview jendela |
| Klik kanan desktop | Menu: Ganti Wallpaper · Ikon Baru · Susun Otomatis · Refresh · Pengaturan Desktop |
| Klik kanan ikon | Menu: Buka · Buka di Jendela Baru · Pin ke Taskbar · Ganti Nama · Properti · Uninstall |
| Klik kanan jendela title bar | Menu: Restore · Minimize · Maximize · Snap kiri · Snap kanan · Selalu di Atas · Tutup |
| Double-click title bar | Maximize / Restore |
| Scroll di desktop | Pindah Desk (multi-desktop) |
| Drag file dari Explorer ke desktop | Membuat shortcut |

### 12.2 Sentuh
| Aksi | Perilaku |
|---|---|
| Long press desktop | Context menu |
| Long press ikon | Menu + mode susun (ikon bergoyang, bisa drag) |
| Drag dari bawah ke atas | Buka drawer semua aplikasi (opsional) |
| Swipe dari tepi atas kanan | Quick Settings |
| Swipe dari tepi atas kiri | Notification Center |
| 3 jari swipe ke atas | Task View (bisa diatur) |
| Pinch di desktop | Atur kerapatan grid ikon |

### 12.3 Keyboard
`Win/Super` Start · `Win+D` Desktop · `Win+E` Explorer · `Alt+Tab` Task View · `Alt+F4` Tutup jendela · `Ctrl+Space` Search · `Win+←/→` Snap kiri/kanan · `Win+↑/↓` Maximize/Minimize · `Esc` Batal/tutup panel · `F2` Rename

---

## 13. Aksesibilitas

| Aspek | Target |
|---|---|
| Kontras teks | ≥ 4.5:1 (AA); teks besar ≥ 3:1 |
| Target sentuh | ≥ 44 × 44 dp |
| TalkBack | Semua elemen shell punya label & urutan fokus logis; jendela diumumkan saat fokus |
| Fokus keyboard | Ring fokus 2 px accent, selalu terlihat, urutan tab masuk akal |
| Skala teks | Dukungan 85% – 200% tanpa layout rusak |
| Mode gerak berkurang | Hormati "Remove animations" → semua transisi jadi fade 0 ms |
| Buta warna | Indikator app berjalan tidak hanya warna (pill + label saat hover) |
| Ukuran kursor | Kursor bisa diperbesar (opsional V2) |

---

## 14. Adaptasi Perangkat

| Perangkat | Penyesuaian |
|---|---|
| **HP 5–6.5"** | Window default = maximize (karena layar sempit), snap 2 zona, taskbar compact 40 dp, ikon desktop 5 kolom |
| **HP landscape** | Snap 3 zona aktif, taskbar 48 dp |
| **Foldable** | Layout berubah dinamis saat dilipat/dibuka tanpa restart activity (Compose adaptive) |
| **Tablet 10"+** | Desktop mode penuh, jendela mengambang default, snap 4 zona, multi-desktop, ikon 56 dp |
| **Monitor eksternal** | Desktop diperluas; jendela bisa dipindah antar layar (perangkat yang mendukung) |
| **Perangkat RAM ≤ 4 GB** | Mode Ringan otomatis: cahaya wallpaper statis (`wall_motion` mati), animasi dipercepat, maks 3 jendela |

---

## 15. Do's & Don'ts

###  Do
- Gunakan **satu** accent color untuk seluruh sistem; biarkan user memilihnya.
- Beri umpan balik visual dalam 100 ms untuk **setiap** sentuhan/klik.
- Jaga jarak antar elemen minimal 8 dp; elemen berbeda kelompok minimal 16 dp.
- Selalu sediakan jalur keluar: `Esc`, klik di luar, atau tombol batal.
- Uji di layar kecil **dulu** sebelum layar besar.

###  Don't
- Jangan menyalin identitas visual sistem operasi lain (warna khas, logo, ikon, suara, proporsi tombol kontrol jendela).
- Jangan mengklaim blur/efek yang tidak dipakai kode; jangan menyalakan animasi wallpaper tanpa jalan mematikan.
- Jangan menaruh lebih dari 3 level teks dalam satu kartu.
- Jangan membuat animasi lebih dari 400 ms untuk aksi yang sering dipakai.
- Jangan meminta permission sebelum menjelaskan manfaatnya ke pengguna.
- Jangan menambah fitur ke MVP "karena keren" — masuk ke backlog V2.

---

## 16. Aset & Handoff untuk Developer

### 16.1 Peta token -> kode (keadaan sebenarnya)

Tidak ada modul `:core:designsystem`; seluruh sistem desain hidup di paket aplikasi supaya
satu APK tetap kecil dan tidak ada lapisan yang harus dijaga sinkron.

```kotlin
SukiTheme.kt     // semua token §4 (konstanta ARGB), ukuran tetap, durasi + EaseOut,
                 // ACCENT_SPECS (6), WALL_SPECS (6), SukiAccent, accentNow()
SukiFonts.kt     // SukiSans, SukiBrand, SukiClockFont, SukiMono (§5)
SukiGlyph.kt     // Glyph(), glyphStroke; data path di SukiGlyphData.kt (hasil generator)
SukiKit.kt       // Glass(), Panel(), tombol, chip, baris kunci-nilai, bayangan
SukiKitLayout.kt // kerangka isi jendela (baris alat tipis + keterangan)
SukiControls.kt  // kontrol interaktif (chip, toggle, tombol ikon)
SukiWallpaper.kt // Wallpaper() dari WALL_SPECS + preferensi wall_motion
SukiWindows.kt   // bilah judul bergradien, pegangan geser/ubah ukuran, pratinjau snap
```

Efek: tidak ada `Modifier.sukiMica()` / `sukiAcrylic()`. Yang ada `Glass()` (gradien + garis
rambut + bayangan) dan `Panel()` (bidang + garis rambut). Lihat §4.3.

### 16.2 Naming Convention
- Token: `kategori/varian` (contoh: `bg/elevated`, `motion/base`)
- Composable: `Suki` + nama komponen (contoh: `SukiTaskbarItem`)
- Animasi: didefinisikan sekali di `SukiMotion`, tidak ada magic number di komponen.

### 16.3 Checklist Desain -> Dev (v2.0)
- [ ] Token warna baru masuk `SukiTheme.kt` sebagai `const val` ARGB, bukan `Color(0x..)` di berkas UI
- [ ] Pasangan teks/latar baru didaftarkan di `ThemeContrastTest` (AA 4,5:1; non-teks 3:1)
- [ ] Ikon baru ditambahkan lewat `tools/gen_glyphs.py`, bukan disunting tangan di `SukiGlyphData.kt`
- [ ] Ukuran komponen tetap di konstanta (`RADIUS_*`, `TASKBAR_DP`, `TITLE_H`), tanpa angka ajaib
- [ ] Gerak memakai `MOTION_*` dan `EaseOut`
- [ ] State kosong, loading, error, dan non-aktif ada untuk komponen baru
- [ ] Tidak ada emoji, tidak ada aset atau istilah dari sistem operasi lain
- [ ] Klaim efek di dokumen cocok dengan yang benar-benar dipakai kode

---

## 17. Referensi Cepat (Cheat Sheet) — keadaan kode `main`

```
AKSEN            6 preset: aurora laguna mekar azur bara mint (default: aurora)
WARNA AKSEN      #7C5CFF (violet) · #35D0BA (teal) · teks aksen #A89BFF
BG / SURFACE     #0B0D12 / #12151D      CHROME #1F2532   OVERLAY #242A38
TEKS             #F2F4F8 · #B4BCCC · #8D96ABL    INK #0B0D12
GARIS            putih 7% / 12% / 20%   SCRIM #8C05060A
FONT             Inter (UI) · Plus Jakarta Sans (merek) · Inter Display Light (jam)
RADIUS           sm 8 · md 12 · jendela 14 · lg 16 · pill 999
TASKBAR          area dipesan 54 dp, bar 48 dp, ikon 42 dp, indikator 3 dp
JENDELA          bilah judul 40 dp, bayangan 30/12 dp, tepi snap kiri-kanan 24 dp, atas 40 dp
JENDELA INTERNAL lebar bawaan 66% area kerja, tinggi 84%, minimum 320x220 dp
START MENU       560 dp x maks 340 dp, 6 kolom, 6 dp di atas taskbar
APK LUAR         kotak awal 62% x 90% area kerja (portrait-only 40% x 94%), dikoreksi hanya bila tidak masuk akal
GERAK            90 / 150 / 220 / 320 ms, EaseOut cubic-bezier(.2,0,0,1)
ORIENTASI        dikunci sensorLandscape (keputusan produk, PRD F-20)
BLUR             tidak ada — kaca = gradien + alpha + garis rambut
IKON             vektor sendiri (SukiGlyph), tanpa emoji
```

---

## 18. Riwayat Revisi

| Versi | Tanggal | Perubahan |
|---|---|---|
| 1.0 | 2026-10-01 | Suki Glass pertama: aurora ungu-teal, kaca, gradien |
| 1.1 | 2026-10-02 | Matte: aksen Steel/Sage/Clay, aurora dicabut, tanpa glow |
| 2.0 | 2026-10-02 | **Aurora kembali** setelah uji perangkat: matte dinilai datar. Palet ungu-teal dengan turunan `fill`/`on`/`text`, 6 aksen, 6 wallpaper, font dipaketkan (OFL), ikon hasil generator, kontras AA dibuktikan di CI, blur dinyatakan tidak dipakai, orientasi dikunci mendatar |

*Lihat `PRD.md` untuk requirement fungsional dan `mockup.html` untuk pratinjau interaktif design system ini.*
