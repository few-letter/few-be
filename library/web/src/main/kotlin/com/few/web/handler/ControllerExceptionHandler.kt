package com.few.web.handler

import com.few.common.exception.BadRequestException
import com.few.web.ApiResponse
import com.few.web.ApiResponseGenerator
import com.few.web.ExceptionMessage
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.beans.TypeMismatchException
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ServerWebInputException
import java.nio.file.AccessDeniedException

@RestControllerAdvice
class ControllerExceptionHandler(
    private val loggingHandler: LoggingHandler,
) {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(
        ex: IllegalArgumentException,
        request: HttpServletRequest,
    ): ApiResponse<ApiResponse.FailureBody> {
        loggingHandler.writeLog(ex, request)
        return ApiResponseGenerator.fail(ExceptionMessage.FAIL.message, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(
        MethodArgumentTypeMismatchException::class,
        TypeMismatchException::class,
        WebExchangeBindException::class,
        BindException::class,
        MethodArgumentNotValidException::class,
        DecodingException::class,
        ConstraintViolationException::class,
        ServerWebInputException::class,
        HttpMessageNotReadableException::class,
    )
    fun handleBadRequest(
        ex: Exception,
        request: HttpServletRequest,
    ): ApiResponse<ApiResponse.FailureBody> {
        loggingHandler.writeLog(ex, request)
        return handleRequestDetails(ex)
    }

    private fun handleRequestDetails(ex: Exception): ApiResponse<ApiResponse.FailureBody> {
        if (ex is MethodArgumentTypeMismatchException) {
            return handleRequestDetail(ex)
        }
        if (ex is MethodArgumentNotValidException) {
            return handleRequestDetail(ex)
        }
        return ApiResponseGenerator.fail(
            ExceptionMessage.FAIL_REQUEST.message,
            HttpStatus.BAD_REQUEST,
        )
    }

    private fun handleRequestDetail(ex: MethodArgumentTypeMismatchException): ApiResponse<ApiResponse.FailureBody> {
        val messageDetail = ExceptionMessage.REQUEST_INVALID_FORMAT.message + " : " + ex.name
        return ApiResponseGenerator.fail(messageDetail, HttpStatus.BAD_REQUEST)
    }

    private fun handleRequestDetail(ex: MethodArgumentNotValidException): ApiResponse<ApiResponse.FailureBody> {
        val filedErrors = ex.fieldErrors.toList()
        val messageDetail = ExceptionMessage.REQUEST_INVALID.message + " : " + filedErrors
        return ApiResponseGenerator.fail(
            messageDetail,
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(
        ex: Exception,
        request: HttpServletRequest,
    ): ApiResponse<ApiResponse.FailureBody> {
        loggingHandler.writeLog(ex, request)
        return ApiResponseGenerator.fail(
            ExceptionMessage.FAIL.message,
            HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleForbidden(
        ex: AccessDeniedException,
        request: HttpServletRequest,
    ): ApiResponse<ApiResponse.FailureBody> {
        loggingHandler.writeLog(ex, request)
        return ApiResponseGenerator.fail(
            ExceptionMessage.ACCESS_DENIED.message,
            HttpStatus.FORBIDDEN,
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleInternalServerError(
        ex: Exception,
        request: HttpServletRequest,
    ): ApiResponse<ApiResponse.FailureBody> {
        loggingHandler.writeLog(ex, request)
        return ApiResponseGenerator.fail(
            ExceptionMessage.FAIL.message,
            HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(
        ex: RuntimeException,
        request: HttpServletRequest,
    ): ApiResponse<ApiResponse.FailureBody> {
        loggingHandler.writeLog(ex, request)
        return ApiResponseGenerator.fail(
            ex.message ?: ExceptionMessage.FAIL.message,
            HttpStatus.BAD_REQUEST, // TODO: 예외 및 코드에 대한 정의 필요
        )
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(
        ex: RuntimeException,
        request: HttpServletRequest,
    ): ApiResponse<ApiResponse.FailureBody> {
        loggingHandler.writeLog(ex, request)
        return ApiResponseGenerator.fail(
            ex.message ?: ExceptionMessage.FAIL.message,
            HttpStatus.INTERNAL_SERVER_ERROR, // TODO: 예외 및 코드에 대한 정의 필요
        )
    }
}