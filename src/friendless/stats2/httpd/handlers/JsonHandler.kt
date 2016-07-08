package friendless.stats2.httpd.handlers

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import friendless.stats2.model.toJson
import friendless.stats2.selectors.Selector
import friendless.stats2.database.Substrate

/**
 * Handler for requests which return JSON.
 */
class JsonHandler(val substrate: Substrate) {
    fun games(selector: Selector): JsonElement {
        return toJson(selector.select())
    }

    fun geeks(): JsonElement {
        return jsonObject(
                "geeks" to jsonArray(substrate.geeks.map { it.toJson() })
        )
    }
}