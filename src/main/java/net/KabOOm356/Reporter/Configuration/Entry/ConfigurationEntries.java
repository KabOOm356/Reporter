package net.KabOOm356.Reporter.Configuration.Entry;

import net.KabOOm356.Configuration.Entry;

public final class ConfigurationEntries {
	public static final Entry<Boolean> limitReports = new Entry<>(
			"general.reporting.limitNumberOfReports",
			true);

	public static final Entry<Boolean> limitReportsAgainstPlayers = new Entry<>(
			"general.reporting.limitReportsAgainstPlayers",
			false);

	public static final Entry<Integer> reportLimit = new Entry<>(
			"general.reporting.limitNumber",
			5);

	public static final Entry<Integer> reportLimitAgainstPlayers = new Entry<>(
			"general.reporting.limitNumberAgainstPlayers",
			2);

	public static final Entry<Integer> limitTime = new Entry<>(
			"general.reporting.limitTime",
			600);

	public static final Entry<Boolean> alertConsoleWhenLimitReached = new Entry<>(
			"general.reporting.alerts.toConsole.limitReached",
			true);

	public static final Entry<Boolean> alertConsoleWhenLimitAgainstPlayerReached = new Entry<>(
			"general.reporting.alerts.toConsole.limitAgainstPlayerReached",
			true);

	public static final Entry<Boolean> alertConsoleWhenAllowedToReportAgain = new Entry<>(
			"general.reporting.alerts.toConsole.allowedToReportAgain",
			true);

	public static final Entry<Boolean> alertConsoleWhenAllowedToReportPlayerAgain = new Entry<>(
			"general.reporting.alerts.toConsole.allowedToReportPlayerAgain",
			true);

	public static final Entry<Boolean> alertPlayerWhenAllowedToReportAgain = new Entry<>(
			"general.reporting.alerts.toPlayer.allowedToReportAgain",
			true);

	public static final Entry<Boolean> alertPlayerWhenAllowedToReportPlayerAgain = new Entry<>(
			"general.reporting.alerts.toPlayer.allowedToReportPlayerAgain",
			true);


	private ConfigurationEntries() {
	}
}
