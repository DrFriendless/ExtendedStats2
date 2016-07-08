package friendless.stats2.httpd

import com.github.salomonbrys.kotson.jsonObject
import friendless.stats2.Config
import friendless.stats2.httpd.handlers.JsonHandler
import friendless.stats2.model.toJson
import friendless.stats2.selectors.parseSelector
import friendless.stats2.database.Substrate
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.wasabi.app.AppConfiguration
import org.wasabi.app.AppServer
import java.sql.Connection

fun AppServer.getLogError(path: kotlin.String, vararg handlers: org.wasabi.routing.RouteHandler.() -> kotlin.Unit): kotlin.Unit {
    val logger = LoggerFactory.getLogger("handler")
    fun wrap(f: org.wasabi.routing.RouteHandler.() -> kotlin.Unit): org.wasabi.routing.RouteHandler.() -> kotlin.Unit {
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
            response.send(result.toString(), "application/json")
        }
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

fun serveFile(path: String): String {
    val u = Substrate::class.java.getResource(path)
    return u?.file ?: "html/error.html"
}


