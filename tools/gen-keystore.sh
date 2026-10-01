#!/usr/bin/env bash
# ============================================================================
# SukiOS — pembuat keystore rilis
#
# Jalankan DI KOMPUTER LOKAL (bukan di CI, bukan di workspace agen).
# Script ini membuat keystore PKCS12 + mencetak nilai yang perlu dimasukkan
# ke GitHub Secrets.
#
# Pakai:
#   bash tools/gen-keystore.sh [alias] [nama-file]
#
# Contoh:
#   bash tools/gen-keystore.sh sukios sukios-release.jks
#
# PENTING:
#   - File .jks hasil script ini TIDAK BOLEH masuk repo (sudah ada di .gitignore).
#   - Simpan backup-nya (password manager / folder terenkripsi). Kalau hilang,
#     kamu tidak bisa lagi merilis update dengan applicationId yang sama.
# ============================================================================
set -euo pipefail

ALIAS="${1:-sukios}"
OUT="${2:-sukios-release.jks}"
REPO="${REPO:-xyikal/SukiOS}"

if [ -e "$OUT" ]; then
  echo "File '$OUT' sudah ada. Hapus atau pakai nama lain agar tidak menimpa." >&2
  exit 1
fi

if ! command -v keytool >/dev/null 2>&1; then
  echo "keytool tidak ditemukan. Install JDK 17 dulu (keytool ikut di dalamnya)." >&2
  exit 1
fi

echo "== Membuat keystore =="
echo "Alias    : $ALIAS"
echo "File     : $OUT"
echo "Berlaku  : 10000 hari (~27 tahun)"
echo

# -storetype PKCS12: format modern, didukung Android Gradle Plugin.
keytool -genkeypair \
  -keystore "$OUT" \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -storetype PKCS12 \
  -dname "CN=SukiOS, OU=Release, O=SukiOS, C=ID"

echo
echo "== Keystore dibuat: $OUT =="
echo

B64_FILE="${OUT}.base64.txt"
if command -v base64 >/dev/null 2>&1; then
  # -w0 (GNU) atau tanpa -w (BSD/macOS): coba keduanya.
  if base64 -w0 "$OUT" > "$B64_FILE" 2>/dev/null; then :; else base64 "$OUT" | tr -d '\n' > "$B64_FILE"; fi
  echo "Base64 disimpan di: $B64_FILE"
  echo
fi

cat <<EOF
------------------------------------------------------------------
LANGKAH BERIKUTNYA — set 4 secrets di repo $REPO
------------------------------------------------------------------
Lewat GitHub CLI (butuh izin admin repo):

  gh secret set SUKIOS_KEYSTORE_BASE64   --repo $REPO < $B64_FILE
  gh secret set SUKIOS_KEYSTORE_PASSWORD --repo $REPO
  gh secret set SUKIOS_KEY_ALIAS         --repo $REPO --body "$ALIAS"
  gh secret set SUKIOS_KEY_PASSWORD      --repo $REPO

Atau lewat web: Settings -> Secrets and variables -> Actions ->
New repository secret, lalu isi nama-nama di atas satu per satu.

Nama secret harus PERSIS sama dengan yang dibaca workflow:
  SUKIOS_KEYSTORE_BASE64  SUKIOS_KEYSTORE_PASSWORD
  SUKIOS_KEY_ALIAS        SUKIOS_KEY_PASSWORD

Setelah selesai: hapus file base64 dan simpan .jks di tempat aman.
  rm -f $B64_FILE
------------------------------------------------------------------
EOF
