package friendless.stats2.substrate

import friendless.stats2.Config
import friendless.stats2.database.Database
import friendless.stats2.database.Games
import friendless.stats2.database.GeekGames
import friendless.stats2.database.Geeks
import friendless.stats2.model.Game
import friendless.stats2.model.Geek
import friendless.stats2.model.GeekGame
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Caching layer above the database.
 */
class Substrate(config: Config): Database(config) {
    private val geekGamesByGeek: MutableMap<String, Iterable<Game>> = hashMapOf()
    private val gamesByBggid: MutableMap<Int, Game> = hashMapOf()
    val geeks: Iterable<Geek> by lazy {
        transaction {
            Geeks.slice(Geeks.username).selectAll().map { row-> Geek(row) }.toList()
        }
    }

    fun collection(geek: String): Iterable<Game> {
        synchronized(geekGamesByGeek) {
            var games = geekGamesByGeek[geek]
            if (games == null) {
                val collection = transaction {
                    GeekGames.
                            slice(GeekGames.columns).
                            select { GeekGames.geek eq geek }.
                            map { row -> GeekGame(row) }.
                            toList()
                }
                val ggByGameId = collection.associate { it.game to it }
                games = games(ggByGameId.keys).values
                geekGamesByGeek[geek] = games
                games.forEach {
                    val gg = ggByGameId[it.bggid]
                    if (gg != null) it.addGeekGame(gg)
                }
            }
            return games
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