package com.aure.security;

import java.util.UUID;

public record AuthenticatedProfessional(Long id, Long professionalId, String email, UUID sessionToken) {
}
