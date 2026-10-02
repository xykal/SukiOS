package app.sukios

/**
 * Sumber tunggal merek. Dibaca oleh jendela Tentang dan laporan diagnostik; BrandTest
 * mengunci tulisannya persis supaya tidak ada yang mengubah atau menerjemahkannya.
 *
 * Hanya awalan "Powered by" yang boleh dilokalkan (lihat strings.xml). Nama perusahaan
 * ditulis persis seperti ini di semua permukaan.
 */
object Brand {
    const val COMPANY = "XyVerse Technology Global"
    const val AUTHOR = "xykal"
    const val YEAR = 2026

    const val CREDIT = "$AUTHOR — $COMPANY"
    const val COPYRIGHT = "Copyright (c) $YEAR $CREDIT"

    fun poweredBy(prefix: String): String = "$prefix $COMPANY"
}
