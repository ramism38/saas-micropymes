package com.micropymes.backend.auth.security;

import java.io.Serializable;
import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        String email
) implements Serializable {
}