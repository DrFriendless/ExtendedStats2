package friendless.stats2.httpd

import friendless.stats2.Config
import friendless.stats2.httpd.handlers.JsonHandler
import friendless.stats2.selectors.parseSelector
import friendless.stats2.substrate.Substrate
import org.slf4j.LoggerFactory
import org.wasabi.app.AppConfiguration
import org.wasabi.app.AppServer
import org.wasabi.http.StatusCodes

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
 * Created by john on 29/06/16.
 */
fun main(args: Array<String>) {
    val httpdConfig = AppConfiguration()
    val server = AppServer(httpdConfig)
    val config = Config()
    val logger = LoggerFactory.getLogger("main")
    server.get("/", {
        response.send("Hello World!")
    })
    server.getLogError("/json/geekgames/:userid", {
        val userId = request.routeParams["userid"]
        if (userId == null) {
            response.setStatus(StatusCodes.BadRequest)
        } else {
            val substrate = Substrate(config)

            val q = request.queryParams["q"] ?: "all"
            val selector = parseSelector(substrate, q)
            response.send(JsonHandler(substrate).geekGames(selector, userId).toString(), "application/json")
        }
    })
    server.getLogError("/json/geeks", {
        val substrate = Substrate(config)
        response.send(JsonHandler(substrate).geeks().toString(), "application/json")
    })
    server.getLogError("/json/games", {
        val q = request.queryParams["q"] ?: "all"
        val substrate = Substrate(config)
        val selector = parseSelector(substrate, q)
        response.send(JsonHandler(substrate).games(selector, null).toString(), "application/json")
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


