package com.few.generator.usecase

import com.few.common.domain.ContentsType
import com.few.common.domain.Region
import com.few.generator.event.TriggerContentsPublishSkillsEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File
import java.nio.file.Files
import java.time.LocalDateTime

/**
 * TriggerContentsPublishSkillsUseCase 가 실제 zsh 프로세스로 스크립트 파일을 실행하는 경로를 검증한다.
 * - 대부분의 케이스는 임시 디렉토리에 만든 stub 스크립트로 ProcessBuilder 경로(출력 캡처, 종료코드 검사, 타임아웃)를 그대로 태운다.
 * - 마지막 Given 은 UseCase 코드가 참조하는 실제 `.claude/scripts/publish-common-news.sh` 파일을 대상으로 한다.
 *   문법 검사는 항상 수행하고, 실제 실행은 부작용(curl / claude CLI)이 있으므로 env RUN_REAL_PUBLISH_SCRIPT=true 일 때만 수행한다.
 */
class TriggerContentsPublishSkillsUseCaseTest :
    BehaviorSpec({
        val zshBinary = File("/bin/zsh")
        val zshAvailable = zshBinary.canExecute()

        val scriptsDir = Files.createTempDirectory("publish-scripts-test").toFile()
        afterSpec { scriptsDir.deleteRecursively() }

        fun writeScript(
            name: String,
            body: String,
        ): File {
            val f = File(scriptsDir, name)
            f.writeText("#!/bin/zsh\n$body\n")
            f.setExecutable(true)
            return f
        }

        fun newUseCase(timeoutMinutes: Long = 1L) =
            TriggerContentsPublishSkillsUseCase(
                publishScriptsDir = scriptsDir.absolutePath,
                publishScriptTimeoutMinutes = timeoutMinutes,
            )

        Given("resolveScriptPath 가 가리키는 스크립트 파일이 존재하지 않을 때") {
            // 임시 디렉토리를 비워둔다
            scriptsDir.listFiles()?.forEach { it.delete() }

            When("execute 를 호출하면") {
                Then("IllegalArgumentException 이 발생한다") {
                    val ex =
                        shouldThrow<IllegalArgumentException> {
                            newUseCase().execute(ContentsType.LOCAL_NEWS)
                        }
                    ex.message shouldContain "발행 스크립트를 찾을 수 없습니다"
                }
            }
        }

        if (zshAvailable) {
            Given("정상 종료(exit 0)하는 발행 스크립트가 있을 때") {
                val marker = File(scriptsDir, "marker-success.txt").also { it.delete() }
                writeScript(
                    "publish-common-news.sh",
                    """
                    echo "running publish-common-news"
                    touch '${marker.absolutePath}'
                    exit 0
                    """.trimIndent(),
                )

                When("execute(LOCAL_NEWS) 를 호출하면") {
                    newUseCase().execute(ContentsType.LOCAL_NEWS)

                    Then("예외 없이 완료되고 스크립트가 실제로 실행된다") {
                        marker.exists() shouldBe true
                    }
                }
            }

            Given("0이 아닌 종료코드로 끝나는 스크립트가 있을 때") {
                writeScript(
                    "publish-common-news.sh",
                    """
                    echo "OUTPUT_TOKEN_ABC"
                    exit 3
                    """.trimIndent(),
                )

                When("execute 를 호출하면") {
                    Then("IllegalStateException 이 발생하고 종료코드와 표준출력이 메시지에 포함된다") {
                        val ex =
                            shouldThrow<IllegalStateException> {
                                newUseCase().execute(ContentsType.LOCAL_NEWS)
                            }
                        ex.message shouldContain "exitCode=3"
                        ex.message shouldContain "OUTPUT_TOKEN_ABC"
                    }
                }
            }

            Given("표준에러로 출력하는 스크립트가 있을 때 (redirectErrorStream)") {
                writeScript(
                    "publish-common-news.sh",
                    """
                    echo "STDERR_TOKEN_XYZ" >&2
                    exit 2
                    """.trimIndent(),
                )

                When("execute 를 호출하면") {
                    Then("표준에러 출력도 캡처되어 메시지에 포함된다") {
                        val ex =
                            shouldThrow<IllegalStateException> {
                                newUseCase().execute(ContentsType.LOCAL_NEWS)
                            }
                        ex.message shouldContain "STDERR_TOKEN_XYZ"
                    }
                }
            }

            Given("타임아웃 시간(0분) 안에 끝나지 않는 스크립트가 있을 때") {
                writeScript("publish-common-news.sh", "sleep 5")

                When("execute 를 호출하면") {
                    Then("IllegalStateException 이 발생하고 프로세스가 강제 종료된다") {
                        val ex =
                            shouldThrow<IllegalStateException> {
                                newUseCase(timeoutMinutes = 0L).execute(ContentsType.LOCAL_NEWS)
                            }
                        ex.message shouldContain "강제 종료"
                    }
                }
            }

            Given("발행 스크립트에 contentsType.code 를 인자로 전달할 때") {
                val argFile = File(scriptsDir, "captured-arg.txt").also { it.delete() }
                writeScript(
                    "publish-common-news.sh",
                    """
                    printf '%s' "${'$'}1" > '${argFile.absolutePath}'
                    exit 0
                    """.trimIndent(),
                )

                When("execute(LOCAL_NEWS) 를 호출하면") {
                    argFile.delete()
                    newUseCase().execute(ContentsType.LOCAL_NEWS)

                    Then("스크립트의 첫 번째 인자로 '0' 이 전달된다") {
                        argFile.readText() shouldBe "0"
                    }
                }

                When("execute(GLOBAL_NEWS) 를 호출하면") {
                    argFile.delete()
                    newUseCase().execute(ContentsType.GLOBAL_NEWS)

                    Then("스크립트의 첫 번째 인자로 '1' 이 전달된다") {
                        argFile.readText() shouldBe "1"
                    }
                }
            }

            Given("contentsType 별로 서로 다른 스크립트 파일명이 매핑될 때") {
                val markers =
                    mapOf(
                        ContentsType.LOCAL_NEWS to File(scriptsDir, "m-local.txt"),
                        ContentsType.GLOBAL_NEWS to File(scriptsDir, "m-global.txt"),
                        ContentsType.STOCK_BRIEFING to File(scriptsDir, "m-briefing.txt"),
                        ContentsType.POPULAR_NASDAQ_STOCK_NEWS to File(scriptsDir, "m-nasdaq.txt"),
                    )
                markers.values.forEach { it.delete() }

                writeScript("publish-common-news.sh", "touch '${markers[ContentsType.LOCAL_NEWS]!!.absolutePath}'")
                writeScript("publish-stock-briefing.sh", "touch '${markers[ContentsType.STOCK_BRIEFING]!!.absolutePath}'")
                writeScript(
                    "publish-popular-nasdaq-stock-news.sh",
                    "touch '${markers[ContentsType.POPULAR_NASDAQ_STOCK_NEWS]!!.absolutePath}'",
                )

                When("LOCAL_NEWS / GLOBAL_NEWS 로 execute 하면") {
                    newUseCase().execute(ContentsType.LOCAL_NEWS)
                    // GLOBAL_NEWS 도 동일하게 publish-common-news.sh 를 사용한다
                    markers[ContentsType.LOCAL_NEWS]!!.delete()
                    newUseCase().execute(ContentsType.GLOBAL_NEWS)

                    Then("공통 스크립트(publish-common-news.sh)가 실행된다") {
                        markers[ContentsType.LOCAL_NEWS]!!.exists() shouldBe true
                    }
                }

                When("STOCK_BRIEFING 으로 execute 하면") {
                    newUseCase().execute(ContentsType.STOCK_BRIEFING)

                    Then("publish-stock-briefing.sh 가 실행된다") {
                        markers[ContentsType.STOCK_BRIEFING]!!.exists() shouldBe true
                    }
                }

                When("POPULAR_NASDAQ_STOCK_NEWS 로 execute 하면") {
                    newUseCase().execute(ContentsType.POPULAR_NASDAQ_STOCK_NEWS)

                    Then("publish-popular-nasdaq-stock-news.sh 가 실행된다") {
                        markers[ContentsType.POPULAR_NASDAQ_STOCK_NEWS]!!.exists() shouldBe true
                    }
                }
            }

            Given("TriggerContentsPublishSkillsEvent 를 수신할 때") {
                val marker = File(scriptsDir, "marker-event.txt").also { it.delete() }
                writeScript("publish-common-news.sh", "touch '${marker.absolutePath}'")

                val event =
                    TriggerContentsPublishSkillsEvent(
                        title = "[국내] 뉴스 스케줄링",
                        startTime = LocalDateTime.now(),
                        region = Region.LOCAL,
                        contentsType = ContentsType.LOCAL_NEWS,
                    )

                When("onTriggerContentsPublishSkills 리스너가 호출되면") {
                    newUseCase().onTriggerContentsPublishSkills(event)

                    Then("스크립트가 실행된다") {
                        marker.exists() shouldBe true
                    }
                }

                When("스크립트가 실패해도") {
                    writeScript("publish-common-news.sh", "exit 1")

                    Then("리스너는 예외를 전파하지 않는다") {
                        // 예외가 던져지면 이 테스트가 실패한다
                        newUseCase().onTriggerContentsPublishSkills(event)
                    }
                }
            }

            Given("executeAsync(관리자 트리거)로 호출할 때") {
                val marker = File(scriptsDir, "marker-async.txt").also { it.delete() }
                writeScript("publish-common-news.sh", "touch '${marker.absolutePath}'")

                When("executeAsync(LOCAL_NEWS) 를 호출하면") {
                    newUseCase().executeAsync(ContentsType.LOCAL_NEWS)

                    Then("스크립트가 실행되고 실패해도 예외를 전파하지 않는다") {
                        marker.exists() shouldBe true
                        writeScript("publish-common-news.sh", "exit 1")
                        newUseCase().executeAsync(ContentsType.LOCAL_NEWS)
                    }
                }
            }

            Given("UseCase 코드가 참조하는 실제 .claude/scripts/publish-common-news.sh 파일") {
                var repoRoot: File? = File(System.getProperty("user.dir")).absoluteFile
                while (repoRoot != null && !File(repoRoot, ".claude/scripts/publish-common-news.sh").isFile) {
                    repoRoot = repoRoot.parentFile
                }
                val realScript = repoRoot?.let { File(it, ".claude/scripts/publish-common-news.sh") }

                When("파일을 확인하면") {
                    Then("파일이 존재한다") {
                        (realScript?.isFile ?: false) shouldBe true
                    }

                    Then("zsh 문법 검사(zsh -n)를 통과한다") {
                        val p =
                            ProcessBuilder("/bin/zsh", "-n", realScript!!.absolutePath)
                                .redirectErrorStream(true)
                                .start()
                        val out = p.inputStream.bufferedReader().readText()
                        p.waitFor()
                        withClue(out) { p.exitValue() shouldBe 0 }
                    }
                }

                When("RUN_REAL_PUBLISH_SCRIPT=true 인 경우 실제 UseCase.execute 로 실행하면") {
                    val runReal = System.getenv("RUN_REAL_PUBLISH_SCRIPT")?.toBoolean() ?: false

                    Then("파일 미존재/타임아웃 없이 프로세스가 정상적으로 끝까지 실행된다").config(enabled = runReal) {
                        val realUseCase =
                            TriggerContentsPublishSkillsUseCase(
                                publishScriptsDir = File(repoRoot!!, ".claude/scripts").absolutePath,
                                publishScriptTimeoutMinutes = 5L,
                            )

                        try {
                            realUseCase.execute(ContentsType.LOCAL_NEWS)
                            // exit 0: 발행 대상이 없었거나 정상 완료
                        } catch (e: IllegalStateException) {
                            // 스크립트는 ProcessBuilder 로 실행됐으나 non-zero 종료 (예: API 서버 미기동으로 curl 실패)
                            e.message shouldContain "exitCode="
                        }
                    }
                }
            }
        }
    })