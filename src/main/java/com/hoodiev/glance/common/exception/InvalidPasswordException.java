package com.hoodiev.glance.common.exception;

public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super("Password does not match");
    }
}
