package com.few.generator.usecase

import com.few.generator.repository.GenRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Duration
import java.time.LocalDateTime

class DeleteExpiredGenSchedulingUseCaseTest :
    BehaviorSpec({
        val expirationDays = 14L

        Given("설정된 만료일(expirationDays)이 지난 Gen이 존재하는 경우") {
            val genRepository = mockk<GenRepository>()
            val useCase = DeleteExpiredGenSchedulingUseCase(genRepository, expirationDays)
            val cutoffSlot = slot<LocalDateTime>()
            every { genRepository.deleteAllByCreatedAtBefore(capture(cutoffSlot)) } returns 3

            When("execute() 를 호출하면") {
                useCase.execute()

                Then("현재 시각으로부터 설정된 만료일만큼 이전을 기준으로 삭제 쿼리를 수행한다") {
                    val expectedCutoff = LocalDateTime.now().minusDays(expirationDays)
                    Duration.between(cutoffSlot.captured, expectedCutoff).abs().seconds shouldBe 0L

                    verify(exactly = 1) { genRepository.deleteAllByCreatedAtBefore(any()) }
                }
            }
        }

        Given("삭제 대상 Gen이 없는 경우") {
            val genRepository = mockk<GenRepository>()
            val useCase = DeleteExpiredGenSchedulingUseCase(genRepository, expirationDays)
            every { genRepository.deleteAllByCreatedAtBefore(any()) } returns 0

            When("execute() 를 호출하면") {
                Then("예외 없이 정상 종료된다") {
                    useCase.execute()

                    verify(exactly = 1) { genRepository.deleteAllByCreatedAtBefore(any()) }
                }
            }
        }

        Given("삭제 쿼리 수행 중 예외가 발생하는 경우") {
            val genRepository = mockk<GenRepository>()
            val useCase = DeleteExpiredGenSchedulingUseCase(genRepository, expirationDays)
            every { genRepository.deleteAllByCreatedAtBefore(any()) } throws RuntimeException("DB 오류")

            When("execute() 를 호출하면") {
                Then("예외를 전파하지 않고 내부에서 처리한다") {
                    useCase.execute()

                    verify(exactly = 1) { genRepository.deleteAllByCreatedAtBefore(any()) }
                }
            }
        }
    })