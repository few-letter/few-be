package com.few.generator.core.kis

import com.few.generator.core.kis.dto.KisStockPriceResponse
import feign.FeignException
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(value = "kis")
interface KisClient {
    @GetMapping("/uapi/overseas-price/v1/quotations/price-detail")
    @Throws(FeignException::class)
    fun getStockPrice(
        @RequestHeader("authorization") authorization: String,
        @RequestHeader("tr_id") trId: String,
        @RequestParam("EXCD") excd: String,
        @RequestParam("SYMB") symb: String,
    ): KisStockPriceResponse
}