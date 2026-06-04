package com.aure.service;

import com.aure.api.dto.ServiceRequestDto;
import com.aure.api.dto.ServiceResponseDto;
import com.aure.domain.Professional;
import com.aure.domain.Service;
import com.aure.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ServiceCatalogService {

	private final ServiceRepository serviceRepository;
	private final ProfessionalService professionalService;

	@Transactional(readOnly = true)
	public List<ServiceResponseDto> findAll(Long professionalId) {
		professionalService.getProfessional(professionalId);
		return serviceRepository.findByProfessionalId(professionalId).stream().map(ServiceResponseDto::from).toList();
	}

	@Transactional(readOnly = true)
	public ServiceResponseDto findById(Long professionalId, Long serviceId) {
		return ServiceResponseDto.from(getService(professionalId, serviceId));
	}

	@Transactional
	public ServiceResponseDto create(Long professionalId, ServiceRequestDto request) {
		professionalService.requireOwnership(professionalId);
		Professional professional = professionalService.getProfessional(professionalId);
		Service service = mapToEntity(new Service(), professional, request);
		return ServiceResponseDto.from(serviceRepository.save(service));
	}

	@Transactional
	public ServiceResponseDto update(Long professionalId, Long serviceId, ServiceRequestDto request) {
		professionalService.requireOwnership(professionalId);
		Service service = getService(professionalId, serviceId);
		mapToEntity(service, service.getProfessional(), request);
		return ServiceResponseDto.from(serviceRepository.save(service));
	}

	@Transactional
	public void delete(Long professionalId, Long serviceId) {
		professionalService.requireOwnership(professionalId);
		getService(professionalId, serviceId);
		serviceRepository.deleteById(serviceId);
	}

	private Service mapToEntity(Service service, Professional professional, ServiceRequestDto request) {
		service.setProfessional(professional);
		service.setName(request.getName());
		service.setDescription(request.getDescription());
		service.setDurationMinutes(request.getDurationMinutes());
		service.setPrice(request.getPrice());
		if (request.getActive() != null) {
			service.setActive(request.getActive());
		} else if (service.getId() == null) {
			service.setActive(true);
		}
		return service;
	}

	private Service getService(Long professionalId, Long serviceId) {
		return serviceRepository.findByIdAndProfessionalId(serviceId, professionalId)
			.orElseThrow(() -> new ResponseStatusException(
				HttpStatus.NOT_FOUND,
				"Serviço não encontrado com id %d".formatted(serviceId)
			));
	}

}
