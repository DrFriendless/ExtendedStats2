package friendless.stats2.database

import friendless.stats2.Config
import org.jetbrains.exposed.sql.*

/**
 * Created by john on 29/06/16.
 */
class Database(config: Config) {
    init {
        val url = "jdbc:mysql://${config.dbHost}:${config.dbPort}/${config.dbName}"
        val db = org.jetbrains.exposed.sql.Database.connect(url, "com.mysql.jdbc.Driver", config.dbUser, config.dbPasswd)
    }

    fun readTable(table: Table, where: Op<Boolean>): Pair<String, List<Pair<String, List<Pair<String, String>>>>> {
        table.select(where)
        return Pair(table.tableName, listOf())
    }
}

fun float(name: String, scale: Int, precision: Int): Column<Float> =
        GeekGames.registerColumn(name, FloatColumnType(scale, precision))

class FloatColumnType(val scale: Int, val precision: Int): ColumnType() {
    override fun sqlType(): String = "FLOAT($scale, $precision)"
    override fun valueFromDB(value: Any): Any = super.valueFromDB(value).let { (it as? Float) ?: it }
}
