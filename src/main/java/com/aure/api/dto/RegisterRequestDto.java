package com.aure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {

	@JsonProperty("professional_id")
	@NotNull(message = "Profissional é obrigatório")
	private Long professionalId;

	@NotBlank(message = "E-mail é obrigatório")
	@Email(message = "E-mail inválido")
	@Size(max = 255)
	private String email;

	@NotBlank(message = "Senha é obrigatória")
	@Size(max = 100, message = "Senha deve ter no máximo 100 caracteres")
	private String password;

}
