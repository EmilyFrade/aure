package com.aure.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ClientOtpVerifyDto(@NotBlank String phone, @NotBlank String code) {
}
