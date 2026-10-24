package com.aure.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "professional_photo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionalPhoto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "professional_id", nullable = false, unique = true)
	private Professional professional;

	@Column(nullable = false)
	private byte[] content;

	@Column(name = "content_type", nullable = false, length = 50)
	private String contentType;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}
}
