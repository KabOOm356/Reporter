package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

/**
 * Class containing static {@link LocalePhrase}s that are used in multiple locations.
 */
public abstract class GeneralPhrases {
	/**
	 * You do not have the required permissions to perform this command!
	 */
	public static final LocalePhrase failedPermissions = new LocalePhrase(
			"failedPermissions",
			"You do not have the required permissions to perform this command!");

	/**
	 * An error has occurred!
	 */
	public static final LocalePhrase error = new LocalePhrase(
			"error",
			"An error has occurred!");

	/**
	 * That player does not exist!
	 */
	public static final LocalePhrase playerDoesNotExist = new LocalePhrase(
			"playerDoesNotExist",
			"That player does not exist!");

	/**
	 * You have not viewed a report yet, or the report was deleted!
	 */
	public static final LocalePhrase noLastReport = new LocalePhrase(
			"noLastReport",
			"You have not viewed a report yet, or the report was deleted!");

	/**
	 * Try using /respond help
	 */
	public static final LocalePhrase tryRespondHelp = new LocalePhrase(
			"tryRespondHelp",
			"Try using /respond help");

	/**
	 * Try using /report help
	 */
	public static final LocalePhrase tryReportHelp = new LocalePhrase(
			"tryReportHelp",
			"Try using /report help");

	/**
	 * There are no reports!
	 */
	public static final LocalePhrase noReports = new LocalePhrase(
			"noReports",
			"There are no reports!");

	/**
	 * Report index must be an integer!
	 */
	public static final LocalePhrase indexInt = new LocalePhrase(
			"indexInt",
			"Report index must be an integer!");

	/**
	 * Report index outside of range!
	 */
	public static final LocalePhrase indexRange = new LocalePhrase(
			"indexRange",
			"Report index outside of range!");

	/**
	 * The priority level given is not in bounds!
	 */
	public static final LocalePhrase priorityLevelNotInBounds = new LocalePhrase(
			"priorityLevelNotInBounds",
			"The priority level given is not in bounds!");

	/**
	 * Please contact them to alter the report!
	 */
	public static final LocalePhrase contactToAlter = new LocalePhrase(
			"contactToAlter",
			"Please contact them to alter the report!");

	/**
	 * The report at index %i requires a clearance of %m or higher!
	 */
	public static final LocalePhrase reportRequiresClearance = new LocalePhrase(
			"reportRequiresClearance",
			"The report at index %i requires a clearance of %m or higher!");

	/**
	 * Your mod clearance level: %m
	 */
	public static final LocalePhrase displayModLevel = new LocalePhrase(
			"displayModLevel",
			"Your mod clearance level: %m");

	/**
	 * %p mod clearance level: %m
	 */
	public static final LocalePhrase displayOtherModLevel = new LocalePhrase(
			"displayOtherModLevel",
			"%p mod clearance level: %m");
}
