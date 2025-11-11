package com.example.tennis.controller.security;

import com.example.tennis.controller.JwtService;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    private final UserDetailsService userDetailsService;

    private final JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ") && authorizationHeader.length() > 100) {

            String token = authorizationHeader.substring(7);
            try {
                var name = jwtService.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(name);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                return true; // Continue processing the request
            } catch (Exception e) {
                // Token validation failed
                log.debug("Token validation failed:" + e);
                throw new UnauthorizedException("Invalid Token");
            }
        } else {
            // No token or invalid format
            log.debug("Invalid token format:" + authorizationHeader);
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
    }
}
