package com.miniportal.common;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String detail) {
        super(detail);
    }
}
