package com.few.generator.fixture

/**
 * 테스트에서 사용하는 상수들을 관리하는 객체
 */
object TestConstants {
    // MediaTypeTest 상수들
    const val MEDIATYPE_TEST_BASIC_PROPERTIES_VERIFICATION = "기본 속성 검증"
    const val MEDIATYPE_TEST_UNIQUE_CODE_TITLE_KEYWORDS = "각 미디어 타입은 고유한 코드와 제목, 키워드를 가진다"
    const val MEDIATYPE_TEST_ALL_MEDIA_TYPES_HAVE_KEYWORDS = "모든 미디어 타입(ETC 제외)은 최소 하나의 키워드를 가진다"
    const val MEDIATYPE_TEST_FROM_CODE_METHOD = "from(code) 메서드"
    const val MEDIATYPE_TEST_INVALID_CODE_THROWS_EXCEPTION = "존재하지 않는 코드는 올바른 메시지와 함께 예외를 던진다"
    const val MEDIATYPE_TEST_FROM_TITLE_METHOD = "from(title) 메서드"
    const val MEDIATYPE_TEST_INVALID_TITLE_THROWS_EXCEPTION = "존재하지 않는 제목은 올바른 메시지와 함께 예외를 던진다"
    const val MEDIATYPE_TEST_FIND_METHOD = "find(input) 메서드"
    const val MEDIATYPE_TEST_INVALID_CODE_MESSAGE_FORMAT = "Invalid MediaType code: %d"
    const val MEDIATYPE_TEST_INVALID_NAME_MESSAGE_FORMAT = "Invalid MediaType name: %s"
    const val MEDIATYPE_KEYWORD_CHOSUN = "chosun"
    const val MEDIATYPE_KEYWORD_KHAN = "khan"
    const val MEDIATYPE_TITLE_CHOSUN = "조선일보"
    const val MEDIATYPE_TITLE_KHAN = "경향신문"
    const val MEDIATYPE_TITLE_SBS = "SBS"
    const val MEDIATYPE_TITLE_ETC = "ETC"
    const val MEDIATYPE_URL_CHOSUN = "https://www.chosun.com/article"
    const val MEDIATYPE_URL_KHAN = "https://khan.co.kr/news"
    const val MEDIATYPE_URL_SBS = "https://news.sbs.co.kr/news"
    const val MEDIATYPE_URL_YONHAPNEWS_TV = "https://www.yonhapnews.co.kr/bulletin"
    const val MEDIATYPE_UNKNOWN_NEWS_SITE = "https://unknown-news-site.com/article"
    const val MEDIATYPE_INVALID_TITLE = "존재하지않는신문"
    const val MEDIATYPE_PARTIAL_MATCH_CHOSUN = "이 기사는 조선일보에서 chosun.com을 통해 제공됩니다"
    const val MEDIATYPE_EMPTY_STRING = ""
}