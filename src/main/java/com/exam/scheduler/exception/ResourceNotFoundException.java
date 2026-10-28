package com.exam.scheduler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Thrown when a requested entity does not exist; resolved by Spring MVC as HTTP 404.
 */
public class ResourceNotFoundException extends ResponseStatusException {

    public ResourceNotFoundException(String resource, Long id) {
        super(HttpStatus.NOT_FOUND, resource + " not found with id: " + id);
    }
}
