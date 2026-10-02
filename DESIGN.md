# SukiOS — Design System & Konsep Visual
## "Suki Glass" v1.1 — matte (tanpa neon/cyber)

| | |
|---|---|
| **Produk** | SukiOS |
| **Nama Bahasa Desain** | Suki Glass |
| **Versi** | 1.0 |
| **Tanggal** | 1 Oktober 2026 |
| **Status** | Siap untuk implementasi Compose |

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

### 1.3 Aturan Visual Wajib — Tenang, Matte, Tanpa Neon

Ini batasan yang tidak bisa ditawar (permintaan kall, 2026-10-02). Berlaku untuk semua permukaan produk: shell, app bawaan, ikon, wallpaper, splash, dan materi pemasaran.

**DILARANG:**
- Garis/tepi menyala (neon stroke), bloom, glow berwarna, atau `box-shadow` berwarna aksen.
- Grid siber, garis scanline, efek holografik, chrome/kilau logam, partikel bercahaya.
- Gradien saturasi tinggi atau lebih dari dua warna dalam satu gradien.
- Warna aksen sebagai dekorasi: aksen hanya untuk elemen aktif, tombol utama, dan status fokus.
- Emoji sebagai ikon antarmuka (pakai badge huruf atau ikon vektor).
- Animasi berkilau/berdenyut yang tidak menyampaikan informasi.

**DIWAJIBKAN:**
- Warna diturunkan saturasinya (lihat §4.1); bayangan selalu netral hitam dengan opasitas rendah.
- Pemisahan antar bidang memakai garis tipis 1 dp dan perbedaan nada, bukan efek cahaya.
- Wallpaper: gradien dua nada gelap, tanpa sorotan radial menyala.
- Getaran gerak minimal: durasi mengikuti §8.1, tidak ada animasi dekoratif.

**Uji cepat:** ubah screenshot menjadi grayscale. Kalau tampilannya masih terbaca jelas dan terasa nyaman, desainnya sehat. Kalau justru kehilangan seluruh hierarki, berarti warna/glow-nya yang jadi penopang — dan itu yang harus dihindari.

---

### 1.4 Catatan Implementasi (v1.1)

SukiOS **tidak memakai tema Material**. Material3 hanya dipakai untuk satu hal:
komposisi teks (`Text`), karena Compose 1.7 menghapus konstruktor `TextStyle`
bergaya lama. Seluruh warna, bentuk, jarak, dan ikon berasal dari `SukiKit.kt`.
Artinya: tampilan SukiOS tidak akan berubah kalau Google mengubah tema Material.

Ikon antarmuka digambar sebagai vektor (`Glyph()`), bukan emoji dan bukan font
ikon pihak ketiga. Ini menjaga konsistensi bentuk di semua perangkat dan
menghindari ketergantungan pada aset luar.

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
| Full color (solid matte) | Default, splash screen, store listing |
| Mono putih | Di atas foto/warna terang, di dalam taskbar |
| Mono hitam | Dokumen, print |
| App icon adaptif | Foreground = mark S, Background = warna aksen solid |

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
| **6** | Turun kualitas, bukan turun fungsi | Di HP lemah, blur diganti warna solid — fungsinya tetap sama. |

---

## 4. Sistem Warna

### 4.1 Palet Inti

**Aksen (matte — bukan gradien menyala):**
```
Suki Steel      #6E8CA8   ← aksen utama (tombol, elemen aktif, fokus)
Suki Sage       #7B9E8C   ← aksen sekunder (status, kategori)
Suki Clay       #A08F76   ← kategori ketiga (akses lanjutan)
```
Catatan revisi: gradien Aurora ungu-teal v1.0 dihapus karena terbaca sebagai "neon".
Nilai lama tidak dipakai lagi di kode, mockup, maupun aset.

**Netral (Dark — tema default)**
| Token | Hex | Penggunaan |
|---|---|---|
| `bg/base` | `#0F1113` | Latar desktop paling belakang |
| `bg/surface` | `#17191C` | Panel, taskbar solid |
| `bg/elevated` | `#1E2124` | Kartu, menu, jendela |
| `bg/overlay` | `#262A2E` | Tooltip, popover, title bar |
| `stroke/subtle` | `rgba(255,255,255,0.06)` | Garis pemisah halus |
| `stroke/strong` | `rgba(255,255,255,0.10)` | Border jendela tidak aktif |
| `text/primary` | `#E7E8EA` | Judul, isi utama |
| `text/secondary` | `#A0A4A9` | Deskripsi, metadata |
| `text/disabled` | `#6B7076` | Elemen non-aktif |

**Netral (Light)**
| Token | Hex |
|---|---|
| `bg/base` | `#EDEEEF` |
| `bg/surface` | `#F6F7F8` |
| `bg/elevated` | `#FFFFFF` |
| `stroke/subtle` | `rgba(10,15,30,0.07)` |
| `text/primary` | `#14161A` |
| `text/secondary` | `#4E5359` |

**Semantik**
| Peran | Dark | Light |
|---|---|---|
| Success | `#7FA98A` | `#3F7A55` |
| Warning | `#C0A06A` | `#8A6A21` |
| Danger (close) | `#B4675F` | `#A44A44` |
| Info | `#7E93AC` | `#4A6684` |

