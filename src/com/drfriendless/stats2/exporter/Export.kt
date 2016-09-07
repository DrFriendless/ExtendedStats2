package com.drfriendless.stats2.exporter

import com.drfriendless.stats2.Config
import com.drfriendless.stats2.database.Substrate

/**
 * Created by john on 7/09/16.
 */
fun main(args: Array<String>) {
    val config = Config()
    val substrate = Substrate(config)
}