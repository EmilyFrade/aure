package com.aure.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

	@NotBlank(message = "E-mail é obrigatório")
	@Email(message = "E-mail inválido")
	@Size(max = 255)
	private String email;

	@NotBlank(message = "Senha é obrigatória")
	@Size(max = 100)
	private String password;

}
