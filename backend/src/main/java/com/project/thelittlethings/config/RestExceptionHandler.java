// src/main/java/com/project/thelittlethings/config/RestExceptionHandler.java
package com.project.thelittlethings.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String,Object>> handleBadReq(IllegalArgumentException ex) {
    // Heuristic mapping: tweak messages to set status precisely.
    String msg = ex.getMessage() == null ? "bad request" : ex.getMessage().toLowerCase();
    HttpStatus status =
        msg.contains("not found") ? HttpStatus.NOT_FOUND :
        msg.contains("already exists") ? HttpStatus.CONFLICT :
        HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status).body(Map.of(
        "error", status.getReasonPhrase(),
        "message", ex.getMessage()
    ));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String,Object>> handleConflict(DataIntegrityViolationException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
        "error", "Conflict",
        "message", "duplicate or constraint violation"
    ));
  }
}
