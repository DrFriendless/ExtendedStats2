package friendless.stats2.database

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
}

object Geeks: Table("geeks") {
    val username = varchar("username", 128)
}

object Expansions: Table("expansions") {
    val basegame = integer("basegame")
    val expansion = integer("expansion")
}