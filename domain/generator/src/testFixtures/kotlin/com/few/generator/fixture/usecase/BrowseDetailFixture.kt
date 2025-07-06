package com.few.generator.fixture.usecase

import com.few.generator.domain.Category
import com.few.generator.domain.MediaType
import com.few.generator.fixture.FixtureMonkeyConfig.builder
import com.few.generator.usecase.out.BrowseContentsUsecaseOut
import com.few.generator.usecase.out.BrowseGenUsecaseOut
import com.few.generator.usecase.out.BrowseProvisioningContentsUsecaseOut
import com.few.generator.usecase.out.BrowseRawContentsUsecaseOut
import com.navercorp.fixturemonkey.kotlin.setExp
import java.time.LocalDateTime

/**
 * BrowseContentsUsecaseOut (상세 조회용)을 위한 FixtureMonkey 기반 픽스쳐
 */
object BrowseDetailFixture {
    /**
     * 기본 랜덤 데이터로 생성
     */
    fun random(): BrowseContentsUsecaseOut =
        com.few.generator.fixture.FixtureMonkeyConfig
            .random()

    /**
     * 기본 테스트 데이터 빌더
     */
    fun default() =
        builder<BrowseContentsUsecaseOut>()
            .setExp(BrowseContentsUsecaseOut::rawContents, createDefaultRawContents())
            .setExp(BrowseContentsUsecaseOut::provisioningContents, createDefaultProvisioningContents())
            .setExp(BrowseContentsUsecaseOut::gen, createDefaultGen())

    /**
     * giveMeDefault() - 기존 API 호환성을 위해 유지
     */
    fun giveMeDefault() = BrowseDetailBuilder()

    /**
     * 커스텀 rawContents로 빌더 생성
     */
    fun withRawContents(rawContents: BrowseRawContentsUsecaseOut) =
        builder<BrowseContentsUsecaseOut>()
            .setExp(BrowseContentsUsecaseOut::rawContents, rawContents)
            .setExp(BrowseContentsUsecaseOut::provisioningContents, createDefaultProvisioningContents())
            .setExp(BrowseContentsUsecaseOut::gen, createDefaultGen())

    /**
     * 커스텀 provisioningContents로 빌더 생성
     */
    fun withProvisioningContents(provisioningContents: BrowseProvisioningContentsUsecaseOut) =
        builder<BrowseContentsUsecaseOut>()
            .setExp(BrowseContentsUsecaseOut::rawContents, createDefaultRawContents())
            .setExp(BrowseContentsUsecaseOut::provisioningContents, provisioningContents)
            .setExp(BrowseContentsUsecaseOut::gen, createDefaultGen())

    /**
     * 커스텀 gen으로 빌더 생성
     */
    fun withGen(gen: BrowseGenUsecaseOut) =
        builder<BrowseContentsUsecaseOut>()
            .setExp(BrowseContentsUsecaseOut::rawContents, createDefaultRawContents())
            .setExp(BrowseContentsUsecaseOut::provisioningContents, createDefaultProvisioningContents())
            .setExp(BrowseContentsUsecaseOut::gen, gen)

    /**
     * 기본 RawContents 생성
     */
    private fun createDefaultRawContents(): BrowseRawContentsUsecaseOut =
        builder<BrowseRawContentsUsecaseOut>()
            .setExp(BrowseRawContentsUsecaseOut::id, 201L)
            .setExp(BrowseRawContentsUsecaseOut::url, "https://example.com/raw/1")
            .setExp(BrowseRawContentsUsecaseOut::title, "원본 제목")
            .setExp(BrowseRawContentsUsecaseOut::description, "원본 설명입니다")
            .setExp(BrowseRawContentsUsecaseOut::thumbnailImageUrl, "https://example.com/raw-thumb/1.jpg")
            .setExp(BrowseRawContentsUsecaseOut::rawTexts, "원본 텍스트 내용")
            .setExp(BrowseRawContentsUsecaseOut::imageUrls, listOf("https://example.com/img1.jpg"))
            .setExp(BrowseRawContentsUsecaseOut::mediaType, MediaType.CHOSUN)
            .setExp(BrowseRawContentsUsecaseOut::createdAt, LocalDateTime.now())
            .sample()

    /**
     * 기본 ProvisioningContents 생성
     */
    private fun createDefaultProvisioningContents(): BrowseProvisioningContentsUsecaseOut =
        builder<BrowseProvisioningContentsUsecaseOut>()
            .setExp(BrowseProvisioningContentsUsecaseOut::id, 101L)
            .setExp(BrowseProvisioningContentsUsecaseOut::rawContentsId, 201L)
            .setExp(BrowseProvisioningContentsUsecaseOut::completionIds, listOf("completion1"))
            .setExp(BrowseProvisioningContentsUsecaseOut::bodyTextsJson, listOf("본문 내용"))
            .setExp(BrowseProvisioningContentsUsecaseOut::coreTextsJson, listOf("핵심 내용"))
            .setExp(BrowseProvisioningContentsUsecaseOut::createdAt, LocalDateTime.now())
            .sample()

