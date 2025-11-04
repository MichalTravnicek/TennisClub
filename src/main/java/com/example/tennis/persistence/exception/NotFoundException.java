package com.example.tennis.persistence.exception;

import lombok.Getter;

public class NotFoundException extends RuntimeException{

    @Getter
    private String internalMessage;

    public NotFoundException(String s) {
        super(s);
    }

    public NotFoundException(String s, String detail) {
        super(s);
        internalMessage = detail;
    }
}
