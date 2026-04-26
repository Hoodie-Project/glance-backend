package com.hoodiev.glance.common.exception

import org.springframework.http.HttpStatus

class EntityNotFoundException(entity: String, id: Long)
    : BusinessException(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", "${entity}를 찾을 수 없습니다.")
