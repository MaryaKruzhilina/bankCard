package com.example.bankcards.exception;

public class InsufficientFundsException extends RuntimeException{

    private final String cardNumber;

    public InsufficientFundsException(String cardLastFourNumber){
        this.cardNumber = cardLastFourNumber;
    }

    @Override
    public String getMessage()
    {
        return "Not enough money to card: **** **** **** " + cardNumber;
    }
}
