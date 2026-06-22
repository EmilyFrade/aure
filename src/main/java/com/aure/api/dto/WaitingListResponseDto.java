package com.aure.api.dto;

import com.aure.domain.WaitingList;
import com.aure.domain.WaitingListStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record WaitingListResponseDto(
		Long id,
		@JsonProperty("professional_id") Long professionalId,
		@JsonProperty("professional_name") String professionalName,
		@JsonProperty("client_id") Long clientId,
		@JsonProperty("client_name") String clientName,
		@JsonProperty("service_id") Long serviceId,
		@JsonProperty("service_name") String serviceName,
		@JsonFormat(pattern = "yyyy-MM-dd") @JsonProperty("preferred_date_start") LocalDate preferredDateStart,
		@JsonFormat(pattern = "yyyy-MM-dd") @JsonProperty("preferred_date_end") LocalDate preferredDateEnd,
		int position,
		WaitingListStatus status
) {
	public static WaitingListResponseDto from(WaitingList w) {
		return new WaitingListResponseDto(
				w.getId(),
				w.getProfessional().getId(),
				w.getProfessional().getName(),
				w.getClient().getId(),
				w.getClient().getName(),
				w.getService().getId(),
				w.getService().getName(),
				w.getPreferredDateStart(),
				w.getPreferredDateEnd(),
				w.getPosition(),
				w.getStatus()
		);
	}
}
