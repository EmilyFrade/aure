package com.aure.api.dto;

import com.aure.domain.Appointment;
import com.aure.domain.AppointmentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponseDto(
		Long id,
		@JsonProperty("professional_id") Long professionalId,
		@JsonProperty("professional_name") String professionalName,
		@JsonProperty("service_id") Long serviceId,
		@JsonProperty("service_name") String serviceName,
		@JsonFormat(pattern = "yyyy-MM-dd") @JsonProperty("scheduled_date") LocalDate scheduledDate,
		@JsonFormat(pattern = "HH:mm") @JsonProperty("scheduled_time") LocalTime scheduledTime,
		@JsonProperty("duration_minutes") int durationMinutes,
		BigDecimal price,
		AppointmentStatus status,
		String notes
) {
	public static AppointmentResponseDto from(Appointment a) {
		return new AppointmentResponseDto(
				a.getId(),
				a.getProfessional().getId(),
				a.getProfessional().getName(),
				a.getService().getId(),
				a.getService().getName(),
				a.getScheduledDate(),
				a.getScheduledTime(),
				a.getDurationMinutes(),
				a.getPrice(),
				a.getStatus(),
				a.getNotes()
		);
	}
}
