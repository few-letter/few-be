package com.few.generator.controller

import com.few.common.domain.ContentsType
import com.few.common.exception.BadRequestException
import com.few.generator.controller.request.ContentsSchedulingRequest
import com.few.generator.usecase.GenImageGenerateSchedulingUseCase
import com.few.generator.usecase.GlobalGenSchedulingUseCase
import com.few.generator.usecase.GlobalGroupGenSchedulingUseCase
import com.few.generator.usecase.LocalGenSchedulingUseCase
import com.few.generator.usecase.LocalGroupGenSchedulingUseCase
import com.few.generator.usecase.SendNewsletterSchedulingUseCase
import com.few.web.ApiResponse
import com.few.web.ApiResponseGenerator
import org.springframework.http.HttpStatus
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
    private val genImageGenerateSchedulingUseCase: GenImageGenerateSchedulingUseCase,
) {
    @PostMapping(
        value = ["/contents/schedule"],
    )
    fun createNewsContents(
        @Validated @RequestBody(required = false) request: ContentsSchedulingRequest,
    ): ApiResponse<ApiResponse.Success> {
        when (request.type.uppercase()) {
            ContentsType.GLOBAL_NEWS.title.uppercase() -> globalGenSchedulingUseCase.execute()
            ContentsType.LOCAL_NEWS.title.uppercase() -> localGenSchedulingUseCase.execute()
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
        value = ["/contents/gen/image"],
    )
    fun createGenImages(): ApiResponse<ApiResponse.Success> {
        genImageGenerateSchedulingUseCase.execute()

        return ApiResponseGenerator.success(
            HttpStatus.OK,
        )
    }
}