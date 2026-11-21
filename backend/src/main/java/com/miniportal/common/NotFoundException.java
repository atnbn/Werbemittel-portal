package com.miniportal.common;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String detail) {
        super(detail);
    }
}
