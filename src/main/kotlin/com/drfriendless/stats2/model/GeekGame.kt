package com.drfriendless.stats2.model

import com.google.gson.JsonObject
import com.drfriendless.statsdb.database.GeekGames
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

/**
 * Created by john on 30/06/16.
 */
class GeekGame(val geek: String, val game: Int, val rating: Double,
               val owned: Boolean, val want: Boolean, val trade: Boolean,
               val wish: Int, val comment: String, val prevowned: Boolean,
               val wanttobuy: Boolean, val wanttoplay: Boolean, val preordered: Boolean): ModelObject {
    constructor(row: ResultRow): this(
            row[GeekGames.geek],
            row[GeekGames.game],
            row[GeekGames.rating].toDouble(),
            row[GeekGames.owned],
            row[GeekGames.want],
            row[GeekGames.trade],
            row[GeekGames.wish],
            row[GeekGames.comment],
            row[GeekGames.prevowned],
            row[GeekGames.wanttobuy],
            row[GeekGames.wanttoplay],
            row[GeekGames.preordered]
            ) {
    }

    override fun <T> get(key: Column<T>): T {
        return when (key) {
            GeekGames.geek -> geek as T
            GeekGames.game -> game as T
            GeekGames.rating -> (Math.round(rating * 10) / 10.0) as T
            GeekGames.owned -> owned as T
            GeekGames.want -> want as T
            GeekGames.trade -> trade as T
            GeekGames.wish -> wish as T
            GeekGames.comment -> comment as T
            GeekGames.prevowned -> prevowned as T
            GeekGames.wanttobuy -> wanttobuy as T
            GeekGames.wanttoplay -> wanttoplay as T
            GeekGames.preordered -> preordered as T
            else -> 0 as T
        }
    }

    override fun toJson(vararg omit: Column<*>): JsonObject {
        return toJson(this, GeekGames.columns, *omit)
    }
}