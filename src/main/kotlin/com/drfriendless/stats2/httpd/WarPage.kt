package com.drfriendless.stats2.httpd

import com.drfriendless.stats2.model.FrontPageGeek
import com.drfriendless.stats2.model.FrontPageGeeksWithRanks
import com.drfriendless.stats2.model.ModelObject
import com.drfriendless.stats2.model.toJson
import com.drfriendless.statsdb.database.FrontPageGeeks
import com.google.gson.JsonObject
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.select

/**
 * Code for the war page.
 *
 * @author John Farrell
 */
fun warPageData(users: Iterable<String>): List<AugmentedModelObject> {
    val rawData = rawFrontPageData(users)
    val allColumns = FrontPageGeeks.columns + FrontPageGeeksWithRanks.columns
    val dataWithRanks = rawData.map { AugmentedModelObject(it, allColumns) }
    addRanks(dataWithRanks, FrontPageGeeks.owned, FrontPageGeeksWithRanks.ownedRank, "Games Owned")
    addRanks(dataWithRanks, FrontPageGeeks.totalPlays, FrontPageGeeksWithRanks.totalPlaysRank, "Plays Recorded")
    addRanks(dataWithRanks, FrontPageGeeks.distinctGames, FrontPageGeeksWithRanks.distinctGames, "Distinct Games Played")
    addRanks(dataWithRanks, FrontPageGeeks.want, FrontPageGeeksWithRanks.wantRank, "Games on Want List")
    addRanks(dataWithRanks, FrontPageGeeks.wish, FrontPageGeeksWithRanks.wishRank, "Games on Wish List")
    addRanks(dataWithRanks, FrontPageGeeks.hindex, FrontPageGeeksWithRanks.hindexRank, "H-Index")
    addRanks(dataWithRanks, FrontPageGeeks.trade, FrontPageGeeksWithRanks.tradeRank, "Games for Trade")
    addRanks(dataWithRanks, FrontPageGeeks.sdj, FrontPageGeeksWithRanks.sdjRank, "Spiel des Jahre Winners Played")
    addRanks(dataWithRanks, FrontPageGeeks.top50, FrontPageGeeksWithRanks.top50Rank, "Top 50 Games Played")
    addRanks(dataWithRanks, FrontPageGeeks.ext100, FrontPageGeeksWithRanks.ext100Rank, "Extended Stats Top 100 Games Played")
    addRanks(dataWithRanks, FrontPageGeeks.mv, FrontPageGeeksWithRanks.mvRank, "Most Voted For Games Played")
    addRanks(dataWithRanks, FrontPageGeeks.prevOwned, FrontPageGeeksWithRanks.prevOwnedRank, "Games Previously Owned")
    addRanks(dataWithRanks, FrontPageGeeks.friendless, FrontPageGeeksWithRanks.friendlessRank, "Friendless Metric")
    addRanks(dataWithRanks, FrontPageGeeks.cfm, FrontPageGeeksWithRanks.cfmRank, "Continuous Friendless Metric")
    addRanks(dataWithRanks, FrontPageGeeks.zeros, FrontPageGeeksWithRanks.zerosRank, "Games Owned Played Zero Times")
    addRanks(dataWithRanks, FrontPageGeeks.tens, FrontPageGeeksWithRanks.tensRank, "Games Owned Played 10 Times")
    return dataWithRanks
}

private fun rawFrontPageData(users: Iterable<String>): List<FrontPageGeek> {
    val us = users.toList()
    return FrontPageGeeks.slice(FrontPageGeeks.columns).select { FrontPageGeeks.geek inList us }.
            map(::FrontPageGeek).
            sortedBy { it.geek.toLowerCase() }
}

class AugmentedModelObject(val geek: ModelObject, val allColumns: Iterable<Column<*>>) : ModelObject {
    val extraProps = mutableMapOf<Column<*>, Any>()

    override operator fun <T> get(key: Column<T>): T {
        return (extraProps[key] ?: geek[key]) as T
    }

    operator fun <T> set(column: Column<T>, value: T) {
        extraProps[column] = value as Any
    }

    override fun toJson(vararg omit: Column<*>): JsonObject {
        return toJson(this, allColumns, *omit)
    }
}

fun <T: Comparable<T>> addRanks(data: List<AugmentedModelObject>, valCol: Column<T>, rankCol: Column<String>, title: String) {
    val sorted = data.sortedBy { it[valCol] }
    val numRows = sorted.size
    var lastValue = -1000
    var lastRank = 0
    sorted.forEachIndexed { index, t ->
        val key = t[valCol] as Int
        if (key == lastValue) {
            val percent = Math.ceil(lastRank * 100.0 / numRows)
            t[rankCol] = "$lastRank (top $percent% of $title)"
        } else {
            lastRank++
            val percent = Math.ceil(lastRank * 100.0 / numRows)
            t[rankCol] = "$lastRank (top $percent% of $title)"
            lastValue = key
        }
    }
}