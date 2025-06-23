package com.few.generator.domain

import web.handler.exception.BadRequestException

enum class MediaType(
    val code: Int,
    val title: String,
    val keywords: List<String>,
) {
    CHOSUN(1, "조선일보", listOf("chosun")),
    KHAN(2, "경향신문", listOf("khan")),
    SBS(3, "SBS", listOf("sbs")),
    HANKYUNG(4, "한국경제", listOf("hankyung")),
    YONHAPNEWS(5, "연합뉴스", listOf("yonhapnews", "yna.co.kr")),
    DONGA(6, "동아일보", listOf("donga")),
    I_NEWS(7, "아이뉴스", listOf("inews")),
    FN_NEWS(8, "파이낸셜뉴스", listOf("fnnews")),
    HERALD(9, "헤럴드경제", listOf("heraldcorp")),
    KBS(10, "KBS", listOf("kbs.co.kr", "kbsnews")),
    HANKOOKILBO(11, "한국일보", listOf("hankookilbo")),
    NEWS1(12, "뉴스1", listOf("news1")),
    EDAILY(13, "이데일리", listOf("edaily")),
    SEGYE(14, "세계일보", listOf("segye")),
    YTN(15, "YTN", listOf("ytn.co.kr")),
    JTBC(16, "JTBC", listOf("jtbc")),
    MBC(17, "MBC", listOf("imnews", "imbc")),
    MONEY_TODAY(18, "머니투데이", listOf("news.mt.co.kr")),
    ASIAE(19, "아시아경제", listOf("asiae.co.kr")),
    ZDNET(20, "ZDNET", listOf("zdnet")),
    DAILIAN(21, "데일리안", listOf("dailian")),
    JOONGANG(22, "중앙일보", listOf("joongang")),
    KOOKMIN(23, "국민일보", listOf("kmib.co.kr")),
    CHANNEL_A(24, "채널A", listOf("channela")),
    ETC(0, "ETC", emptyList()),

    ;

    companion object {
        fun from(code: Int): MediaType =
            MediaType.entries.find { it.code == code }
                ?: throw BadRequestException("Invalid MediaType code: $code")

        fun from(title: String): MediaType =
            MediaType.entries.find { it.title == title }
                ?: throw BadRequestException("Invalid MediaType name: $title")

        fun find(input: String): MediaType =
            MediaType.entries.find { mediaType ->
                mediaType.keywords.any { keyword ->
                    input.contains(keyword, ignoreCase = true)
                }
            } ?: ETC
    }
}