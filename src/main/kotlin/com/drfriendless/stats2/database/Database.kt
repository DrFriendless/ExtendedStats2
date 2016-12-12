package com.drfriendless.stats2.database

import com.drfriendless.stats2.Config
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import com.drfriendless.statsdb.database.GeekGames

/**
 * Connecting to the database and enhancing the given functionality. Nothing application-specific here.
 */
open class Database(config: Config) {
    companion object {
        // make sure we only initialise once because of a bug in exposed.
        val initialised = mutableSetOf<String>()

        fun initialised(url: String): Boolean {
            synchronized(initialised) {
                val result = url in initialised
                initialised.add(url)
                return result
            }
        }
    }

    private lateinit var db: org.jetbrains.exposed.sql.Database
    init {
        val url = config.dbURL
        if (!initialised(url)) {
            db = Database.connect(url, config.driver, config.dbUser, config.dbPasswd)
        }
    }
}

fun float(name: String, scale: Int, precision: Int): Column<Float> =
        GeekGames.registerColumn(name, FloatColumnType(scale, precision))

class FloatColumnType(val scale: Int, val precision: Int): ColumnType() {
    override fun sqlType(): String = "FLOAT($scale, $precision)"
    override fun valueFromDB(value: Any): Any = super.valueFromDB(value).let { (it as? Float) ?: it }
}

