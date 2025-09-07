package com.few.generator.service

import com.few.generator.domain.Gen
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.RawContents
import com.few.generator.repository.ProvisioningContentsRepository
import com.few.generator.repository.RawContentsRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class GenUrlServiceTest :
    StringSpec({

        val provisioningContentsRepository = mockk<ProvisioningContentsRepository>()
        val rawContentsRepository = mockk<RawContentsRepository>()
        val genUrlService = GenUrlService(provisioningContentsRepository, rawContentsRepository)

        "빈 Gen 리스트일 때 빈 맵 반환" {
            val result = genUrlService.getRawContentsUrlsByGens(emptyList())
            result shouldBe emptyMap()
        }

        "정상적인 Gen URL 매핑" {
            val gen1 = Gen(id = 1L, provisioningContentsId = 10L, headline = "헤드라인1", summary = "요약1", category = 1)
            val gen2 = Gen(id = 2L, provisioningContentsId = 20L, headline = "헤드라인2", summary = "요약2", category = 1)
            val gens = listOf(gen1, gen2)

            val provisioning1 = ProvisioningContents(id = 10L, rawContentsId = 100L, category = 1)
            val provisioning2 = ProvisioningContents(id = 20L, rawContentsId = 200L, category = 1)
            val provisioningContents = listOf(provisioning1, provisioning2)

            val rawContent1 =
                RawContents(id = 100L, url = "https://example1.com", title = "제목1", rawTexts = "내용1", category = 1, mediaType = 1)
            val rawContent2 =
                RawContents(id = 200L, url = "https://example2.com", title = "제목2", rawTexts = "내용2", category = 1, mediaType = 1)
            val rawContents = listOf(rawContent1, rawContent2)

            every { provisioningContentsRepository.findAllByIdIn(listOf(10L, 20L)) } returns provisioningContents
            every { rawContentsRepository.findAllByIdIn(listOf(100L, 200L)) } returns rawContents

            val result = genUrlService.getRawContentsUrlsByGens(gens)

            result shouldBe mapOf(1L to "https://example1.com", 2L to "https://example2.com")
        }

        "Gen id가 null인 경우 해당 Gen 제외" {
            val gen1 = Gen(id = 1L, provisioningContentsId = 10L, headline = "헤드라인1", summary = "요약1", category = 1)
            val gen2 = Gen(id = null, provisioningContentsId = 20L, headline = "헤드라인2", summary = "요약2", category = 1)
            val gens = listOf(gen1, gen2)

            val provisioning1 = ProvisioningContents(id = 10L, rawContentsId = 100L, category = 1)
            val provisioningContents = listOf(provisioning1)

            val rawContent1 =
                RawContents(id = 100L, url = "https://example1.com", title = "제목1", rawTexts = "내용1", category = 1, mediaType = 1)
            val rawContents = listOf(rawContent1)

            every { provisioningContentsRepository.findAllByIdIn(listOf(10L, 20L)) } returns provisioningContents
            every { rawContentsRepository.findAllByIdIn(listOf(100L)) } returns rawContents

            val result = genUrlService.getRawContentsUrlsByGens(gens)

            result shouldBe mapOf(1L to "https://example1.com")
        }

        "ProvisioningContents가 없는 경우 빈 맵 반환" {
            val gen1 = Gen(id = 1L, provisioningContentsId = 10L, headline = "헤드라인1", summary = "요약1", category = 1)
            val gens = listOf(gen1)

            every { provisioningContentsRepository.findAllByIdIn(listOf(10L)) } returns emptyList()

            val result = genUrlService.getRawContentsUrlsByGens(gens)

            result shouldBe emptyMap()
        }

        "RawContents가 없는 경우 빈 맵 반환" {
            val gen1 = Gen(id = 1L, provisioningContentsId = 10L, headline = "헤드라인1", summary = "요약1", category = 1)
            val gens = listOf(gen1)

            val provisioning1 = ProvisioningContents(id = 10L, rawContentsId = 100L, category = 1)
            val provisioningContents = listOf(provisioning1)

            every { provisioningContentsRepository.findAllByIdIn(listOf(10L)) } returns provisioningContents
            every { rawContentsRepository.findAllByIdIn(listOf(100L)) } returns emptyList()

            val result = genUrlService.getRawContentsUrlsByGens(gens)

            result shouldBe emptyMap()
        }

        "일부 매핑이 실패해도 성공한 것들만 반환" {
            val gen1 = Gen(id = 1L, provisioningContentsId = 10L, headline = "헤드라인1", summary = "요약1", category = 1)
            val gen2 = Gen(id = 2L, provisioningContentsId = 99L, headline = "헤드라인2", summary = "요약2", category = 1)
            val gens = listOf(gen1, gen2)

            val provisioning1 = ProvisioningContents(id = 10L, rawContentsId = 100L, category = 1)
            val provisioningContents = listOf(provisioning1)

            val rawContent1 =
                RawContents(id = 100L, url = "https://example1.com", title = "제목1", rawTexts = "내용1", category = 1, mediaType = 1)
            val rawContents = listOf(rawContent1)

            every { provisioningContentsRepository.findAllByIdIn(listOf(10L, 99L)) } returns provisioningContents
            every { rawContentsRepository.findAllByIdIn(listOf(100L)) } returns rawContents

            val result = genUrlService.getRawContentsUrlsByGens(gens)

            result shouldBe mapOf(1L to "https://example1.com")
        }
    })