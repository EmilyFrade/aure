package com.aure.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequestDto {

	@NotNull(message = "Dia da semana é obrigatório")
	@JsonProperty("day_of_week")
	private DayOfWeek dayOfWeek;

	@NotNull(message = "Hora de início é obrigatória")
	@JsonProperty("start_time")
	@JsonFormat(pattern = "HH:mm")
	private LocalTime startTime;

	@NotNull(message = "Hora de término é obrigatória")
	@JsonProperty("end_time")
	@JsonFormat(pattern = "HH:mm")
	private LocalTime endTime;
}
