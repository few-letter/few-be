package com.few.generator.fixture

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne

/**
 * FixtureMonkey 설정 및 공통 픽스쳐 생성기
 */
object FixtureMonkeyConfig {
    val fixtureMonkey: FixtureMonkey =
        FixtureMonkey
            .builder()
            .plugin(KotlinPlugin())
            .build()

    /**
     * 기본 랜덤 객체 생성
     */
    inline fun <reified T> random(): T = fixtureMonkey.giveMeOne()

    /**
     * 커스텀 설정이 가능한 빌더 반환
     */
    inline fun <reified T> builder() = fixtureMonkey.giveMeBuilder<T>()

    /**
     * 여러 개의 랜덤 객체 생성
     */
    inline fun <reified T> randomList(size: Int = 3): List<T> = (1..size).map { fixtureMonkey.giveMeOne<T>() }
}