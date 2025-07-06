package com.few.generator.fixture.gpt

/**
 * Keywords 테스트에서 사용하는 테스트 데이터 상수들
 */
object KeywordsTestData {
    /**
     * 기본 키워드 생성 테스트를 위한 핵심 텍스트
     */
    const val DEFAULT_CORE_TEXT = "인공지능 기술이 발전하면서 자동화가 가속화되고 있다"

    /**
     * 빈 문자열 입력 테스트를 위한 핵심 텍스트
     */
    const val EMPTY_TEXT = ""

    /**
     * ChatGPT 응답이 Keywords 타입이 아닐 때 예외 발생 테스트를 위한 핵심 텍스트
     */
    const val ERROR_TEXT = "오류 테스트 텍스트"

    /**
     * 비동기 키워드 생성 테스트를 위한 핵심 텍스트
     */
    const val ASYNC_TEXT = "비동기 테스트를 위한 핵심 텍스트"

    /**
     * 코루틴 키워드 생성 테스트를 위한 핵심 텍스트
     */
    const val COROUTINE_TEXT = "코루틴 테스트를 위한 핵심 텍스트"

    /**
     * 비동기 처리 중 예외 발생 테스트를 위한 핵심 텍스트
     */
    const val ASYNC_ERROR_TEXT = "비동기 오류 테스트"

    /**
     * 코루틴 처리 중 예외 발생 테스트를 위한 핵심 텍스트
     */
    const val COROUTINE_ERROR_TEXT = "코루틴 오류 테스트"

    /**
     * 키워드 추출 실패 시 반환되는 메시지
     */
    const val FAILURE_MESSAGE = "키워드 추출 실패"
}