package com.drfriendless.stats2.httpd.handlers

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import com.drfriendless.stats2.model.toJson
import com.drfriendless.stats2.selectors.Selector
import com.drfriendless.stats2.database.Substrate
import com.drfriendless.stats2.httpd.warPageData
import com.google.gson.JsonObject
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

/**
 * Handler for requests which return JSON.
 */
class JsonHandler(val substrate: Substrate) {
    fun games(selector: Selector): JsonElement {
        return toJson(selector.select())
    }

    fun geeks(): JsonElement {
        return jsonObject(
                "geeks" to jsonArray(substrate.australians)
        )
    }

    fun war(): JsonElement {
        return jsonObject(
                // TODO
                "data" to jsonArray(warPageData(substrate.australians).map { it.toJson() })
        )
    }

    fun newGames(yearParam: String?, usersParam: String?): JsonElement {
        if (yearParam == null) throw BadRequestException("No start year given")
        if (usersParam == null) throw BadRequestException("No users given")
        val startYear = try {
            Integer.parseInt(yearParam)
        } catch (ex: NumberFormatException) {
            throw BadRequestException("Invalid year")
        }
        val users = usersParam.replace("%20", " ").split(",").map(String::trim)
        if (users.isEmpty()) throw BadRequestException("No users specified.")
        val series = mutableListOf<JsonObject>()
        val cal = GregorianCalendar()
        users.forEach { user ->
            transaction {
                val firstPlays = substrate.firstPlays(user)
                substrate.games(firstPlays.map { it.game })
                val data = mutableListOf<JsonObject>()
                var count = 0
                firstPlays.forEach { play ->
                    count++
                    cal.time = play.playDate
                    if (cal[Calendar.YEAR] >= startYear) {
                        val game = transaction { substrate.game(play.game) }
                        if (game != null) {
                            val entry = jsonObject(Pair("x", play.playDate.time), Pair("y", count), Pair("name", game.name))
                            data.add(entry)
                        }
                    }
                }
                series.add(jsonObject(Pair("name", user), Pair("data", jsonArray(data))))
            }
        }
        return jsonArray(series)
    }
}