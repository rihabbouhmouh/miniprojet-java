package com.eventmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotFoundException(Class<?> clazz, Long id) {
        super(String.format("%s avec ID %d non trouvé", clazz.getSimpleName(), id));
    }

    public ResourceNotFoundException(Class<?> clazz, String identifier) {
        super(String.format("%s avec identifiant %s non trouvé", clazz.getSimpleName(), identifier));
    }
}