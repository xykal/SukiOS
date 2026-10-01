# AGENTS.md — SukiOS

Panduan kerja untuk agen/developer yang melanjutkan repo ini. Baca ini sebelum mengubah apa pun.

## Identitas proyek
- **SukiOS** — desktop environment untuk Android (launcher + window manager). Bahasa desain: **Suki Glass**.
- Dokumen penting: `PRD.md` (§16 = temuan riset pembatasan platform), `DESIGN.md`, `mockup.html`, `poc/` (project Android PoC).
- Aturan merek: **jangan** memakai nama, logo, ikon, suara, atau aset sistem operasi lain. Lihat PRD §4.2.

## Struktur
```
poc/                       Gradle root project PoC (modul :app)
  app/src/main/java/app/sukios/poc/
.github/workflows/         build.yml · release.yml · cleanup.yml
tools/                     script lokal (keystore)
docs/                      panduan rilis & signing
PRD.md · DESIGN.md · mockup.html · PROGRESS.md
```

## Aturan wajib

1. **Rahasia tidak pernah masuk repo.** Kredensial disimpan di luar repo (folder `uploads/` pada workspace); di CI memakai GitHub Secrets. Periksa `git status` sebelum commit; `.gitignore` sudah menutup pola umum.
2. **Signing rilis** memakai 4 secrets: `SUKIOS_KEYSTORE_BASE64`, `SUKIOS_KEYSTORE_PASSWORD`, `SUKIOS_KEY_ALIAS`, `SUKIOS_KEY_PASSWORD`. Tanpa itu, workflow rilis jatuh ke debug key dan menandainya sebagai peringatan di catatan rilis.
3. **Kebijakan cleanup (milik kall):** setelah setiap build/release selesai, workflow `Cleanup CI traces` menghapus riwayat run + artifact yang dibuat oleh task itu. **Release/deliverable tidak dihapus.** Cache tidak dihapus kecuali diminta eksplisit (`purge_caches: true`) karena cache Gradle dipakai bersama antar-run.
4. **Jangan hapus** release, tag, branch, atau riwayat run milik orang lain. **Jangan** bersih-bersih untuk menyembunyikan aktivitas.
5. **Gagal build tidak dibersihkan otomatis** — log error dibiarkan agar bisa didiagnosis dulu (naikkan `include_failed: true` bila memang ingin dibersihkan).

## Label verifikasi
`LOCAL-VERIFIED` · `CI-VERIFIED <run ID>` · `UNVERIFIED` · `BLOCKED <alasan>` — jangan pakai kata "verified" tanpa bukti yang sesuai.

## Alur kerja
- Ubah kode → push ke `main` → workflow `Build APK` memverifikasi kompilasi (tanpa menyimpan artifact).
- APK yang bisa dipasang: `git tag vX.Y.Z && git push origin vX.Y.Z` → **draft release** berisi APK. Unduh dari tab Releases.
- Catat pekerjaan di `PROGRESS.md` (UTC). Perubahan khusus dokumen boleh memakai penanda `[skip ci]`.

## Konvensi teknis
- Versi dipin: AGP 8.7.3 · Gradle 8.11.1 · Kotlin 2.0.21 · Compose BOM 2024.10.01 · compileSdk 35 · minSdk 29.
- Naikkan versi **setelah** ada build hijau, bukan sebaliknya. Satu perubahan versi = satu build.
- `minSdk 29` adalah keputusan produk (PRD §8), bukan nilai default yang boleh dinaikkan tanpa alasan.
