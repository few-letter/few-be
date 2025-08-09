package com.few.provider.controller

import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import web.ApiResponse
import web.ApiResponseGenerator

@Validated
@RestController
@RequestMapping("/api/v1")
class SubscriptionController {
    @PostMapping(
        value = ["/subscriptions"],
    )
    fun example(): ApiResponse<ApiResponse.Success> =
        ApiResponseGenerator.success(
            HttpStatus.OK,
        )
}