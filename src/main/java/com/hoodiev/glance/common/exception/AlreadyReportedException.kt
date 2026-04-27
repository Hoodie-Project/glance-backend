package com.hoodiev.glance.common.exception

import org.springframework.http.HttpStatus

class AlreadyReportedException : BusinessException(
    status = HttpStatus.CONFLICT,
    code = "ALREADY_REPORTED",
    message = "이미 신고한 대상입니다."
)
