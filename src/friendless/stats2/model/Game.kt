package friendless.stats2.model

import com.google.gson.JsonObject
import friendless.stats2.database.Games
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

/**
 * Created by john on 30/06/16.
 */
class Game(val bggid: Int, val name: String, val minPlayers: Int, val maxPlayers: Int,
           val geekGames: MutableMap<String, GeekGame> = mutableMapOf(),
           val plays: MutableMap<String, Int> = mutableMapOf()): ModelObject {
    var score = 0

    constructor(row: ResultRow): this(
            row[Games.bggid],
            row[Games.name],
            row[Games.minPlayers],
            row[Games.maxPlayers]) {
    }

    override fun <T> get(key: Column<T>): Any {
        return when (key) {
            Games.bggid -> bggid
            Games.name -> name
            Games.minPlayers -> minPlayers
            Games.maxPlayers -> maxPlayers
            else -> 0
        }
    }

    fun addGeekGame(gg: GeekGame) {
        geekGames[gg.geek] = gg
    }

    fun forGeek(geek: String): GeekGame? {
        return geekGames[geek]
    }

    fun playsForGeek(geek: String): Int {
        return plays[geek] ?: 0
    }

    override fun toJson(vararg omit: Column<*>): JsonObject {
        return toJson(this, Games.columns, *omit)
    }

    fun setPlays(geek: String, count: Int) {
        plays[geek] = count
    }
}