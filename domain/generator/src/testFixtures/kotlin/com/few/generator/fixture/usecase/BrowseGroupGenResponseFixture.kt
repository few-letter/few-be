package com.few.generator.fixture.usecase

import com.few.generator.controller.response.BrowseGroupGenResponse
import com.few.generator.fixture.FixtureMonkeyConfig.builder
import com.navercorp.fixturemonkey.kotlin.setExp
import java.time.LocalDateTime

/**
 * BrowseGroupGenResponse를 위한 FixtureMonkey 기반 픽스쳐
 */
object BrowseGroupGenResponseFixture {
    /**
     * 기본 랜덤 데이터로 생성
     */
    fun random(): BrowseGroupGenResponse =
        com.few.generator.fixture.FixtureMonkeyConfig
            .random()

    /**
     * 기본 테스트 데이터 빌더
     */
    fun default() =
        builder<BrowseGroupGenResponse>()
            .setExp(BrowseGroupGenResponse::id, 1L)
            .setExp(BrowseGroupGenResponse::category, 2)
            .setExp(BrowseGroupGenResponse::selectedGroupIds, "[1, 2, 3]")
            .setExp(BrowseGroupGenResponse::headline, "그룹 헤드라인")
            .setExp(BrowseGroupGenResponse::summary, "그룹 요약")
            .setExp(BrowseGroupGenResponse::highlightTexts, listOf("그룹 하이라이트1", "그룹 하이라이트2"))
            .setExp(BrowseGroupGenResponse::groupSourceHeadlines, GroupSourceHeadlineDataFixture.multipleHeadlines())
            .setExp(BrowseGroupGenResponse::createdAt, LocalDateTime.now())

    /**
     * 두 번째 테스트 데이터 빌더
     */
    fun second() =
        builder<BrowseGroupGenResponse>()
            .setExp(BrowseGroupGenResponse::id, 2L)
            .setExp(BrowseGroupGenResponse::category, 4)
            .setExp(BrowseGroupGenResponse::selectedGroupIds, "[4, 5, 6]")
            .setExp(BrowseGroupGenResponse::headline, "두 번째 그룹 헤드라인")
            .setExp(BrowseGroupGenResponse::summary, "두 번째 그룹 요약")
            .setExp(BrowseGroupGenResponse::highlightTexts, listOf("두 번째 하이라이트1", "두 번째 하이라이트2"))
            .setExp(BrowseGroupGenResponse::groupSourceHeadlines, GroupSourceHeadlineDataFixture.multipleHeadlines())
            .setExp(BrowseGroupGenResponse::createdAt, LocalDateTime.now())

    /**
     * 여러 그룹 응답 생성
     */
    fun multipleResponses(): List<BrowseGroupGenResponse> =
        listOf(
            default().sample(),
            second().sample(),
        )
}