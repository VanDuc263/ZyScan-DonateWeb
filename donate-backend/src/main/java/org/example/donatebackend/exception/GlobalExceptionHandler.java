package org.example.donatebackend.exception;

import org.example.donatebackend.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException e){
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST).body(
                        new ErrorResponse(
                                e.getCode().name(),
                                e.getMessage()
                        )
                );
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e){
        return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
        ).body(
                new ErrorResponse(
                        "INTERNAL_ERROR",
                        "Something went wrong"
                )
        );
    }

}
