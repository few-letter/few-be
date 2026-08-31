package com.few.generator.controller

import com.few.common.domain.ContentsType
import com.few.common.exception.BadRequestException
import com.few.generator.controller.request.ContentsSchedulingRequest
import com.few.generator.controller.response.CheckPublishableContentResponse
import com.few.generator.usecase.CheckPublishableContentUseCase
import com.few.generator.usecase.GenCardNewsImageGenerateSchedulingUseCase
import com.few.generator.usecase.GlobalGenSchedulingUseCase
import com.few.generator.usecase.GlobalGroupGenSchedulingUseCase
import com.few.generator.usecase.LocalGenSchedulingUseCase
import com.few.generator.usecase.LocalGroupGenSchedulingUseCase
import com.few.generator.usecase.PopularNasdaqStockScrapingSchedulingUseCase
import com.few.generator.usecase.SendNewsletterSchedulingUseCase
import com.few.generator.usecase.StockBriefingSchedulingUseCase
import com.few.generator.usecase.TriggerContentsPublishSkillsUseCase
import com.few.web.ApiResponse
import com.few.web.ApiResponseGenerator
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/api/v1")
class AdminControllerV1(
    private val localGenSchedulingUseCase: LocalGenSchedulingUseCase,
    private val globalGenSchedulingUseCase: GlobalGenSchedulingUseCase,
    private val newsletterSchedulingUseCase: SendNewsletterSchedulingUseCase,
    private val localGroupGenSchedulingUseCase: LocalGroupGenSchedulingUseCase,
    private val globalGroupGenSchedulingUseCase: GlobalGroupGenSchedulingUseCase,
    private val genCardNewsImageGenerateSchedulingUseCase: GenCardNewsImageGenerateSchedulingUseCase,
    private val stockBriefingSchedulingUseCase: StockBriefingSchedulingUseCase,
    private val popularNasdaqStockScrapingSchedulingUseCase: PopularNasdaqStockScrapingSchedulingUseCase,
    private val checkPublishableContentUseCase: CheckPublishableContentUseCase,
    private val triggerContentsPublishSkillsUseCase: TriggerContentsPublishSkillsUseCase,
) {
    @PostMapping(
        value = ["/contents/skills/publish"],
    )
    fun triggerContentsPublishSkills(
        @RequestParam(value = "contentsType") contentsType: Int,
    ): ApiResponse<ApiResponse.Success> {
        triggerContentsPublishSkillsUseCase.executeAsync(ContentsType.fromCode(contentsType))

        return ApiResponseGenerator.success(
            HttpStatus.OK,
        )
    }

    @GetMapping(
        value = ["/contents/exists/publishable"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun checkPublishableContent(
        @RequestParam(value = "contentsType", required = false) contentsType: Int?,
    ): ApiResponse<ApiResponse.SuccessBody<CheckPublishableContentResponse>> {
        // contentsType(ContentsType code) 미전달 시 전체 ContentsType 으로 간주 (null 전달)
        val targetContentsType = contentsType?.let { ContentsType.fromCode(it) }

        val out = checkPublishableContentUseCase.execute(targetContentsType)

        val response =
            CheckPublishableContentResponse(
                hasPublishableContent = out.hasPublishableContent,
                count = out.count,
                contentsType = out.contentsTypes?.map { it.title },
            )

        return ApiResponseGenerator.success(response, HttpStatus.OK)
    }

    @PostMapping(
        value = ["/contents/schedule"],
    )
    fun createNewsContents(
        @Validated @RequestBody(required = false) request: ContentsSchedulingRequest,
    ): ApiResponse<ApiResponse.Success> {
        when (request.type.uppercase()) {
            ContentsType.GLOBAL_NEWS.title.uppercase() -> globalGenSchedulingUseCase.executeAsync()
            ContentsType.LOCAL_NEWS.title.uppercase() -> localGenSchedulingUseCase.executeAsync()
            else -> throw BadRequestException("Invalid Contents Type: ${request.type}")
        }

        return ApiResponseGenerator.success(
            HttpStatus.OK,
        )
    }

    @PostMapping(
        value = ["/contents/send"],
    )
    fun sendAll(): ApiResponse<ApiResponse.Success> {
        newsletterSchedulingUseCase.execute()

        return ApiResponseGenerator.success(
            HttpStatus.OK,
        )
    }

    @PostMapping(
        value = ["/contents/groups/schedule"],
    )
    fun createAllGroupGen(
        @Validated @RequestBody(required = false) request: ContentsSchedulingRequest,
    ): ApiResponse<ApiResponse.Success> {
        when (request.type.uppercase()) {
            ContentsType.GLOBAL_NEWS.title.uppercase() -> globalGroupGenSchedulingUseCase.execute()
            ContentsType.LOCAL_NEWS.title.uppercase() -> localGroupGenSchedulingUseCase.execute()
            else -> throw BadRequestException("Invalid Contents Type: ${request.type}")
        }

        return ApiResponseGenerator.success(
            HttpStatus.OK,
        )
    }

    @PostMapping(
        value = ["/contents/cardnews/generate"],
    )
    fun createGenImages(
        @RequestParam region: String,
    ): ApiResponse<ApiResponse.Success> {
        val targetRegion =
            when (region.uppercase()) {
                "LOCAL" -> com.few.common.domain.Region.LOCAL
                "GLOBAL" -> com.few.common.domain.Region.GLOBAL
                else -> throw BadRequestException("Invalid region: $region. Must be LOCAL or GLOBAL.")
            }

        genCardNewsImageGenerateSchedulingUseCase.execute(targetRegion)

        return ApiResponseGenerator.success(
            HttpStatus.OK,
        )
    }

    @PostMapping(
        value = ["/contents/cardnews/briefing"],
    )
    fun triggerStockBriefing(): ApiResponse<ApiResponse.Success> {
        stockBriefingSchedulingUseCase.executeAsync()

        return ApiResponseGenerator.success(
            HttpStatus.OK,
        )
    }

    @PostMapping(
        value = ["/contents/cardnews/popularNasdaq"],
    )
    fun triggerTopPopularityNasdaqStockNews(): ApiResponse<ApiResponse.Success> {
        popularNasdaqStockScrapingSchedulingUseCase.executeAsync()

        return ApiResponseGenerator.success(
            HttpStatus.OK,
        )
    }
}