package com.aure.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ManualAppointmentRequestDto(
		@NotNull @JsonProperty("service_id") Long serviceId,
		@NotNull @JsonFormat(pattern = "yyyy-MM-dd") @JsonProperty("scheduled_date") LocalDate scheduledDate,
		@NotNull @JsonFormat(pattern = "HH:mm") @JsonProperty("scheduled_time") LocalTime scheduledTime,
		@NotBlank @JsonProperty("client_phone") String clientPhone,
		@JsonProperty("client_name") String clientName,
		String notes
) {
}
