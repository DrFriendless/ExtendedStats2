package friendless.stats2.selectors

import friendless.stats2.model.Game
import friendless.stats2.substrate.Substrate
import java.util.*
import java.util.logging.Logger
import kotlin.reflect.KClass
import kotlin.reflect.primaryConstructor

/**
 * A selector is essentially an expresion which can be evaluated against the database to select a set of games.
 * Currently I have two types of selectors: those that return games, and those that return geekgames (games associated
 * with a user). An example of first is "games design by Reiner Knizia" and of the second "games that Eduardo rates 8
 * or higher". For the first we return information only about the game, for the second we return information about
 * the user's interactions with the games. So far I'm not seeing the value in the distinction so this may change.
 */
abstract class Selector(val substrate: Substrate) {
    abstract fun select(): Iterable<Game>
}

enum class SelectorType { GAME, GEEKGAME, OPERATOR }

data class SelectorDescriptor(val key: String, val arity: Int, val pop: Int, val clazz: KClass<*>, val type: SelectorType) {
}

val AND_SELECTOR_DESCRIPTOR = SelectorDescriptor("and", 0, 2, AndSelector::class, SelectorType.OPERATOR)
class AndSelector(substrate: Substrate, val right: Selector, val left: Selector): Selector(substrate) {
    override fun select(): Iterable<Game> {
        val l = left.select()
        val r = right.select()
        return l.intersect(r)
    }
}

val OR_SELECTOR_DESCRIPTOR = SelectorDescriptor("or", 0, 2, OrSelector::class, SelectorType.OPERATOR)
class OrSelector(substrate: Substrate, val right: Selector, val left: Selector): Selector(substrate) {
    override fun select(): Iterable<Game> {
        val l = left.select()
        val r = right.select()
        return l.union(r)
    }
}

val MINUS_SELECTOR_DESCRIPTOR = SelectorDescriptor("minus", 0, 2, MinusSelector::class, SelectorType.OPERATOR)
class MinusSelector(substrate: Substrate, val right: Selector, val left: Selector): Selector(substrate) {
    override fun select(): Iterable<Game> {
        val l = left.select()
        val r = right.select()
        return l.minus(r)
    }
}

val SELECTOR_DESCRIPTORS: List<SelectorDescriptor> = listOf(
        // games selectors
        EXPANSION_DESCRIPTOR, PLAYERS_DESCRIPTOR,
        // geekgames selectors
        ALL_SELECTOR_DESCRIPTOR, OWNED_SELECTOR_DESCRIPTOR, RATED_SELECTOR_DESCRIPTOR,
        ANNOTATE_SELECTOR_DESCRIPTOR, SCORE_SELECTOR_DESCRIPTOR,
        // operators
        AND_SELECTOR_DESCRIPTOR, OR_SELECTOR_DESCRIPTOR, MINUS_SELECTOR_DESCRIPTOR
)

fun findDescriptor(key: String): SelectorDescriptor? {
    return SELECTOR_DESCRIPTORS.firstOrNull() { it.key == key }
}

fun parseSelector(substrate: Substrate, url: String): Selector {
    val LOG = Logger.getLogger("Selectors")
    var fields: MutableList<String> = ArrayList(url.split(","))
    val stack = Stack<Selector>()
    try {
        while (fields.size > 0) {
            val key = fields.removeAt(0)
            val sd = findDescriptor(key) ?: throw IllegalArgumentException(key)
            val args = ArrayList<Any>()
            val argClasses = ArrayList<KClass<*>>()
            args.add(substrate)
            argClasses.add(Substrate::class)
            args.addAll(fields.subList(0, sd.arity))
            for (p in 1..sd.arity) argClasses.add(String::class)
            for (p in sd.pop downTo 1) {
                args.add(stack.pop())
                argClasses.add(Selector::class)
            }
            if (sd.arity > 0) fields = fields.subList(sd.arity, fields.size)
            val constructor = sd.clazz.primaryConstructor
            try {
                val newSelector = constructor?.call(*args.toArray()) as Selector
                stack.push(newSelector)
            } catch (e: IllegalArgumentException) {
                LOG.severe { "Failed to invoke $constructor with $argClasses" }
            }
        }
    } catch (ex : Throwable) {
        ex.printStackTrace()
    }
    return stack.pop()
}

