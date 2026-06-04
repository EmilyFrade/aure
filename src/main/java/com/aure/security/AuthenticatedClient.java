package com.aure.security;

import java.util.UUID;

public record AuthenticatedClient(Long clientId, UUID sessionToken) {
}
