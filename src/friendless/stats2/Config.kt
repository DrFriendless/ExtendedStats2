package friendless.stats2

import java.util.*

/**
 * Created by john on 29/06/16.
 */
class Config() {
    val prop = Properties()
    val dbUser: String by prop
    val dbPasswd: String by prop
    val dbHost: String by prop
    val dbPort: String by prop
    val dbName: String by prop
    val serverTimeZone: String by prop

    init {
        Config::class.java.getResourceAsStream("/config.properties").use {
            prop.load(it)
        }
    }
}