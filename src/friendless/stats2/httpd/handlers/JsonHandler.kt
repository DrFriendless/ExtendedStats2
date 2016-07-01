package friendless.stats2.httpd.handlers

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import friendless.stats2.database.GeekGames
import friendless.stats2.model.ModelObject
import friendless.stats2.model.toJson
import friendless.stats2.selectors.Selector
import friendless.stats2.substrate.Substrate

/**
 * Created by john on 29/06/16.
 */
class JsonHandler(val substrate: Substrate) {
    fun geekGames(selector: Selector<ModelObject>): JsonElement {
        return postProcess(toJson(selector.select(), GeekGames.geek))
    }

    fun games(selector: Selector<ModelObject>): JsonElement {
        return toJson(selector.select())
    }

    private fun postProcess(ggs: JsonArray): JsonArray {
        val gamesById = substrate.games(ggs.map { (it as JsonObject)[GeekGames.game.name].asInt })
        ggs.forEach {
            val jo = it as JsonObject
            jo["name"] = gamesById[jo[GeekGames.game.name].asInt]?.name
        }
        return ggs
    }
}