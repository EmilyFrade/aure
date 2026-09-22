package com.aure.repository;

import com.aure.domain.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {

	List<Service> findByProfessionalId(Long professionalId);

	List<Service> findByProfessionalIdAndActiveTrue(Long professionalId);

	Optional<Service> findByIdAndProfessionalId(Long id, Long professionalId);

	default List<Service> findActiveByBrandIds(List<Long> brandIds, String service) {
		return findActiveByBrandIds(brandIds, service, SearchText.ACCENTED, SearchText.PLAIN);
	}

	@Query("""
			SELECT s FROM Service s
			WHERE s.professional.brand.id IN :brandIds
			AND s.active = true
			AND s.professional.active = true
			AND LOWER(FUNCTION('translate', s.name, :accented, :plain)) LIKE CONCAT('%', :service, '%')
			ORDER BY s.price
			""")
	List<Service> findActiveByBrandIds(
			@Param("brandIds") List<Long> brandIds,
			@Param("service") String service,
			@Param("accented") String accented,
			@Param("plain") String plain
	);
}
