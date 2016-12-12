package com.drfriendless.stats2.httpd.handlers

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import com.drfriendless.stats2.model.toJson
import com.drfriendless.stats2.selectors.Selector
import com.drfriendless.stats2.database.Substrate
import com.drfriendless.stats2.httpd.warPageData

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
                "data" to jsonArray(warPageData(substrate.australians))
        )
    }
}