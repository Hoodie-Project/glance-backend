package com.hoodiev.glance.common.exception

class EntityNotFoundException(entity: String, id: Long)
    : RuntimeException("$entity not found with id: $id")
