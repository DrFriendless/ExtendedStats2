package friendless.stats2.database

import org.jetbrains.exposed.sql.Table

/**
 * Created by john on 29/06/16.
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