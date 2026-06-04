package com.aure.service;

import com.aure.api.dto.AvailabilityResponseDto;
import com.aure.api.dto.BlockRequestDto;
import com.aure.api.dto.BlockResponseDto;
import com.aure.api.dto.ScheduleRequestDto;
import com.aure.api.dto.ScheduleResponseDto;
import com.aure.domain.Professional;
import com.aure.domain.ProfessionalBlock;
import com.aure.domain.ProfessionalSchedule;
import com.aure.domain.Service;
import com.aure.repository.ProfessionalBlockRepository;
import com.aure.repository.ProfessionalScheduleRepository;
import com.aure.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AvailabilityService {

	private final ProfessionalScheduleRepository scheduleRepository;
	private final ProfessionalBlockRepository blockRepository;
	private final ProfessionalService professionalService;
	private final ServiceRepository serviceRepository;

	@Transactional(readOnly = true)
	public List<ScheduleResponseDto> findSchedules(Long professionalId) {
		professionalService.getProfessional(professionalId);
		return scheduleRepository.findByProfessionalId(professionalId).stream().map(ScheduleResponseDto::from).toList();
	}

	@Transactional
	public ScheduleResponseDto createSchedule(Long professionalId, ScheduleRequestDto request) {
		professionalService.requireOwnership(professionalId);
		validateScheduleTimes(request);
		Professional professional = professionalService.getProfessional(professionalId);
		ProfessionalSchedule schedule = mapToSchedule(new ProfessionalSchedule(), professional, request);
		return ScheduleResponseDto.from(scheduleRepository.save(schedule));
	}

	@Transactional
	public ScheduleResponseDto updateSchedule(Long professionalId, Long scheduleId, ScheduleRequestDto request) {
		professionalService.requireOwnership(professionalId);
		validateScheduleTimes(request);
		ProfessionalSchedule schedule = getSchedule(professionalId, scheduleId);
		mapToSchedule(schedule, schedule.getProfessional(), request);
		return ScheduleResponseDto.from(scheduleRepository.save(schedule));
	}

	@Transactional
	public void deleteSchedule(Long professionalId, Long scheduleId) {
		professionalService.requireOwnership(professionalId);
		getSchedule(professionalId, scheduleId);
		scheduleRepository.deleteById(scheduleId);
	}

	@Transactional(readOnly = true)
	public List<BlockResponseDto> findBlocks(Long professionalId) {
		professionalService.getProfessional(professionalId);
		return blockRepository.findByProfessionalId(professionalId).stream().map(BlockResponseDto::from).toList();
	}

	@Transactional
	public BlockResponseDto createBlock(Long professionalId, BlockRequestDto request) {
		professionalService.requireOwnership(professionalId);
		if (!request.getStartDatetime().isBefore(request.getEndDatetime())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O início do bloqueio deve ser anterior ao fim");
		}

		Professional professional = professionalService.getProfessional(professionalId);
		ProfessionalBlock block = ProfessionalBlock.builder()
			.professional(professional)
			.startDatetime(request.getStartDatetime())
			.endDatetime(request.getEndDatetime())
			.reason(request.getReason())
			.build();

		return BlockResponseDto.from(blockRepository.save(block));
	}

	@Transactional
	public void deleteBlock(Long professionalId, Long blockId) {
		professionalService.requireOwnership(professionalId);
		getBlock(professionalId, blockId);
		blockRepository.deleteById(blockId);
	}

	@Transactional(readOnly = true)
	public AvailabilityResponseDto getAvailableSlots(Long professionalId, LocalDate date, Long serviceId) {
		professionalService.getProfessional(professionalId);

		Service service = serviceRepository.findByIdAndProfessionalId(serviceId, professionalId)
			.orElseThrow(() -> new ResponseStatusException(
				HttpStatus.NOT_FOUND,
				"Serviço não encontrado com id %d".formatted(serviceId)
			));

		List<ProfessionalSchedule> segments = scheduleRepository.findByProfessionalIdAndDayOfWeek(professionalId, date.getDayOfWeek());
		if (segments.isEmpty()) {
			return new AvailabilityResponseDto(date, List.of());
		}

		List<LocalTime> allSlots = segments.stream()
			.flatMap(seg -> generateSlots(seg.getStartTime(), seg.getEndTime(), service.getDurationMinutes()).stream())
			.sorted()
			.toList();

		LocalDateTime dayStart = date.atStartOfDay();
		LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
		List<ProfessionalBlock> blocks = blockRepository.findByProfessionalIdAndStartDatetimeLessThanAndEndDatetimeGreaterThan(professionalId, dayEnd, dayStart);

		List<LocalTime> freeSlots = allSlots.stream()
			.filter(slot -> {
				LocalDateTime slotStart = date.atTime(slot);
				LocalDateTime slotEnd = slotStart.plusMinutes(service.getDurationMinutes());
				return blocks.stream().noneMatch(block ->
					slotStart.isBefore(block.getEndDatetime()) && slotEnd.isAfter(block.getStartDatetime())
				);
			})
			.toList();

		return new AvailabilityResponseDto(date, freeSlots);
	}

	private List<LocalTime> generateSlots(LocalTime start, LocalTime end, int durationMinutes) {
		List<LocalTime> slots = new ArrayList<>();
		LocalTime current = start;
		while (!current.plusMinutes(durationMinutes).isAfter(end)) {
			slots.add(current);
			current = current.plusMinutes(durationMinutes);
		}
		return slots;
	}

	private ProfessionalSchedule mapToSchedule(ProfessionalSchedule schedule, Professional professional, ScheduleRequestDto request) {
		schedule.setProfessional(professional);
		schedule.setDayOfWeek(request.getDayOfWeek());
		schedule.setStartTime(request.getStartTime());
		schedule.setEndTime(request.getEndTime());
		return schedule;
	}

	private void validateScheduleTimes(ScheduleRequestDto request) {
		if (!request.getStartTime().isBefore(request.getEndTime())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O horário de início deve ser anterior ao de término");
		}
	}

	private ProfessionalSchedule getSchedule(Long professionalId, Long scheduleId) {
		return scheduleRepository.findByIdAndProfessionalId(scheduleId, professionalId)
			.orElseThrow(() -> new ResponseStatusException(
				HttpStatus.NOT_FOUND,
				"Horário não encontrado com id %d".formatted(scheduleId)
			));
	}

	private ProfessionalBlock getBlock(Long professionalId, Long blockId) {
		return blockRepository.findByIdAndProfessionalId(blockId, professionalId)
			.orElseThrow(() -> new ResponseStatusException(
				HttpStatus.NOT_FOUND,
				"Bloqueio não encontrado com id %d".formatted(blockId)
			));
	}
}
