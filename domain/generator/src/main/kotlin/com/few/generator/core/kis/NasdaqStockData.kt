package com.few.generator.core.kis

data class NasdaqStockData(
    val symbol: String,
    val koreanName: String,
    /** 현재가 (USD) */
    val currentPrice: String,
    /** 등락률 (%) */
    val changeRate: String,
) {
    /** 상승 여부 (true: 상승, false: 하락, null: 보합) */
    val isRise: Boolean?
        get() =
            when {
                changeRate.startsWith("+") -> true
                changeRate.startsWith("-") -> false
                else -> null
            }
}