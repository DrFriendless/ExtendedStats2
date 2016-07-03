package friendless.stats2.selectors

import friendless.stats2.model.ModelObject
import friendless.stats2.substrate.Substrate
import java.util.*
import java.util.logging.Logger

/**
 * Created by john on 30/06/16.
 */
abstract class Selector<T>(val substrate: Substrate, val descriptor: SelectorDescriptor) {
    abstract fun select(): Iterable<T>
}

enum class SelectorType { GAME, GEEKGAME, OPERATOR }

data class SelectorDescriptor(val key: String, val arity: Int, val pop: Int, val clazz: Class<*>, val type: SelectorType) {
    fun requiresUser(): Boolean {
        return type == SelectorType.GEEKGAME
    }
}

val AND_SELECTOR_DESCRIPTOR = SelectorDescriptor("and", 0, 2, AndSelector::class.java, SelectorType.OPERATOR)
class AndSelector<T>(substrate: Substrate, val right: Selector<T>, val left: Selector<T>): Selector<T>(substrate, AND_SELECTOR_DESCRIPTOR) {
    override fun select(): Iterable<T> {
        throw UnsupportedOperationException()
    }
}

val OR_SELECTOR_DESCRIPTOR = SelectorDescriptor("or", 0, 2, OrSelector::class.java, SelectorType.OPERATOR)
class OrSelector<T>(substrate: Substrate, val right: Selector<T>, val left: Selector<T>): Selector<T>(substrate, OR_SELECTOR_DESCRIPTOR) {
    override fun select(): Iterable<T> {
        throw UnsupportedOperationException()
    }
}

val MINUS_SELECTOR_DESCRIPTOR = SelectorDescriptor("minus", 0, 2, MinusSelector::class.java, SelectorType.OPERATOR)
class MinusSelector<T>(substrate: Substrate, val right: Selector<T>, val left: Selector<T>): Selector<T>(substrate, MINUS_SELECTOR_DESCRIPTOR) {
    override fun select(): Iterable<T> {
        throw UnsupportedOperationException()
    }
}

val SELECTOR_DESCRIPTORS: List<SelectorDescriptor> = listOf(
        GAMES_SELECTOR_DESCRIPTOR, ALL_SELECTOR_DESCRIPTOR, OWNED_SELECTOR_DESCRIPTOR, RATED_SELECTOR_DESCRIPTOR,
        AND_SELECTOR_DESCRIPTOR, OR_SELECTOR_DESCRIPTOR, MINUS_SELECTOR_DESCRIPTOR
)

fun findDescriptor(key: String): SelectorDescriptor? {
    return SELECTOR_DESCRIPTORS.firstOrNull() { it.key == key }
}

fun parseSelector(substrate: Substrate, url: String, geek: String?): Selector<ModelObject> {
    val LOG = Logger.getLogger("Selectors")
    var fields: MutableList<String> = ArrayList(url.split(","))
    val stack = Stack<Selector<ModelObject>>()
    while (fields.size > 0) {
        val key = fields.removeAt(0)
        val sd = findDescriptor(key) ?: throw IllegalArgumentException(key)
        val args = ArrayList<Any>()
        val argClasses = ArrayList<Class<*>>()
        args.add(substrate)
        argClasses.add(Substrate::class.java)
        if (sd.requiresUser()) {
            if (geek == null) throw IllegalArgumentException("geek can't be null - required by ${sd.key}")
            args.add(geek)
            argClasses.add(String::class.java)
        }
        args.addAll(fields.subList(0, sd.arity))
        for (p in 1..sd.arity) argClasses.add(String::class.java)
        for (p in sd.pop downTo 1) {
            args.add(stack.pop())
            argClasses.add(Selector::class.java)
        }
        if (sd.arity > 0) fields = fields.subList(sd.arity, fields.size)
        try {
            val constructor = sd.clazz.getConstructor(*argClasses.toTypedArray())
            val newSelector = constructor.newInstance(args) as Selector<ModelObject>
            stack.push(newSelector)
        } catch (ex: NoSuchMethodException) {
            LOG.severe { "Class ${sd.clazz} has no constructor which takes $argClasses" }
            ex.printStackTrace()
        }
    }
    return stack.pop()
}

