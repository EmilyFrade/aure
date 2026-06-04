package com.aure.repository;

import com.aure.domain.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {

	List<Service> findByProfessionalId(Long professionalId);

	Optional<Service> findByIdAndProfessionalId(Long id, Long professionalId);
}
