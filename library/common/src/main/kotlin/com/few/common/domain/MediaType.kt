package com.few.common.domain

import com.few.common.exception.BadRequestException

enum class MediaType(
    val code: Int,
    val title: String,
    val keywords: List<String>,
) {
    /** Local **/
    CHOSUN(1, "조선일보", listOf("chosun")),
    KHAN(2, "경향신문", listOf("khan")),
    SBS(3, "SBS", listOf("sbs")),
    HANKYUNG(4, "한국경제", listOf("hankyung")),
    YONHAPNEWS_TV(5, "연합뉴스TV", listOf("yonhapnews", "yna.co.kr")),
    DONGA(6, "동아일보", listOf("donga")),
    I_NEWS(7, "아이뉴스", listOf("inews")),
    FN_NEWS(8, "파이낸셜뉴스", listOf("fnnews")),
    HERALD(9, "헤럴드경제", listOf("heraldcorp")),
    KBS(10, "KBS 뉴스", listOf("kbs.co.kr", "kbsnews")),
    HANKOOKILBO(11, "한국일보", listOf("hankookilbo")),
    NEWS1(12, "뉴스1", listOf("news1.kr")),
    EDAILY(13, "이데일리", listOf("edaily")),
    SEGYE(14, "세계일보", listOf("segye")),
    YTN(15, "YTN", listOf("ytn.co.kr")),
    JTBC(16, "JTBC 뉴스", listOf("jtbc")),
    MBC(17, "MBC", listOf("imnews", "imbc")),
    MONEY_TODAY(18, "머니투데이", listOf("news.mt.co.kr")),
    ASIAE(19, "아시아경제", listOf("asiae.co.kr")),
    ZDNET(20, "ZDNet korea", listOf("zdnet.co.kr")),
    DAILIAN(21, "데일리안", listOf("dailian")),
    JOONGANG(22, "중앙일보", listOf("joongang")),
    KOOKMIN(23, "국민일보", listOf("kmib.co.kr")),
    CHANNEL_A(24, "채널A 뉴스", listOf("channela")),
    DIGITAL_DAILY(25, "디지털데일리", listOf("ddaily.co.kr")),
    SEOUL_ECONOMY(26, "서울경제", listOf("sedaily.com")),
    DIGITAL_TIMES(27, "디지털타임스", listOf("dt.co.kr")),
    THE_FACT(28, "더팩트", listOf("news.tf.co.kr")),
    SEOUL_NEWSPAPER(29, "서울신문", listOf("seoul.co.kr")),
    CHOSUN_BIZ(30, "조선비즈", listOf("biz.chosun.com")),
    SBS_BIZ(31, "SBS Biz", listOf("biz.sbs.co.kr")),
    NOCUTNEWS(32, "노컷뉴스", listOf("nocutnews")),
    KWANGWON_ILBO(33, "강원일보", listOf("kwnews.co.kr")),
    MK(34, "매일경제", listOf("mk.co.kr")),
    BLOTTER(35, "블로터", listOf("bloter.net")),
    ETNEWS(36, "전자신문", listOf("etnews.com")),
    YONHAPNEWS(37, "연합뉴스", listOf("yonhapnews", "yna.co.kr")),
    MAEIL(38, "매일신문", listOf("imaeil.com")),
    JOSEILBO(39, "조세일보", listOf("joseilbo.com")),

    /** Global **/
    CNBC(40, "CNBC", listOf("cnbc.com")),
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