package com.aure.domain;

import java.time.LocalTime;

public enum DayPeriod {
	MORNING, AFTERNOON, EVENING;

	public boolean contains(LocalTime time) {
		int hour = time.getHour();
		return switch (this) {
			case MORNING -> hour < 12;
			case AFTERNOON -> hour >= 12 && hour < 18;
			case EVENING -> hour >= 18;
		};
	}
}
