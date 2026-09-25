package com.aure.repository;

import com.aure.domain.Professional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProfessionalRepository extends JpaRepository<Professional, Long> {

	List<Professional> findByBrandIdAndActiveTrue(Long brandId);

	default Page<Professional> search(String city, String state, String service, Pageable pageable) {
		return search(city, state, service, SearchText.ACCENTED, SearchText.PLAIN, pageable);
	}

	@EntityGraph(attributePaths = "brand")
	@Query(value = """
			SELECT DISTINCT p FROM Professional p
			JOIN p.brand b
			JOIN Service s ON s.professional = p AND s.active = true
			WHERE p.active = true
			AND EXISTS (SELECT 1 FROM ProfessionalSchedule sc WHERE sc.professional = p)
			AND LOWER(FUNCTION('translate', b.city, :accented, :plain)) LIKE CONCAT('%', :city, '%')
			AND (:state = '' OR LOWER(b.state) = :state)
			AND LOWER(FUNCTION('translate', s.name, :accented, :plain)) LIKE CONCAT('%', :service, '%')
			ORDER BY p.name
			""",
			countQuery = """
			SELECT COUNT(DISTINCT p) FROM Professional p
			JOIN p.brand b
			JOIN Service s ON s.professional = p AND s.active = true
			WHERE p.active = true
			AND EXISTS (SELECT 1 FROM ProfessionalSchedule sc WHERE sc.professional = p)
			AND LOWER(FUNCTION('translate', b.city, :accented, :plain)) LIKE CONCAT('%', :city, '%')
			AND (:state = '' OR LOWER(b.state) = :state)
			AND LOWER(FUNCTION('translate', s.name, :accented, :plain)) LIKE CONCAT('%', :service, '%')
			""")
	Page<Professional> search(
			@Param("city") String city,
			@Param("state") String state,
			@Param("service") String service,
			@Param("accented") String accented,
			@Param("plain") String plain,
			Pageable pageable
	);
}
