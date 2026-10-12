package com.aure.api.dto;

import com.aure.domain.DayPeriod;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record WaitingListRequestDto(
		@NotNull @JsonProperty("professional_id") Long professionalId,
		@NotNull @JsonProperty("service_id") Long serviceId,
		@NotNull @JsonFormat(pattern = "yyyy-MM-dd") @JsonProperty("preferred_date_start") LocalDate preferredDateStart,
		@NotNull @JsonFormat(pattern = "yyyy-MM-dd") @JsonProperty("preferred_date_end") LocalDate preferredDateEnd,
		@JsonProperty("preferred_period") DayPeriod preferredPeriod
) {
}
