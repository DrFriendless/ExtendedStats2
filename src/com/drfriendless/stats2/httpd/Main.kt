package com.drfriendless.stats2.httpd

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.drfriendless.stats2.Config
import com.drfriendless.stats2.httpd.handlers.JsonHandler
import com.drfriendless.stats2.model.toJson
import com.drfriendless.stats2.selectors.parseSelector
import com.drfriendless.stats2.database.Substrate
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.wasabi.app.AppConfiguration
import org.wasabi.app.AppServer
import org.wasabi.routing.RouteHandler

fun AppServer.getLogError(path: String, vararg handlers: RouteHandler.() -> Unit): Unit {
    val logger = LoggerFactory.getLogger("handler")
    fun wrap(f: RouteHandler.() -> Unit): RouteHandler.() -> Unit {
        return {
            try {
                f()
            } catch (e: Throwable) {
                logger.error("Broken", e)
            }
        }
    }
    val wrapped = handlers.map { wrap(it) }.toTypedArray()
    this.get(path, *wrapped)
}

/**
 * Web server main process.
 */
fun main(args: Array<String>) {
    val httpdConfig = AppConfiguration()
    if (System.getenv("PORT") != null) {
        // use the port assigned by Heroku.
        httpdConfig.port = Integer.parseInt(System.getenv("PORT"))
    }
    val server = AppServer(httpdConfig)
    val config = Config()
    val logger = LoggerFactory.getLogger("main")
    server.get("/", {
        response.send("Hello World!")
    })
    server.getLogError("/json/geeks", {
        val substrate = Substrate(config)
        response.send(JsonHandler(substrate).geeks().toString(), "application/json")
    })
    server.getLogError("/json/games", {
        val q = request.queryParams["q"] ?: "all"
        val substrate = Substrate(config)
        val selector = parseSelector(substrate, q)
        transaction {
            val games = selector.select().toList()
            val truncated = if (games.size > 100) games.subList(0, 100) else games
            val result = jsonObject("count" to games.size, "games" to toJson(truncated))
            stripJson(result)
            response.send(result.toString(), "application/json")
        }
    })
    // favicon.ico
    server.getLogError("/favicon.ico", {
        response.setFileResponseHeaders(serveFile("/images/stats.gif"), "image/gif")
    })
    // static javascript files
    server.getLogError("/js/:file", {
        response.setFileResponseHeaders(serveFile(request.path), "application/javascript")
    })
    // static CSS files
    server.getLogError("/css/:file", {
        response.setFileResponseHeaders(serveFile(request.path), "text/css")
    })
    // HTML for a user's collection
    server.getLogError("/collection/:userid", {
        response.setFileResponseHeaders(serveFile("/html/collection.html"), "text/html")
    })
    // HTML for the Chooser application
    server.getLogError("/chooser/:userid", {
        response.setFileResponseHeaders(serveFile("/html/chooser.html"), "text/html")
    })

    logger.info("Starting Stats2Server")
    server.start()
}

private fun serveFile(path: String): String {
    val u = Substrate::class.java.getResource(path)
    println(u)
    return u?.file ?: "html/error.html"
}

private fun stripJson(obj: JsonObject) {
    stripGames(obj["games"] as JsonArray)
}

private fun stripGames(games: JsonArray) {
    games.forEach { stripGame(it as JsonObject) }
}

private fun stripGame(game: JsonObject) {
    game.remove("minPlayers")
    game.remove("maxPlayers")
    stripGeeks(game["geeks"] as JsonArray)
}

private fun stripGeeks(geeks: JsonArray) {
    geeks.forEach { stripGeek(it as JsonObject) }
}

private fun stripGeek(geek: JsonObject) {
    geek.remove("comment")
    geek.remove("prevowned")
    geek.remove("preordered")
    geek.remove("trade")
    geek.set("wtb", geek["wanttobuy"])
    geek.set("wtp", geek["wanttoplay"])
    geek.remove("wanttobuy")
    geek.remove("wanttoplay")
    val flags = JsonArray()
    listOf("wtb", "wtp", "want", "owned").forEach {
        if (geek[it].asString == "true") {
            flags.add(it)
        }
        geek.remove(it)
    }
    geek.set("flags", flags)
}

