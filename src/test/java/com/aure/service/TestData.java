package com.aure.service;

import com.aure.domain.Appointment;
import com.aure.domain.AppointmentStatus;
import com.aure.domain.Brand;
import com.aure.domain.Client;
import com.aure.domain.Professional;
import com.aure.domain.Service;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class TestData {

	static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
	static final LocalDate TODAY = LocalDate.of(2026, 11, 10);
	static final LocalDate TOMORROW = TODAY.plusDays(1);
	static final LocalDateTime NOW = TODAY.atTime(10, 0);
	static final Clock CLOCK = Clock.fixed(at(NOW), ZONE);

	static final Long PROFESSIONAL_ID = 1L;
	static final Long CLIENT_ID = 10L;
	static final Long SERVICE_ID = 1L;
	static final String CLIENT_PHONE = "31999998888";

	private TestData() {
	}

	static Instant at(LocalDateTime dateTime) {
		return dateTime.atZone(ZONE).toInstant();
	}

	static Professional professional() {
		Brand brand = Brand.builder().id(1L).name("Studio Beatriz").slug("studio-beatriz").build();
		return Professional.builder().id(PROFESSIONAL_ID).brand(brand).name("Beatriz").active(true).build();
	}

	static Client client() {
		return client(CLIENT_ID);
	}

	static Client client(Long id) {
		return Client.builder().id(id).name("Ana").phone(CLIENT_PHONE).build();
	}

	static Service service(int durationMinutes) {
		return service(SERVICE_ID, durationMinutes);
	}

	static Service service(Long id, int durationMinutes) {
		return Service.builder()
				.id(id)
				.professional(professional())
				.name("Serviço " + id)
				.durationMinutes(durationMinutes)
				.price(new BigDecimal("50.00"))
				.active(true)
				.build();
	}

	static Appointment appointment(LocalDate date, LocalTime time, int durationMinutes, AppointmentStatus status) {
		return Appointment.builder()
				.id(5L)
				.professional(professional())
				.client(client())
				.service(service(durationMinutes))
				.scheduledDate(date)
				.scheduledTime(time)
				.durationMinutes(durationMinutes)
				.price(new BigDecimal("50.00"))
				.status(status)
				.createdAt(at(TODAY.minusDays(5).atTime(9, 0)))
				.build();
	}

	static void assertStatus(ThrowingCallable call, HttpStatus status) {
		assertThatThrownBy(call)
				.isInstanceOf(ResponseStatusException.class)
				.extracting(e -> ((ResponseStatusException) e).getStatusCode())
				.isEqualTo(status);
	}
}
