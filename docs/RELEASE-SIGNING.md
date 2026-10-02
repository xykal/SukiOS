# Panduan Rilis & Signing — SukiOS

Cara menghasilkan APK rilis yang **ditandatangani** dan menerbitkannya sebagai **draft release**, sesuai alur repo ini.

## Alur singkat

1. Push tag `vX.Y.Z` → workflow **Release APK (signed, draft)** jalan.
2. Workflow membangun APK release dan menandatanganinya dengan keystore dari GitHub Secrets.
3. APK dilampirkan ke **draft release** (belum publik sampai dipublikasikan manual oleh kall).
4. Workflow **Cleanup CI traces** menghapus riwayat run + artifact setelah selesai.
   Draft release dan APK di dalamnya **tetap ada** — itu deliverable, bukan artifact sementara.

```
git tag v0.1.0-poc
git push origin v0.1.0-poc
```

Buka tab **Releases** → draft `SukiOS PoC v0.1.0-poc` → unduh APK.

## Sekali saja: membuat keystore

Jalankan di komputer lokal (bukan di CI):

```bash
bash tools/gen-keystore.sh sukios sukios-release.jks
```

Script membuat keystore PKCS12, mencetak base64, dan menampilkan perintah `gh secret set`.

- Simpan `sukios-release.jks` di tempat aman (password manager / folder terenkripsi).
- **Jangan pernah** commit file itu. `.gitignore` sudah menutup `*.jks`.
- Simpan backup. Kalau hilang, tidak ada cara merilis update dengan `applicationId` yang sama.

## Sekali saja: menyiapkan 4 secrets

| Nama secret | Isi |
|---|---|
| `SUKIOS_KEYSTORE_BASE64` | hasil `base64 -w0 sukios-release.jks` |
| `SUKIOS_KEYSTORE_PASSWORD` | password keystore |
| `SUKIOS_KEY_ALIAS` | alias kunci (default: `sukios`) |
| `SUKIOS_KEY_PASSWORD` | password kunci tersebut |

Lewat CLI:

```bash
B64=$(base64 -w0 sukios-release.jks)
printf '%s' "$B64" | gh secret set SUKIOS_KEYSTORE_BASE64 --repo xykal/SukiOS
gh secret set SUKIOS_KEY_ALIAS --repo xykal/SukiOS --body "sukios"
gh secret set SUKIOS_KEYSTORE_PASSWORD --repo xykal/SukiOS   # akan meminta input
gh secret set SUKIOS_KEY_PASSWORD --repo xykal/SukiOS        # akan meminta input
unset B64
```

Lewat web: **Settings → Secrets and variables → Actions → New repository secret**.

## Kalau secrets belum diset

Workflow tetap jalan, tetapi APK ditandatangani dengan **debug key** dan catatan rilis menyatakan itu secara eksplisit. APK semacam ini hanya untuk uji internal, bukan distribusi.

## Rotasi dan insiden

- **Rotasi rutin:** buat keystore baru, perbarui 4 secrets, rilis versi baru. Pengguna mungkin perlu uninstall APK lama karena tanda tangan berbeda.
- **Keystore bocor:** segera ganti keystore. Bila aplikasi sudah terbit di Play Store dan memakai Play App Signing, signing key aplikasi bisa direset lewat Play Console; upload key cukup diganti.
- **Secret bocor:** hapus secret lama, buat baru, lalu rilis ulang. Riwayat GitHub Actions yang sudah dihapus **tidak** mengurangi risiko kebocoran — anggap nilai yang pernah bocor sebagai kompromi.

## Yang dilakukan cleanup (dan yang tidak)

| Objek | Setelah rilis | Setelah build biasa |
|---|---|---|
| Riwayat run Actions buatan task itu | **dihapus** | **dihapus** |
| Artifact buatan task itu | **dihapus** | **dihapus** |
| Cache Gradle | **dihapus** (`purge_mode=all`) — jejak rilis jadi nol | **dibiarkan** agar build berikutnya cepat |
| Draft release + APK | **tidak dihapus** (deliverable) | tidak berlaku |
| Tag | **tidak dihapus** | tidak berlaku |
| Run yang gagal | **tidak dihapus** (log error dibiarkan untuk diagnosis) | sama |

Catatan: menghapus cache memperlambat build berikutnya (unduh ulang Gradle + dependensi, sekitar 5-8 menit pada repo ini). Karena itu cache hanya dipurge setelah rilis, bukan setiap build.
