package com.exam.scheduler.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

/**
 * Shared error translation for REST endpoints that report unexpected failures as
 * HTTP 500 with a descriptive reason ({@code prefix + exception message}).
 */
final class ControllerErrors {

    private ControllerErrors() {
    }

    /**
     * Runs {@code action}. A {@link ResponseStatusException} (e.g. a 404) propagates
     * unchanged; any other exception becomes a 500.
     */
    static <T> T withErrorPrefix(String prefix, Supplier<T> action) {
        try {
            return action.get();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw internalServerError(prefix, e);
        }
    }

    /**
     * Runs {@code action}, turning every exception into a 500, including a
     * {@link ResponseStatusException} raised inside it.
     */
    static <T> T withErrorPrefixWrappingAll(String prefix, Supplier<T> action) {
        try {
            return action.get();
        } catch (Exception e) {
            throw internalServerError(prefix, e);
        }
    }

    private static ResponseStatusException internalServerError(String prefix, Exception e) {
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, prefix + e.getMessage());
    }
}
