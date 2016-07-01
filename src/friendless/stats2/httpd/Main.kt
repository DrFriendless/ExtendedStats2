package friendless.stats2.httpd

import friendless.stats2.Config
import friendless.stats2.httpd.handlers.JsonHandler
import friendless.stats2.selectors.*
import friendless.stats2.substrate.Substrate
import org.slf4j.LoggerFactory
import org.wasabi.app.AppConfiguration
import org.wasabi.app.AppServer
import org.wasabi.http.StatusCodes

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
    server.get("/json/geekgames/:userid", {
        try {
            val userId = request.routeParams["userid"]
            if (userId == null) {
                response.setStatus(StatusCodes.BadRequest)
            } else {
                val substrate = Substrate(config)
                val q = request.queryParams["q"] ?: "all"
                val selector = parseSelector(substrate, q)
                response.send(JsonHandler(substrate).geekGames(selector).toString(), "application/json")
            }
        } catch (e: Throwable) {
            logger.error("Brkoen", e)
        }
    })
    server.get("/json/games", {
        try {
            val q = request.queryParams["q"] ?: "all"
            val substrate = Substrate(config)
            val selector = parseSelector(substrate, q)
//                val selector = GameSelectorForGeekGames(substrate, RatedSelector(substrate, userId))
            response.send(JsonHandler(substrate).games(selector).toString(), "application/json")
        } catch (e: Throwable) {
            logger.error("Brekon", e)
        }
    })
    logger.info("Starting Stats2Server")
    server.start()
}
