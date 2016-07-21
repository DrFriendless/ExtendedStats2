package friendless.stats2.model

import com.google.gson.JsonObject
import friendless.stats2.database.Geeks
import friendless.stats2.database.Users
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

/**
 * Created by john on 21/07/16.
 */
class User(val name: String, val bggid: Int, val country: String): ModelObject {
    constructor(row: ResultRow): this(
            row[Users.geek], row[Users.bggid], row[Users.country]) {
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