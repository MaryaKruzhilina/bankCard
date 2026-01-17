package com.example.bankcards.exception;

public class CardAlreadyExistsException extends RuntimeException{
    @Override
    public String getMessage() {
        return "Card already exists";
    }
}
