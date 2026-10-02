package com.micropymes.backend.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.micropymes.backend.common.error.ApiError;
import com.micropymes.backend.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class RestAccessDeniedHandler
        implements AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    public RestAccessDeniedHandler(
            JsonMapper jsonMapper
    ) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        ErrorCode errorCode =
                ErrorCode.ACCESS_DENIED;

        ApiError error =
                new ApiError(
                        "about:blank",
                        errorCode.getCode(),
                        errorCode.getTitle(),
                        errorCode.getStatus().value(),
                        "You do not have permission to access this resource",
                        request.getRequestURI(),
                        Instant.now(),
                        List.of()
                );

        response.setStatus(
                errorCode.getStatus().value()
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        jsonMapper.writeValue(
                response.getOutputStream(),
                error
        );
    }
}