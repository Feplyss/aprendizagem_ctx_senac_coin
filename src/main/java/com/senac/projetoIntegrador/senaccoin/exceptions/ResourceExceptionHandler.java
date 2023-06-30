package com.senac.projetoIntegrador.senaccoin.exceptions;

import java.sql.Timestamp;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.senac.projetoIntegrador.senaccoin.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ResourceExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<StandardError> userNotFound(UserNotFoundException ex, HttpServletRequest request){
        StandardError errors = new StandardError(new Timestamp(System.currentTimeMillis()), HttpStatus.FORBIDDEN.value(), "Validation error", "Usuário não encontrado.", request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errors);
    }

    @ExceptionHandler(BalanceNotFoundException.class)
    public ResponseEntity<StandardError> userNotFound(BalanceNotFoundException ex, HttpServletRequest request){
        StandardError errors = new StandardError(new Timestamp(System.currentTimeMillis()), HttpStatus.FORBIDDEN.value(), "Validation error", "Id do Usuário ou SenacCoin inválido.", request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errors);
    }

    @ExceptionHandler(InsuficientBalanceException.class)
    public ResponseEntity<StandardError> insuficientBalance(InsuficientBalanceException ex, HttpServletRequest request){
        StandardError errors = new StandardError(new Timestamp(System.currentTimeMillis()), HttpStatus.FORBIDDEN.value(), "Invalid value error", "Saldo insuficiente para realizar a transação.", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Error parsing JSON", e.getMessage(), null));
    }
}
