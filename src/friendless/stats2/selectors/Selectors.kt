package friendless.stats2.selectors

import friendless.stats2.model.ModelObject
import friendless.stats2.substrate.Substrate
import java.util.*
import java.util.logging.Logger
import kotlin.reflect.KClass
import kotlin.reflect.primaryConstructor

/**
 * Created by john on 30/06/16.
 */
abstract class Selector<out T>(val substrate: Substrate) {
    abstract fun select(geek: String?): Iterable<T>
}

enum class SelectorType { GAME, GEEKGAME, OPERATOR }

data class SelectorDescriptor(val key: String, val arity: Int, val pop: Int, val clazz: KClass<*>, val type: SelectorType) {
}

val AND_SELECTOR_DESCRIPTOR = SelectorDescriptor("and", 0, 2, AndSelector::class, SelectorType.OPERATOR)
class AndSelector<out T>(substrate: Substrate, val right: Selector<T>, val left: Selector<T>): Selector<T>(substrate) {
    override fun select(geek: String?): Iterable<T> {
        val l = left.select(geek)
        val r = right.select(geek)
        return l.intersect(r)
    }
}

val OR_SELECTOR_DESCRIPTOR = SelectorDescriptor("or", 0, 2, OrSelector::class, SelectorType.OPERATOR)
class OrSelector<out T>(substrate: Substrate, val right: Selector<T>, val left: Selector<T>): Selector<T>(substrate) {
    override fun select(geek: String?): Iterable<T> {
        val l = left.select(geek)
        val r = right.select(geek)
        return l.union(r)
    }
}

val MINUS_SELECTOR_DESCRIPTOR = SelectorDescriptor("minus", 0, 2, MinusSelector::class, SelectorType.OPERATOR)
class MinusSelector<out T>(substrate: Substrate, val right: Selector<T>, val left: Selector<T>): Selector<T>(substrate) {
    override fun select(geek: String?): Iterable<T> {
        val l = left.select(geek)
        val r = right.select(geek)
        return l.minus(r)
    }
}

val USER_SELECTOR_DESCRIPTOR = SelectorDescriptor("user", 1, 1, UserSelector::class, SelectorType.OPERATOR)
class UserSelector<out T>(substrate: Substrate, val newGeek: String, val selector: Selector<T>):
        Selector<T>(substrate) {
    override fun select(geek: String?): Iterable<T> {
        return selector.select(newGeek)
    }
}

val SELECTOR_DESCRIPTORS: List<SelectorDescriptor> = listOf(
        // games selectors
        GAMES_SELECTOR_DESCRIPTOR,
        // geekgames selectors
        ALL_SELECTOR_DESCRIPTOR, OWNED_SELECTOR_DESCRIPTOR, RATED_SELECTOR_DESCRIPTOR,
        // operators
        AND_SELECTOR_DESCRIPTOR, OR_SELECTOR_DESCRIPTOR, MINUS_SELECTOR_DESCRIPTOR,
        // metaselectors
        USER_SELECTOR_DESCRIPTOR
)

fun findDescriptor(key: String): SelectorDescriptor? {
    return SELECTOR_DESCRIPTORS.firstOrNull() { it.key == key }
}

fun parseSelector(substrate: Substrate, url: String): Selector<ModelObject> {
    val LOG = Logger.getLogger("Selectors")
    var fields: MutableList<String> = ArrayList(url.split(","))
    val stack = Stack<Selector<ModelObject>>()
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
                val newSelector = constructor?.call(*args.toArray()) as Selector<ModelObject>
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

