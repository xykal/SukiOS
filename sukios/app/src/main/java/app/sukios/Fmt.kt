package app.sukios

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Format yang hasilnya tidak bergantung pada zona waktu atau bahasa perangkat. */
object Fmt {

    private val UTC_STAMP: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT).withZone(ZoneOffset.UTC)

    /** Contoh: "2026-10-02 15:40:00". Selalu UTC, jadi label "UTC" di laporan tidak berbohong. */
    fun utcStamp(epochMillis: Long): String = UTC_STAMP.format(Instant.ofEpochMilli(epochMillis))
}
