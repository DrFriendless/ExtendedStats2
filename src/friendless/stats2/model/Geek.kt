package friendless.stats2.model

import com.google.gson.JsonObject
import friendless.stats2.database.Games
import friendless.stats2.database.Geeks
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

/**
 * Created by john on 30/06/16.
 */
class Geek(val name: String): ModelObject {
    constructor(row: ResultRow): this(
            row[Geeks.username]) {
    }

    override fun <T> get(key: Column<T>): Any {
        return when (key) {
            Geeks.username -> name
            else -> 0
        }
    }

    override fun toJson(vararg omit: Column<*>): JsonObject {
        return toJson(this, Geeks.columns, *omit)
    }
}