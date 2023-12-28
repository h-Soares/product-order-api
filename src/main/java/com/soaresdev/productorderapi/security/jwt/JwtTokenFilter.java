package com.soaresdev.productorderapi.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.soaresdev.productorderapi.exceptions.StandardError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = jwtTokenProvider.fixRequestTokenFormat(request);
            if(token != null && jwtTokenProvider.isAccessToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                if(authentication != null)
                    SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        }catch(Exception e) {
            handleException(request, response, e);
        }
    }
    private void handleException(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        // Create an object containing error information
        StandardError standardError = new StandardError();
        standardError.setTimestamp(Instant.now());
        standardError.setStatus(HttpStatus.UNAUTHORIZED.value());
        standardError.setError(e.getClass().getSimpleName());
        standardError.setPath(request.getRequestURI());
        standardError.setMessage(e.getMessage());
        // Convert object to JSON and write in response
        String errorJson = objectMapper.writeValueAsString(standardError);
        OutputStream out = response.getOutputStream();
        out.write(errorJson.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }
}