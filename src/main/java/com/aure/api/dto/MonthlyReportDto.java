package com.aure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyReportDto(
		String month,
		@JsonProperty("completed_count") int completedCount,
		BigDecimal revenue,
		@JsonProperty("upcoming_count") int upcomingCount,
		@JsonProperty("expected_revenue") BigDecimal expectedRevenue,
		@JsonProperty("unmarked_count") int unmarkedCount,
		@JsonProperty("no_show_count") int noShowCount,
		@JsonProperty("cancelled_count") int cancelledCount,
		@JsonProperty("attendance_rate") Double attendanceRate,
		@JsonProperty("reminders_sent") int remindersSent,
		@JsonProperty("reminders_confirmed") int remindersConfirmed,
		@JsonProperty("reminders_cancelled") int remindersCancelled,
		@JsonProperty("top_services") List<ServiceSummary> topServices
) {

	public record ServiceSummary(String name, int count, BigDecimal revenue) {
	}
}
