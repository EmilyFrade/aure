package com.aure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record AuthResponseDto(UUID token, @JsonProperty("professional_id") Long professionalId) {
}
