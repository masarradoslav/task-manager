package com.company.task_manager.controller;

import com.company.task_manager.exception.ForbiddenOperationException;
import com.company.task_manager.exception.OptimisticLockConflictException;
import com.company.task_manager.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

// Maps service-layer exceptions to short plain-text HTTP error responses.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Business-rule violations (duplicate username, missing password, ...) -> 400.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // Unknown task/worker ids -> 404.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    // Role/department authorization failures -> 403.
    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<String> handleForbidden(ForbiddenOperationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    // Safety net for DB constraint violations (e.g. a duplicate username slipping past the
    // service-layer check in a race) - returns a short 400 instead of a 500 with a stack trace.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.badRequest().body("The change conflicts with existing data (e.g. a username that is already taken)");
    }

    // Stale write: the client's declared version doesn't match the current row -> 409.
    @ExceptionHandler(OptimisticLockConflictException.class)
    public ResponseEntity<String> handleOptimisticLockConflict(OptimisticLockConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    // Safety net for the narrow race window between our own version check and the actual
    // UPDATE (Hibernate's own @Version check firing first) - same outcome as the check above.
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<String> handleHibernateOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("This record was changed by someone else since you loaded it. Reload and try again.");
    }

    // Bean Validation failures on request DTOs -> 400 listing each offending field.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(message);
    }
}
