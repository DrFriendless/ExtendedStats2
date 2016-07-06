package friendless.stats2.database

import friendless.stats2.Config
import org.jetbrains.exposed.sql.*

/**
 * Created by john on 29/06/16.
 */
open class Database(config: Config) {
    init {
        val url = "jdbc:mysql://${config.dbHost}:${config.dbPort}/${config.dbName}?serverTimezone=${config.serverTimeZone}"
        org.jetbrains.exposed.sql.Database.connect(url, "com.mysql.cj.jdbc.Driver", config.dbUser, config.dbPasswd)
    }
}

fun float(name: String, scale: Int, precision: Int): Column<Float> =
        GeekGames.registerColumn(name, FloatColumnType(scale, precision))

class FloatColumnType(val scale: Int, val precision: Int): ColumnType() {
    override fun sqlType(): String = "FLOAT($scale, $precision)"
    override fun valueFromDB(value: Any): Any = super.valueFromDB(value).let { (it as? Float) ?: it }
}
