package com.aure.api.controller;

import com.aure.api.dto.AvailabilityResponseDto;
import com.aure.api.dto.BlockRequestDto;
import com.aure.api.dto.BlockResponseDto;
import com.aure.api.dto.ScheduleRequestDto;
import com.aure.api.dto.ScheduleResponseDto;
import com.aure.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/professionals/{professionalId}")
@RequiredArgsConstructor
public class AvailabilityController {

	private final AvailabilityService availabilityService;

	@GetMapping("/schedules")
	public List<ScheduleResponseDto> findSchedules(@PathVariable Long professionalId) {
		return availabilityService.findSchedules(professionalId);
	}

	@PostMapping("/schedules")
	@ResponseStatus(HttpStatus.CREATED)
	public ScheduleResponseDto createSchedule(
		@PathVariable Long professionalId,
		@Valid @RequestBody ScheduleRequestDto request
	) {
		return availabilityService.createSchedule(professionalId, request);
	}

	@PutMapping("/schedules/{scheduleId}")
	public ScheduleResponseDto updateSchedule(
		@PathVariable Long professionalId,
		@PathVariable Long scheduleId,
		@Valid @RequestBody ScheduleRequestDto request
	) {
		return availabilityService.updateSchedule(professionalId, scheduleId, request);
	}

	@DeleteMapping("/schedules/{scheduleId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteSchedule(@PathVariable Long professionalId, @PathVariable Long scheduleId) {
		availabilityService.deleteSchedule(professionalId, scheduleId);
	}

	@GetMapping("/blocks")
	public List<BlockResponseDto> findBlocks(@PathVariable Long professionalId) {
		return availabilityService.findBlocks(professionalId);
	}

	@PostMapping("/blocks")
	@ResponseStatus(HttpStatus.CREATED)
	public BlockResponseDto createBlock(
		@PathVariable Long professionalId,
		@Valid @RequestBody BlockRequestDto request
	) {
		return availabilityService.createBlock(professionalId, request);
	}

	@DeleteMapping("/blocks/{blockId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteBlock(@PathVariable Long professionalId, @PathVariable Long blockId) {
		availabilityService.deleteBlock(professionalId, blockId);
	}

	@GetMapping("/availability")
	public AvailabilityResponseDto getAvailableSlots(
		@PathVariable Long professionalId,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
		@RequestParam("service_id") Long serviceId
	) {
		return availabilityService.getAvailableSlots(professionalId, date, serviceId);
	}
}
