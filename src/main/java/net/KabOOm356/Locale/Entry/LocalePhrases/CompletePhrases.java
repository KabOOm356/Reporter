package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

/**
 * Class containing static {@link LocalePhrase}s that are used by
 * {@link net.KabOOm356.Command.Commands.CompleteCommand}.
 */
public abstract class CompletePhrases {
	/**
	 * Report completion submitted. Thank you.
	 */
	public static final LocalePhrase playerComplete = new LocalePhrase(
			"playerComplete",
			"Report completion submitted. Thank you.");

	/**
	 * You must write a summary to complete a report!
	 */
	public static final LocalePhrase completeNoSummary = new LocalePhrase(
			"completeNoSummary",
			"You must write a summary to complete a report!");

	/**
	 * Report %i has just been completed
	 */
	public static final LocalePhrase broadcastCompleted = new LocalePhrase(
			"broadcastCompleted",
			"Report %i has just been completed");

	/**
	 * Your report at index: %i has been completed
	 */
	public static final LocalePhrase broadcastYourReportCompleted = new LocalePhrase(
			"broadcastYourReportCompleted",
			"Your report at index: %i has been completed");

	/**
	 * Your reports have been completed at indexes: %i
	 */
	public static final LocalePhrase yourReportsCompleted = new LocalePhrase(
			"yourReportsCompleted",
			"Your reports have been completed at indexes: %i");

	/**
	 * /report complete/finish Index/last Report Summary
	 */
	public static final LocalePhrase completeHelp = new LocalePhrase(
			"completeHelp",
			"/report complete/finish <Index/last> <Report Summary>");

	/**
	 * Marks the report at Index as completed and stores a summary of the outcome of the report.
	 */
	public static final LocalePhrase completeHelpDetails = new LocalePhrase(
			"completeHelpDetails",
			"Marks the report at Index as completed and stores a summary of the outcome of the report.");
}
