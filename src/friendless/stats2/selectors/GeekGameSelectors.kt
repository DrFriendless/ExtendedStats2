package friendless.stats2.selectors

import friendless.stats2.model.Game
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

val ANNOTATE_SELECTOR_DESCRIPTOR = SelectorDescriptor("annotate", 1, 1, AnnotateSelector::class, SelectorType.GEEKGAME)
class AnnotateSelector(substrate: Substrate, val geek: String, val selector: Selector): Selector(substrate) {
    override fun select(): Iterable<Game> {
        substrate.collection(geek)
        return selector.select()
    }
}

val SCORE_SELECTOR_DESCRIPTOR = SelectorDescriptor("score", 1, 1, ScoreSelector::class, SelectorType.GEEKGAME)
class ScoreSelector(substrate: Substrate, val scoreMethod: String, val selector: Selector): Selector(substrate) {
    override fun select(): Iterable<Game> {
        val games = selector.select()
        games.forEach {
            it.score = score(scoreMethod, it)
        }
        return games.sortedBy { -it.score }
    }
}

// TODO - implement scoreMethods
fun score(scoreMethod: String, game: Game): Int {
    return game.geekGames.values.map { (Math.max(it.rating, 0.0) * 10).toInt() }.fold(0) { a, b -> a + b }
}
