package com.hoodiev.glance.common.exception

import org.springframework.http.HttpStatus

abstract class BusinessException(
    val status: HttpStatus,
    val code: String,
    message: String
) : RuntimeException(message)
