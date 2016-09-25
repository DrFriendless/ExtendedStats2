package com.drfriendless.stats2.model

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject
import com.drfriendless.stats2.database.Games
import com.drfriendless.stats2.database.GeekGames
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

/**
 * Created by john on 30/06/16.
 */
class Game(val bggid: Int, val name: String, val minPlayers: Int, val maxPlayers: Int, val playTime: Int,
           val geekGames: MutableMap<String, GeekGame> = mutableMapOf(),
           val plays: MutableMap<String, Int> = mutableMapOf()): ModelObject {
    var score = 0

    constructor(row: ResultRow): this(
            row[Games.bggid],
            row[Games.name],
            row[Games.minPlayers],
            row[Games.maxPlayers],
            row[Games.playTime]) {
    }

    override fun <T> get(key: Column<T>): Any {
        return when (key) {
            Games.bggid -> bggid
            Games.name -> name
            Games.minPlayers -> minPlayers
            Games.maxPlayers -> maxPlayers
            Games.playTime -> playTime
            else -> 0
        }
    }

    fun addGeekGame(gg: GeekGame) {
        geekGames[gg.geek] = gg
    }

    fun forGeek(geek: String): GeekGame? = geekGames[geek]

    fun playsForGeek(geek: String): Int = plays[geek] ?: 0

    override fun toJson(vararg omit: Column<*>): JsonObject {
        val result = toJson(this, Games.columns, *omit)
        val geeks = jsonArray(geekGames.values.map { toJson(it, GeekGames.columns, GeekGames.game) })
        result.add("geeks", geeks)
        val plays = jsonObject(plays.entries.map { it.key to it.value })
        result.add("plays", plays)
        return result
    }

    fun setPlays(geek: String, count: Int) {
        plays[geek] = count
    }
}