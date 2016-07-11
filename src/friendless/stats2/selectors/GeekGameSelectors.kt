package friendless.stats2.selectors

import friendless.stats2.model.Game
import friendless.stats2.database.Substrate

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
        // force the geekgames to be loaded and to annotate the games in the cache
        substrate.collection(geek)
        return selector.select()
    }
}

val PLAYS_ANNOTATE_SELECTOR_DESCRIPTOR = SelectorDescriptor("playsAnnotate", 1, 1, PlaysAnnotateSelector::class, SelectorType.GEEKGAME)
class PlaysAnnotateSelector(substrate: Substrate, val geek: String, val selector: Selector): Selector(substrate) {
    override fun select(): Iterable<Game> {
        val plays = substrate.plays(geek)
        val counts = mutableMapOf<Int, Int>()
        plays.forEach { play ->
            val gs = setOf(play.game) + play.expansions
            gs.forEach { g -> counts[g] = (counts[g] ?: 0) + play.quantity }
        }
        counts.entries.forEach { entry ->
            substrate.game(entry.key)?.setPlays(geek, entry.value)
        }
        return selector.select()
    }
}

val WANTTOBUY_SELECTOR_DESCRIPTOR = SelectorDescriptor("wanttobuy", 1, 0, WantToBuySelector::class, SelectorType.GEEKGAME)
class WantToBuySelector(substrate: Substrate, val geek: String): Selector(substrate) {
    override fun select(): Iterable<Game> {
        return substrate.collection(geek).filter { it.forGeek(geek)?.wanttobuy ?: false }
    }
}

val WANTINTRADE_SELECTOR_DESCRIPTOR = SelectorDescriptor("wantintrade", 1, 0, WantInTradeSelector::class, SelectorType.GEEKGAME)
class WantInTradeSelector(substrate: Substrate, val geek: String): Selector(substrate) {
    override fun select(): Iterable<Game> {
        return substrate.collection(geek).filter { it.forGeek(geek)?.want ?: false }
    }
}

val WANTTOPLAY_SELECTOR_DESCRIPTOR = SelectorDescriptor("wanttoplay", 1, 0, WantToPlaySelector::class, SelectorType.GEEKGAME)
class WantToPlaySelector(substrate: Substrate, val geek: String): Selector(substrate) {
    override fun select(): Iterable<Game> {
        return substrate.collection(geek).filter { it.forGeek(geek)?.wanttoplay ?: false }
    }
}

val ALLWANTTOPLAY_SELECTOR_DESCRIPTOR = SelectorDescriptor("allwanttoplay", 1, 0, AllWantToPlaySelector::class, SelectorType.GEEKGAME)
class AllWantToPlaySelector(substrate: Substrate, val geek: String): Selector(substrate) {
    override fun select(): Iterable<Game> {
        return substrate.collection(geek).filter { it.geekGames.values.all { it.wanttoplay } }
    }
}

val TWOWANTTOPLAY_SELECTOR_DESCRIPTOR = SelectorDescriptor("twowantto", 1, 0, TwoWantToSelector::class, SelectorType.GEEKGAME)
class TwoWantToSelector(substrate: Substrate, val geek: String): Selector(substrate) {
    override fun select(): Iterable<Game> {
        return substrate.collection(geek).filter { game ->
            game.geekGames.values.filter { it.wanttobuy || it.wanttoplay || it.want }.size > 1
        }
    }
}

val OWNEDTWOWANTTOPLAY_SELECTOR_DESCRIPTOR = SelectorDescriptor("ownedtwowantto", 1, 0, OwnedTwoWantToSelector::class, SelectorType.GEEKGAME)
class OwnedTwoWantToSelector(substrate: Substrate, val geek: String): Selector(substrate) {
    override fun select(): Iterable<Game> {
        return substrate.collection(geek).filter { game -> game.geekGames.values.any { it.owned }}.
                filter { game ->
                    game.geekGames.values.filter { it.wanttobuy || it.wanttoplay || it.want }.size > 1
                }
    }
}

val SCORE_SELECTOR_DESCRIPTOR = SelectorDescriptor("score", 1, 1, ScoreSelector::class, SelectorType.GEEKGAME)
class ScoreSelector(substrate: Substrate, val scoreMethod: String, val selector: Selector): Selector(substrate) {
    override fun select(): Iterable<Game> {
        val evalFunction = getScoreMethod(scoreMethod)
        val games = selector.select()
        games.forEach {
            it.score = evalFunction.evaluate(it)
        }
        return games.sortedBy { -it.score }
    }
}

