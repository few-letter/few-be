package com.few.generator.config.instagram

import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class InstagramOkHttpConfig {
    @Bean
    fun instagramOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            // 1. 타임아웃 설정
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            // 2. 커넥션 풀 설정 (Max Idle: 5, Keep-Alive: 60s)
            .connectionPool(ConnectionPool(5, 60, TimeUnit.SECONDS))
            // 3. 공통 헤더 추가 (Interceptor)
            .addInterceptor(
                Interceptor { chain ->
                    val original = chain.request()
                    val requestBuilder =
                        original
                            .newBuilder()
//                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                            .header("Connection", "keep-alive")
                            .method(original.method, original.body)

                    chain.proceed(requestBuilder.build())
                },
            ).build()
}