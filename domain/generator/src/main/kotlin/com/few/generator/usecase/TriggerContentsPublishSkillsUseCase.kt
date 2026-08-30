package com.few.generator.usecase

import com.few.common.domain.ContentsType
import com.few.generator.event.TriggerContentsPublishSkillsEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureTimeMillis

@Component
class TriggerContentsPublishSkillsUseCase(
    @Value("\${generator.skills.publish-scripts-dir:.claude/scripts}")
    private val publishScriptsDir: String,
    @Value("\${generator.skills.publish-script-timeout-minutes:30}")
    private val publishScriptTimeoutMinutes: Long,
) {
    private val log = KotlinLogging.logger {}
    private val isRunning = AtomicBoolean(false)

    @Async("generatorSchedulingExecutor")
    @EventListener
    fun onTriggerContentsPublishSkills(event: TriggerContentsPublishSkillsEvent) {
        log.info {
            "${event.region?.name ?: "UNKNOWN"} 콘텐츠 발행 Skills 트리거 감지 " +
                "(title=${event.title}, contentsType=${event.contentsType.title}, startTime=${event.startTime})"
        }

        if (!isRunning.compareAndSet(false, true)) {
            log.warn { "콘텐츠 발행 Skills 스크립트가 이미 실행 중입니다." }
            return
        }

        try {
            execute(event.contentsType)
        } catch (e: Exception) {
            log.error(e) { "콘텐츠 발행 Skills 스크립트 실행 실패: ${e.message}" }
        } finally {
            isRunning.set(false)
        }
    }

    fun execute(contentsType: ContentsType) {
        val scriptFile = File(resolveScriptPath(contentsType))
        require(scriptFile.exists()) {
            "발행 스크립트를 찾을 수 없습니다: ${scriptFile.absolutePath} (contentsType=${contentsType.title})"
        }

        val logTag = scriptFile.nameWithoutExtension

        log.info { "zsh 기반 발행 스크립트 실행 시작: ${scriptFile.absolutePath} (contentsType=${contentsType.title})" }

        val output = StringBuilder()
        var exitCode = -1

        val executionTimeSec =
            measureTimeMillis {
                val process =
                    ProcessBuilder("/bin/zsh", scriptFile.absolutePath)
                        .redirectErrorStream(true)
                        .start()

                // 별도 스레드로 출력을 소비하여 파이프 버퍼가 가득 차 프로세스가 멈추는 것을 방지
                val readerThread =
                    Thread {
                        process.inputStream.bufferedReader().useLines { lines ->
                            lines.forEach { line ->
                                output.appendLine(line)
                                log.info { "[$logTag] $line" }
                            }
                        }
                    }
                readerThread.start()

                val finished = process.waitFor(publishScriptTimeoutMinutes, TimeUnit.MINUTES)
                if (!finished) {
                    process.destroyForcibly()
                    readerThread.join(5_000)
                    throw IllegalStateException(
                        "발행 스크립트 실행이 ${publishScriptTimeoutMinutes}분 내에 완료되지 않아 강제 종료했습니다.",
                    )
                }

                readerThread.join(5_000)
                exitCode = process.exitValue()
            }.msToSeconds()

        check(exitCode == 0) {
            "발행 스크립트가 비정상 종료되었습니다. exitCode=$exitCode, output=\n$output"
        }

        log.info {
            buildString {
                appendLine("✅ 콘텐츠 발행 Skills 스크립트 실행 완료 (contentsType=${contentsType.title})")
                appendLine("✅ exitCode: $exitCode")
                append("✅ 소요 시간: ${executionTimeSec}초")
            }
        }
    }

    /**
     * contentsType 에 따라 실행할 발행 스크립트 경로를 결정한다.
     * LOCAL_NEWS / GLOBAL_NEWS 는 공통 뉴스 발행 스크립트를 사용한다.
     */
    private fun resolveScriptPath(contentsType: ContentsType): String {
        val fileName =
            when (contentsType) {
                ContentsType.LOCAL_NEWS, ContentsType.GLOBAL_NEWS -> "publish-common-news.sh"
                ContentsType.STOCK_BRIEFING -> "publish-stock-briefing.sh"
                ContentsType.POPULAR_NASDAQ_STOCK_NEWS -> "publish-popular-nasdaq-stock-news.sh"
            }

        return "$publishScriptsDir/$fileName"
    }

    private fun Long.msToSeconds(): Double = this / 1000.0
}