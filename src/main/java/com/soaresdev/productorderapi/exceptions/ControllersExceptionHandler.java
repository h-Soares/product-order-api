package com.soaresdev.productorderapi.exceptions;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.List;

@RestControllerAdvice
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

    @ExceptionHandler(MethodArgumentNotValidException.class) //thrown by insert DTOs
    public ResponseEntity<StandardInsertDTOError> methodArgumentNotValid(MethodArgumentNotValidException e,
                                                                         HttpServletRequest request) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted().toList();
        StandardInsertDTOError insertDTOError = getStandardInsertDTOError(HttpStatus.BAD_REQUEST, e,
                request, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(insertDTOError);
    }

    @ExceptionHandler(NotPaidException.class)
    public ResponseEntity<StandardError> notPaid(NotPaidException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).
                body(getStandardError(HttpStatus.PAYMENT_REQUIRED, e, request));
    }

    @ExceptionHandler(AlreadyPaidException.class)
    public ResponseEntity<StandardError> alreadyPaid(AlreadyPaidException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(getStandardError(HttpStatus.FORBIDDEN, e, request));
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<StandardError> badCredentials(BadCredentialsException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(getStandardError(HttpStatus.UNAUTHORIZED, e, request));
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

    private StandardInsertDTOError getStandardInsertDTOError(HttpStatus httpStatus, Exception e,
                                                             HttpServletRequest request, List<String> errors) {
        StandardInsertDTOError insertDTOError = new StandardInsertDTOError();
        insertDTOError.setTimestamp(Instant.now());
        insertDTOError.setStatus(httpStatus.value());
        insertDTOError.setErrors(errors);
        insertDTOError.setPath(request.getRequestURI());
        return insertDTOError;
    }
}