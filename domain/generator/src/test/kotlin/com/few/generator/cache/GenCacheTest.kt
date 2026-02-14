package com.few.generator.cache

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.time.Duration

/**
 * EHCache 동작 확인 단위 테스트
 *
 * 캐시가 제대로 설정되었는지, hit/miss가 정상 작동하는지 검증합니다.
 */
class GenCacheTest :
    DescribeSpec({

        describe("Ehcache 기본 동작 테스트") {
            it("Ehcache 캐시 생성 및 사용이 가능해야 한다") {
                // Ehcache 네이티브 방식으로 캐시 매니저 생성
                val cacheManager =
                    CacheManagerBuilder
                        .newCacheManagerBuilder()
                        .withCache(
                            "testCache",
                            CacheConfigurationBuilder
                                .newCacheConfigurationBuilder(
                                    String::class.java,
                                    String::class.java,
                                    ResourcePoolsBuilder.heap(100),
                                ).withExpiry(
                                    ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(1)),
                                ),
                        ).build(true)

                val cache = cacheManager.getCache("testCache", String::class.java, String::class.java)

                // 캐시에 데이터 저장
                cache.put("key1", "value1")

                // 캐시에서 데이터 조회
                val value = cache.get("key1")

                value shouldBe "value1"

                cacheManager.close()
            }
        }
    })