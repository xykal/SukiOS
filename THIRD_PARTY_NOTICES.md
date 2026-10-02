# THIRD_PARTY_NOTICES

> DRAFT — review by licensed counsel before publishing. / DRAFT — tinjau oleh konsultan hukum berlisensi sebelum dipublikasikan.

SukiOS memakai pustaka pihak ketiga berikut. Daftar ini mengikuti `sukios/app/build.gradle.kts`; perbarui setiap kali dependensi berubah.
Daftar dependensi transitif lengkap belum dibuat otomatis (usulan: SBOM CycloneDX di CI, lihat `IDEAS.md`).

| Komponen | Versi | Lisensi | Ikut di APK |
|---|---|---|---|
| Shizuku-API (`dev.rikka.shizuku:api`, `dev.rikka.shizuku:provider`) | 13.1.5 | MIT | ya |
| AndroidX Core KTX, Activity Compose | 1.13.1, 1.9.3 | Apache-2.0 | ya |
| Jetpack Compose (BOM 2024.10.01: ui, foundation, runtime, material3) | mengikuti BOM | Apache-2.0 | ya |
| Kotlin standard library, kotlinx.coroutines | 2.0.21, transitif | Apache-2.0 | ya |
| JUnit 4 | 4.13.2 | EPL-1.0 | tidak (hanya uji) |

Aplikasi Shizuku (`moe.shizuku.privileged.api`, Apache-2.0) bukan bagian dari APK SukiOS; pengguna memasangnya sendiri. SukiOS tidak menyertakan font atau
ikon pihak ketiga: teks memakai font sistem, ikon digambar sebagai vektor di `SukiKit.kt`.

## Shizuku-API — MIT License

Sumber: https://github.com/RikkaApps/Shizuku-API (berkas `LICENSE`, dibaca lewat API GitHub pada 2026-10-02).

```
MIT License

Copyright (c) 2021 RikkaW

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Apache License 2.0 (AndroidX, Jetpack Compose, Kotlin, kotlinx.coroutines)

Teks lengkap: https://www.apache.org/licenses/LICENSE-2.0.txt. Masing-masing proyek memegang hak ciptanya sendiri.

Belum dikerjakan: salinan teks lisensi di dalam APK (layar "Lisensi sumber terbuka"). Apache-2.0 meminta penerima distribusi mendapat salinan lisensi;
tautan di berkas ini belum memenuhinya untuk APK. Dicatat di `IDEAS.md`.

## Merek

"XyVerse Technology Global" adalah nama pembuat SukiOS. Penggunaan nama itu pada karya turunan perlu izin tertulis; catatan merek lengkap menyusul bersama
dokumen hukum publik (Terms, Privacy) saat produk dirilis ke publik.

Built by xykal — XyVerse Technology Global
