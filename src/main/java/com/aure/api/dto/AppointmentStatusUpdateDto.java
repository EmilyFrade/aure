package com.aure.api.dto;

import com.aure.domain.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record AppointmentStatusUpdateDto(
		@NotNull AppointmentStatus status
) {
}
