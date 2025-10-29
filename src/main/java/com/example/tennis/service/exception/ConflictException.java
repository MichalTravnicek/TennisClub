package com.example.tennis.service.exception;

import jakarta.validation.constraints.NotNull;

public class ConflictException extends RuntimeException {
    public ConflictException(@NotNull String s) {
        super(s);
    }
}
