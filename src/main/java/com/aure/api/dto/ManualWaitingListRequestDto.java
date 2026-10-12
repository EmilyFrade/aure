package com.aure.api.dto;

import com.aure.domain.DayPeriod;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ManualWaitingListRequestDto(
		@NotNull @JsonProperty("service_id") Long serviceId,
		@NotBlank @JsonProperty("client_phone") String clientPhone,
		@JsonProperty("client_name") String clientName,
		@NotNull @JsonFormat(pattern = "yyyy-MM-dd") @JsonProperty("preferred_date_start") LocalDate preferredDateStart,
		@NotNull @JsonFormat(pattern = "yyyy-MM-dd") @JsonProperty("preferred_date_end") LocalDate preferredDateEnd,
		@JsonProperty("preferred_period") DayPeriod preferredPeriod
) {
}
