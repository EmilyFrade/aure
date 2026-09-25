package com.aure.service;

import com.aure.api.dto.SearchResponseDto;
import com.aure.api.dto.SearchResponseDto.MatchedServiceDto;
import com.aure.api.dto.SearchResponseDto.SearchResultDto;
import com.aure.domain.Brand;
import com.aure.domain.Professional;
import com.aure.repository.ProfessionalRepository;
import com.aure.repository.SearchText;
import com.aure.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SearchService {

	private static final int MAX_PAGE_SIZE = 50;

	private final ProfessionalRepository professionalRepository;
	private final ServiceRepository serviceRepository;

	@Transactional(readOnly = true)
	public SearchResponseDto search(String city, String state, String service, int page, int size) {
		String serviceFilter = SearchText.normalize(service);

		Page<Professional> professionals = professionalRepository.search(
				SearchText.normalize(city),
				SearchText.normalize(state),
				serviceFilter,
				PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE))
		);

		Map<Long, List<MatchedServiceDto>> servicesByProfessional = servicesByProfessional(professionals.getContent(), serviceFilter);

		List<SearchResultDto> content = professionals.getContent().stream()
				.map(professional -> {
					Brand brand = professional.getBrand();
					return new SearchResultDto(
							professional.getId(),
							professional.getName(),
							professional.getBio(),
							professional.getPhotoUrl(),
							brand.getId(),
							brand.getName(),
							brand.getSlug(),
							brand.getCity(),
							brand.getState(),
							servicesByProfessional.getOrDefault(professional.getId(), List.of())
					);
				})
				.toList();

		return new SearchResponseDto(
				content,
				professionals.getNumber(),
				professionals.getSize(),
				professionals.getTotalElements(),
				professionals.getTotalPages()
		);
	}

	private Map<Long, List<MatchedServiceDto>> servicesByProfessional(List<Professional> professionals, String serviceFilter) {
		if (professionals.isEmpty()) {
			return Map.of();
		}

		List<Long> ids = professionals.stream().map(Professional::getId).toList();

		return serviceRepository.findActiveByProfessionalIds(ids, serviceFilter).stream()
				.collect(Collectors.groupingBy(
						s -> s.getProfessional().getId(),
						Collectors.mapping(
								s -> new MatchedServiceDto(s.getId(), s.getName(), s.getDurationMinutes(), s.getPrice()),
								Collectors.toList()
						)
				));
	}
}
