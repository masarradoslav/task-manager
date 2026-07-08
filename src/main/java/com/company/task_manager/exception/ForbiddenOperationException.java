package com.company.task_manager.exception;

public class ForbiddenOperationException extends RuntimeException {
    // Creates the exception with a message describing why the operation is not allowed.
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
