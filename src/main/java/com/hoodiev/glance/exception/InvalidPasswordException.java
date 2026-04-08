package com.hoodiev.glance.exception;

public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super("Password does not match");
    }
}
