# SukiOS — Mockup Interaktif v0.9

Prototipe HTML/CSS/JS satu file untuk memvalidasi **konsep visual & interaksi** SukiOS sebelum masuk ke implementasi Kotlin/Jetpack Compose.

## Cara Pakai
Buka `mockup.html` di browser (atau langsung lihat di preview). Tidak butuh internet, tidak butuh install apa pun.

## Yang Bisa Dicoba

| Aksi | Cara |
|---|---|
| **Geser jendela** | Drag title bar jendela |
| **Resize jendela** | Drag dari tepi atau sudut jendela (kursor berubah) |
| **Snap ke separuh layar** | Drag jendela ke tepi kiri/kanan sampai muncul indikator biru, lalu lepas |
| **Snap ke seperempat** | Drag ke sudut layar |
| **Maximize** | Double-tap title bar, atau drag ke atas layar |
| **Minimize / Restore** | Tombol `—` di title bar, atau klik ikonnya di taskbar |
| **Tutup jendela** | Tombol `×` |
| **Ganti fokus jendela** | Klik jendela mana pun (z-order naik ke depan) |
| **Start Menu** | Klik tombol Start (kiri-bawah) atau tekan `Super`/`Ctrl+Space` |
| **Cari aplikasi** | Ketik di search start menu |
| **Task View** | Klik ikon Task View di taskbar, atau `Alt+Tab` |
| **Quick Settings** | Klik area jam/baterai kanan-bawah (slider kecerahan & volume beneran jalan) |
| **Context menu** | Klik kanan di desktop, atau klik kanan pada ikon |
| **Geser ikon desktop** | Drag ikon di desktop — otomatis snap ke grid |
| **Ganti accent color** | Buka app **Settings** → Personalisasi → pilih warna |
| **Ganti wallpaper** | Settings → Personalisasi → Wallpaper |
| **Mode Terang/Gelap** | Settings → Personalisasi → Tema |
| **Efek blur on/off** | Settings → Performa → Efek Transparansi |

## App yang Bisa Dibuka
- **Files** — file explorer mock (sidebar + daftar file)
- **Notes** — catatan (textarea beneran)
- **Calculator** — kalkulator yang berfungsi
- **Browser** — mock halaman
- **Photos** — grid foto
- **Music** — player mock
- **Task Manager** — daftar jendela aktif + tombol kill (mendemokan konsep window manager)
- **Settings** — pengaturan yang benar-benar mengubah tampilan

## Yang Belum Ada di Mockup Ini (baru di app Android nanti)
- Blur kaca asli dengan `RenderEffect` native
- Widget di desktop (`AppWidgetHost`)
- Notification center dengan notifikasi sistem asli
- File explorer yang menyentuh filesystem asli
- Multi-desktop (Desk)
- Lock screen

## Catatan
Mockup ini memakai DOM/HTML, sedangkan aplikasi final akan memakai **Jetpack Compose** dengan token desain identik dari `DESIGN.md`. Anggap ini sebagai *spesifikasi hidup* — apa pun yang terasa bagus di sini, itu yang diimplementasikan.
