package com.aure.api.controller;

import com.aure.api.dto.ClientAuthResponseDto;
import com.aure.api.dto.ClientNameUpdateDto;
import com.aure.api.dto.ClientOtpRequestDto;
import com.aure.api.dto.ClientOtpVerifyDto;
import com.aure.api.dto.ClientProfileDto;
import com.aure.service.ClientAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/client")
@RequiredArgsConstructor
public class ClientAuthController {

	private final ClientAuthService clientAuthService;

	@PostMapping("/request-otp")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void requestOtp(@Valid @RequestBody ClientOtpRequestDto request) {
		clientAuthService.requestOtp(request);
	}

	@PostMapping("/verify")
	public ClientAuthResponseDto verify(@Valid @RequestBody ClientOtpVerifyDto request) {
		return clientAuthService.verifyOtp(request);
	}

	@GetMapping("/me")
	public ClientProfileDto me() {
		return clientAuthService.me();
	}

	@PatchMapping("/me")
	public ClientProfileDto updateName(@Valid @RequestBody ClientNameUpdateDto request) {
		return clientAuthService.updateName(request);
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout() {
		clientAuthService.logout();
	}
}
