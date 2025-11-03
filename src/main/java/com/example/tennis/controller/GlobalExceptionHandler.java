package com.example.tennis.controller;

import com.example.tennis.persistence.exception.NotFoundException;
import com.example.tennis.service.exception.BadArgumentException;
import com.example.tennis.service.exception.ConflictException;
import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(Exception ex, WebRequest request) {
        final String parameters = request.getParameterMap().entrySet().stream().map(x -> x.getKey() + "=" +
                Arrays.toString(x.getValue())).collect(Collectors.joining(";"));
        var exception = new NoResourceFoundException(((ServletWebRequest) request).getHttpMethod(), parameters);
        return handleNoResourceFoundException(exception, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Object> handleConflictException(Exception ex, WebRequest request) {
        System.err.println(ex.getMessage());
        final ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Conflict with existing item");
        return handleExceptionInternal(ex, detail, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler({BadArgumentException.class, ConstraintViolationException.class})
    public ResponseEntity<Object> handleBadArgumentException(Exception ex, WebRequest request) {
        System.err.println(ex.getMessage());
        final ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Bad request");
        return handleExceptionInternal(ex, detail, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnknownException(Exception ex, WebRequest request) {
        final ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        return handleExceptionInternal(ex, detail, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex, @Nullable Object body,
                                                             @Nonnull HttpHeaders headers, @NonNull HttpStatusCode statusCode,
                                                             @NonNull WebRequest request) {
        if (body == null && ex instanceof org.springframework.web.ErrorResponse errorResponse) {
            body = errorResponse.updateAndGetBody(getMessageSource(), LocaleContextHolder.getLocale());
        }

        if (body instanceof ProblemDetail detail){
            String exceptionMessage = getExceptionMessage(ex);
            if (StringUtils.hasLength(exceptionMessage)) {
                detail.setProperty("exception", ex.getClass().getSimpleName());
                detail.setProperty("message", exceptionMessage);
            }
        }
        return super.handleExceptionInternal(ex,body,headers,statusCode,request);
    }

    public static String getExceptionMessage(Exception ex){
        return switch (ex) {
            case NoResourceFoundException e -> "";
            case HandlerMethodValidationException e -> {
                var errors = e.getParameterValidationResults().stream()
                        .map(error->error.getMethodParameter().getParameterName()+":"+ error.getResolvableErrors().getFirst()
                                .getDefaultMessage()).toList();
                yield errors.toString();
            }
            case MethodArgumentNotValidException e -> {
                var errors = e.getBindingResult().getFieldErrors().stream().map(
                        error -> error.getField() + "=" + error.getRejectedValue() + ":" + error.getDefaultMessage()).toList();
                yield errors.toString();
            }
            case Exception e -> e.getMessage(); // in production we can suppress that (ie. internal exception)
        };
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(@Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        if (body instanceof ProblemDetail detail){
            detail.setType(URI.create("ReservationsApi-V1"));
        }
        return new ResponseEntity(body, headers, statusCode);
    }

}

