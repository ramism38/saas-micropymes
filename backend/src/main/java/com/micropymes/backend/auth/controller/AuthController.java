package com.micropymes.backend.auth.controller;

import com.micropymes.backend.auth.dto.CurrentUserResponse;
import com.micropymes.backend.auth.dto.LoginRequest;
import com.micropymes.backend.auth.dto.RegisterRequest;
import com.micropymes.backend.auth.security.AuthenticatedUser;
import com.micropymes.backend.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import com.micropymes.backend.auth.dto.LoginRequest;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication management")
public class AuthController {

        private final AuthService authService;
        private final SecurityContextRepository securityContextRepository;

        public AuthController(
                        AuthService authService,
                        SecurityContextRepository securityContextRepository) {
                this.authService = authService;
                this.securityContextRepository = securityContextRepository;
        }

        @PostMapping("/register")
        public ResponseEntity<CurrentUserResponse> register(
                        @Valid @RequestBody RegisterRequest request,
                        HttpServletRequest httpRequest,
                        HttpServletResponse httpResponse) {

                CurrentUserResponse user = authService.register(request);

                createSession(
                                user,
                                httpRequest,
                                httpResponse);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(user);
        }

        @PostMapping("/login")
        public CurrentUserResponse login(
                        @Valid @RequestBody LoginRequest request,
                        HttpServletRequest httpRequest,
                        HttpServletResponse httpResponse) {

                CurrentUserResponse user = authService.login(request);

                createSession(
                                user,
                                httpRequest,
                                httpResponse);

                return user;
        }

        @PostMapping("/logout")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void logout(
                        HttpServletRequest request) {

                if (request.getSession(false) != null) {
                        request.getSession(false).invalidate();
                }

                SecurityContextHolder.clearContext();
        }

        @GetMapping("/me")
        public CurrentUserResponse me(
                        @AuthenticationPrincipal AuthenticatedUser principal) {
                return authService.getCurrentUser(
                                principal.userId());
        }

        private void createSession(
                        CurrentUserResponse user,
                        HttpServletRequest httpRequest,
                        HttpServletResponse httpResponse) {

                AuthenticatedUser principal = new AuthenticatedUser(
                                user.id(),
                                user.email());

                Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                                principal,
                                null,
                                List.of(
                                                new SimpleGrantedAuthority(
                                                                "ROLE_USER")));

                SecurityContext context = SecurityContextHolder.createEmptyContext();

                context.setAuthentication(authentication);

                SecurityContextHolder.setContext(context);

                securityContextRepository.saveContext(
                                context,
                                httpRequest,
                                httpResponse);
        }
}