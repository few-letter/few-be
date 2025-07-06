package com.few.generator.fixture.usecase

import com.few.generator.fixture.FixtureMonkeyConfig.builder
import com.few.generator.usecase.out.BrowseContentsUsecaseOuts
import com.few.generator.usecase.out.ContentsUsecaseOut
import com.navercorp.fixturemonkey.kotlin.setExp

/**
 * BrowseContentsUsecaseOuts를 위한 FixtureMonkey 기반 픽스쳐
 */
object BrowseContentsFixture {
    /**
     * 기본 랜덤 데이터로 생성
     */
    fun random(): BrowseContentsUsecaseOuts =
        com.few.generator.fixture.FixtureMonkeyConfig
            .random()

    /**
     * 기본 테스트 데이터 빌더 (단일 콘텐츠)
     */
    fun default() =
        builder<BrowseContentsUsecaseOuts>()
            .setExp(BrowseContentsUsecaseOuts::contents, listOf(ContentsFixture.default().sample()))
            .setExp(BrowseContentsUsecaseOuts::isLast, false)

    /**
     * 빈 결과 빌더
     */
    fun empty() =
        builder<BrowseContentsUsecaseOuts>()
            .setExp(BrowseContentsUsecaseOuts::contents, emptyList<ContentsUsecaseOut>())
            .setExp(BrowseContentsUsecaseOuts::isLast, true)

    /**
     * 여러 콘텐츠 포함 빌더
     */
    fun multiple() =
        builder<BrowseContentsUsecaseOuts>()
            .setExp(BrowseContentsUsecaseOuts::contents, ContentsFixture.multipleContents())
            .setExp(BrowseContentsUsecaseOuts::isLast, false)

    /**
     * 커스텀 콘텐츠 리스트로 빌더 생성
     */
    fun withContents(contents: List<ContentsUsecaseOut>) =
        builder<BrowseContentsUsecaseOuts>()
            .setExp(BrowseContentsUsecaseOuts::contents, contents)
            .setExp(BrowseContentsUsecaseOuts::isLast, false)
}