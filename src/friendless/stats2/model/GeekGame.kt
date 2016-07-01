package friendless.stats2.model

import com.google.gson.JsonObject
import friendless.stats2.database.GeekGames
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

/**
 * Created by john on 30/06/16.
 */
class GeekGame(var geek: String, var game: Int, var rating: Double,
               var owned: Boolean, var want: Boolean, var trade: Boolean,
               var wish: Int, var comment: String, var prevowned: Boolean,
               var wanttobuy: Boolean, var wanttoplay: Boolean, var preordered: Boolean): ModelObject {
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

    override fun <T> get(key: Column<T>): Any {
        return when (key) {
            GeekGames.geek -> geek
            GeekGames.game -> game
            GeekGames.rating -> rating
            GeekGames.owned -> owned
            GeekGames.want -> want
            GeekGames.trade -> trade
            GeekGames.wish -> wish
            GeekGames.comment -> comment
            GeekGames.prevowned -> prevowned
            GeekGames.wanttobuy -> wanttobuy
            GeekGames.wanttoplay -> wanttoplay
            GeekGames.preordered -> preordered
            else -> 0
        }
    }

    override fun toJson(vararg omit: Column<*>): JsonObject {
        return toJson(this, GeekGames.columns, *omit)
    }
}