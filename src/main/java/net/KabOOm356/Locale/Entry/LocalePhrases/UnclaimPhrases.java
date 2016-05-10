package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

/**
 * Class containing static {@link LocalePhrase}s that are used by
 * {@link net.KabOOm356.Command.Commands.UnclaimCommand}.
 */
public abstract class UnclaimPhrases {
	/**
	 * The report at index %i is already claimed by %c, whose priority clearance is equal to or above yours!
	 */
	public static final LocalePhrase reportAlreadyClaimed = new LocalePhrase(
			"reportAlreadyClaimed",
			"The report at index %i is already claimed by %c, whose priority clearance is equal to or above yours!");

	/**
	 * The report at index %i is not claimed yet!
	 */
	public static final LocalePhrase reportIsNotClaimed = new LocalePhrase(
			"reportIsNotClaimed",
			"The report at index %i is not claimed yet!");

	/**
	 * You have successfully unclaimed the report at index %i!
	 */
	public static final LocalePhrase reportUnclaimSuccess = new LocalePhrase(
			"reportUnclaimSuccess",
			"You have successfully unclaimed the report at index %i!");

	/**
	 * /report unclaim Index/last
	 */
	public static final LocalePhrase unclaimHelp = new LocalePhrase(
			"unclaimHelp",
			"/report unclaim <Index/last>");

	/**
	 * Opposite of claiming a report, states you would like to step down from being in charge of dealing with this report.
	 */
	public static final LocalePhrase unclaimHelpDetails = new LocalePhrase(
			"unclaimHelpDetails",
			"Opposite of claiming a report, states you would like to step down from being in charge of dealing with this report.");
}
