package com.example.bankcards.exception;

public class CardNotFoundException extends RuntimeException{

    @Override
    public String getMessage() {
        return "Card not found";
    }
}
