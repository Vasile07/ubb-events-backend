package com.ubb.events.common.errors;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() { super("Invalid email or password"); }
}
