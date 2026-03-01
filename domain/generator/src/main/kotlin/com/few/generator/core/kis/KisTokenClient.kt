package com.few.generator.core.kis

import com.few.generator.core.kis.dto.KisTokenRequest
import com.few.generator.core.kis.dto.KisTokenResponse
import feign.FeignException
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(value = "kis-token")
interface KisTokenClient {
    @PostMapping("/oauth2/tokenP")
    @Throws(FeignException::class)
    fun getToken(
        @RequestBody request: KisTokenRequest,
    ): KisTokenResponse
}