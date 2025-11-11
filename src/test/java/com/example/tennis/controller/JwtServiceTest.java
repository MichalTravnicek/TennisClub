package com.example.tennis.controller;

import com.example.tennis.security.AuthenticationConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@EnableWebSecurity
@SpringBootTest(classes = {JwtService.class, AuthenticationConfig.class})
class JwtServiceTest {

    String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwicm9sZSI6IlJPTEVfVVNFUiIsImlhdCI6MT" +
            "c2MjMyNzMxMSwiZXhwIjoxNzYyMzMwOTExfQ.ysuhk8lUazZ_5fKrZdrrpdAXBeW294yM1w36-QVpB4E";

    String validToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwicm9sZSI6IlJPTEVfVVNFUiIsImlhdCI6MTc2M" +
            "jYzMjQ2OSwiZXhwIjo1MzYyNjMyNDY5fQ.eSLS9MZqbXgw5mtgwdi6JZB8qI1YrFjtgl9bPS74KqI";

    String invalidToken = "eyJhbGciOiJIUzI1NiJ9.ysuhk8lUazZ_5fKrZdrrpdAXBeW294yM1w36-QVpB4E.c2MjMyNzMxMSwiZX";

    @Autowired
    JwtService jwtService;

    @Autowired
    UserDetailsService userDetailsService;

    @Test
    public void testJwtTokenExtraction(){
        var userName = jwtService.extractUsername(validToken);
        Assertions.assertThat(userName).isEqualTo("user");
    }

    @Test
    @DirtiesContext
    public void testMalformedSecret(){
        ReflectionTestUtils.setField(jwtService,"secret","XXXXX");
        assertThrows(IllegalStateException.class, ()->jwtService.extractUsername(validToken));
    }

    @Test
    public void testGenerateToken(){
        var userDetails = this.userDetailsService.loadUserByUsername("user");
        var token = jwtService.generateToken(userDetails);
        System.err.println(token);
        Assertions.assertThat(token).isNotNull();
        Assertions.assertThat(token).hasSizeGreaterThan(100);
    }

    @Test
    public void testValidToken(){
        var userDetails = this.userDetailsService.loadUserByUsername("user");
        var isValid = jwtService.isTokenValid(validToken, userDetails);
        Assertions.assertThat(isValid).isTrue();
        var isValid2 = jwtService.isTokenValid(invalidToken, userDetails);
        Assertions.assertThat(isValid2).isFalse();
        var isValid3 = jwtService.isTokenValid(expiredToken, userDetails);
        Assertions.assertThat(isValid3).isFalse();
    }
}
