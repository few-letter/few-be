package com.few.provider.domain

enum class SubscriptionAction(
    val code: Int,
) {
    ENROLL(1),
    CANCEL(2),
    ;

    companion object {
        fun fromCode(code: Int) = SubscriptionAction.entries.first { it.code == code }
    }
}