package com.aure.api.controller;

import com.aure.api.dto.MonthlyReportDto;
import com.aure.service.ProfessionalReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/professionals/{professionalId}/report")
@RequiredArgsConstructor
public class ProfessionalReportController {

	private final ProfessionalReportService professionalReportService;

	@GetMapping
	public MonthlyReportDto monthly(
			@PathVariable Long professionalId,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
	) {
		return professionalReportService.monthly(professionalId, month);
	}
}
