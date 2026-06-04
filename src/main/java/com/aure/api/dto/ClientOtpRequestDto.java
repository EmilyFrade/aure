package com.aure.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ClientOtpRequestDto(@NotBlank String phone) {
}
