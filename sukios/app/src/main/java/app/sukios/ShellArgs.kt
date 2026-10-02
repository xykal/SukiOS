package app.sukios

/**
 * ShellArgs — validasi argumen sebelum menjadi argv untuk SukiShell.
 *
 * Allowlist ketat: yang tidak cocok ditolak, tidak dibersihkan. Tidak ada pola yang
 * boleh diawali "-", jadi nilai dari luar tidak bisa menyelinap menjadi opsi perintah
 * (argument injection). Tanpa jangkar ^ dan $: Regex.matches sudah mewajibkan seluruh
 * string cocok, sedangkan $ di Java masih menerima baris baru di ujung.
 */
object ShellArgs {

    private const val MAX_DISPLAY_ID = 999_999

    private val PACKAGE = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)*")
    private val COMPONENT = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)*/\\.?[A-Za-z_][A-Za-z0-9_.\$]*")
    private val PROPERTY = Regex("[A-Za-z0-9][A-Za-z0-9_.\\-]*")

    fun isPackage(s: String): Boolean = PACKAGE.matches(s)

    fun isComponent(s: String): Boolean = COMPONENT.matches(s)

    fun isProperty(s: String): Boolean = PROPERTY.matches(s)

    fun isDisplayId(id: Int): Boolean = id in 0..MAX_DISPLAY_ID
}
