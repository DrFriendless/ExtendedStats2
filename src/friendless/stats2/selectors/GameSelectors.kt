package friendless.stats2.selectors

import friendless.stats2.model.Game
import friendless.stats2.model.GeekGame
import friendless.stats2.substrate.Substrate

/**
 * Created by john on 30/06/16.
 */
abstract class GameSelector(substrate: Substrate, descriptor: SelectorDescriptor): Selector<Game>(substrate, descriptor) {
}

val GAMES_SELECTOR_DESCRIPTOR = SelectorDescriptor("games", 0, 1, GameSelectorForGeekGames::class, SelectorType.GAME)
class GameSelectorForGeekGames(substrate: Substrate, val ggSelector: Selector<GeekGame>):
        GameSelector(substrate, GAMES_SELECTOR_DESCRIPTOR) {
    override fun select(geek: String?): Iterable<Game> {
        return substrate.games(ggSelector.select(geek).map { it.game }).values
    }
}

