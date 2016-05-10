package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

/**
 * Class containing static {@link LocalePhrase}s that are used by
 * {@link net.KabOOm356.Command.Commands.AssignCommand}.
 */
public abstract class AssignPhrases {
	/**
	 * Player %p has a priority of %m
	 */
	public static final LocalePhrase playerPriority = new LocalePhrase(
			"playerPriority",
			"Player %p has a priority of %m");

	/**
	 * Player %p was successfully assigned to report %i!
	 */
	public static final LocalePhrase assignSuccessful = new LocalePhrase(
			"assignSuccessful",
			"Player %p was successfully assigned to report %i!");

	/**
	 * %p has assigned you to report %i!
	 */
	public static final LocalePhrase assignedToReport = new LocalePhrase(
			"assignedToReport",
			"%p has assigned you to report %i!");

	/**
	 * The player to assign must be online!
	 */
	public static final LocalePhrase assignedPlayerMustBeOnline = new LocalePhrase(
			"assignedPlayerMustBeOnline",
			"The player to assign must be online!");

	/**
	 * Use /report claim to assign yourself to a report.
	 */
	public static final LocalePhrase useClaimToAssignSelf = new LocalePhrase(
			"useClaimToAssignSelf",
			"Use /report claim to assign yourself to a report.");

	/**
	 * You cannot assign reports to players with the same or higher priority than you!
	 */
	public static final LocalePhrase cannotAssignHigherPriority = new LocalePhrase(
			"cannotAssignHigherPriority",
			"You cannot assign reports to players with the same or higher priority than you!");

	/**
	 * /report assign Index/last Player
	 */
	public static final LocalePhrase assignHelp = new LocalePhrase(
			"assignHelp",
			"/report assign <Index/last> <Player>");

	/**
	 * Assigns a player to a report, basically makes the assigned player claim the specified report.
	 */
	public static final LocalePhrase assignHelpDetails = new LocalePhrase(
			"assignHelpDetails",
			"Assigns a player to a report, basically makes the assigned player claim the specified report.");
}
