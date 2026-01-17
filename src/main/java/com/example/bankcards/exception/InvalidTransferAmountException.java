package com.example.bankcards.exception;

public class InvalidTransferAmountException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Amount must be > 0";
    }
}
