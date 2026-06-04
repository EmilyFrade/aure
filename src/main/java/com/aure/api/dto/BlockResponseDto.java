package com.aure.api.dto;

import com.aure.domain.ProfessionalBlock;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BlockResponseDto {

	private Long id;

	@JsonProperty("professional_id")
	private Long professionalId;

	@JsonProperty("start_datetime")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime startDatetime;

	@JsonProperty("end_datetime")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime endDatetime;

	private String reason;

	public static BlockResponseDto from(ProfessionalBlock block) {
		return new BlockResponseDto(
			block.getId(),
			block.getProfessional().getId(),
			block.getStartDatetime(),
			block.getEndDatetime(),
			block.getReason()
		);
	}
}
