package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

/**
 * Class containing static {@link LocalePhrase}s that are used by
 * {@link net.KabOOm356.Command.Commands.ClaimCommand}.
 */
public abstract class ClaimPhrases {
	/**
	 * You have successfully claimed report %i!
	 */
	public static final LocalePhrase reportClaimSuccess = new LocalePhrase(
			"reportClaimSuccess",
			"You have successfully claimed report %i!");

	/**
	 * /report claim Index/last
	 */
	public static final LocalePhrase claimHelp = new LocalePhrase(
			"claimHelp",
			"/report claim <Index/last>");

	/**
	 * Claims a report, states you would like to be in charge of dealing with this report.
	 */
	public static final LocalePhrase claimHelpDetails = new LocalePhrase(
			"claimHelpDetails",
			"Claims a report, states you would like to be in charge of dealing with this report.");

	/**
	 * The report at index %i is already claimed by %c, whose priority clearance is equal to or above yours!
	 */
	public static final LocalePhrase reportAlreadyClaimed = new LocalePhrase(
			"reportAlreadyClaimed",
			"The report at index %i is already claimed by %c, whose priority clearance is equal to or above yours!");
}
