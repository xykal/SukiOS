// ============================================================================
// ISukiShell — kontrak SukiShell, engine akses lanjutan milik SukiOS sendiri.
//
// Cara kerja: Shizuku hanya menjadi kurir binder. Kode di balik kontrak ini
// berjalan di proses terpisah dengan identitas shell (uid 2000, atau uid 0
// kalau pengguna memakai Sui/root). Tidak ada pemanggilan newProcess di mana
// pun — jalur itu sudah dihapus dari API Shizuku 13.x dan memang ditinggalkan.
//
// destroy() WAJIB ada dengan ID 16777114: itu kontrak Shizuku untuk meminta
// UserService membersihkan diri dan keluar.
// ============================================================================

package app.sukios.shell;

interface ISukiShell {

    /** Dipanggil Shizuku saat service harus berhenti. Jangan ubah ID-nya. */
    void destroy() = 16777114;

    /** UID proses ini: 2000 untuk shell/ADB, 0 untuk root/Sui. */
    int getUid() = 1;

    /** PID proses ini, berguna untuk memastikan service benar-benar hidup. */
    int getPid() = 2;

    /** Versi protokol SukiShell, naik kalau kontrak berubah. */
    int getProtocolVersion() = 3;

    /** Ringkasan perangkat dari sudut pandang shell (satu baris per fakta). */
    String deviceSummary() = 4;

    /**
     * Menjalankan satu perintah TANPA shell parsing.
     *
     * argv adalah daftar argumen apa adanya: argv[0] nama program, sisanya
     * argumen. Tidak ada string yang digabung, jadi tidak ada celah injeksi
     * dari input pengguna.
     *
     * Hasil dikodekan sebagai tiga baris:
     *   baris 1 : kode keluar (integer)
     *   baris 2 : stdout, dikodekan Base64
     *   baris 3 : stderr, dikodekan Base64
     */
    String exec(in List<String> argv) = 5;
}
