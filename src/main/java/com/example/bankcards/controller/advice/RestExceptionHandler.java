package com.example.bankcards.controller.advice;

import com.example.bankcards.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(CardAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handleAlreadyExistsException(CardAlreadyExistsException ex) {
        String ms = LocalDateTime.now() + ex.getMessage();
        //409
        HttpStatus status = HttpStatus.CONFLICT;
        ExceptionResponse response = new ExceptionResponse(ms, status.value());
        return new ResponseEntity<>(response, status);
    }
    @ExceptionHandler(CardNotActiveException.class)
    public ResponseEntity<ExceptionResponse> handleNotActiveException(CardNotActiveException ex) {
        String ms = LocalDateTime.now() + ex.getMessage();
        HttpStatus status = HttpStatus.CONFLICT;
        ExceptionResponse response = new ExceptionResponse(ms, status.value());
        return new ResponseEntity<>(response, status);
    }
    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(CardNotFoundException ex) {
        String ms = LocalDateTime.now() + ex.getMessage();
        HttpStatus status = HttpStatus.NOT_FOUND;
        ExceptionResponse response = new ExceptionResponse(ms, status.value());
        return new ResponseEntity<>(response, status);
    }
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ExceptionResponse> handleInsufficientFundsException(InsufficientFundsException ex) {
        String ms = LocalDateTime.now() + ex.getMessage();
        HttpStatus status = HttpStatus.CONFLICT;
        ExceptionResponse response = new ExceptionResponse(ms, status.value());
        return new ResponseEntity<>(response, status);
    }
    @ExceptionHandler(InvalidTransferAmountException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidAmountException(InvalidTransferAmountException ex) {
        String ms = LocalDateTime.now() + ex.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionResponse response = new ExceptionResponse(ms, status.value());
        return new ResponseEntity<>(response, status);
    }
    @ExceptionHandler(InvalidTransferToSameCardException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidToSameCardException(InvalidTransferToSameCardException ex) {
        String ms = LocalDateTime.now() + ex.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionResponse response = new ExceptionResponse(ms, status.value());
        return new ResponseEntity<>(response, status);
    }



}