    /**
     * 기본 Gen 생성
     */
    private fun createDefaultGen(): BrowseGenUsecaseOut =
        builder<BrowseGenUsecaseOut>()
            .setExp(BrowseGenUsecaseOut::id, 1L)
            .setExp(BrowseGenUsecaseOut::provisioningContentsId, 101L)
            .setExp(BrowseGenUsecaseOut::completionIds, listOf("gen-completion1"))
            .setExp(BrowseGenUsecaseOut::headline, "생성된 헤드라인")
            .setExp(BrowseGenUsecaseOut::summary, "생성된 요약입니다")
            .setExp(BrowseGenUsecaseOut::highlightTexts, listOf("생성된 하이라이트"))
            .setExp(BrowseGenUsecaseOut::category, Category.TECHNOLOGY)
            .setExp(BrowseGenUsecaseOut::createdAt, LocalDateTime.now())
            .sample()
}

/**
 * 기존 builder 패턴 호환성을 위한 클래스
 * 기존 테스트에서 .build() 호출을 지원
 */
class BrowseDetailBuilder {
    private var rawContentsId: Long = 201L
    private var rawContentsUrl: String = "https://example.com/raw/1"
    private var rawContentsTitle: String = "원본 제목"
    private var rawContentsDescription: String = "원본 설명입니다"
    private var provisioningContentsId: Long = 101L
    private var genId: Long = 1L
    private var genHeadline: String = "생성된 헤드라인"
    private var genSummary: String = "생성된 요약입니다"
    private var genCategory: Category = Category.TECHNOLOGY

    fun withRawContentsId(id: Long) = apply { this.rawContentsId = id }

    fun withRawContentsUrl(url: String) = apply { this.rawContentsUrl = url }

    fun withRawContentsTitle(title: String) = apply { this.rawContentsTitle = title }

    fun withRawContentsDescription(description: String) = apply { this.rawContentsDescription = description }

    fun withProvisioningContentsId(id: Long) = apply { this.provisioningContentsId = id }

    fun withGenId(id: Long) = apply { this.genId = id }

    fun withGenHeadline(headline: String) = apply { this.genHeadline = headline }

    fun withGenSummary(summary: String) = apply { this.genSummary = summary }

    fun withGenCategory(category: Category) = apply { this.genCategory = category }

    fun build(): BrowseContentsUsecaseOut {
        val rawContents =
            builder<BrowseRawContentsUsecaseOut>()
                .setExp(BrowseRawContentsUsecaseOut::id, rawContentsId)
                .setExp(BrowseRawContentsUsecaseOut::url, rawContentsUrl)
                .setExp(BrowseRawContentsUsecaseOut::title, rawContentsTitle)
                .setExp(BrowseRawContentsUsecaseOut::description, rawContentsDescription)
                .setExp(BrowseRawContentsUsecaseOut::thumbnailImageUrl, "https://example.com/raw-thumb/1.jpg")
                .setExp(BrowseRawContentsUsecaseOut::rawTexts, "원본 텍스트 내용")
                .setExp(BrowseRawContentsUsecaseOut::imageUrls, listOf("https://example.com/img1.jpg"))
                .setExp(BrowseRawContentsUsecaseOut::mediaType, MediaType.CHOSUN)
                .setExp(BrowseRawContentsUsecaseOut::createdAt, LocalDateTime.now())
                .sample()

        val provisioningContents =
            builder<BrowseProvisioningContentsUsecaseOut>()
                .setExp(BrowseProvisioningContentsUsecaseOut::id, provisioningContentsId)
                .setExp(BrowseProvisioningContentsUsecaseOut::rawContentsId, rawContentsId)
                .setExp(BrowseProvisioningContentsUsecaseOut::completionIds, listOf("completion1"))
                .setExp(BrowseProvisioningContentsUsecaseOut::bodyTextsJson, listOf("본문 내용"))
                .setExp(BrowseProvisioningContentsUsecaseOut::coreTextsJson, listOf("핵심 내용"))
                .setExp(BrowseProvisioningContentsUsecaseOut::createdAt, LocalDateTime.now())
                .sample()

        val gen =
            builder<BrowseGenUsecaseOut>()
                .setExp(BrowseGenUsecaseOut::id, genId)
                .setExp(BrowseGenUsecaseOut::provisioningContentsId, provisioningContentsId)
                .setExp(BrowseGenUsecaseOut::completionIds, listOf("gen-completion1"))
                .setExp(BrowseGenUsecaseOut::headline, genHeadline)
                .setExp(BrowseGenUsecaseOut::summary, genSummary)
                .setExp(BrowseGenUsecaseOut::highlightTexts, listOf("생성된 하이라이트"))
                .setExp(BrowseGenUsecaseOut::category, genCategory)
                .setExp(BrowseGenUsecaseOut::createdAt, LocalDateTime.now())
                .sample()

        return builder<BrowseContentsUsecaseOut>()
            .setExp(BrowseContentsUsecaseOut::rawContents, rawContents)
            .setExp(BrowseContentsUsecaseOut::provisioningContents, provisioningContents)
            .setExp(BrowseContentsUsecaseOut::gen, gen)
            .sample()
    }
}