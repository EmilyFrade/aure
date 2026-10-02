package com.aure.api.dto;

import com.aure.domain.Client;

public record ClientProfileDto(Long id, String name, String phone) {

	public static ClientProfileDto from(Client client) {
		return new ClientProfileDto(client.getId(), client.getName(), client.getPhone());
	}
}
