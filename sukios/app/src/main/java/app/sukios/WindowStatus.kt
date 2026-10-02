package app.sukios

/** Hasil uji jendela yang disimpan di preferensi. [verdict]: OK, FULLSCREEN, atau NOT_STARTED. */
data class ProbeRecord(val verdict: String, val at: Long, val note: String) {
    val ok: Boolean get() = verdict == VERDICT_OK

    companion object {
        const val VERDICT_OK = "OK"
        const val VERDICT_FULLSCREEN = "FULLSCREEN"
        const val VERDICT_NOT_STARTED = "NOT_STARTED"
    }
}

data class WindowStatus(val label: String, val tone: Tone, val detail: String)

/**
 * WindowStatus — satu kalimat jujur tentang kesiapan jendela, dari fakta yang diukur. Murni, diuji di JVM.
 * Kata "aktif" hanya muncul bila uji jendela benar-benar lulus di perangkat ini.
 */
object WindowStatusLogic {

    fun formatProbe(verdict: String, at: Long, note: String): String =
        "$verdict|$at|${note.replace('|', '/').replace('\n', ' ')}"

    fun parseProbe(raw: String): ProbeRecord? {
        if (raw.isBlank()) return null
        val parts = raw.split("|", limit = 3)
        if (parts.size < 2) return null
        val verdict = parts[0]
        if (verdict !in setOf(ProbeRecord.VERDICT_OK, ProbeRecord.VERDICT_FULLSCREEN, ProbeRecord.VERDICT_NOT_STARTED)) return null
        val at = parts[1].toLongOrNull() ?: return null
        return ProbeRecord(verdict, at, parts.getOrElse(2) { "" })
    }

    fun of(
        installed: Boolean,
        alive: Boolean,
        granted: Boolean,
        bound: Boolean,
        coreOn: Boolean,
        setupRunning: Boolean,
        probe: ProbeRecord?,
    ): WindowStatus = when {
        !installed -> WindowStatus("Shizuku belum terpasang", Tone.WARN, "Pasang aplikasi Shizuku, lalu jalankan.")
        !alive -> WindowStatus("Shizuku belum berjalan", Tone.WARN, "Buka Shizuku dan mulai lewat Debugging nirkabel.")
        !granted -> WindowStatus("Menunggu izin Shizuku", Tone.WARN, "Setujui dialog izin dari Shizuku.")
        !bound -> WindowStatus("Menyambungkan SukiShell", Tone.INFO, "Sebentar lagi siap.")
        setupRunning -> WindowStatus("Menyiapkan mode jendela", Tone.INFO, "Menerapkan setelan lewat Shizuku.")
        !coreOn -> WindowStatus("Mode jendela belum aktif", Tone.WARN, "Jalankan persiapan otomatis.")
        probe == null -> WindowStatus("Jendela siap, belum diuji", Tone.INFO, "Jalankan uji jendela untuk membuktikannya.")
        probe.ok -> WindowStatus("Jendela aktif", Tone.OK, "Terbukti: uji jendela lulus di perangkat ini.")
        probe.verdict == ProbeRecord.VERDICT_FULLSCREEN ->
            WindowStatus("Perangkat menolak jendela", Tone.ERR, "Mulai ulang HP sekali (beberapa ROM menerapkan mode jendela saat boot), lalu uji lagi.")
        else -> WindowStatus("Uji jendela gagal dimulai", Tone.WARN, "Ulangi uji; bila tetap gagal, salin laporan diagnostik.")
    }
}
