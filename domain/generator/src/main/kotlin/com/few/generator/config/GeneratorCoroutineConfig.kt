package com.few.generator.config

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeneratorCoroutineConfig {
    /**
     * Instagram 업로드용 코루틴 스코프
     *
     * - [SupervisorJob]: 카테고리별 업로드가 독립적으로 실행되므로, 한 카테고리 실패가 다른 카테고리에 영향을 주지 않도록 격리
     * - [Dispatchers.Default]: HTTP I/O는 OkHttp 내부 스레드풀(enqueue + suspendCancellableCoroutine)이 처리하며,
     *   스코프의 스레드는 suspend delay 대기, 조건 체크 등 경량 작업만 수행하므로 IO 스레드풀 불필요
     * - 이벤트 리스너에서 fire-and-forget으로 업로드 작업을 비동기 실행할 때 사용
     */
    @Bean(name = ["instagramCoroutineScope"])
    fun instagramCoroutineScope(): CoroutineScope =
        CoroutineScope(
            Dispatchers.Default + SupervisorJob() + CoroutineName("instagram-coroutine"),
        )

    /**
     * GroupGen 스케줄링용 코루틴 스코프
     *
     * - [SupervisorJob]: 카테고리별 GroupGen 생성이 독립적으로 실행되므로, 한 카테고리 실패가 다른 카테고리에 영향을 주지 않도록 격리
     * - [Dispatchers.IO]: runBlocking(groupGenScope.coroutineContext) 내부에서 JPA 쿼리(블로킹 DB I/O)와
     *   GPT API 호출(블로킹 HTTP I/O)이 발생하므로 IO 스레드풀 필요
     * - 키워드 추출, 그룹화, 그룹 콘텐츠 생성 등 외부 I/O가 포함된 배치 처리 작업에 사용
     */
    @Bean(name = ["groupGenCoroutineScope"])
    fun groupGenCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName("group-gen-coroutine"))
}