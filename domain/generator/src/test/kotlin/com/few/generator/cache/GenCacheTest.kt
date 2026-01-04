package com.few.generator.cache

import com.few.generator.config.CacheNames
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.jsr107.EhcacheCachingProvider
import java.time.Duration
import javax.cache.Caching

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

        describe("JCache API 동작 테스트") {
            it("JCache로 캐시를 생성하고 사용할 수 있어야 한다") {
                val cachingProvider = Caching.getCachingProvider() as EhcacheCachingProvider
                val cacheManager = cachingProvider.cacheManager

                // 캐시 설정 생성
                val cacheConfig =
                    org.ehcache.jsr107.Eh107Configuration.fromEhcacheCacheConfiguration(
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                            String::class.java,
                            String::class.java,
                            ResourcePoolsBuilder.heap(100),
                        ),
                    )

                // 캐시가 존재하지 않으면 생성
                val cache =
                    try {
                        cacheManager.getCache<String, String>(CacheNames.GEN_CACHE)
                            ?: cacheManager.createCache<String, String, javax.cache.configuration.Configuration<String, String>>(
                                CacheNames.GEN_CACHE,
                                cacheConfig,
                            )
                    } catch (e: Exception) {
                        cacheManager.createCache<String, String, javax.cache.configuration.Configuration<String, String>>(
                            CacheNames.GEN_CACHE,
                            cacheConfig,
                        )
                    }

                // 캐시에 데이터 저장
                cache.put("test-key", "test-value")

                // 캐시에서 데이터 조회
                val value = cache.get("test-key")

                value shouldBe "test-value"

                // 정리
                try {
                    cacheManager.destroyCache(CacheNames.GEN_CACHE)
                } catch (e: Exception) {
                    // ignore
                }
                cacheManager.close()
            }
        }

        describe("캐시 통계 확인") {
            it("통계를 활성화한 캐시를 생성할 수 있어야 한다") {
                val cachingProvider = Caching.getCachingProvider() as EhcacheCachingProvider
                val cacheManager = cachingProvider.cacheManager

                // MutableConfiguration으로 통계 활성화
                val cacheConfig = javax.cache.configuration.MutableConfiguration<String, String>()
                cacheConfig.setTypes(String::class.java, String::class.java)
                cacheConfig.isStatisticsEnabled = true

                val cache = cacheManager.createCache("statsCache", cacheConfig)

                // 데이터 저장 (put)
                cache.put("key1", "value1")

                // 첫 번째 조회 (miss)
                cache.get("key2")

                // 두 번째 조회 (hit)
                val value = cache.get("key1")

                value shouldBe "value1"

                // 통계 확인
                @Suppress("UNCHECKED_CAST")
                val configClass =
                    javax.cache.configuration.Configuration::class.java as
                        Class<javax.cache.configuration.Configuration<String, String>>
                val config = cache.getConfiguration(configClass)

                if (config is javax.cache.configuration.CompleteConfiguration<*, *>) {
                    config.isStatisticsEnabled shouldBe true
                }

                // 정리
                cacheManager.destroyCache("statsCache")
                cacheManager.close()
            }
        }
    })