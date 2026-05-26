package com.aure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalRequestDto {

	@JsonProperty("brand_id")
	@NotNull(message = "Marca é obrigatória")
	private Long brandId;

	@NotBlank(message = "Nome é obrigatório")
	@Size(max = 255)
	private String name;

	@Size(max = 1000)
	private String bio;

	@Pattern(
			regexp = "^((\\+55\\s?)?(\\(?\\d{2}\\)?\\s?)?9?\\d{4}-?\\d{4})?$",
			message = "Telefone inválido, use DDD + número"
	)
	@Size(max = 20)
	private String phone;

	@Email(message = "E-mail inválido")
	@Size(max = 255)
	private String email;

	@JsonProperty("photo_url")
	@URL(message = "URL da foto inválida")
	@Size(max = 500)
	private String photoUrl;

	@JsonProperty("is_active")
	private Boolean active;

}
