package com.hoodiev.glance.common.exception

import org.springframework.http.HttpStatus

class BoundingBoxTooLargeException(maxDeg: Double)
    : BusinessException(HttpStatus.BAD_REQUEST, "BOUNDING_BOX_TOO_LARGE", "조회 범위가 너무 넓습니다. 위도/경도 범위를 각 ${maxDeg}° 이하로 줄여주세요.")
