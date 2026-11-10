package com.example.zerowaste.exception;

public class EmptyShoppingCartException extends RuntimeException {

    public EmptyShoppingCartException(String message) {
        super(message);
    }
}
