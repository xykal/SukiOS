# SukiOS

Android desktop environment: launcher + window manager with its own design identity (**Suki Glass / Aurora v2**).
Desktop environment untuk Android: launcher + window manager dengan identitas desain sendiri (**Suki Glass / Aurora v2**).

[English](#english) · [Bahasa Indonesia](#bahasa-indonesia)

---

## English

**What it is.** SukiOS replaces the Android home screen with a desktop: taskbar, start menu, floating windows for its own tools,
and third-party apps opened as Android freeform windows instead of fullscreen when the device allows it. Orientation is locked to landscape,
because this is a desktop. Advanced access runs through SukiShell (Shizuku) and is used to set up windowed launch and then *prove* it works on this device.

**For whom.** People who use a phone or tablet like a small computer: students, tinkerers, mouse-and-keyboard users.
Not an imitation of any brand: name, logo, icons, colors and sounds are made from scratch (`PRD.md` section 4.2).

### Status (honest)

| Item | State |
|---|---|
| Latest tag | `v0.4.2-alpha` (draft release, debug-signed until release keystore secrets are configured) |
| Compile + unit tests | verified in CI; run IDs and captured evidence are in `PROGRESS.md` |
| On a real device | `v0.3.1-alpha` was tested by the maintainer: the matte UI read as flat and third-party apps opened fullscreen. Both were addressed in `v0.4.0-alpha`; `v0.4.2-alpha` adds system live wallpaper mode, tighter R8/fullscreen settings, and a flatter liquid-glass UI pass. This new build is **not device-tested yet** |
| License | none chosen yet; all rights reserved until the maintainer decides |

### Get the APK

APKs are not kept as CI artifacts. Push a tag (`git tag vX.Y.Z-suffix && git push origin vX.Y.Z-suffix`) and the release workflow
attaches the APK to a **draft release**. Signed releases need 4 repository secrets (`docs/RELEASE-SIGNING.md`); without them the APK uses
the debug key and the release notes say so.

For current device testing, use the draft `v0.4.2-alpha` APK and follow `docs/release-notes/v0.4.2-alpha.md`.

### Repository

| Path | Contents |
|---|---|
| `sukios/` | Android app (Kotlin + Compose): launcher + own engines (SukiWin, SukiWindowing, SukiShell, SukiIndex, SukiTheme/SukiKit) |
| `PRD.md`, `DESIGN.md` | product requirements (section 16 = platform limits, must read) and the Aurora v2 design system |
| `mockup.html`, `assets/` | legacy interactive prototype (currently stale matte v1.1), logo and icon assets |
| `docs/` | audits (`AUDIT-*.md`), release signing guide, release notes |
| `tools/` | `check_workflows.py`, `cleanup_ci.py`, `gen-keystore.sh`, glyph/font helpers |
| `PROGRESS.md`, `IDEAS.md` | dated work log, idea backlog |
| `SECURITY.md`, `THIRD_PARTY_NOTICES.md` | disclosure policy, third-party notices (DRAFT) |

### Quality gates (CI)

`Build APK` runs on every push to `main`: workflow security check (action pins, no untrusted input in scripts) with its own self-test, JVM unit tests
(window engine, freeform launch parsing, setup plan, shell policy and argument allowlists, WCAG contrast of the Aurora tokens, icon data vs its generator, manifest rules, brand string, source hygiene), then debug and release builds.
`Release APK (signed, draft)` repeats the unit tests before it builds. `Cleanup CI traces` removes run history after a successful run (policy in `AGENTS.md`).

Built by xykal — XyVerse Technology Global

---

## Bahasa Indonesia

**Apa ini.** SukiOS mengganti layar utama Android dengan desktop: taskbar, start menu, jendela mengambang untuk alat bawaannya,
dan aplikasi pihak ketiga yang dicoba dibuka sebagai jendela freeform Android saat perangkat mengizinkan. Orientasi dikunci mendatar karena ini desktop.
Akses lanjutan opsional berjalan lewat SukiShell (Shizuku) untuk menyiapkan mode jendela dan membuktikan hasilnya di perangkat itu.

**Untuk siapa.** Orang yang memakai HP atau tablet seperti komputer kecil: mahasiswa, tinkerer, pengguna mouse dan keyboard.
Bukan tiruan merek apa pun: nama, logo, ikon, warna, dan suara dibuat dari nol (`PRD.md` bagian 4.2).

### Status (jujur)

| Hal | Keadaan |
|---|---|
| Tag terbaru | `v0.4.2-alpha` (draft release, debug key sampai secrets keystore rilis diset) |
| Kompilasi + uji unit | terverifikasi di CI; ID run dan bukti log ada di `PROGRESS.md` |
| Di perangkat nyata | `v0.3.1-alpha` sudah diuji pemilik produk: tampilan matte dinilai datar dan aplikasi luar terbuka layar penuh. Keduanya ditindaklanjuti di `v0.4.0-alpha`; `v0.4.2-alpha` menambah mode live wallpaper sistem, setelan R8/fullscreen lebih ketat, dan pass UI liquid-glass yang lebih rata. Build baru ini **belum diuji di perangkat** |
| Lisensi | belum dipilih; semua hak dipegang pemilik sampai keputusan diambil |

### Mendapatkan APK

APK tidak disimpan sebagai artifact CI. Dorong tag (`git tag vX.Y.Z-sufiks && git push origin vX.Y.Z-sufiks`), lalu workflow rilis melampirkan APK
ke **draft release**. Rilis bertanda tangan butuh 4 secrets repo (`docs/RELEASE-SIGNING.md`); tanpa itu APK memakai debug key dan catatan rilis
menyatakannya.

Untuk uji perangkat sekarang, pakai draft APK `v0.4.2-alpha` dan ikuti `docs/release-notes/v0.4.2-alpha.md`.

### Isi repo

| Path | Isi |
|---|---|
| `sukios/` | Aplikasi Android (Kotlin + Compose): launcher + mesin sendiri (SukiWin, SukiWindowing, SukiShell, SukiIndex, SukiTheme/SukiKit) |
| `PRD.md`, `DESIGN.md` | kebutuhan produk (bagian 16 = batas platform, wajib dibaca) dan design system Aurora v2 |
| `mockup.html`, `assets/` | prototipe interaktif lama (saat ini stale matte v1.1), logo, dan aset ikon |
| `docs/` | audit (`AUDIT-*.md`), panduan signing rilis, catatan rilis |
| `tools/` | `check_workflows.py`, `cleanup_ci.py`, `gen-keystore.sh`, helper glyph/font |
| `PROGRESS.md`, `IDEAS.md` | log kerja bertanggal, backlog ide |
| `SECURITY.md`, `THIRD_PARTY_NOTICES.md` | kebijakan pelaporan celah, catatan lisensi pihak ketiga (DRAFT) |

### Gerbang kualitas (CI)

`Build APK` jalan di setiap push ke `main`: pemeriksaan keamanan workflow (pin action, tanpa input tak tepercaya di skrip) lengkap dengan self-test-nya,
uji unit JVM (mesin jendela, pengurai peluncuran freeform, rencana penyiapan, kebijakan shell dan allowlist argumen, kontras WCAG token Aurora, data ikon terhadap generatornya, aturan manifest, tulisan merek, kebersihan sumber),
lalu build debug dan release.
`Release APK (signed, draft)` mengulang uji unit sebelum membangun. `Cleanup CI traces` menghapus riwayat run setelah run sukses (kebijakan di `AGENTS.md`).

### Kebijakan repo

1. **Rahasia tidak pernah masuk repo.** Kredensial disimpan di luar repo; di CI memakai GitHub Secrets. `.gitignore` menutup pola umum, tetap periksa `git status` sebelum commit.
2. **Cleanup CI.** Riwayat run + artifact task itu dihapus setelah sukses; setelah rilis cache ikut dihapus. Draft release, tag, dan run yang gagal tidak dihapus otomatis.
3. **Label verifikasi:** `LOCAL-VERIFIED`, `CI-VERIFIED <run ID>`, `UNVERIFIED`, `BLOCKED <alasan>`.

Dibuat oleh xykal — XyVerse Technology Global
