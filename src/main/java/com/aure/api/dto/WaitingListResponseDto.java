package com.aure.api.dto;

import com.aure.domain.DayPeriod;
import com.aure.domain.WaitingList;
import com.aure.domain.WaitingListStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingListResponseDto(
		Long id,
		@JsonProperty("professional_id") Long professionalId,
		@JsonProperty("professional_name") String professionalName,
		@JsonProperty("professional_photo_url") String professionalPhotoUrl,
		@JsonProperty("brand_name") String brandName,
		@JsonProperty("brand_slug") String brandSlug,
		@JsonProperty("client_id") Long clientId,
		@JsonProperty("client_name") String clientName,
		@JsonProperty("client_phone") String clientPhone,
		@JsonProperty("service_id") Long serviceId,
		@JsonProperty("service_name") String serviceName,
		@JsonFormat(pattern = "yyyy-MM-dd") @JsonProperty("preferred_date_start") LocalDate preferredDateStart,
		@JsonFormat(pattern = "yyyy-MM-dd") @JsonProperty("preferred_date_end") LocalDate preferredDateEnd,
		@JsonProperty("preferred_period") DayPeriod preferredPeriod,
		int position,
		WaitingListStatus status,
		@JsonFormat(pattern = "yyyy-MM-dd") @JsonProperty("offered_date") LocalDate offeredDate,
		@JsonFormat(pattern = "HH:mm") @JsonProperty("offered_time") LocalTime offeredTime,
		@JsonProperty("created_at") Instant createdAt
) {
	public static WaitingListResponseDto from(WaitingList w, int position) {
		return new WaitingListResponseDto(
				w.getId(),
				w.getProfessional().getId(),
				w.getProfessional().getName(),
				w.getProfessional().getPhotoUrl(),
				w.getProfessional().getBrand().getName(),
				w.getProfessional().getBrand().getSlug(),
				w.getClient().getId(),
				w.getClient().getName(),
				w.getClient().getPhone(),
				w.getService().getId(),
				w.getService().getName(),
				w.getPreferredDateStart(),
				w.getPreferredDateEnd(),
				w.getPreferredPeriod(),
				position,
				w.getStatus(),
				w.getOfferedDate(),
				w.getOfferedTime(),
				w.getCreatedAt()
		);
	}
}
