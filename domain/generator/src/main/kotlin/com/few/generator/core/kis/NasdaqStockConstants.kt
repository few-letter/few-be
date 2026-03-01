package com.few.generator.core.kis

object NasdaqStockConstants {
    const val OVERSEA_PRICE_DETAIL_TR_ID = "HHDFS76200200"

    /** KIS 거래소 코드 */
    const val EXCD_NAS = "NAS" // 나스닥
    const val EXCD_AMS = "AMS" // 아멕스

    data class NasdaqStock(
        val symbol: String,
        val koreanName: String,
        val excd: String,
    )

    val M7_STOCKS =
        listOf(
            NasdaqStock("AAPL", "애플", EXCD_NAS),
            NasdaqStock("MSFT", "마이크로소프트", EXCD_NAS),
            NasdaqStock("GOOGL", "알파벳", EXCD_NAS),
            NasdaqStock("AMZN", "아마존", EXCD_NAS),
            NasdaqStock("NVDA", "엔비디아", EXCD_NAS),
            NasdaqStock("META", "메타", EXCD_NAS),
            NasdaqStock("TSLA", "테슬라", EXCD_NAS),
        )

    val ETF_STOCKS =
        listOf(
            NasdaqStock("SPY", "S&P500 ETF", EXCD_AMS),
            NasdaqStock("QQQ", "나스닥100 ETF", EXCD_NAS),
        )

    val ALL_STOCKS = M7_STOCKS + ETF_STOCKS
}