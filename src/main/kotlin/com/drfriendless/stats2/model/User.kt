package com.drfriendless.stats2.model

import com.google.gson.JsonObject
import com.drfriendless.statsdb.database.Users
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

/**
 * Created by john on 21/07/16.
 */
class User(val name: String, val bggid: Int, val country: String?): ModelObject {
    constructor(row: ResultRow): this(
            row[Users.geek], row[Users.bggid], row[Users.country]) {
    }

    override fun <T> get(key: Column<T>): T {
        return when (key) {
            Users.geek -> name as T
            else -> 0 as T
        }
    }

    override fun toJson(vararg omit: Column<*>): JsonObject {
        return toJson(this, Users.columns, *omit)
    }
}