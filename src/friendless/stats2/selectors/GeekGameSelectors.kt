package friendless.stats2.selectors

import friendless.stats2.model.Game
import friendless.stats2.model.GeekGame
import friendless.stats2.substrate.Substrate

/**
 * Created by john on 30/06/16.
 */
abstract class GeekGameSelector(substrate: Substrate, val geek: String): Selector(substrate) {
}

val ALL_SELECTOR_DESCRIPTOR = SelectorDescriptor("all", 1, 0, AllSelector::class, SelectorType.GEEKGAME)
open class AllSelector(substrate: Substrate, geek: String):
        GeekGameSelector(substrate, geek) {
    override fun select(): Iterable<Game> = substrate.collection(geek)
}

val OWNED_SELECTOR_DESCRIPTOR = SelectorDescriptor("owned", 1, 0, OwnedSelector::class, SelectorType.GEEKGAME)
class OwnedSelector(substrate: Substrate, geek: String): AllSelector(substrate, geek) {
    override fun select(): Iterable<Game> = super.select().filter { it.forGeek(geek)?.owned ?: false }
}

val RATED_SELECTOR_DESCRIPTOR = SelectorDescriptor("rated", 1, 0, RatedSelector::class, SelectorType.GEEKGAME)
class RatedSelector(substrate: Substrate, geek: String): AllSelector(substrate, geek) {
    override fun select(): Iterable<Game> = super.select().filter { it.forGeek(geek)?.rating ?: -1.0 > 0.0 }
}
