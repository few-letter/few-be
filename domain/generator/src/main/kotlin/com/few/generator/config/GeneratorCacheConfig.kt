package com.few.generator.config

import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.EntryUnit
import org.ehcache.jsr107.Eh107Configuration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import javax.cache.CacheManager
import javax.cache.Caching

@Configuration
@EnableCaching
class GeneratorCacheConfig {
    @Bean
    fun jCacheCacheManager(): CacheManager {
        val cacheManager =
            CacheManagerBuilder
                .newCacheManagerBuilder()
                .withCache(
                    "groupGen",
                    CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(
                            Long::class.javaObjectType,
                            com.few.generator.domain.GroupGen::class.java,
                            ResourcePoolsBuilder
                                .newResourcePoolsBuilder()
                                .heap(1000, EntryUnit.ENTRIES),
                        ).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(24))),
                ).withCache(
                    "groupGenByDateRange",
                    CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(
                            String::class.java,
                            List::class.java,
                            ResourcePoolsBuilder
                                .newResourcePoolsBuilder()
                                .heap(100, EntryUnit.ENTRIES),
                        ).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(1))),
                ).build(true)

        return Caching
            .getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider")
            .cacheManager
            .apply {
                createCache(
                    "groupGen",
                    Eh107Configuration.fromEhcacheCacheConfiguration(
                        CacheConfigurationBuilder
                            .newCacheConfigurationBuilder(
                                Long::class.javaObjectType,
                                com.few.generator.domain.GroupGen::class.java,
                                ResourcePoolsBuilder
                                    .newResourcePoolsBuilder()
                                    .heap(1000, EntryUnit.ENTRIES),
                            ).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(24)))
                            .build(),
                    ),
                )
                createCache(
                    "groupGenByDateRange",
                    Eh107Configuration.fromEhcacheCacheConfiguration(
                        CacheConfigurationBuilder
                            .newCacheConfigurationBuilder(
                                String::class.java,
                                List::class.java,
                                ResourcePoolsBuilder
                                    .newResourcePoolsBuilder()
                                    .heap(100, EntryUnit.ENTRIES),
                            ).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(1)))
                            .build(),
                    ),
                )
            }
    }
}