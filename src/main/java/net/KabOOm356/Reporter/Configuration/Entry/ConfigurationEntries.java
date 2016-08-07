package net.KabOOm356.Reporter.Configuration.Entry;

import net.KabOOm356.Configuration.Entry;

public final class ConfigurationEntries {
	public static final Entry<Boolean> limitReports = new Entry<Boolean>(
			"general.reporting.limitNumberOfReports",
			true);

	public static final Entry<Boolean> limitReportsAgainstPlayers = new Entry<Boolean>(
			"general.reporting.limitReportsAgainstPlayers",
			false);

	public static final Entry<Integer> reportLimit = new Entry<Integer>(
			"general.reporting.limitNumber",
			5);

	public static final Entry<Integer> reportLimitAgainstPlayers = new Entry<Integer>(
			"general.reporting.limitNumberAgainstPlayers",
			2);

	public static final Entry<Integer> limitTime = new Entry<Integer>(
			"general.reporting.limitTime",
			600);

	public static final Entry<Boolean> alertConsoleWhenLimitReached = new Entry<Boolean>(
			"general.reporting.alerts.toConsole.limitReached",
			true);

	public static final Entry<Boolean> alertConsoleWhenLimitAgainstPlayerReached = new Entry<Boolean>(
			"general.reporting.alerts.toConsole.limitAgainstPlayerReached",
			true);

	public static final Entry<Boolean> alertConsoleWhenAllowedToReportAgain = new Entry<Boolean>(
			"general.reporting.alerts.toConsole.allowedToReportAgain",
			true);

	public static final Entry<Boolean> alertConsoleWhenAllowedToReportPlayerAgain = new Entry<Boolean>(
			"general.reporting.alerts.toConsole.allowedToReportPlayerAgain",
			true);

	public static final Entry<Boolean> alertPlayerWhenAllowedToReportAgain = new Entry<Boolean>(
			"general.reporting.alerts.toPlayer.allowedToReportAgain",
			true);

	public static final Entry<Boolean> alertPlayerWhenAllowedToReportPlayerAgain = new Entry<Boolean>(
			"general.reporting.alerts.toPlayer.allowedToReportPlayerAgain",
			true);


	private ConfigurationEntries() {
	}
}
