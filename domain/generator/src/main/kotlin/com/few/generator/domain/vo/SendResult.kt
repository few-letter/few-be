package com.few.generator.domain.vo

data class SendResult(
    val successCount: Int,
    val failCount: Int,
) {
    fun total(): Int = successCount + failCount

    fun isAllSuccess(): Boolean = failCount == 0

    operator fun plus(other: SendResult): SendResult =
        SendResult(
            successCount = this.successCount + other.successCount,
            failCount = this.failCount + other.failCount,
        )
}