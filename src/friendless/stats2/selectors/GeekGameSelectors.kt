package friendless.stats2.selectors

import friendless.stats2.model.GeekGame
import friendless.stats2.substrate.Substrate

/**
 * Created by john on 30/06/16.
 */
abstract class GeekGameSelector(substrate: Substrate, descriptor: SelectorDescriptor):
        Selector<GeekGame>(substrate, descriptor) {
}

val ALL_SELECTOR_DESCRIPTOR = SelectorDescriptor("all", 0, 0, AllSelector::class, SelectorType.GEEKGAME)
open class AllSelector(substrate: Substrate, descriptor: SelectorDescriptor = ALL_SELECTOR_DESCRIPTOR):
        GeekGameSelector(substrate, descriptor) {
    override fun select(geek: String?): Iterable<GeekGame> {
        return if (geek != null) substrate.collection(geek) else throw IllegalArgumentException("geek must be known")
    }
}

val OWNED_SELECTOR_DESCRIPTOR = SelectorDescriptor("owned", 0, 0, OwnedSelector::class, SelectorType.GEEKGAME)
class OwnedSelector(substrate: Substrate): AllSelector(substrate, OWNED_SELECTOR_DESCRIPTOR) {
    override fun select(geek: String?): Iterable<GeekGame> {
        return super.select(geek).filter { it.owned }
    }
}

val RATED_SELECTOR_DESCRIPTOR = SelectorDescriptor("rated", 0, 0, RatedSelector::class, SelectorType.GEEKGAME)
class RatedSelector(substrate: Substrate): AllSelector(substrate, RATED_SELECTOR_DESCRIPTOR) {
    override fun select(geek: String?): Iterable<GeekGame> {
        return super.select(geek).filter { it.rating > 0.0 }
    }
}
