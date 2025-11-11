package com.example.tennis.controller;

import com.example.tennis.persistence.exception.NotFoundException;
import com.example.tennis.security.AuthenticationConfig;
import com.example.tennis.security.SecurityConfig;
import com.example.tennis.service.exception.BadArgumentException;
import com.example.tennis.service.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RestController
class DummyController {
    @GetMapping("/test-not-found")
    public void throwNotFound() {
        throw new NotFoundException("Item not found");
    }

    @GetMapping("/test-handler-exception")
    public void throwHandlerException() throws NoSuchMethodException {
        var result = List.of(new ParameterValidationResult(new MethodParameter(
                // DefaultMethodValidationResult.class.forReturnValue - true je VYSTUP / false VSTUP
                // parameterindex -1 nastavi validaci vystup - stav 500 /ostatni hodnoty vstup - stav 400
                String.class.getMethod("valueOf", int.class), 0),
                "The API URL is not a valid URL",
                List.of(new FieldError("Object","foo","Test message"))));

        throw new HandlerMethodValidationException(
                MethodValidationResult.create(
                this, this.getClass().getMethods()[1],
                result)
        );
    }

    @GetMapping("/test-conflict")
    public void throwConflict() {
        throw new ConflictException("Conflict occurred");
    }

    @GetMapping("/test-method-argument-exception")
    public void throwMethodArgumentException() throws MethodArgumentNotValidException, NoSuchMethodException {
        var param = new MethodParameter(String.class.getMethod("valueOf", int.class), 0);
        var result = new BindException(this,"MyObject");
        throw new MethodArgumentNotValidException(param,result);
    }

    @GetMapping("/test-bad-argument")
    public void throwBadArgument() {
        throw new BadArgumentException("Invalid input");
    }

    @GetMapping("/test-unknown-exception")
    public void throwUnknownException() throws Exception {
        throw new Exception("Something unexpected happened");
    }

    @GetMapping("/test-empty-exception")
    public void throwEmptyException() throws Exception {
        throw new Exception();
    }
}

@WebMvcTest(DummyController.class)
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@Import({JwtService.class, SecurityConfig.class, AuthenticationConfig.class})
public class GlobalExceptionHandlerTest {

    String validToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwicm9sZSI6IlJPTEVfVVNFUiIsImlhdCI6MTc2M" +
            "jM2MjczNCwiZXhwIjoxNzY1OTYyNzM0fQ.afeaizQkDms8GKn4CeGXpjNiJHgn29j_qsOrh8r-G0k";

    private MockHttpServletRequestBuilder tokenGet(String uri, String token){
        return get(uri).header("Authorization", "Bearer " + token);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void handleNotFoundException_ShouldReturnStatusNotFoundAndProblemDetail() throws Exception {
        mockMvc.perform(tokenGet("/test-not-found", validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("ReservationsApi-V1"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test
    public void handleHandlerMethodValidationException_ShouldReturnStatusBadRequest() throws Exception {
        mockMvc.perform(tokenGet("/test-handler-exception", validToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("ReservationsApi-V1"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    public void handleMethodArgumentNotValidException_ShouldReturnStatusBadRequest() throws Exception {
        mockMvc.perform(tokenGet("/test-method-argument-exception",validToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("ReservationsApi-V1"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    public void handleConflictException_ShouldReturnStatusConflictAndProblemDetail() throws Exception {
        mockMvc.perform(get("/test-conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("ReservationsApi-V1"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Conflict with existing item"))
                .andExpect(jsonPath("$.exception").value("ConflictException"))
                .andExpect(jsonPath("$.message").value("Conflict occurred"));
    }

    @Test
    public void handleBadArgumentException_ShouldReturnStatusBadRequestAndProblemDetail() throws Exception {
        mockMvc.perform(get("/test-bad-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("ReservationsApi-V1"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Bad request"))
                .andExpect(jsonPath("$.exception").value("BadArgumentException"))
                .andExpect(jsonPath("$.message").value("Invalid input"));
    }

    @Test
    public void handleUnknownException_ShouldReturnStatusInternalServerErrorAndProblemDetail() throws Exception {
        mockMvc.perform(tokenGet("/test-unknown-exception",validToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.type").value("ReservationsApi-V1"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("Internal server error"))
                .andExpect(jsonPath("$.exception").value("Exception"))
                .andExpect(jsonPath("$.message").value("Something unexpected happened"));
    }

    @Test
    public void handleEmptyException_ShouldReturnStatusInternalServerErrorAndProblemDetail() throws Exception {
        mockMvc.perform(tokenGet("/test-unknown-exception",validToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.type").value("ReservationsApi-V1"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("Internal server error"));
    }

    @Test
    public void testEmptyBody(){
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        handler.handleExceptionInternal(null,null,null, HttpStatusCode.valueOf(400),null);
    }
}
