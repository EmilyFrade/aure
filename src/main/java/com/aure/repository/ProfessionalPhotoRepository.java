package com.aure.repository;

import com.aure.domain.ProfessionalPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfessionalPhotoRepository extends JpaRepository<ProfessionalPhoto, Long> {

	Optional<ProfessionalPhoto> findByProfessionalId(Long professionalId);
}
