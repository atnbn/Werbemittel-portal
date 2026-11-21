package com.miniportal.common;

public class InvalidFieldValueException extends RuntimeException {

    public InvalidFieldValueException(String detail) {
        super(detail);
    }
}
