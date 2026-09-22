package com.aure.api.controller;

import com.aure.api.dto.SearchResponseDto;
import com.aure.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

	private final SearchService searchService;

	@GetMapping
	public SearchResponseDto search(
			@RequestParam(required = false) String city,
			@RequestParam(required = false) String state,
			@RequestParam(required = false) String service,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		return searchService.search(city, state, service, page, size);
	}
}
