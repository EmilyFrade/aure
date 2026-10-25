package com.aure.service;

import com.aure.domain.Professional;
import com.aure.repository.ProfessionalRepository;
import com.aure.security.AuthenticatedProfessional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfessionalService {

	private final ProfessionalRepository professionalRepository;

	public Professional getProfessional(Long id) {
		return professionalRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(
				HttpStatus.NOT_FOUND,
				"Profissional não encontrado com id %d".formatted(id)
			));
	}

	public void requireOwnership(Long professionalId) {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedProfessional principal)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autenticado");
		}

		if (!principal.professionalId().equals(professionalId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso não autorizado");
		}
	}

}
