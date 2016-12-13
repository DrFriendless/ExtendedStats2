package com.drfriendless.stats2.model

import com.drfriendless.statsdb.database.FrontPageGeeks
import com.drfriendless.statsdb.database.Games
import com.google.gson.JsonObject
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

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

    override fun <T> get(key: Column<T>): T {
        return when (key) {
            FrontPageGeeks.geek -> geek as T
            FrontPageGeeks.totalPlays -> totalPlays as T
            FrontPageGeeks.distinctGames -> distinctGames as T
            FrontPageGeeks.top50 -> top50 as T
            FrontPageGeeks.the100 -> the100 as T
            FrontPageGeeks.owned -> owned as T
            FrontPageGeeks.want -> want as T
            FrontPageGeeks.wish -> wish as T
            FrontPageGeeks.trade -> trade as T
            FrontPageGeeks.prevOwned -> prevOwned as T
            FrontPageGeeks.friendless -> friendless as T
            FrontPageGeeks.cfm -> cfm as T
            FrontPageGeeks.utilisation -> utilisation as T
            FrontPageGeeks.tens -> tens as T
            FrontPageGeeks.zeros -> zeros as T
            FrontPageGeeks.ext100 -> ext100 as T
            FrontPageGeeks.mv -> mv as T
            FrontPageGeeks.hindex -> hindex as T
            else -> 0 as T
        }
    }

    override fun toJson(vararg omit: Column<*>): JsonObject {
        return toJson(this, Games.columns, *omit)
    }
}

object FrontPageGeeksWithRanks : Table("notreallyatableatall") {
    val ownedRank = varchar("ownedRank", 128)
    val totalPlaysRank = varchar("playsRank", 128)
    val distinctGames = varchar("distinctRank", 128)
    val wantRank = varchar("wantRank", 128)
    val wishRank = varchar("wishRank", 128)
    val hindexRank = varchar("hindexRank", 128)
    val tradeRank = varchar("tradeRank", 128)
    val sdjRank = varchar("sdjRank", 128)
    val top50Rank = varchar("top50Rank", 128)
    val ext100Rank = varchar("ext100Rank", 128)
    val mvRank = varchar("mvRank", 128)
    val prevOwnedRank = varchar("prevOwnedRank", 128)
    val friendlessRank = varchar("friendlessRank", 128)
    val cfmRank = varchar("cfmRank", 128)
    val zerosRank = varchar("zerosRank", 128)
    val tensRank = varchar("tensRank", 128)
}