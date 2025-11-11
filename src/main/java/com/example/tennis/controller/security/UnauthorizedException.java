package com.example.tennis.controller.security;

public class UnauthorizedException extends RuntimeException{

    public UnauthorizedException(String s) {
        super(s);
    }
}
