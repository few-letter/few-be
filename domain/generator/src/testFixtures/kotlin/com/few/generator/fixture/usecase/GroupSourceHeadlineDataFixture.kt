package com.few.generator.fixture.usecase

import com.few.generator.controller.response.GroupSourceHeadlineData
import com.few.generator.fixture.FixtureMonkeyConfig.builder
import com.navercorp.fixturemonkey.kotlin.setExp

/**
 * GroupSourceHeadlineData를 위한 FixtureMonkey 기반 픽스쳐
 */
object GroupSourceHeadlineDataFixture {
    /**
     * 기본 랜덤 데이터로 생성
     */
    fun random(): GroupSourceHeadlineData =
        com.few.generator.fixture.FixtureMonkeyConfig
            .random()

    /**
     * 기본 테스트 데이터 빌더
     */
    fun default() =
        builder<GroupSourceHeadlineData>()
            .setExp(GroupSourceHeadlineData::headline, "소스 헤드라인1")
            .setExp(GroupSourceHeadlineData::url, "https://example.com/1")

    /**
     * 두 번째 테스트 데이터 빌더
     */
    fun second() =
        builder<GroupSourceHeadlineData>()
            .setExp(GroupSourceHeadlineData::headline, "소스 헤드라인2")
            .setExp(GroupSourceHeadlineData::url, "https://example.com/2")

    /**
     * 여러 소스 헤드라인 데이터 생성
     */
    fun multipleHeadlines(): List<GroupSourceHeadlineData> =
        listOf(
            default().sample(),
            second().sample(),
        )
}