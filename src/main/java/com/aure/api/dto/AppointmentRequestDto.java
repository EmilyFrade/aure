package com.aure.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentRequestDto(
		@NotNull @JsonProperty("professional_id") Long professionalId,
		@NotNull @JsonProperty("service_id") Long serviceId,
		@NotNull @JsonFormat(pattern = "yyyy-MM-dd") @JsonProperty("scheduled_date") LocalDate scheduledDate,
		@NotNull @JsonFormat(pattern = "HH:mm") @JsonProperty("scheduled_time") LocalTime scheduledTime,
		String notes
) {
}
