package com.drfriendless.stats2

import java.util.*

/**
 * The config file which tells us about the environment we're hosted in.
 */
class Config(filename: String = "/config.properties") {
    val prop = Properties()
    val driver: String by prop
    val dbUser: String by prop
    val dbPasswd: String by prop
    val countries: String by prop
    val dbURL: String by prop

    init {
        Config::class.java.getResourceAsStream(filename).use {
            prop.load(it)
        }
    }

    fun allowedCountries(): List<String> {
        return countries.split(",").map { it.trim() }
    }
}