### 4.2 Accent Color
User bisa memilih aksen. Setiap aksen punya 4 turunan otomatis:

```
accent/base      100%  → ikon aktif, tombol utama
accent/hover     115% lightness
accent/pressed    90%
accent/subtle     12% opacity  → latar chip, state terpilih
accent/on-accent  otomatis putih/hitam berdasarkan kontras WCAG
```

**12 preset (semua saturasi rendah):** Steel (default) · Sage · Slate · Mauve · Sand · Moss · Clay · Graphite · Dust · Olive · Ash · Denim.

Aturan: aksen tidak boleh dipakai sebagai latar besar atau gradien dekoratif. Maksimal 8% area layar.

### 4.3 Material & Efek
| Nama | Implementasi | Penggunaan |
|---|---|---|
| **Suki Mica** | `surface` + 62% opasitas + blur 28 dp + noise 2% | Latar taskbar, start menu, panel besar |
| **Suki Acrylic** | `elevated` + 72% opasitas + blur 20 dp + border 1px putih 8% | Jendela, kartu |
| **Suki Shadow** | shadow netral hitam, blur 24-40, opacity 22-30% | Jendela aktif, menu, dialog (tanpa warna) |
| **Fallback (API < 31 / mode ringan)** | warna solid + border 1px, tanpa blur | Semua di atas saat blur dimatikan |

> **Aturan blur:** maksimal **3 layer blur** di satu layar. Blur di-nested hanya jika elemen di atasnya opaque.

---

## 5. Tipografi

**Font utama:** Inter (SIL OFL 1.1 — aman untuk komersial)
**Fallback:** `system-ui, -apple-system, "Segoe UI"*, Roboto, sans-serif`
*(Sebagai fallback sistem, bukan dipaketkan di aplikasi.)*

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

### 10.1 Taskbar ("Suki Bar")
```
Tinggi              48 dp (default) · 40 dp (compact) · 64 dp (besar)
Padding horizontal  12 dp
Radius (floating)   16 dp  [mode floating: taskbar terpisah dari tepi, margin 8 dp]
Background          Suki Mica + border atas 1px stroke/subtle
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
Background        Suki Mica (blur 32) + border 1px putih 8%
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
| **Perangkat RAM ≤ 4 GB** | Mode Ringan otomatis: blur off, animasi dipercepat, maks 3 jendela |

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
- Jangan memakai blur lebih dari 3 layer atau di perangkat kelas bawah tanpa mode ringan.
- Jangan menaruh lebih dari 3 level teks dalam satu kartu.
- Jangan membuat animasi lebih dari 400 ms untuk aksi yang sering dipakai.
- Jangan meminta permission sebelum menjelaskan manfaatnya ke pengguna.
- Jangan menambah fitur ke MVP "karena keren" — masuk ke backlog V2.

---

## 16. Aset & Handoff untuk Developer

### 16.1 Yang harus ada di `:core:designsystem`
```kotlin
// Warna
SukiColors            // semua token dari §4
SukiAccentScheme      // 12 preset + generator turunan
SukiTypography        // token dari §5
SukiShapes            // radius dari §7.1
SukiSpacing           // skala 4 dp dari §7.2
SukiElevation         // e0–e4
SukiMotion            // durasi + easing dari §8.1

// Komponen
SukiTaskbar, SukiStartMenu, SukiSearchField
SukiWindowChrome, SukiTitleBar, SukiSnapPreview
SukiToast, SukiCard, SukiButton, SukiToggle, SukiSlider
SukiContextMenu, SukiIconButton, SukiTooltip

// Efek
Modifier.sukiMica(), Modifier.sukiAcrylic(), Modifier.sukiShadow()
Modifier.sukiDragResize(...)   // untuk window engine
```

### 16.2 Naming Convention
- Token: `kategori/varian` (contoh: `bg/elevated`, `motion/base`)
- Composable: `Suki` + nama komponen (contoh: `SukiTaskbarItem`)
- Animasi: didefinisikan sekali di `SukiMotion`, tidak ada magic number di komponen.

### 16.3 Checklist Handoff Desain → Dev
- [ ] Semua token warna tersedia dalam `SukiColors` (dark + light)
- [ ] Semua ikon dalam format SVG 24 dp
- [ ] Ukuran semua komponen terdokumentasi (§10)
- [ ] Interaksi & animasi tercatat (§8)
- [ ] State kosong, loading, error, dan disabled untuk setiap komponen
- [ ] Screenshot/mockup untuk orientasi portrait, landscape, dan tablet
- [ ] Aksesibilitas: label TalkBack & urutan fokus

---

## 17. Referensi Cepat (Cheat Sheet)

```
WARNA AKSEN      #6E8CA8 (steel)  ·  #7B9E8C (sage)
DARK BG          #0F1113
LIGHT BG         #EDEEEF
FONT             Inter (fallback system-ui)
RADIUS JENDELA   16 dp      TASKBAR  48 dp
ICON DESKTOP     48 dp      IKON TASKBAR 32 dp
START MENU       640 × 640 dp
ANIMASI CEPAT    150 ms     ANIMASI NORMAL 220 ms
MAX BLUR LAYER   3        ATURAN  tanpa neon, tanpa glow, tanpa emoji ikon
```

---

*Lihat `PRD.md` untuk requirement fungsional dan `mockup.html` untuk pratinjau interaktif design system ini.*
