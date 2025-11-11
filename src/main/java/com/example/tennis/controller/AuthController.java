package com.example.tennis.controller;

import com.example.tennis.controller.security.AuthResponse;
import com.example.tennis.service.exception.BadArgumentException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


@Slf4j
@RestController
@RequiredArgsConstructor
@SecurityScheme(
        name = "Basic Auth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "Basic",
        scheme = "basic"
)
@RequestMapping(AuthController.BASE_URL)
public class AuthController {

    public static final String BASE_URL = "/api/v1/auth";

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Operation(tags = "0 - Auth",
            summary = "Authentication",
            description = "Authenticate user"
    )
    @PostMapping("/login")
    @SecurityRequirement(name = "Basic Auth")
    public ResponseEntity<AuthResponse> login(
            @Parameter(examples = {
                    @ExampleObject(name = "User", description = "Login user - in Swagger UI click Authorize and fill Basic auth",
                            value = "Basic dXNlcjpwYXNzd29yZA==")
            }
            )
            @Nullable @RequestHeader("Authorization") String authHeader
    ) {
        System.err.println("POST request handled successfully with data: " + authHeader);

        if (!StringUtils.hasLength(authHeader) || !authHeader.startsWith("Basic ")){
            throw new BadArgumentException("No Authorization supplied");
        }
        String base64Credentials = authHeader.substring("Basic ".length()).trim();
        if (!StringUtils.hasLength(base64Credentials) || base64Credentials.length() < 10){
            throw new BadArgumentException("No valid Basic authorization supplied");
        }

        var credentials = getCredentials(base64Credentials);
        authenticationManager.authenticate(credentials);

        var userDetails = userDetailsService.loadUserByUsername(credentials.getPrincipal().toString());
        String jwtToken = jwtService.generateToken(userDetails);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .body(AuthResponse.builder().accessToken(jwtToken).build());
    }

    private UsernamePasswordAuthenticationToken getCredentials(String base64Credentials){
        String credentials;
        try {
            credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            String[] parts = credentials.split(":", 2);
            String username = parts[0];
            String password = parts[1];
            return new UsernamePasswordAuthenticationToken(username, password);
        } catch (Exception e) {
            log.debug("Invalid Basic authorization format:" + e);
            throw new BadArgumentException("Invalid Basic authorization format");
        }

    }
}
