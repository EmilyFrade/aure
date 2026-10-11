package com.aure.api.controller;

import com.aure.api.dto.WaitingListResponseDto;
import com.aure.service.WaitingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/professionals/{professionalId}/waiting-list")
@RequiredArgsConstructor
public class WaitingListController {

	private final WaitingListService waitingListService;

	@GetMapping
	public List<WaitingListResponseDto> findAll(@PathVariable Long professionalId) {
		return waitingListService.listForProfessional(professionalId);
	}

	@DeleteMapping("/{waitingListId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remove(@PathVariable Long professionalId, @PathVariable Long waitingListId) {
		waitingListService.remove(professionalId, waitingListId);
	}
}
