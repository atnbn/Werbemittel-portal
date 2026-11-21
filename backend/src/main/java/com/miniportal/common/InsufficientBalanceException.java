package com.miniportal.common;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String detail) {
        super(detail);
    }
}
