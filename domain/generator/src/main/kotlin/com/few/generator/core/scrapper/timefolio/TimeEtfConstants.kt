package com.few.generator.core.scrapper.timefolio

object TimeEtfConstants {
    const val URL = "https://timeetf.co.kr/m11_view.php?idx=2"
    const val TBODY_SELECTOR = "#constituentItems > div.table > table > tbody"
    const val TOP_ITEM_COUNT = 10
    val EXCLUDED_TICKER_KEYWORDS = listOf("NQU6", "INDEX")
}

private val COMPANY_SUFFIX_PATTERN =
    Regex("""(?i)\s+(?:Corp|Inc|Ltd|PLC|NV|LLC|Co|SA|AG|SE|GmbH|Holdings|Holding|Group|Technologies|Technology)\.?$""")

fun String.removeCompanySuffix(): String {
    var result = this
    while (true) {
        val next = COMPANY_SUFFIX_PATTERN.replace(result, "").trim()
        if (next == result) return result
        result = next
    }
}