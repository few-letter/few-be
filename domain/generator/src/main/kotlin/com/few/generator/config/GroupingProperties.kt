package com.few.generator.config

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@ConfigurationProperties(prefix = "generator.grouping")
@Validated
data class GroupingProperties(
    @field:Min(1, message = "타겟 비율은 1% 이상이어야 합니다")
    @field:Max(100, message = "타겟 비율은 100% 이하여야 합니다")
    val targetPercentage: Int = 30,
    @field:Min(1, message = "최소 그룹 크기는 1 이상이어야 합니다")
    val minGroupSize: Int = 3,
    @field:Min(1, message = "최대 그룹 크기는 1 이상이어야 합니다")
    val maxGroupSize: Int = 10,
    @field:DecimalMin(value = "0.0", message = "유사도 임계값은 0.0 이상이어야 합니다")
    @field:DecimalMax(value = "1.0", message = "유사도 임계값은 1.0 이하여야 합니다")
    val similarityThreshold: Double = 0.7,
) {
    init {
        require(minGroupSize <= maxGroupSize) {
            "최소 그룹 크기($minGroupSize)는 최대 그룹 크기($maxGroupSize)보다 작거나 같아야 합니다"
        }
    }
}