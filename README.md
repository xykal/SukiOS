# SukiOS

Desktop environment untuk Android — launcher + window manager dengan identitas desain sendiri (**Suki Glass**).
Bukan tiruan merek apa pun: nama, logo, ikon, warna, dan suara dibuat dari nol (lihat `PRD.md` §4.2).

---

## Isi Repo

| Path | Isi |
|---|---|
| `poc/` | Project Android (Kotlin + Compose) — PoC window engine: 5 lane uji + capability probe |
| `PRD.md` | Dokumen kebutuhan produk. **§16 = temuan riset pembatasan platform** (wajib dibaca) |
| `DESIGN.md` | Design system Suki Glass: warna, tipografi, motion, ukuran komponen |
| `mockup.html` | Prototipe interaktif (drag, resize, snap, start menu, settings) |
| `assets/` | Logo + set ikon bawaan |
| `tools/` | `gen-keystore.sh` (pembuat keystore rilis), `cleanup_ci.py` (pembersih jejak CI) |
| `docs/RELEASE-SIGNING.md` | Panduan signing dan rilis |
| `PROGRESS.md` | Log pekerjaan (UTC) |
| `AGENTS.md` | Panduan kerja untuk agen/developer yang melanjutkan repo ini |

---

## Cara Mendapatkan APK

APK tidak disimpan di artifact CI (artifact dibersihkan setelah build selesai). Deliverable APK diterbitkan lewat **draft release**:

```bash
git tag v0.1.0-poc
git push origin v0.1.0-poc
```

Lalu buka tab **Releases** → draft `SukiOS PoC v0.1.0-poc` → unduh APK → kirim ke HP → install (aktifkan "Install unknown apps").

Syarat APK **resmi (signed)**: 4 secrets keystore harus diset lebih dulu.
Tanpa itu workflow tetap jalan, tetapi APK memakai debug key dan catatan rilis menandainya sebagai tidak resmi.
Panduan: `docs/RELEASE-SIGNING.md`.

---

## Workflow CI

| Workflow | Pemicu | Hasil |
|---|---|---|
| `Build APK` | push ke `main`, pull request, manual | verifikasi kompilasi (debug + release), tanpa artifact |
| `Release APK (signed, draft)` | push tag `v*`, manual | APK bertanda tangan → draft release |
| `Cleanup CI traces` | selesai-nya build/release, manual | hapus riwayat run + artifact task itu |

---

## Kebijakan Repo

1. **Rahasia tidak pernah masuk repo.** Kredensial disimpan di luar repo; di CI memakai GitHub Secrets. `.gitignore` menutup pola umum, tetapi tetap periksa `git status` sebelum commit.
2. **Cleanup CI.** Setelah setiap build/release selesai, riwayat run + artifact task itu dihapus. Yang **tidak** dihapus: draft release, APK di dalamnya, tag, dan run yang gagal (log error dibiarkan untuk diagnosis).
3. **Cache Gradle tidak dihapus** secara default karena dipakai bersama antar-run; hapus hanya dengan `purge_caches: true` saat dispatch manual.
4. **Label verifikasi** yang dipakai di repo ini: `LOCAL-VERIFIED`, `CI-VERIFIED <run ID>`, `UNVERIFIED`, `BLOCKED <alasan>`.

---

## Status

- Fase 0 (Discovery & Design): **selesai** — PRD v1.1, DESIGN v1.0, mockup, aset.
- PoC window engine: **ditulis, belum terverifikasi** (`UNVERIFIED`).
- Langkah berikutnya: build hijau → uji 5 lane di perangkat → kunci arsitektur window (PRD §16).

Lihat `PROGRESS.md` untuk log per tanggal.
