package com.few.provider.controller

import com.few.provider.controller.request.EnrollSubscriptionRequest
import com.few.provider.controller.response.CodeValueResponse
import com.few.provider.controller.response.EnrollSubscriptionResponse
import com.few.provider.usecase.EnrollSubscriptionUseCase
import com.few.provider.usecase.input.EnrollSubscriptionUseCaseIn
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import web.ApiResponse
import web.ApiResponseGenerator

@Validated
@RestController
@RequestMapping("/api/v1")
class SubscriptionController(
    private val enrollSubscriptionUseCase: EnrollSubscriptionUseCase,
) {
    @PostMapping("/subscriptions")
    fun enrollSubscription(
        @Validated @RequestBody request: EnrollSubscriptionRequest,
    ): ApiResponse<ApiResponse.SuccessBody<EnrollSubscriptionResponse>> {
        val ucOuts =
            enrollSubscriptionUseCase.execute(
                EnrollSubscriptionUseCaseIn(
                    email = request.email,
                    categoryCodes = request.categoryCodes,
                ),
            )

        return ApiResponseGenerator.success(
            EnrollSubscriptionResponse(
                ucOuts.existingCategories.map { CodeValueResponse(it.code, it.title) },
                ucOuts.newCategories.map { CodeValueResponse(it.code, it.title) },
            ),
            HttpStatus.CREATED,
        )
    }
}