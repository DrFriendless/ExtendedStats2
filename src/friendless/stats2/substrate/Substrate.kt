package friendless.stats2.substrate

import friendless.stats2.Config
import friendless.stats2.database.Database
import friendless.stats2.database.Games
import friendless.stats2.database.GeekGames
import friendless.stats2.model.Game
import friendless.stats2.model.GeekGame
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by john on 30/06/16.
 */
class Substrate(config: Config) {
    private val database = Database(config)
    private val geekGamesByGeek: MutableMap<String, Iterable<GeekGame>> = hashMapOf();
    private val gamesByBggid: MutableMap<Int, Game> = hashMapOf();

    fun collection(geek: String): Iterable<GeekGame> {
        synchronized(geekGamesByGeek) {
            if (!geekGamesByGeek.containsKey(geek)) {
                geekGamesByGeek[geek] = transaction {
                    GeekGames.
                            slice(GeekGames.columns).
                            select { GeekGames.geek eq geek }
                            .map { row -> GeekGame(row) }
                }
            }
            return geekGamesByGeek[geek]!!
        }
    }

    fun games(bggids: Iterable<Int>): Map<Int, Game> {
        synchronized(gamesByBggid) {
            val toGet = bggids.filter { !gamesByBggid.containsKey(it) }
            if (!toGet.isEmpty()) {
                val games = transaction {
                    Games.
                            slice(Games.bggid, Games.name).
                            select { InListOrNotInListOp(Games.bggid, toGet) }
                            .map { row -> Game(row) }
                }
                games.forEach { gamesByBggid[it.bggid] = it }
            }
            toGet.filter { !gamesByBggid.containsKey(it) }.forEach { gamesByBggid[it] = Game(it, "No Such Game") }
            return bggids.associate { it.to(gamesByBggid[it]!!) }
        }
    }
}