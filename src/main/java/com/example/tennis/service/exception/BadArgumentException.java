package com.example.tennis.service.exception;

public class BadArgumentException extends RuntimeException{
    public BadArgumentException(String s) {
        super (s);
    }
}
