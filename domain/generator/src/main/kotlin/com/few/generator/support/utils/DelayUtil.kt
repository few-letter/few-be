package com.few.generator.support.utils

import io.github.oshai.kotlinlogging.KotlinLogging

object DelayUtil {
    private val log = KotlinLogging.logger {}

    /**
     * fromSec와 toSec 사이의 시간을 랜덤하게 선택하여 현재 스레드를 정지시킵니다.
     */
    fun randomDelay(
        fromSec: Int,
        toSec: Int,
    ) {
        // 1. from과 to 사이의 랜덤한 밀리초(ms) 계산
        val range = (fromSec.toLong() * 1000)..(toSec.toLong() * 1000)
        val delayMillis = range.random()

        try {
            // 2. 해당 시간만큼 스레드 정지
            log.info { "Thread will be Delayed... ${delayMillis}초 후 다음 작업 진행" }
            Thread.sleep(delayMillis)
        } catch (e: InterruptedException) {
            // 3. 인터럽트 발생 시 현재 스레드의 상태 재설정
            Thread.currentThread().interrupt()
        }
    }
}