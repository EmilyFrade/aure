package com.aure.api.dto;

import java.util.UUID;

public record ClientAuthResponseDto(UUID token, String name) {
}
