package com.company.task_manager.exception;

public class ResourceNotFoundException extends RuntimeException {
    // Creates the exception with a message describing what wasn't found.
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
