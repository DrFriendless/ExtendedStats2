package com.drfriendless.stats2.database

import org.jetbrains.exposed.sql.Table

/**
 * Definitions of tables. Note that we don't create the schema in this project, the schema already exists and is
 * populated by the downloader component of https://github.com/DrFriendless/ExtendedStats.
 */
object GeekGames: Table("geekgames") {
    val geek = varchar("geek", 128)
    val game = integer("game")
    val wish = integer("wish")
    val rating = float("rating", 3, 1)
    val owned = bool("owned")
    val want = bool("want")
    val trade = bool("trade")
    val prevowned = bool("prevowned")
    val wanttobuy = bool("wanttobuy")
    val wanttoplay = bool("wanttoplay")
    val preordered = bool("preordered")
    val comment = varchar("comment", 1024)
}

object Games: Table("games") {
    val bggid = integer("bggid")
    val name = varchar("name", 256)
    val minPlayers = integer("minPlayers")
    val maxPlayers = integer("maxPlayers")
}

object Geeks: Table("geeks") {
    val username = varchar("username", 128)
}

object Users: Table("users") {
    val geek = varchar("geek", 128)
    val bggid = integer("bggid")
    val country = varchar("country", 64)
}

object Expansions: Table("expansions") {
    val basegame = integer("basegame")
    val expansion = integer("expansion")
}

object Plays: Table("plays") {
    val game = integer("game")
    val geek = varchar("geek", 128)
    val playDate = date("playDate")
    val quantity = integer("quantity")
    val basegame = integer("basegame")
    val raters = integer("raters")
    val ratingsTotal = integer("ratingsTotal")
    val location = varchar("location", 256)
}