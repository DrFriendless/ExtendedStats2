package com.drfriendless.stats2.model

import com.google.gson.JsonObject
import com.drfriendless.stats2.database.Plays
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import java.sql.Date

/**
 * Created by john on 9/07/16.
 */
class Play(val geek: String, val game: Int, val playDate: Date,
           var quantity: Int, val basegame: Int, val raters: Int,
           val ratingsTotal: Int, val location: String,
           val expansions: Set<Int> = setOf()): ModelObject {

    constructor(row: ResultRow) : this(
            row[Plays.geek],
            row[Plays.game],
            Date(row[Plays.playDate].toDate().time),
            row[Plays.quantity],
            row[Plays.basegame],
            row[Plays.raters],
            row[Plays.ratingsTotal],
            row[Plays.location]
    ) {
    }

    override fun <T> get(key: Column<T>): Any {
        return when (key) {
            Plays.geek -> geek
            Plays.game -> game
            Plays.playDate -> playDate
            Plays.quantity -> quantity
            Plays.basegame -> basegame
            Plays.raters -> raters
            Plays.ratingsTotal -> ratingsTotal
            Plays.location -> location
            else -> 0
        }
    }

    override fun toJson(vararg omit: Column<*>): JsonObject {
        return toJson(this, Plays.columns, *omit)
    }
}