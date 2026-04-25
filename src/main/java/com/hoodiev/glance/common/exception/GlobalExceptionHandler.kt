package com.hoodiev.glance.common.exception

import com.hoodiev.glance.common.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(e: EntityNotFoundException) =
        ErrorResponse(404, e.message!!, LocalDateTime.now())

    @ExceptionHandler(InvalidPasswordException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleInvalidPassword(e: InvalidPasswordException) =
        ErrorResponse(403, e.message!!, LocalDateTime.now())

    @ExceptionHandler(RateLimitExceededException::class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    fun handleRateLimit(e: RateLimitExceededException) =
        ErrorResponse(429, e.message!!, LocalDateTime.now())

    @ExceptionHandler(BoundingBoxTooLargeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBoundingBoxTooLarge(e: BoundingBoxTooLargeException) =
        ErrorResponse(400, e.message!!, LocalDateTime.now())

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(e: MethodArgumentNotValidException): ErrorResponse {
        val message = e.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
            .ifBlank { "Validation failed" }
        return ErrorResponse(400, message, LocalDateTime.now())
    }
}
