package com.hoodiev.glance.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entity, Long id) {
        super(entity + " not found with id: " + id);
    }
}
