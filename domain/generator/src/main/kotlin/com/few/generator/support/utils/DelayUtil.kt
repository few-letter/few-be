package com.few.generator.support.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay

object DelayUtil {
    private val log = KotlinLogging.logger {}

    suspend fun randomDelay(
        fromSec: Int,
        toSec: Int,
    ) {
        require(fromSec >= 0 && toSec >= 0) { "fromSec/toSec는 0 이상이어야 합니다." }
        require(fromSec <= toSec) { "fromSec는 toSec 이하여야 합니다." }

        val range = (fromSec.toLong() * 1000)..(toSec.toLong() * 1000)
        val delayMillis = range.random()

        log.info { "Coroutine suspended for ${delayMillis}ms" }
        delay(delayMillis)
    }
}