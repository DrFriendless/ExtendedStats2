package com.drfriendless.stats2.model

import com.github.salomonbrys.kotson.jsonArray
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.jetbrains.exposed.sql.Column

/**
 * Created by john on 30/06/16.
 */
interface ModelObject {
    operator fun <T> get(key: Column<T>): T

    fun toJson(vararg omit: Column<*>): JsonObject
}

fun toJson(mo: ModelObject, columns: Iterable<Column<*>>, vararg omit: Column<*>): JsonObject {
    val result = JsonObject()
    columns.filter { !omit.contains(it) }.forEach {
        val v = mo[it]
        when (v) {
            is String -> result.addProperty(it.name, v)
            is Char -> result.addProperty(it.name, v)
            is Number -> result.addProperty(it.name, v)
            is Boolean -> result.addProperty(it.name, v)
        }
    }
    return result
}

fun toJson(games: Iterable<ModelObject>, vararg omit: Column<*>): JsonArray {
    return jsonArray(games.map { it.toJson(*omit) })
}

class AugmentedModelObject(val geek: ModelObject, val allColumns: Iterable<Column<*>>) : ModelObject {
    val extraProps = mutableMapOf<Column<*>, Any>()

    override operator fun <T> get(key: Column<T>): T {
        return (extraProps[key] ?: geek[key]) as T
    }

    operator fun <T> set(column: Column<T>, value: T) {
        extraProps[column] = value as Any
    }

    override fun toJson(vararg omit: Column<*>): JsonObject {
        return toJson(this, allColumns, *omit)
    }
}

