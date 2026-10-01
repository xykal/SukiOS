# PROGRESS — SukiOS

Zona waktu: **UTC**. Hari 1 = **2026-10-01**.
Log ini hanya mencatat pekerjaan yang benar-benar selesai. Label verifikasi mengikuti aturan di `AGENTS.md`.

---

## 2026-10-01 (Hari 1)

### Fase 0 — Discovery & Design: selesai
- `PRD.md` v1.1 — 27 fitur berprioritas, user story + acceptance criteria, peta permission Android, roadmap 3 fase, analisis risiko. Termasuk **Addendum §16** berisi temuan riset window engine (pembatasan platform untuk embed app pihak ketiga).
- `DESIGN.md` — design system Suki Glass v1.0.
- `mockup.html` — prototipe interaktif (drag/resize/snap jendela, start menu, quick settings, settings yang benar-benar mengubah tampilan).
- `assets/` — logo SVG + set ikon bawaan.

### PoC window engine: kompilasi terverifikasi, pengujian perangkat belum
- 9 file Kotlin di `poc/` — window manager, 5 lane uji, capability probe, laporan teks.
- Kompilasi (debug + release) berhasil di CI. Pengujian di perangkat nyata **belum** dilakukan.

### Infrastruktur repo: aktif
- Repo publik `xykal/SukiOS`.
- Workflow: `Build APK`, `Release APK (signed, draft)`, `Cleanup CI traces`.
- Script: `tools/gen-keystore.sh`, `tools/cleanup_ci.py`, panduan `docs/RELEASE-SIGNING.md`.

### Catatan verifikasi

| Waktu (UTC) | Hasil | Bukti |
|---|---|---|
| 2026-10-01 23:34 | `Build APK` run **36941499802** (commit `23cbd3a`, branch `main`) — kesimpulan **success** | `CI-VERIFIED`; riwayat run + artifact dihapus otomatis setelah rekaman diambil, URL run tidak bisa dibuka lagi |
| 2026-10-01 23:41 | `Release APK (signed, draft)` run **36942422305** — draft release `v0.1.0-poc` dibuat | APK `SukiOS-PoC-v0.1.0-poc.apk` (~14 MB) terlampir di draft release; riwayat run sudah dibersihkan |

Catatan penting:
- APK pada rilis ini **debug-signed** karena 4 secrets keystore rilis belum diset. Bukan untuk distribusi publik. Lihat `docs/RELEASE-SIGNING.md`.
- Artifact build tidak disimpan (workflow build tidak mengunggah artifact) — jalur distribusi APK adalah lewat draft release.
- Cache Gradle **tidak** dihapus (sesuai kebijakan) agar build berikutnya tetap cepat.

### Langkah berikutnya
1. Uji 5 lane di perangkat nyata, kirim laporan lewat tombol "Salin laporan" di app.
2. Set 4 secrets keystore supaya rilis berikutnya benar-benar signed.
3. Setelah hasil uji masuk: kunci arsitektur window (PRD §16.2) lalu mulai Fase 1.
