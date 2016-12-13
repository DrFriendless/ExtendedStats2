package com.drfriendless.stats2.model

import com.drfriendless.statsdb.database.Plays
import com.google.gson.JsonObject
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

    override fun <T> get(key: Column<T>): T {
        return when (key) {
            Plays.geek -> geek as T
            Plays.game -> game as T
            Plays.playDate -> playDate as T
            Plays.quantity -> quantity as T
            Plays.basegame -> basegame as T
            Plays.raters -> raters as T
            Plays.ratingsTotal -> ratingsTotal as T
            Plays.location -> location as T
            else -> 0 as T
        }
    }

    override fun toJson(vararg omit: Column<*>): JsonObject {
        return toJson(this, Plays.columns, *omit)
    }
}