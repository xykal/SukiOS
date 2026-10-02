# Security Policy / Kebijakan Keamanan

## English

### Supported versions

SukiOS is alpha software. Only the latest tagged pre-release (currently `v0.3.1-alpha`) receives fixes.
Releases are debug-signed until the signing secrets are configured; do not distribute them.

### Reporting a vulnerability

Use GitHub private vulnerability reporting: repository **Security** tab, **Report a vulnerability**. Please do not open a public issue for a vulnerability.
Include the version or commit, device and Android version, reproduction steps, and the impact you observed.

Targets (solo maintainer, best effort, no bounty): acknowledgement within 72 hours; fix or mitigation for HIGH and CRITICAL reports within 14 days.

### Scope

In scope: SukiShell (the user service that runs commands as uid 2000 or 0 through Shizuku), `ShellArgs` and `ShellExec`, the Shizuku provider declaration,
the overlay service, release signing, and the workflows in `.github/workflows`.
Out of scope: Shizuku itself, Android platform behavior, modified operating systems, and anything that requires the user to type commands into the Terminal window
(it is a terminal by design).

### What is enforced, and where it is proven

| Control | Proving test (CI) |
|---|---|
| Commands run as an argument list, never through a shell | `ShellExecTest.arguments_are_never_interpreted_by_a_shell` |
| 15 s deadline; hanging commands are killed (code 124) | `ShellExecTest.hanging_command_is_killed_at_the_deadline` |
| 64 KiB cap per output stream, reply far below the binder limit | `ShellExecTest.output_is_capped_and_marked_as_truncated` |
| Package, component, property names pass an allowlist; nothing starts with a dash | `ShellArgsTest` |
| Actions pinned by commit SHA; no attacker-controlled input inside scripts | `tools/check_workflows.py --self-test` and the real run |

### Known limits

- SukiShell does not yet verify the calling UID. The binder is only handed to the SukiOS process, so exposure is low, but there is no second layer.
- The app has not been tested on a physical device yet.

## Bahasa Indonesia

### Versi yang didukung

SukiOS masih alpha. Hanya pra-rilis bertag terbaru (saat ini `v0.3.1-alpha`) yang mendapat perbaikan.
Rilis ditandatangani debug key sampai secrets signing diset; jangan didistribusikan.

### Melaporkan celah

Pakai pelaporan celah privat GitHub: tab **Security** repo, **Report a vulnerability**. Jangan membuka issue publik untuk celah keamanan.
Sertakan versi atau commit, perangkat dan versi Android, langkah mengulang, dan dampak yang terlihat.

Target (pengelola tunggal, usaha terbaik, tanpa hadiah): konfirmasi dalam 72 jam; perbaikan atau mitigasi untuk laporan HIGH dan CRITICAL dalam 14 hari.

### Cakupan

Masuk cakupan: SukiShell (user service yang menjalankan perintah sebagai uid 2000 atau 0 lewat Shizuku), `ShellArgs` dan `ShellExec`, deklarasi provider Shizuku,
layanan overlay, signing rilis, dan workflow di `.github/workflows`.
Di luar cakupan: Shizuku itu sendiri, perilaku platform Android, sistem operasi yang dimodifikasi, dan apa pun yang mengharuskan pengguna mengetik perintah
di jendela Terminal (memang terminal).

### Batas yang diketahui

- SukiShell belum memeriksa UID pemanggil. Binder hanya diserahkan ke proses SukiOS, jadi paparannya kecil, tetapi belum ada lapisan kedua.
- Aplikasi belum diuji di perangkat fisik.

Built by xykal — XyVerse Technology Global
