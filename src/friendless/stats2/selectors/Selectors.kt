package friendless.stats2.selectors

import friendless.stats2.model.ModelObject
import friendless.stats2.substrate.Substrate
import java.util.*

/**
 * Created by john on 30/06/16.
 */
abstract class Selector<T>(val substrate: Substrate, val descriptor: SelectorDescriptor) {
    abstract fun select(): Iterable<T>
}

enum class SelectorType { GAME, GEEKGAME, OPERATOR }

data class SelectorDescriptor(val key: String, val arity: Int, val pop: Int, val clazz: Class<*>, val type: SelectorType)

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

fun parseSelector(substrate: Substrate, url: String): Selector<ModelObject> {
    var fields: MutableList<String> = ArrayList(url.split(","))
    val stack = Stack<Selector<ModelObject>>()
    while (fields.size > 0) {
        val key = fields.removeAt(0)
        val sd = findDescriptor(key) ?: throw IllegalArgumentException(key)
        val args = ArrayList<Any>()
        args.add(substrate)
        args.addAll(fields.subList(0, sd.arity))
        for (p in sd.pop downTo 1) args.add(stack.pop())
        if (sd.arity > 0) fields = fields.subList(sd.arity, fields.size)
        val argTypes = Array<Class<*>>(sd.arity + sd.pop + 1,
                { i -> if (i == 0) Substrate::class.java else if (i < sd.arity) String::class.java else Selector::class.java })
        val constructor = sd.clazz.getConstructor(*argTypes)

        val newSelector = constructor.newInstance(args) as Selector<ModelObject>
        stack.push(newSelector)
    }
    return stack.pop()
}

