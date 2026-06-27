package com.few.generator.core.scrapper.timefolio

object TimeEtfConstants {
    const val URL = "https://timeetf.co.kr/m11_view.php?idx=2"
    const val TBODY_SELECTOR = "#constituentItems > div.table > table > tbody"
    const val TOP_ITEM_COUNT = 10
    val EXCLUDED_TICKER_KEYWORDS = listOf("NQU6", "INDEX")
}