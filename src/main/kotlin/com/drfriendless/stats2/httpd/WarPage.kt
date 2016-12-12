package com.drfriendless.stats2.httpd

import com.drfriendless.stats2.model.FrontPageGeek
import com.drfriendless.statsdb.database.FrontPageGeeks
import org.jetbrains.exposed.sql.select

/**
 * Created by john on 12/12/16.
 */
fun warPageData(users: Iterable<String>): List<FrontPageGeek> {
    return rawFrontPageData(users)
}

private fun rawFrontPageData(users: Iterable<String>): List<FrontPageGeek> {
    val us = users.toList()
    return FrontPageGeeks.slice(FrontPageGeeks.columns).select { FrontPageGeeks.geek inList us }.map(::FrontPageGeek)
}
