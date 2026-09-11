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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {

	@JsonProperty("brand_name")
	@NotBlank(message = "Nome do negócio é obrigatório")
	@Size(max = 255)
	private String brandName;

	@JsonProperty("professional_name")
	@NotBlank(message = "Seu nome é obrigatório")
	@Size(max = 255)
	private String professionalName;

	@NotBlank(message = "E-mail é obrigatório")
	@Email(message = "E-mail inválido")
	@Size(max = 255)
	private String email;

	@NotBlank(message = "Senha é obrigatória")
	@Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
	private String password;

	@Pattern(
			regexp = "^((\\+55\\s?)?(\\(?\\d{2}\\)?\\s?)?9?\\d{4}-?\\d{4})?$",
			message = "Telefone inválido, use DDD + número"
	)
	@Size(max = 20)
	private String phone;

	@NotBlank(message = "Cidade é obrigatória")
	@Size(max = 100)
	private String city;

	@NotBlank(message = "Estado é obrigatório")
	@Pattern(regexp = "^[A-Z]{2}$", message = "Estado deve ser a sigla UF com 2 letras maiúsculas")
	private String state;

}
