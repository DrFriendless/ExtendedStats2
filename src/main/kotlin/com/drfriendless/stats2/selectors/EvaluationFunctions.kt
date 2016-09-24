package com.drfriendless.stats2.selectors

import com.drfriendless.stats2.model.Game
import kotlin.reflect.KClass
import kotlin.reflect.primaryConstructor

/**
 * Functions for evaluating games in different ways.
 */

/** Higher is better */
abstract class EvaluationFunction() {
    abstract fun evaluate(g: Game): Int
}

data class  EvaluationFunctionDescriptor<T : EvaluationFunction>(val key: String, val clazz: KClass<T>) {
}

val UNKNOWN_RATING = 5.5
private fun standardise(rating: Double): Double {
    return if (rating <= 0.0) UNKNOWN_RATING else rating
}

val TOTAL_RATINGS_DESCRIPTOR = EvaluationFunctionDescriptor("totalRating", TotalRatingsEvaluationFunction::class)
class TotalRatingsEvaluationFunction(): EvaluationFunction() {
    override fun evaluate(g: Game): Int {
        return g.geekGames.values.map { standardise(it.rating) * 10 }.sum().toInt()
    }
}

val MINIMUM_RATINGS_DESCRIPTOR = EvaluationFunctionDescriptor("minimumRating", MinimumRatingsEvaluationFunction::class)
class MinimumRatingsEvaluationFunction(): EvaluationFunction() {
    override fun evaluate(g: Game): Int {
        return (g.geekGames.values.map { standardise(it.rating) * 10 }.min() ?: UNKNOWN_RATING).toInt()
    }
}

val ALL_SAME_DESCRIPTOR = EvaluationFunctionDescriptor("same", AllSameEvaluationFunction::class)
class AllSameEvaluationFunction(): EvaluationFunction() {
    override fun evaluate(g: Game): Int = 1
}

val WANT_TO_PLAY_DESCRIPTOR = EvaluationFunctionDescriptor("wantToPlay", WantToPlayEvaluationFunction::class)
class WantToPlayEvaluationFunction(): EvaluationFunction() {
    override fun evaluate(g: Game): Int {
        return g.geekGames.values.map { if (it.wanttoplay) 1 else 0 }.sum()
    }
}

val WANT_TO_PLAYBUYTRADE_DESCRIPTOR = EvaluationFunctionDescriptor("wantToPlayBuyTrade", WantToPlayBuyTradeEvaluationFunction::class)
class WantToPlayBuyTradeEvaluationFunction(): EvaluationFunction() {
    override fun evaluate(g: Game): Int {
        return g.geekGames.values.map { if (it.wanttoplay or it.wanttobuy or it.want) 1 else 0 }.sum()
    }
}

val MOST_PLAYS_DESCRIPTOR = EvaluationFunctionDescriptor("totalPlays", MostPlaysEvaluationFunction::class)
class MostPlaysEvaluationFunction(): EvaluationFunction() {
    override fun evaluate(g: Game): Int {
        return g.plays.values.sum()
    }
}

val EVALUATION_FUNCTION_DESCRIPTORS: List<EvaluationFunctionDescriptor<*>> = listOf(
        TOTAL_RATINGS_DESCRIPTOR, MINIMUM_RATINGS_DESCRIPTOR, ALL_SAME_DESCRIPTOR, WANT_TO_PLAY_DESCRIPTOR,
        WANT_TO_PLAYBUYTRADE_DESCRIPTOR, MOST_PLAYS_DESCRIPTOR
)

fun getScoreMethod(key: String): EvaluationFunction {
    val descriptor = EVALUATION_FUNCTION_DESCRIPTORS.filter { it.key == key }.firstOrNull() ?: ALL_SAME_DESCRIPTOR
    return descriptor.clazz.primaryConstructor?.call() ?: throw IllegalArgumentException()
}