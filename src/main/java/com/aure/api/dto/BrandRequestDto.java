package com.aure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class BrandRequestDto {

	@NotBlank(message = "Nome é obrigatório")
	@Size(max = 255)
	private String name;

	@NotBlank(message = "Slug é obrigatório")
	@Size(max = 100)
	@Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Slug deve conter apenas letras minúsculas, números e hífens")
	private String slug;

	@Size(max = 500)
	private String description;

	@JsonProperty("logo_url")
	@URL(message = "URL do logo inválida")
	@Size(max = 500)
	private String logoUrl;

	@Size(max = 100)
	private String city;

	@Pattern(regexp = "^[A-Z]{2}$", message = "Estado deve ser a sigla UF com 2 letras maiúsculas")
	private String state;

	@Pattern(
			regexp = "^((\\+55\\s?)?(\\(?\\d{2}\\)?\\s?)?9?\\d{4}-?\\d{4})?$",
			message = "Telefone inválido, use DDD + número"
	)
	@Size(max = 20)
	private String phone;

	@Email(message = "E-mail inválido")
	@Size(max = 255)
	private String email;

	@URL(message = "URL do site inválida")
	@Size(max = 500)
	private String website;

}
