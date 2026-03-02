package com.few.generator.core.kis

object OverseaStockConstants {
    const val OVERSEA_PRICE_DETAIL_TR_ID = "HHDFS76200200"

    /** KIS 거래소 코드 */
    const val EXCD_NAS = "NAS" // 나스닥
    const val EXCD_AMS = "AMS" // 아멕스

    enum class StockGroup { M7, ETF }

    /** M7 개별 종목 */
    val AAPL = Stock("AAPL", "애플", EXCD_NAS)
    val MSFT = Stock("MSFT", "마이크로소프트", EXCD_NAS)
    val GOOGL = Stock("GOOGL", "알파벳", EXCD_NAS)
    val AMZN = Stock("AMZN", "아마존", EXCD_NAS)
    val NVDA = Stock("NVDA", "엔비디아", EXCD_NAS)
    val META = Stock("META", "메타", EXCD_NAS)
    val TSLA = Stock("TSLA", "테슬라", EXCD_NAS)

    /** ETF 개별 종목 */
    val SPY = Stock("SPY", "S&P500", EXCD_AMS)
    val QQQ = Stock("QQQ", "나스닥100", EXCD_NAS)
    val SCHD = Stock("SCHD", "다우존스", EXCD_AMS)

    val DAILY_NASDAQ_STOCK_GROUP_MAP: Map<StockGroup, List<Stock>> =
        mapOf(
            StockGroup.ETF to listOf(SPY, QQQ, SCHD),
            StockGroup.M7 to listOf(AAPL, MSFT, GOOGL, AMZN, NVDA, META, TSLA),
        )
}
