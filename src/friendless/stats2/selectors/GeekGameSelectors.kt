package friendless.stats2.selectors

import friendless.stats2.model.GeekGame
import friendless.stats2.substrate.Substrate

/**
 * Created by john on 30/06/16.
 */
abstract class GeekGameSelector(substrate: Substrate, val userId: String, descriptor: SelectorDescriptor):
        Selector<GeekGame>(substrate, descriptor) {
}

val ALL_SELECTOR_DESCRIPTOR = SelectorDescriptor("all", 0, 0, AllSelector::class.java, SelectorType.GEEKGAME)
open class AllSelector(substrate: Substrate, userId: String, descriptor: SelectorDescriptor = ALL_SELECTOR_DESCRIPTOR):
        GeekGameSelector(substrate, userId, descriptor) {
    override fun select(): Iterable<GeekGame> {
        return substrate.collection(userId)
    }
}

val OWNED_SELECTOR_DESCRIPTOR = SelectorDescriptor("owned", 0, 0, OwnedSelector::class.java, SelectorType.GEEKGAME)
class OwnedSelector(substrate: Substrate, userId: String): AllSelector(substrate, userId, OWNED_SELECTOR_DESCRIPTOR) {
    override fun select(): Iterable<GeekGame> {
        return super.select().filter { it.owned }
    }
}

val RATED_SELECTOR_DESCRIPTOR = SelectorDescriptor("rated", 0, 0, RatedSelector::class.java, SelectorType.GEEKGAME)
class RatedSelector(substrate: Substrate, userId: String): AllSelector(substrate, userId, RATED_SELECTOR_DESCRIPTOR) {
    override fun select(): Iterable<GeekGame> {
        return super.select().filter { it.rating > 0.0 }
    }
}
