package com.example.bankcards.exception;

import java.util.UUID;

public class CardNotActiveException extends RuntimeException{
    private final String cardNumber;
    public CardNotActiveException(String cardLastFourNumber){
        this.cardNumber = cardLastFourNumber;
    }
    @Override
    public String getMessage()
    {
        return "Card is not active: **** **** **** " + cardNumber;
    }
}
