package com.drfriendless.stats2.httpd

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.drfriendless.stats2.Config
import com.drfriendless.stats2.database.*
import com.drfriendless.stats2.httpd.handlers.JsonHandler
import com.drfriendless.stats2.model.toJson
import com.drfriendless.stats2.selectors.parseSelector
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import org.wasabifx.wasabi.app.AppConfiguration
import org.wasabifx.wasabi.app.AppServer
import org.wasabifx.wasabi.protocol.http.Response
import org.wasabifx.wasabi.protocol.http.StatusCodes
import org.wasabifx.wasabi.routing.RouteHandler
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import javax.activation.MimetypesFileTypeMap

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

fun extractDatabase(config: Config) {
    val u = Substrate::class.java.getResource("/data/stats2db.jar")
    if (u == null) {
        System.err.println("Database file not found. Looking for /data/stats2db.jar on the classpath.")
    } else {
        System.err.println("Extracting database.")
        Database(config)
        transaction {
            listOf(GeekGames, Games, Plays, Users, Expansions).forEach { table ->
                if (table.exists()) SchemaUtils.drop(table)
            }
        }
        val jf = JarFile(u.file)
        val csv = Regex("(^[a-z]+).csv")
        jf.entries().iterator().forEach { entry ->
            println("Found ${entry.name}.")
            transaction {
                warnLongQueriesDuration = 10000
                csv.matchEntire(entry.name)?.let {
                    extractEntry(entry, jf)
                    when (it.groupValues[1]) {
                        "expansions" -> exec("create memory table expansions (basegame int, expansion int) as select * from csvread('/tmp/expansions.csv', 'BASEGAME,EXPANSION')")
                        "games" -> exec("create memory table games (bggid int primary key, name varchar(256), minplayers int, maxplayers int) as select * from csvread('/tmp/games.csv', 'BGGID,NAME,MINPLAYERS,MAXPLAYERS', 'escape=\\')")
                        "geekgames" -> exec("create memory table geekgames (geek varchar(256), game int, rating real, owned boolean, want boolean, wish int, trade boolean, comment varchar(1024), prevowned boolean, wanttobuy boolean, wanttoplay boolean, preordered boolean) as select * from csvread('/tmp/geekgames.csv', 'GEEK,GAME,RATING,OWNED,WANT,WISH,TRADE,COMMENT,PREVOWNED,WANTTOBUY,WANTTOPLAY,PREORDERED')")
                        "plays" -> exec("create memory table plays (game int, geek varchar(256), playdate varchar(10), quantity int, basegame int, raters int, ratingstotal int, location varchar(256)) as select * from csvread('/tmp/plays.csv', 'GAME,GEEK,PLAYDATE,QUANTITY,BASEGAME,RATERS,RATINGSTOTAL,LOCATION')")
                        "users" -> exec("create memory table users (geek varchar(128), bggid int, country varchar(64)) as select * from csvread('/tmp/users.csv', 'GEEK,BGGID,COUNTRY')")
                        else ->
                            System.err.println("Found unknown CSV file: ${entry.name}")
                    }
                }
            }
        }
        transaction {
            println("Indexing data")
            exec("CREATE UNIQUE HASH INDEX GAMESPK ON GAMES(BGGID)")
            exec("CREATE HASH INDEX PLAYSGEEK ON PLAYS(GEEK)")
            exec("CREATE HASH INDEX GEEKGAMESGEEK ON GEEKGAMES(GEEK)")
        }
    }
}

private fun extractEntry(entry: JarEntry, jf: JarFile) {
    val f = File("/tmp/${entry.name}")
    val fos = FileOutputStream(f)
    val str = jf.getInputStream(entry)
    while (str.available() > 0) {
        fos.write(str.read())
    }
    fos.close()
    str.close()
}

/**
 * Web server main process.
 */
fun main(args: Array<String>) {
    if (args.size == 0) {
        println("Usage: stats2 <configFile>")
        return
    }
    val configFile = args[0]
    val config = Config(configFile)
    if ("true" == config.extract) extractDatabase(config)
    val httpdConfig = AppConfiguration()
    if (System.getenv("PORT") != null) {
        // use the port assigned by Heroku.
        httpdConfig.port = Integer.parseInt(System.getenv("PORT"))
    }
    println("httpd running on port ${httpdConfig.port}")
    val server = AppServer(httpdConfig)
    val logger = LoggerFactory.getLogger("main")
    server.get("/", {
        response.redirect("/chooser")
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
        response.returnFileContents("/images/stats.gif", "image/gif")
    })
    // static javascript files
    server.getLogError("/js/:file", {
        response.returnFileContents(request.path, "application/javascript")
    })
    // static CSS files
    server.getLogError("/css/:file", {
        response.returnFileContents(request.path, "text/css")
    })
    // HTML for a user's collection
    server.getLogError("/collection/:userid", {
        response.returnFileContents("/html/collection.html", "text/html")
    })
    // HTML for the Chooser application
    server.getLogError("/chooser", {
        response.returnFileContents("/html/chooser.html", "text/html")
    })

    logger.info("Starting Stats2Server")
    server.start()
}

fun Response.returnFileContents(path: String, contentType: String) {
    val u = Substrate::class.java.getResource(path)
    println("returnFileContents $path $u")
    if (u != null) {
        val text = u.readText()
        this.contentLength = text.length.toLong()
        send(text, contentType)
    } else {
        setStatus(StatusCodes.NotFound)
    }
}

private fun serveFile(path: String): String {
    val u = Substrate::class.java.getResource(path)
    println("serveFile $path $u")
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

