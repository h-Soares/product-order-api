package com.soaresdev.productorderapi.exceptions;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.Instant;

@ControllerAdvice
public class ControllersExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandardError> illegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(getStandardError(HttpStatus.BAD_REQUEST, e, request));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<StandardError> entityNotFound(EntityNotFoundException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).
               body(getStandardError(HttpStatus.NOT_FOUND, e, request));
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<StandardError> entityExists(EntityExistsException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).
               body(getStandardError(HttpStatus.CONFLICT, e, request));
    }

    private StandardError getStandardError(HttpStatus hs, Exception e, HttpServletRequest request) {
        StandardError standardError = new StandardError();
        standardError.setTimestamp(Instant.now());
        standardError.setStatus(hs.value());
        standardError.setError(e.getClass().getSimpleName());
        standardError.setMessage(e.getMessage());
        standardError.setPath(request.getRequestURI());
        return standardError;
    }
}