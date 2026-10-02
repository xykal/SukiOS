# SukiOS

Android desktop environment: launcher + window manager with its own design identity (**Suki Glass**).
Desktop environment untuk Android: launcher + window manager dengan identitas desain sendiri (**Suki Glass**).

[English](#english) · [Bahasa Indonesia](#bahasa-indonesia)

---

## English

**What it is.** SukiOS replaces the Android home screen with a desktop: taskbar, start menu, floating windows for its own tools,
and an optional advanced-access engine (SukiShell, running through Shizuku).
**For whom.** People who use a phone or tablet like a small computer: students, tinkerers, mouse-and-keyboard users.
Not an imitation of any brand: name, logo, icons, colors and sounds are made from scratch (`PRD.md` section 4.2).

### Status (honest)

| Item | State |
|---|---|
| Latest tag | `v0.3.1-alpha`, draft release, debug-signed. Not for distribution |
| Compile + unit tests | verified in CI; run IDs in `PROGRESS.md` |
| On a real device | **not tested yet.** Nobody should claim it works until a device report exists |
| License | none chosen yet; all rights reserved until the maintainer decides |

### Get the APK

APKs are not kept as CI artifacts. Push a tag (`git tag vX.Y.Z-suffix && git push origin vX.Y.Z-suffix`) and the release workflow
attaches the APK to a **draft release**. Signed releases need 4 repository secrets (`docs/RELEASE-SIGNING.md`); without them the APK uses
the debug key and the release notes say so.

### Repository

| Path | Contents |
|---|---|
| `sukios/` | Android app (Kotlin + Compose): launcher + own engines (SukiShell, SukiWin, SukiIndex, SukiKit) |
| `PRD.md`, `DESIGN.md` | product requirements (section 16 = platform limits, must read) and the design system |
| `mockup.html`, `assets/` | interactive prototype, logo and icon set |
| `docs/` | audits (`AUDIT-*.md`), release signing guide, release notes |
| `tools/` | `check_workflows.py`, `cleanup_ci.py`, `gen-keystore.sh` |
| `PROGRESS.md`, `IDEAS.md` | dated work log, idea backlog |
| `SECURITY.md`, `THIRD_PARTY_NOTICES.md` | disclosure policy, third-party licenses (DRAFT) |

### Quality gates (CI)

`Build APK` runs on every push to `main`: workflow security check (action pins, no untrusted input in scripts) with its own self-test, JVM unit tests
(window engine, shell execution limits, argument allowlists, brand string, source hygiene), then debug and release builds.
`Release APK (signed, draft)` repeats the unit tests before it builds. `Cleanup CI traces` removes run history after a successful run (policy in `AGENTS.md`).

Built by xykal — XyVerse Technology Global

---

## Bahasa Indonesia

**Apa ini.** SukiOS mengganti layar utama Android dengan desktop: taskbar, start menu, jendela mengambang untuk alat bawaannya,
dan mesin akses lanjutan opsional (SukiShell, lewat Shizuku).
**Untuk siapa.** Orang yang memakai HP atau tablet seperti komputer kecil: mahasiswa, tinkerer, pengguna mouse dan keyboard.
Bukan tiruan merek apa pun: nama, logo, ikon, warna, dan suara dibuat dari nol (`PRD.md` bagian 4.2).

### Status (jujur)

| Hal | Keadaan |
|---|---|
| Tag terbaru | `v0.3.1-alpha`, draft release, ditandatangani debug key. Bukan untuk distribusi |
| Kompilasi + uji unit | terverifikasi di CI; ID run ada di `PROGRESS.md` |
| Di perangkat nyata | **belum diuji.** Jangan klaim berfungsi sebelum ada laporan dari perangkat |
| Lisensi | belum dipilih; semua hak dipegang pemilik sampai keputusan diambil |

### Mendapatkan APK

APK tidak disimpan sebagai artifact CI. Dorong tag (`git tag vX.Y.Z-sufiks && git push origin vX.Y.Z-sufiks`), lalu workflow rilis melampirkan APK
ke **draft release**. Rilis bertanda tangan butuh 4 secrets repo (`docs/RELEASE-SIGNING.md`); tanpa itu APK memakai debug key dan catatan rilis
menyatakannya.

### Isi repo

| Path | Isi |
|---|---|
| `sukios/` | Aplikasi Android (Kotlin + Compose): launcher + mesin sendiri (SukiShell, SukiWin, SukiIndex, SukiKit) |
| `PRD.md`, `DESIGN.md` | kebutuhan produk (bagian 16 = batas platform, wajib dibaca) dan design system |
| `mockup.html`, `assets/` | prototipe interaktif, logo, dan set ikon |
| `docs/` | audit (`AUDIT-*.md`), panduan signing rilis, catatan rilis |
| `tools/` | `check_workflows.py`, `cleanup_ci.py`, `gen-keystore.sh` |
| `PROGRESS.md`, `IDEAS.md` | log kerja bertanggal, backlog ide |
| `SECURITY.md`, `THIRD_PARTY_NOTICES.md` | kebijakan pelaporan celah, lisensi pihak ketiga (DRAFT) |

### Gerbang kualitas (CI)

`Build APK` jalan di setiap push ke `main`: pemeriksaan keamanan workflow (pin action, tanpa input tak tepercaya di skrip) lengkap dengan self-test-nya,
uji unit JVM (mesin jendela, batas eksekusi shell, allowlist argumen, tulisan merek, kebersihan sumber), lalu build debug dan release.
`Release APK (signed, draft)` mengulang uji unit sebelum membangun. `Cleanup CI traces` menghapus riwayat run setelah run sukses (kebijakan di `AGENTS.md`).

### Kebijakan repo

1. **Rahasia tidak pernah masuk repo.** Kredensial disimpan di luar repo; di CI memakai GitHub Secrets. `.gitignore` menutup pola umum, tetap periksa `git status` sebelum commit.
2. **Cleanup CI.** Riwayat run + artifact task itu dihapus setelah sukses; setelah rilis cache ikut dihapus. Draft release, tag, dan run yang gagal tidak dihapus otomatis.
3. **Label verifikasi:** `LOCAL-VERIFIED`, `CI-VERIFIED <run ID>`, `UNVERIFIED`, `BLOCKED <alasan>`.

Dibuat oleh xykal — XyVerse Technology Global
