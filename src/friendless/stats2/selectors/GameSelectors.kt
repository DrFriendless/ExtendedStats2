package friendless.stats2.selectors

import friendless.stats2.model.Game
import friendless.stats2.substrate.Substrate

/**
 * Created by john on 7/07/16.
 */
val EXPANSION_DESCRIPTOR = SelectorDescriptor("expansions", 0, 0, ExpansionSelector::class, SelectorType.GAME)
open class ExpansionSelector(substrate: Substrate): Selector(substrate) {
    override fun select(): Iterable<Game> {
        return substrate.games(substrate.expansions).values
    }
}
