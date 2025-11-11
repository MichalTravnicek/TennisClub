package com.example.tennis.controller;

import com.example.tennis.service.exception.BadArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Base64;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService; // You need a mock for your JwtService

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).alwaysDo(print())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String createBasicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encodedCredentials;
    }

    @Test
    public void testLogin_Success() throws Exception {
        String username = "testuser";
        String password = "password";
        String authHeader = createBasicAuthHeader(username, password);
        String mockJwtToken = "mock.jwt.token";
        UserDetails mockUserDetails = new User(username, password, Collections.emptyList());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(username, password));
        when(userDetailsService.loadUserByUsername(username)).thenReturn(mockUserDetails);
        when(jwtService.generateToken(mockUserDetails)).thenReturn(mockJwtToken);

        mockMvc.perform(post(AuthController.BASE_URL + "/login")
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer " + mockJwtToken))
                .andExpect(jsonPath("$.accessToken").value(mockJwtToken));
    }

    @Test
    public void testLogin_Fail() throws Exception {
        String username = "testuser2";
        String password = "password2";
        String authHeader = createBasicAuthHeader(username, password);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post(AuthController.BASE_URL + "/login")
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Bearer"})
    public void testLogin_NoAuthHeader(String header) throws Exception {
        // Perform request without Authorization header
        mockMvc.perform(post(AuthController.BASE_URL + "/login")
                        .header(HttpHeaders.AUTHORIZATION, header)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(result.getResolvedException() instanceof BadArgumentException))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertEquals("No Authorization supplied", result.getResolvedException().getMessage()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Basic ", "Basic invalid"})
    public void testLogin_InvalidBasicHeader(String header) throws Exception {
        // Perform request with invalid Basic header
        mockMvc.perform(post(AuthController.BASE_URL + "/login")
                        .header(HttpHeaders.AUTHORIZATION, header)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(result.getResolvedException() instanceof BadArgumentException))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertEquals("No valid Basic authorization supplied", result.getResolvedException().getMessage()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Basic XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "Basic !23456747894?"})
    public void testLogin_InvalidBase64Header(String header) throws Exception {
        // Perform request with invalid Basic header
        mockMvc.perform(post(AuthController.BASE_URL + "/login")
                        .header(HttpHeaders.AUTHORIZATION, header)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(result.getResolvedException() instanceof BadArgumentException))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertEquals("Invalid Basic authorization format", result.getResolvedException().getMessage()));
    }

}
