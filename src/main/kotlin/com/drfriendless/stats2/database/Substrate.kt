package com.drfriendless.stats2.database

import com.drfriendless.stats2.model.*
import com.drfriendless.stats2.Config
import com.drfriendless.statsdb.database.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.sql.Date
import java.util.*

/**
 * Caching layer above the database.
 */
class Substrate(config: Config): Database(config) {
    private val geekGamesByGeek: MutableMap<String, Iterable<Game>> = hashMapOf()
    private val playsByGeek: MutableMap<String, Iterable<Play>> = hashMapOf()
    private val gamesByBggid: MutableMap<Int, Game> = hashMapOf()
    private val baseGamesByExpansion: MutableMap<Int, Set<Int>> = hashMapOf()
    val geeks: Iterable<String> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        transaction {
            Users.slice(Users.geek).selectAll().map { row -> row[Users.geek] }.toSortedSet()
        }
    }
    val australians: Iterable<String> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        transaction {
            Users.
                    slice(Users.geek).
                    select { Users.country inList config.allowedCountries() }.
                    orderBy(Users.geek, true).
                    map { it[Users.geek]}
        }
    }
    val expansionData: List<Pair<Int, Int>> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        transaction {
            Expansions.
                    slice(Expansions.columns).
                    selectAll().
                    map { row -> Pair(row[Expansions.basegame], row[Expansions.expansion]) }
        }
    }
    val expansions: Set<Int> by lazy {
        expansionData.map { it.second }.toSet()
    }

    fun collection(geek: String): Iterable<Game> {
        return geekGamesByGeek.findOrAdd(geek) {
            val ggByGameId = GeekGames.
                    slice(GeekGames.columns).
                    select { GeekGames.geek eq geek }.
                    map(::GeekGame).
                    associate { it.game to it }
            val games = games(ggByGameId.keys).values
            games.forEach {
                g -> ggByGameId[g.bggid] ?.let { gg -> g.addGeekGame(gg) }
            }
            games
        }
    }

    fun baseGamesForExpansion(expansion: Int): Set<Int> {
        return baseGamesByExpansion.findOrAdd(expansion) {
            expansionData.filter { it.second == expansion }.map { it.first }.toSet()
        }
    }

    fun plays(geek: String): Iterable<Play> {
        return playsByGeek.findOrAdd(geek) {
            val never = DateTime(1900,1,1,1,1)
            reconstructPlays(Plays.
                    slice(Plays.columns).
                    select { (Plays.geek eq geek) and (Plays.playDate greater never) }.
                    map(::Play))
        }
    }

    fun firstPlays(geek: String): Iterable<Play> {
        val allPlays = plays(geek).sortedBy { it.playDate }
        val firstPlays = mutableListOf<Play>()
        val found = mutableSetOf<Int>()
        allPlays.forEach { p ->
            if (!found.contains(p.game)) {
                firstPlays.add(p)
                found.add(p.game)
            }
        }
        return firstPlays
    }

    fun reconstructPlays(ps: List<Play>): List<Play> {
        val playsByDate = HashMap<Date, MutableList<Play>>()
        ps.groupByTo(playsByDate) { it.playDate }
        playsByDate.values.forEach { inferExtraPlaysForADate(it) }
        return playsByDate.flatMap { it.value }
    }

    fun inferExtraPlaysForADate(plays: MutableList<Play>) {
        var startSize: Int
        var iterations = 0
        do {
            startSize = plays.size
            val expansionPlays = plays.filter { it.game in expansions }
            var newPlays: MutableList<Play>? = null
            for (expPlay in expansionPlays) {
                // try to find a base game play fot this expansion
                // some of these possible base games could be expansions themselves.
                val possibleBaseGames = baseGamesForExpansion(expPlay.game)
                val candidateBaseGamePlays = plays.filter { it != expPlay &&
                        (it.game in possibleBaseGames || !possibleBaseGames.intersect(it.expansions).isEmpty()) &&
                        expPlay.game !in it.expansions
                }
                candidateBaseGamePlays.firstOrNull()?.let { bgPlay ->
                    val newQuantity = Math.min(expPlay.quantity, bgPlay.quantity)
                    expPlay.quantity -= newQuantity
                    bgPlay.quantity -= newQuantity
                    val newExpansions = bgPlay.expansions + setOf(expPlay.game)
                    val p = Play(expPlay.geek, bgPlay.game, bgPlay.playDate, newQuantity, -1, bgPlay.raters, bgPlay.ratingsTotal, bgPlay.location, newExpansions)
                    val result = mutableListOf(p)
                    if (expPlay.quantity > 0) result.add(expPlay)
                    if (bgPlay.quantity > 0) result.add(bgPlay)
                    result.addAll(plays.filter { it != expPlay && it != bgPlay })
                    newPlays = result
                }
                if (newPlays != null) {
                    plays.clear()
                    newPlays?.let { plays.addAll(it) }
                    iterations++
                    break
                } else if (possibleBaseGames.size == 1) {
                    // assume this is the base game although the user didn't log such a play.
                    val bg = possibleBaseGames.first()
                    val expansions = setOf(expPlay.game) + expPlay.expansions
                    val p = Play(expPlay.geek, bg, expPlay.playDate, expPlay.quantity, -1, expPlay.raters, expPlay.ratingsTotal, expPlay.location, expansions)
                    val others = plays.filter { it != expPlay }
                    plays.clear()
                    plays.add(p)
                    plays.addAll(others)
                } else {
                    // we don't know what to do with this expansion play.
                }

            }
        } while (plays.size > startSize && iterations < 200)
    }

    fun games(bggids: Collection<Int>): Map<Int, Game> {
        synchronized(gamesByBggid) {
            val toGet = bggids.filter { !gamesByBggid.containsKey(it) }
            if (!toGet.isEmpty()) {
                // todo add inlist!
                val games = Games.
                        slice(Games.columns).selectAll().map { row -> Game(row) }
                games.forEach { gamesByBggid[it.bggid] = it }
                toGet.filter { !gamesByBggid.containsKey(it) }.forEach { gamesByBggid[it] = Game(it, "No Such Game", 1, 6, 0) }
            }
            return bggids.associate { it.to(gamesByBggid[it]!!) }
        }
    }

    fun game(bggId: Int): Game? {
        synchronized(gamesByBggid) {
            val result = gamesByBggid[bggId]
            if (result == null) {
                games(setOf(bggId))
            }
            return gamesByBggid[bggId]
        }
    }

    fun gamesWhere(where: SqlExpressionBuilder.()-> Op<Boolean>): Iterable<Game> {
        val bggIds = Games.slice(Games.bggid).select(where).map { row -> row[Games.bggid] }
        return games(bggIds).values
    }
}

/**
 * Find an entry in the map, or calculate its value and keep it for later.
 */
fun <K, V> MutableMap<K, V>.findOrAdd(key: K, calc: (K) -> V): V {
    synchronized(this) {
        val result = this[key]
        if (result == null) {
            val v = calc(key)
            this[key] = v
            return v
        }
        return result
    }
}


