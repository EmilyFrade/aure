package com.aure.api.dto;

import com.aure.domain.ProfessionalSchedule;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponseDto {

	private Long id;

	@JsonProperty("professional_id")
	private Long professionalId;

	@JsonProperty("day_of_week")
	private DayOfWeek dayOfWeek;

	@JsonProperty("start_time")
	@JsonFormat(pattern = "HH:mm")
	private LocalTime startTime;

	@JsonProperty("end_time")
	@JsonFormat(pattern = "HH:mm")
	private LocalTime endTime;

	public static ScheduleResponseDto from(ProfessionalSchedule schedule) {
		return new ScheduleResponseDto(
			schedule.getId(),
			schedule.getProfessional().getId(),
			schedule.getDayOfWeek(),
			schedule.getStartTime(),
			schedule.getEndTime()
		);
	}
}
