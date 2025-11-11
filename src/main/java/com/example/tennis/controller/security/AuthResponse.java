package com.example.tennis.controller.security;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AuthResponse {

    private String accessToken;

    private String refreshToken;
}