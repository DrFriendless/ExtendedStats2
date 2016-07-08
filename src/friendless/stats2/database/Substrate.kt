package friendless.stats2.database

import friendless.stats2.Config
import friendless.stats2.model.Game
import friendless.stats2.model.Geek
import friendless.stats2.model.GeekGame
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Caching layer above the database.
 */
class Substrate(config: Config): Database(config) {
    private val name = ThreadLocal<String>()
    init {
        name.set("substrate" + System.currentTimeMillis())
    }

    private val geekGamesByGeek: MutableMap<String, Iterable<Game>> = hashMapOf()
    private val gamesByBggid: MutableMap<Int, Game> = hashMapOf()
    val geeks: Iterable<Geek> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        transaction {
            Geeks.slice(Geeks.username).selectAll().map { row -> Geek(row) }.toList()
        }
    }
    val expansions: Set<Int> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        transaction {
            Expansions.slice(Expansions.expansion).selectAll().distinct().map { row -> row[Expansions.expansion] }.toSet()
        }
    }

    fun collection(geek: String): Iterable<Game> {
        synchronized(geekGamesByGeek) {
            var games = geekGamesByGeek[geek]
            if (games == null) {
                val ggByGameId = GeekGames.
                        slice(GeekGames.columns).
                        select { GeekGames.geek eq geek }.
                        map { row -> GeekGame(row) }.
                        associate { it.game to it }
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

    fun games(bggids: Set<Int>): Map<Int, Game> {
        synchronized(gamesByBggid) {
            val toGet = bggids.filter { !gamesByBggid.containsKey(it) }
            if (!toGet.isEmpty()) {
                val games = Games.
                        slice(Games.columns).
                        select { InListOrNotInListOp(Games.bggid, toGet) }.
                        map { row -> Game(row) }
                games.forEach { gamesByBggid[it.bggid] = it }
                toGet.filter { !gamesByBggid.containsKey(it) }.forEach { gamesByBggid[it] = Game(it, "No Such Game", 1, 6) }
            }
            return bggids.associate { it.to(gamesByBggid[it]!!) }
        }
    }

    fun gamesWhere(where: SqlExpressionBuilder.()-> Op<Boolean>): Iterable<Game> {
        val bggIds =
                Games.slice(Games.bggid).select(where).map { row -> row[Games.bggid] }.toSet()
        return games(bggIds).values
    }
}