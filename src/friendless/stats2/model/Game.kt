package friendless.stats2.model

import com.google.gson.JsonObject
import friendless.stats2.database.Games
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

/**
 * Created by john on 30/06/16.
 */
class Game(val bggid: Int, val name: String): ModelObject {
    constructor(row: ResultRow): this(
            row[Games.bggid],
            row[Games.name]) {
    }

    override fun <T> get(key: Column<T>): Any {
        return when (key) {
            Games.bggid -> bggid
            Games.name -> name
            else -> 0
        }
    }

    override fun toJson(vararg omit: Column<*>): JsonObject {
        return toJson(this, Games.columns, *omit)
    }
}