package com.drfriendless.stats2.selectors

import com.drfriendless.stats2.database.Games
import com.drfriendless.stats2.model.Game
import com.drfriendless.stats2.database.Substrate
import org.jetbrains.exposed.sql.AndOp

/**
 * Created by john on 7/07/16.
 */
val EXPANSION_DESCRIPTOR = SelectorDescriptor("expansions", 0, 0, ExpansionSelector::class, SelectorType.GAME)
open class ExpansionSelector(substrate: Substrate): Selector(substrate) {
    override fun select(): Iterable<Game> {
        return substrate.games(substrate.expansions).values
    }
}

val PLAYERS_DESCRIPTOR = SelectorDescriptor("players", 1, 0, PlayersSelector::class, SelectorType.GAME)
open class PlayersSelector(substrate: Substrate, players: String): Selector(substrate) {
    val playerCount: Int = Integer.parseInt(players)

    override fun select(): Iterable<Game> {
        return substrate.gamesWhere { AndOp(Games.minPlayers lessEq playerCount, Games.maxPlayers greaterEq playerCount) }
    }
}
