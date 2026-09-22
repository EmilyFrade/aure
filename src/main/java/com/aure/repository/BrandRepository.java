package com.aure.repository;

import com.aure.domain.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

	Optional<Brand> findBySlug(String slug);

	default Page<Brand> search(String city, String state, String service, Pageable pageable) {
		return search(city, state, service, SearchText.ACCENTED, SearchText.PLAIN, pageable);
	}

	@Query(value = """
			SELECT DISTINCT b FROM Brand b
			JOIN Professional p ON p.brand = b AND p.active = true
			JOIN Service s ON s.professional = p AND s.active = true
			WHERE LOWER(FUNCTION('translate', b.city, :accented, :plain)) LIKE CONCAT('%', :city, '%')
			AND (:state = '' OR LOWER(b.state) = :state)
			AND LOWER(FUNCTION('translate', s.name, :accented, :plain)) LIKE CONCAT('%', :service, '%')
			ORDER BY b.name
			""",
			countQuery = """
			SELECT COUNT(DISTINCT b) FROM Brand b
			JOIN Professional p ON p.brand = b AND p.active = true
			JOIN Service s ON s.professional = p AND s.active = true
			WHERE LOWER(FUNCTION('translate', b.city, :accented, :plain)) LIKE CONCAT('%', :city, '%')
			AND (:state = '' OR LOWER(b.state) = :state)
			AND LOWER(FUNCTION('translate', s.name, :accented, :plain)) LIKE CONCAT('%', :service, '%')
			""")
	Page<Brand> search(
			@Param("city") String city,
			@Param("state") String state,
			@Param("service") String service,
			@Param("accented") String accented,
			@Param("plain") String plain,
			Pageable pageable
	);
}
