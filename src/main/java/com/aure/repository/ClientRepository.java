package com.aure.repository;

import com.aure.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

	Optional<Client> findByPhone(String phone);

	@Query(value = "SELECT * FROM client WHERE regexp_replace(phone, '[^0-9]', '', 'g') IN (:phones) ORDER BY id LIMIT 1", nativeQuery = true)
	Optional<Client> findByPhoneDigits(@Param("phones") Collection<String> phones);
}
