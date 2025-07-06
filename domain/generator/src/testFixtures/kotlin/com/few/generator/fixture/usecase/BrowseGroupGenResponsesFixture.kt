package com.few.generator.fixture.usecase

import com.few.generator.controller.response.BrowseGroupGenResponse
import com.few.generator.controller.response.BrowseGroupGenResponses
import com.few.generator.fixture.FixtureMonkeyConfig.builder
import com.navercorp.fixturemonkey.kotlin.setExp

/**
 * BrowseGroupGenResponses를 위한 FixtureMonkey 기반 픽스쳐
 */
object BrowseGroupGenResponsesFixture {
    /**
     * 기본 랜덤 데이터로 생성
     */
    fun random(): BrowseGroupGenResponses =
        com.few.generator.fixture.FixtureMonkeyConfig
            .random()

    /**
     * 기본 테스트 데이터 빌더 (단일 그룹)
     */
    fun default() =
        builder<BrowseGroupGenResponses>()
            .setExp(BrowseGroupGenResponses::groups, listOf(BrowseGroupGenResponseFixture.default().sample()))

    /**
     * 빈 결과 빌더
     */
    fun empty() =
        builder<BrowseGroupGenResponses>()
            .setExp(BrowseGroupGenResponses::groups, emptyList<BrowseGroupGenResponse>())

    /**
     * 여러 그룹 포함 빌더
     */
    fun multiple() =
        builder<BrowseGroupGenResponses>()
            .setExp(BrowseGroupGenResponses::groups, BrowseGroupGenResponseFixture.multipleResponses())

    /**
     * 커스텀 그룹 리스트로 빌더 생성
     */
    fun withGroups(groups: List<BrowseGroupGenResponse>) =
        builder<BrowseGroupGenResponses>()
            .setExp(BrowseGroupGenResponses::groups, groups)
}