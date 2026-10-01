# PROGRESS — SukiOS

Zona waktu: **UTC**. Hari 1 = **2026-10-01**.
Log ini hanya mencatat pekerjaan yang benar-benar selesai. Label verifikasi mengikuti aturan di `AGENTS.md`.

---

## 2026-10-01 (Hari 1)

**Fase 0 — Discovery & Design: selesai**
- `PRD.md` v1.1 — 27 fitur berprioritas, user story + acceptance criteria, peta permission Android, roadmap 3 fase, analisis risiko. Termasuk **Addendum §16** berisi temuan riset window engine (pembatasan platform untuk embed app pihak ketiga).
- `DESIGN.md` — design system Suki Glass v1.0 (token warna, tipografi, motion, ukuran komponen).
- `mockup.html` — prototipe interaktif (drag/resize/snap jendela, start menu, quick settings, settings yang benar-benar mengubah tampilan).
- `assets/` — logo SVG + set ikon bawaan.

**PoC window engine: ditulis, belum diverifikasi**
- 9 file Kotlin di `poc/` — window manager, 5 lane uji, capability probe, laporan teks.
- Status: `UNVERIFIED` (belum ada build yang berhasil).

**Infrastruktur repo: dibuat**
- Repo publik `xykal/SukiOS`.
- Workflow: `Build APK` (verifikasi kompilasi), `Release APK (signed, draft)`, `Cleanup CI traces` (policy cleanup).
- Signing rilis: menyiapkan 4 secrets + script `tools/gen-keystore.sh` (belum dijalankan kall).

### Catatan verifikasi
- Build pertama: **<diisi setelah run selesai>**
- Riwayat run + artifact dibersihkan otomatis setelah build selesai (kebijakan cleanup). Karena itu, URL run tidak lagi bisa dibuka setelah pembersihan.
