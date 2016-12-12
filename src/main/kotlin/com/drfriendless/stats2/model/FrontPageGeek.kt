package com.drfriendless.stats2.model

import com.drfriendless.statsdb.database.FrontPageGeeks
import com.drfriendless.statsdb.database.Games
import com.google.gson.JsonObject
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

/**
 * DTO for a geek's entry in the front page data table.
 *
 * @author John Farrell
 */
class FrontPageGeek(val geek: String, val totalPlays: Int, val distinctGames: Int, val top50: Int, val the100: Int,
                    val owned: Int, val want: Int, val wish: Int, val trade: Int, val prevOwned: Int, val friendless: Int,
                    val cfm: Float, val utilisation: Float, val tens: Int, val zeros: Int, val ext100: Int, val mv: Int,
                    val hindex: Int): ModelObject {
    constructor(row: ResultRow): this(
            row[FrontPageGeeks.geek], row[FrontPageGeeks.totalPlays], row[FrontPageGeeks.distinctGames],
            row[FrontPageGeeks.top50], row[FrontPageGeeks.the100], row[FrontPageGeeks.owned], row[FrontPageGeeks.want],
            row[FrontPageGeeks.wish], row[FrontPageGeeks.trade], row[FrontPageGeeks.prevOwned], row[FrontPageGeeks.friendless],
            row[FrontPageGeeks.cfm], row[FrontPageGeeks.utilisation], row[FrontPageGeeks.tens], row[FrontPageGeeks.zeros],
            row[FrontPageGeeks.ext100], row[FrontPageGeeks.mv], row[FrontPageGeeks.hindex]) {
    }

    override fun <T> get(key: Column<T>): Any {
        return when (key) {
            FrontPageGeeks.geek -> geek
            FrontPageGeeks.totalPlays -> totalPlays
            FrontPageGeeks.distinctGames -> distinctGames
            FrontPageGeeks.top50 -> top50
            FrontPageGeeks.the100 -> the100
            FrontPageGeeks.owned -> owned
            FrontPageGeeks.want -> want
            FrontPageGeeks.wish -> wish
            FrontPageGeeks.trade -> trade
            FrontPageGeeks.prevOwned -> prevOwned
            FrontPageGeeks.friendless -> friendless
            FrontPageGeeks.cfm -> cfm
            FrontPageGeeks.utilisation -> utilisation
            FrontPageGeeks.tens -> tens
            FrontPageGeeks.zeros -> zeros
            FrontPageGeeks.ext100 -> ext100
            FrontPageGeeks.mv -> mv
            FrontPageGeeks.hindex -> hindex
            else -> 0
        }
    }

    override fun toJson(vararg omit: Column<*>): JsonObject {
        return toJson(this, Games.columns, *omit)
    }
}