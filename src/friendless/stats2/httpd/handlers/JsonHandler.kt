package friendless.stats2.httpd.handlers

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import friendless.stats2.database.GeekGames
import friendless.stats2.model.ModelObject
import friendless.stats2.model.toJson
import friendless.stats2.selectors.Selector
import friendless.stats2.substrate.Substrate

/**
 * Handler for requests which return JSON.
 */
class JsonHandler(val substrate: Substrate) {
    fun geekGames(selector: Selector<ModelObject>, geek: String): JsonElement {
        return postProcess(toJson(selector.select(geek), GeekGames.geek))
    }

    fun games(selector: Selector<ModelObject>, geek: String?): JsonElement {
        return toJson(selector.select(geek))
    }

    private fun postProcess(geekgames: JsonArray): JsonArray {
        val gamesById = substrate.games(geekgames.map { (it as JsonObject)[GeekGames.game.name].asInt })
        geekgames.forEach {
            val jo = it as JsonObject
            jo["name"] = gamesById[jo[GeekGames.game.name].asInt]?.name
        }
        return geekgames
    }

    fun geeks(): JsonElement {
        return jsonObject(
                "geeks" to jsonArray(substrate.geeks.map { it.toJson() })
        )
    }
}