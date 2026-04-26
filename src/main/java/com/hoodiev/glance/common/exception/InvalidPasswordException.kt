package com.hoodiev.glance.common.exception

import org.springframework.http.HttpStatus

class InvalidPasswordException
    : BusinessException(HttpStatus.FORBIDDEN, "INVALID_PASSWORD", "비밀번호가 올바르지 않습니다.")
