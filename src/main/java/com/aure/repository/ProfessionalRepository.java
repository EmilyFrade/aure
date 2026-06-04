package com.aure.repository;

import com.aure.domain.Professional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfessionalRepository extends JpaRepository<Professional, Long> {

	List<Professional> findByBrandIdAndActiveTrue(Long brandId);
}
