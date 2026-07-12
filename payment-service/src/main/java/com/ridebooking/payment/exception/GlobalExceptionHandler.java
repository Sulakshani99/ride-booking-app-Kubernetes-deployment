package com.ridebooking.payment.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Validation failed");
        body.put("details", ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Validation failed");
        body.put("details", ex.getConstraintViolations().stream()
                .map(err -> err.getPropertyPath() + ": " + err.getMessage())
                .toList());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(PaymentNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Payment not found");
        body.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidPaymentStatusTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTransition(InvalidPaymentStatusTransitionException ex) 
    {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Invalid payment status transition");
        body.put("details", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(PaymentServiceException.class)
    public ResponseEntity<Map<String, Object>> handleServiceError(PaymentServiceException ex) 
    {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Payment service error");
        body.put("details", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }
}
