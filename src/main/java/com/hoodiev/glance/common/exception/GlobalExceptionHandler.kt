package com.hoodiev.glance.common.exception

import com.hoodiev.glance.common.dto.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(e: BusinessException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(e.status)
            .body(ErrorResponse(e.status.value(), e.code, e.message ?: "오류가 발생했습니다.", LocalDateTime.now()))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(e: MethodArgumentNotValidException): ErrorResponse {
        val message = e.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
            .ifBlank { "Validation failed" }
        return ErrorResponse(400, "VALIDATION_FAILED", message, LocalDateTime.now())
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUnexpected(e: Exception): ErrorResponse {
        log.error("Unexpected error", e)
        return ErrorResponse(500, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.", LocalDateTime.now())
    }
}
