package com.few.generator.core.kis

data class Stock(
    val symbol: String, // 주식 티커
    val koreanName: String, // 회사 이름
    val excd: String, // 거래소
)