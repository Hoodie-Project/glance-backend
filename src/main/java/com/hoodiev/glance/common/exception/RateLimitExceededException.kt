package com.hoodiev.glance.common.exception

import org.springframework.http.HttpStatus

class RateLimitExceededException
    : BusinessException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", "요청이 너무 많습니다.")
