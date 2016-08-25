package friendless.stats2.database

import friendless.stats2.Config
import org.jetbrains.exposed.sql.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Connecting to the database and enhancing the given functionality. Nothing application-specific here.
 */
open class Database(config: Config) {
    companion object {
        // make sure we only initialise once because of a bug in exposed.
        val initialised = AtomicBoolean(false)
    }
    init {
        if (!initialised.get()) {
            val url = "jdbc:mysql://${config.dbHost}:${config.dbPort}/${config.dbName}?serverTimezone=${config.serverTimeZone}"
            org.jetbrains.exposed.sql.Database.connect(url, "com.mysql.cj.jdbc.Driver", config.dbUser, config.dbPasswd)
            initialised.set(true)
        }
    }
}

fun float(name: String, scale: Int, precision: Int): Column<Float> =
        GeekGames.registerColumn(name, FloatColumnType(scale, precision))

class FloatColumnType(val scale: Int, val precision: Int): ColumnType() {
    override fun sqlType(): String = "FLOAT($scale, $precision)"
    override fun valueFromDB(value: Any): Any = super.valueFromDB(value).let { (it as? Float) ?: it }
}
