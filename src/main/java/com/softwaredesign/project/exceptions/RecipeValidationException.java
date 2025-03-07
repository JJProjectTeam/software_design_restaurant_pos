package com.softwaredesign.project.exceptions;

public class RecipeValidationException extends RuntimeException {
    
    public RecipeValidationException(String message) {
        super(message);
    }

    public RecipeValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}