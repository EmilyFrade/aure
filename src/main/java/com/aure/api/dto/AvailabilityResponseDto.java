package com.aure.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponseDto {

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;

	@JsonFormat(pattern = "HH:mm")
	private List<LocalTime> slots;
}
