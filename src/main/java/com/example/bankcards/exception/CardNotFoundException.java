package com.example.bankcards.exception;

import java.util.UUID;

public class CardNotFoundException extends RuntimeException{
    @Override
    public String getMessage() {
        return "Card not found";
    }
}
