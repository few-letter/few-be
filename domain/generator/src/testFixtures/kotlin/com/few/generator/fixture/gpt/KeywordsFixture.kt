package com.few.generator.fixture.gpt

import com.few.generator.core.gpt.prompt.schema.Keywords
import com.few.generator.fixture.FixtureMonkeyConfig.builder
import com.navercorp.fixturemonkey.kotlin.setExp

/**
 * Keywords를 위한 FixtureMonkey 기반 픽스쳐
 */
object KeywordsFixture {
    /**
     * 기본 랜덤 키워드
     */
    fun random(): Keywords =
        com.few.generator.fixture.FixtureMonkeyConfig
            .random()

    /**
     * 기본 테스트 키워드 빌더
     */
    fun default() =
        builder<Keywords>()
            .setExp(Keywords::keywords, listOf("인공지능", "기술", "자동화", "발전"))

    /**
     * 빈 키워드 빌더
     */
    fun empty() =
        builder<Keywords>()
            .setExp(Keywords::keywords, emptyList<String>())

    /**
     * 비동기 테스트용 키워드 빌더
     */
    fun async() =
        builder<Keywords>()
            .setExp(Keywords::keywords, listOf("비동기", "테스트", "핵심텍스트"))

    /**
     * 코루틴 테스트용 키워드 빌더
     */
    fun coroutine() =
        builder<Keywords>()
            .setExp(Keywords::keywords, listOf("코루틴", "테스트", "핵심텍스트"))

    /**
     * 커스텀 키워드 리스트로 빌더 생성
     */
    fun withKeywords(keywords: List<String>) =
        builder<Keywords>()
            .setExp(Keywords::keywords, keywords)

    /**
     * 단일 키워드 빌더
     */
    fun single(keyword: String) = withKeywords(listOf(keyword))
}