package com.few.generator.fixture.usecase

import com.few.generator.domain.Category
import com.few.generator.domain.MediaType
import com.few.generator.fixture.FixtureMonkeyConfig.builder
import com.few.generator.usecase.out.ContentsUsecaseOut
import com.navercorp.fixturemonkey.kotlin.setExp

/**
 * ContentsUsecaseOut을 위한 FixtureMonkey 기반 픽스쳐
 */
object ContentsFixture {
    /**
     * 기본 랜덤 데이터로 생성
     */
    fun random(): ContentsUsecaseOut =
        com.few.generator.fixture.FixtureMonkeyConfig
            .random()

    /**
     * 테스트용 기본 데이터 빌더
     */
    fun default() =
        builder<ContentsUsecaseOut>()
            .setExp(ContentsUsecaseOut::id, 1L)
            .setExp(ContentsUsecaseOut::url, "https://example.com/news/1")
            .setExp(ContentsUsecaseOut::thumbnailImageUrl, "https://example.com/thumb/1.jpg")
            .setExp(ContentsUsecaseOut::mediaType, MediaType.CHOSUN)
            .setExp(ContentsUsecaseOut::headline, "테스트 헤드라인")
            .setExp(ContentsUsecaseOut::summary, "테스트 요약")
            .setExp(ContentsUsecaseOut::highlightTexts, listOf("하이라이트1", "하이라이트2"))
            .setExp(ContentsUsecaseOut::category, Category.TECHNOLOGY)

    /**
     * 조선일보 뉴스 데이터 빌더
     */
    fun chosunNews() =
        builder<ContentsUsecaseOut>()
            .setExp(ContentsUsecaseOut::id, 1L)
            .setExp(ContentsUsecaseOut::mediaType, MediaType.CHOSUN)
            .setExp(ContentsUsecaseOut::category, Category.TECHNOLOGY)
            .setExp(ContentsUsecaseOut::headline, "첫 번째 뉴스")
            .setExp(ContentsUsecaseOut::summary, "첫 번째 요약")
            .setExp(ContentsUsecaseOut::highlightTexts, listOf("첫 번째 하이라이트"))

    /**
     * 경향신문 뉴스 데이터 빌더
     */
    fun khanNews() =
        builder<ContentsUsecaseOut>()
            .setExp(ContentsUsecaseOut::id, 2L)
            .setExp(ContentsUsecaseOut::url, "https://example.com/news/2")
            .setExp(ContentsUsecaseOut::thumbnailImageUrl, "https://example.com/thumb/2.jpg")
            .setExp(ContentsUsecaseOut::mediaType, MediaType.KHAN)
            .setExp(ContentsUsecaseOut::category, Category.LIFE)
            .setExp(ContentsUsecaseOut::headline, "두 번째 뉴스")
            .setExp(ContentsUsecaseOut::summary, "두 번째 요약")
            .setExp(ContentsUsecaseOut::highlightTexts, listOf("두 번째 하이라이트"))

    /**
     * 여러 콘텐츠 리스트
     */
    fun multipleContents(): List<ContentsUsecaseOut> =
        listOf(
            chosunNews().sample(),
            khanNews().sample(),
        )

    /**
     * 특정 미디어 타입으로 빌더 생성
     */
    fun withMediaType(mediaType: MediaType) =
        builder<ContentsUsecaseOut>()
            .setExp(ContentsUsecaseOut::mediaType, mediaType)

    /**
     * 특정 카테고리로 빌더 생성
     */
    fun withCategory(category: Category) =
        builder<ContentsUsecaseOut>()
            .setExp(ContentsUsecaseOut::category, category)

    /**
     * 커스텀 헤드라인으로 빌더 생성
     */
    fun withHeadline(headline: String) =
        builder<ContentsUsecaseOut>()
            .setExp(ContentsUsecaseOut::headline, headline)
}