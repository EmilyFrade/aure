package com.aure.api.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final String APPOINTMENT_OVERLAP_CONSTRAINT = "appointment_no_overlap";

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setTitle("Erro de validação");
		problem.setProperty("errors", errors);
		return problem;
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ProblemDetail handleResponseStatus(ResponseStatusException ex) {
        return ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getReason());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
		String cause = ex.getMostSpecificCause().getMessage();
		if (cause == null || !cause.contains(APPOINTMENT_OVERLAP_CONSTRAINT)) throw ex;
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Horário indisponível");
	}
}
