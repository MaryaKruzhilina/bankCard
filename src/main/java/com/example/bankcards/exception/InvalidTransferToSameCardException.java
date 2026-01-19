package com.example.bankcards.exception;

public class InvalidTransferToSameCardException extends RuntimeException{

    @Override
    public String getMessage() {
        return "Cannot transfer to the same card";
    }
}
