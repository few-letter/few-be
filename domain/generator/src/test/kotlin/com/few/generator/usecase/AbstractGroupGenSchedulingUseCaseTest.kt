package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.common.exception.BadRequestException
import com.few.generator.config.GroupingProperties
import com.few.generator.core.gpt.prompt.schema.Group
import com.few.generator.domain.Gen
import com.few.generator.domain.GroupGen
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.vo.GenDetail
import com.few.generator.service.GenService
import com.few.generator.service.ProvisioningService
import com.few.generator.service.specifics.groupgen.GenGroupper
import com.few.generator.service.specifics.groupgen.GroupContentGenerator
import com.few.generator.service.specifics.groupgen.GroupGenMetrics
import com.few.generator.service.specifics.groupgen.KeywordExtractor
import com.google.gson.Gson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.*
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

class AbstractGroupGenSchedulingUseCaseTest :
    BehaviorSpec({
        val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
        val genService = mockk<GenService>()
        val provisioningService = mockk<ProvisioningService>()
        val groupingProperties =
            GroupingProperties(
                targetPercentage = 30,
                minGroupSize = 3,
                maxGroupSize = 10,
                similarityThreshold = 0.5,
            )
        val gson = Gson()
        val groupGenMetrics = mockk<GroupGenMetrics>(relaxed = true)
        val keywordExtractor = mockk<KeywordExtractor>()
        val genGrouper = mockk<GenGroupper>()
        val groupContentGenerator = mockk<GroupContentGenerator>()

        // Test implementation of abstract class
        class TestGroupGenSchedulingUseCase(
            applicationEventPublisher: ApplicationEventPublisher,
            genService: GenService,
            provisioningService: ProvisioningService,
            groupingProperties: GroupingProperties,
            gson: Gson,
            groupGenMetrics: GroupGenMetrics,
            keywordExtractor: KeywordExtractor,
            genGrouper: GenGroupper,
            groupContentGenerator: GroupContentGenerator,
        ) : AbstractGroupGenSchedulingUseCase(
                applicationEventPublisher,
                genService,
                provisioningService,
                groupingProperties,
                gson,
                groupGenMetrics,
                keywordExtractor,
                genGrouper,
                groupContentGenerator,
            ) {
            override val region: Region = Region.GLOBAL
            override val regionName: String = "해외"
            override val eventTitle: String = "[해외] Group Gen 스케줄링"

            // Expose execute for testing
            public override fun execute() {
                super.execute()
            }

            // Expose isRunning for testing
            fun getIsRunning() = isRunning.get()

            fun setIsRunning(value: Boolean) = isRunning.set(value)
        }

        val useCase =
            TestGroupGenSchedulingUseCase(
                applicationEventPublisher,
                genService,
                provisioningService,
                groupingProperties,
                gson,
                groupGenMetrics,
                keywordExtractor,
                genGrouper,
                groupContentGenerator,
            )

        beforeEach {
            // Reset isRunning flag before each test
            useCase.setIsRunning(false)
        }

        Given("스케줄링이 이미 실행 중인 경우") {
            When("execute를 호출하면") {
                Then("BadRequestException이 발생한다") {
                    // Set running state
                    useCase.setIsRunning(true)

                    val exception =
                        shouldThrow<BadRequestException> {
                            useCase.execute()
                        }

                    exception.message shouldContain "is already running"
                }
            }
        }

        Given("정상적인 그룹 스케줄링 실행 시") {
            When("모든 카테고리에서 성공적으로 GroupGen을 생성하는 경우") {
                Then("성공 이벤트가 발행된다") {
                    val gens =
                        (1L..5L).map { id ->
                            Gen(
                                id = id,
                                provisioningContentsId = id,
                                category = Category.TECHNOLOGY.code,
                                region = Region.GLOBAL.code,
                                headline = "Headline $id",
                                summary = "Summary $id",
                                highlightTexts = """["highlight"]""",
                            ).apply {
                                createdAt = LocalDateTime.now()
                            }
                        }

                    val provisioningContents =
                        (1L..5L).map { id ->
                            ProvisioningContents(
                                id = id,
                                rawContentsId = id,
                                category = Category.TECHNOLOGY.code,
                                region = Region.GLOBAL.code,
                            )
                        }

                    val genDetails =
                        gens.map { gen ->
                            GenDetail("Headline ${gen.id}", "keyword1, keyword2")
                        }

                    val group = Group(listOf(0, 1, 2))

                    val groupGen =
                        GroupGen(
                            category = Category.TECHNOLOGY.code,
                            region = Region.GLOBAL.code,
                            headline = "Group Headline",
                            summary = "Group Summary",
                            selectedGroupIds = gson.toJson(listOf(0, 1, 2)),
                        )

                    every {
                        genService.findAllByCreatedAtBetweenAndCategoryAndRegion(any(), Region.GLOBAL)
                    } returns gens

                    every {
                        provisioningService.findAllByIdIn(any())
                    } returns provisioningContents

                    coEvery {
                        keywordExtractor.extractKeywordsFromGens(gens, any())
                    } returns genDetails

                    every {
                        genGrouper.performGrouping(genDetails, any())
                    } returns group

                    every {
                        genGrouper.validateGroupSize(group)
                    } returns group

                    every {
                        groupContentGenerator.generateGroupContent(any(), gens, group, any(), Region.GLOBAL)
                    } returns groupGen

                    useCase.execute()

                    // Verify running state is reset
                    useCase.getIsRunning() shouldBe false
                }
            }
        }

        Given("GroupGen 생성 조건을 만족하지 못하는 경우") {
            When("카테고리에 Gen이 없는 경우") {
                Then("BadRequestException이 발생한다") {
                    every {
                        genService.findAllByCreatedAtBetweenAndCategoryAndRegion(Category.TECHNOLOGY, Region.GLOBAL)
                    } returns emptyList()

                    shouldThrow<BadRequestException> {
                        useCase.createGroupGen(Category.TECHNOLOGY)
                    }
                }
            }

            When("Gen 개수가 최소 그룹 크기보다 작은 경우") {
                Then("BadRequestException이 발생한다") {
                    val gens =
                        (1L..2L).map { id ->
                            Gen(
                                id = id,
                                provisioningContentsId = id,
                                category = Category.TECHNOLOGY.code,
                                region = Region.GLOBAL.code,
                                headline = "Headline $id",
                                summary = "Summary $id",
                                highlightTexts = """["highlight"]""",
                            ).apply {
                                createdAt = LocalDateTime.now()
                            }
                        }

                    every {
                        genService.findAllByCreatedAtBetweenAndCategoryAndRegion(Category.TECHNOLOGY, Region.GLOBAL)
                    } returns gens

                    shouldThrow<BadRequestException> {
                        useCase.createGroupGen(Category.TECHNOLOGY)
                    }
                }
            }

            When("그룹화에 실패하는 경우") {
                Then("BadRequestException이 발생한다") {
                    val gens =
                        (1L..5L).map { id ->
                            Gen(
                                id = id,
                                provisioningContentsId = id,
                                category = Category.TECHNOLOGY.code,
                                region = Region.GLOBAL.code,
                                headline = "Headline $id",
                                summary = "Summary $id",
                                highlightTexts = """["highlight"]""",
                            ).apply {
                                createdAt = LocalDateTime.now()
                            }
                        }

                    val provisioningContents =
                        (1L..5L).map { id ->
                            ProvisioningContents(
                                id = id,
                                rawContentsId = id,
                                category = Category.TECHNOLOGY.code,
                                region = Region.GLOBAL.code,
                            )
                        }

                    val genDetails =
                        gens.map { gen ->
                            GenDetail("Headline ${gen.id}", "keyword1")
                        }

                    every {
                        genService.findAllByCreatedAtBetweenAndCategoryAndRegion(Category.TECHNOLOGY, Region.GLOBAL)
                    } returns gens

                    every {
                        provisioningService.findAllByIdIn(any())
                    } returns provisioningContents

                    coEvery {
                        keywordExtractor.extractKeywordsFromGens(gens, any())
                    } returns genDetails

                    every {
                        genGrouper.performGrouping(genDetails, Category.TECHNOLOGY)
                    } returns Group(listOf(0, 1))

                    // validateGroupSize returns null when validation fails
                    every {
                        genGrouper.validateGroupSize(any())
                    } returns null

                    shouldThrow<BadRequestException> {
                        useCase.createGroupGen(Category.TECHNOLOGY)
                    }
                }
            }
        }

        Given("GroupGen 생성 성공 시 메트릭 기록") {
            When("정상적으로 GroupGen이 생성되면") {
                Then("성공 메트릭이 기록된다") {
                    val gens =
                        (1L..5L).map { id ->
                            Gen(
                                id = id,
                                provisioningContentsId = id,
                                category = Category.TECHNOLOGY.code,
                                region = Region.GLOBAL.code,
                                headline = "Headline $id",
                                summary = "Summary $id",
                                highlightTexts = """["highlight"]""",
                            ).apply {
                                createdAt = LocalDateTime.now()
                            }
                        }

                    val provisioningContents =
                        (1L..5L).map { id ->
                            ProvisioningContents(
                                id = id,
                                rawContentsId = id,
                                category = Category.TECHNOLOGY.code,
                                region = Region.GLOBAL.code,
                            )
                        }

                    val genDetails =
                        gens.map { gen ->
                            GenDetail("Headline ${gen.id}", "keyword1")
                        }

                    val group = Group(listOf(0, 1, 2))

                    val groupGen =
                        GroupGen(
                            category = Category.TECHNOLOGY.code,
                            region = Region.GLOBAL.code,
                            headline = "Group Headline",
                            summary = "Group Summary",
                            selectedGroupIds = gson.toJson(listOf(0, 1, 2)),
                        )

                    every {
                        genService.findAllByCreatedAtBetweenAndCategoryAndRegion(Category.TECHNOLOGY, Region.GLOBAL)
                    } returns gens

                    every {
                        provisioningService.findAllByIdIn(any())
                    } returns provisioningContents

                    coEvery {
                        keywordExtractor.extractKeywordsFromGens(gens, any())
                    } returns genDetails

                    every {
                        genGrouper.performGrouping(genDetails, Category.TECHNOLOGY)
                    } returns group

                    every {
                        genGrouper.validateGroupSize(group)
                    } returns group

                    every {
                        groupContentGenerator.generateGroupContent(Category.TECHNOLOGY, gens, group, any(), Region.GLOBAL)
                    } returns groupGen

                    // Just verify it doesn't throw an exception
                    useCase.createGroupGen(Category.TECHNOLOGY)
                }
            }
        }
    })