package com.few.generator.controller

import com.few.generator.controller.request.EnrollSubscriptionRequest
import com.few.generator.controller.response.BrowseSubscriptionResponse
import com.few.generator.controller.response.CodeValueResponse
import com.few.generator.usecase.BrowseSubscriptionUseCase
import com.few.generator.usecase.EnrollSubscriptionUseCase
import com.few.generator.usecase.UnsubscribeUseCase
import com.few.generator.usecase.input.BrowseSubscriptionUseCaseIn
import com.few.generator.usecase.input.EnrollSubscriptionUseCaseIn
import com.few.generator.usecase.input.UnsubscribeUseCaseIn
import com.few.web.ApiResponse
import com.few.web.ApiResponseGenerator
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1")
class SubscriptionControllerV1(
    private val enrollSubscriptionUseCase: EnrollSubscriptionUseCase,
    private val browseSubscriptionUseCase: BrowseSubscriptionUseCase,
    private val unsubscribeUseCase: UnsubscribeUseCase,
) {
    @PostMapping("/subscriptions")
    fun enrollSubscription(
        @Validated @RequestBody request: EnrollSubscriptionRequest,
    ): ApiResponse<ApiResponse.SuccessBody<BrowseSubscriptionResponse>> {
        val ucOuts =
            enrollSubscriptionUseCase.execute(
                EnrollSubscriptionUseCaseIn(
                    email = request.email,
                    categoryCodes = request.categoryCodes,
                ),
            )

        return ApiResponseGenerator.success(
            BrowseSubscriptionResponse(
                ucOuts.subscribedCategories.map { CodeValueResponse(it.code, it.title) },
            ),
            HttpStatus.CREATED,
        )
    }

    @GetMapping("/subscriptions")
    fun browseSubscription(
        @RequestHeader("email") email: String, // TODO: auth 적용
    ): ApiResponse<ApiResponse.SuccessBody<BrowseSubscriptionResponse>> {
        val ucOuts =
            browseSubscriptionUseCase.execute(
                BrowseSubscriptionUseCaseIn(
                    email,
                ),
            )

        return ApiResponseGenerator.success(
            BrowseSubscriptionResponse(
                ucOuts.subscribedCategories.map { CodeValueResponse(it.code, it.title) },
            ),
            HttpStatus.OK,
        )
    }

    @DeleteMapping("/subscriptions")
    fun unsubscribe(
        @RequestHeader("email") email: String, // TODO: auth 적용
    ): ApiResponse<ApiResponse.SuccessBody<Unit>> {
        unsubscribeUseCase.execute(
            UnsubscribeUseCaseIn(email),
        )

        return ApiResponseGenerator.success(
            Unit,
            HttpStatus.NO_CONTENT,
        )
    }
}