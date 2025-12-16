package org.example.chickendirect;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex, HttpServletRequest request){

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", new Date());
        error.put("status", ex.getStatusCode().value());
        error.put("error", ex.getStatusCode().toString());
        error.put("message", ex.getReason());
        error.put("path", request.getRequestURI());

        return new ResponseEntity<>(error, ex.getStatusCode());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleInvalidProductEnum(HttpMessageNotReadableException ex) {
        log.warn("Invalid ProductStatus value received: {}", ex.getMessage());

        String message = "Invalid value provided. ProductStatus values are: IN_STOCK, OUT_OF_STOCK, PENDING_RESTOCK";
        return ResponseEntity.badRequest().body(message);
    }

}



