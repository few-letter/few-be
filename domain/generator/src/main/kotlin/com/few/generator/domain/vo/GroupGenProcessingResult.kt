package com.few.generator.domain.vo

import com.few.generator.domain.GroupGen

/**
 * 그룹 생성 처리 결과를 나타내는 데이터 클래스
 *
 * @property groupGen 생성된 그룹 Gen 객체
 * @property keywordExtractionTime 키워드 추출에 소요된 시간 (밀리초)
 * @property totalGens 처리 대상이었던 전체 Gen 개수
 */
data class GroupGenProcessingResult(
    val groupGen: GroupGen,
    val keywordExtractionTime: Long,
    val totalGens: Int,
)