package com.micropymes.backend.common.security;

import tools.jackson.databind.json.JsonMapper;
import com.micropymes.backend.common.error.ApiError;
import com.micropymes.backend.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class RestAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final JsonMapper jsonMapper;

    public RestAuthenticationEntryPoint(
            JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        ApiError error = new ApiError(
                "about:blank",
                errorCode.getCode(),
                errorCode.getTitle(),
                errorCode.getStatus().value(),
                "Authentication is required to access this resource",
                request.getRequestURI(),
                Instant.now(),
                List.of());

        response.setStatus(
                errorCode.getStatus().value());

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE);

        jsonMapper.writeValue(
                response.getOutputStream(),
                error);
    }
}