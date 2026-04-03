package com.few.generator.core.kis.dto

import com.google.gson.annotations.SerializedName

data class KisStockPriceResponse(
    @SerializedName("rt_cd")
    val rtCd: String,
    @SerializedName("msg1")
    val msg1: String,
    val output: Output?,
) {
    data class Output(
        /** 현재가 */
        val last: String,
        /** 원환산 등락률 (%) */
        val t_xrat: String,
    )

    fun isSuccess(): Boolean = rtCd == "0"
}