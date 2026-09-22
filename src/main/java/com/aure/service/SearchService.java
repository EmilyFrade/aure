package com.aure.service;

import com.aure.api.dto.SearchResponseDto;
import com.aure.api.dto.SearchResponseDto.MatchedServiceDto;
import com.aure.api.dto.SearchResponseDto.SearchResultDto;
import com.aure.domain.Brand;
import com.aure.repository.BrandRepository;
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

	private final BrandRepository brandRepository;
	private final ServiceRepository serviceRepository;

	@Transactional(readOnly = true)
	public SearchResponseDto search(String city, String state, String service, int page, int size) {
		String cityFilter = SearchText.normalize(city);
		String stateFilter = SearchText.normalize(state);
		String serviceFilter = SearchText.normalize(service);

		Page<Brand> brands = brandRepository.search(
				cityFilter,
				stateFilter,
				serviceFilter,
				PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE))
		);

		Map<Long, List<MatchedServiceDto>> servicesByBrand = servicesByBrand(brands.getContent(), serviceFilter);

		List<SearchResultDto> content = brands.getContent().stream()
				.map(brand -> new SearchResultDto(
						brand.getId(),
						brand.getName(),
						brand.getSlug(),
						brand.getDescription(),
						brand.getCity(),
						brand.getState(),
						brand.getLogoUrl(),
						servicesByBrand.getOrDefault(brand.getId(), List.of())
				))
				.toList();

		return new SearchResponseDto(
				content,
				brands.getNumber(),
				brands.getSize(),
				brands.getTotalElements(),
				brands.getTotalPages()
		);
	}

	private Map<Long, List<MatchedServiceDto>> servicesByBrand(List<Brand> brands, String serviceFilter) {
		if (brands.isEmpty()) {
			return Map.of();
		}

		List<Long> brandIds = brands.stream().map(Brand::getId).toList();

		return serviceRepository.findActiveByBrandIds(brandIds, serviceFilter).stream()
				.collect(Collectors.groupingBy(
						s -> s.getProfessional().getBrand().getId(),
						Collectors.mapping(
								s -> new MatchedServiceDto(s.getId(), s.getName(), s.getDurationMinutes(), s.getPrice()),
								Collectors.toList()
						)
				));
	}
}
