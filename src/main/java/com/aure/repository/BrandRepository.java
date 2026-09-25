package com.aure.repository;

import com.aure.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

	Optional<Brand> findBySlug(String slug);
}
