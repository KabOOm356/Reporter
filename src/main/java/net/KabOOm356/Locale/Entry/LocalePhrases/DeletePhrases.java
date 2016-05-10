package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

/**
 * Class containing static {@link LocalePhrase}s that are used by
 * {@link net.KabOOm356.Command.Commands.DeleteCommand}.
 */
public abstract class DeletePhrases {
	/**
	 * Report index %i deleted successfully.
	 */
	public static final LocalePhrase deleteReport = new LocalePhrase(
			"deleteReport",
			"Report index %i deleted successfully.");

	/**
	 * All reports have been removed!
	 */
	public static final LocalePhrase deleteAll = new LocalePhrase(
			"deleteAll",
			"All reports have been removed!");

	/**
	 * All completed reports deleted successfully!
	 */
	public static final LocalePhrase deleteComplete = new LocalePhrase(
			"deleteComplete",
			"All completed reports deleted successfully!");

	/**
	 * All incomplete reports deleted successfully!
	 */
	public static final LocalePhrase deleteIncomplete = new LocalePhrase(
			"deleteIncomplete",
			"All incomplete reports deleted successfully!");

	/**
	 * All reports where %p was reported have been deleted!
	 */
	public static final LocalePhrase deletePlayerReported = new LocalePhrase(
			"deletePlayerReported",
			"All reports where %p was reported have been deleted!");

	/**
	 * All reports that %p submitted have been deleted!
	 */
	public static final LocalePhrase deletePlayerSender = new LocalePhrase(
			"deletePlayerSender",
			"All reports that %p submitted have been deleted!");

	/**
	 * SQL tables reformatted successfully.
	 */
	public static final LocalePhrase SQLTablesReformat = new LocalePhrase(
			"SQLTablesReformat",
			"SQL tables reformatted successfully.");

	/**
	 * /report delete/remove Index/last
	 */
	public static final LocalePhrase deleteHelp = new LocalePhrase(
			"deleteHelp",
			"/report delete/remove <Index/last>");

	/**
	 * Deletes a specific report.
	 */
	public static final LocalePhrase deleteHelpDetails = new LocalePhrase(
			"deleteHelpDetails",
			"Deletes a specific report.");

	/**
	 * Deletes all reports.
	 */
	public static final LocalePhrase deleteHelpAllDetails = new LocalePhrase(
			"deleteHelpAllDetails",
			"Deletes all reports.");

	/**
	 * Deletes all completed reports.
	 */
	public static final LocalePhrase deleteHelpCompletedDetails = new LocalePhrase(
			"deleteHelpCompletedDetails",
			"Deletes all completed reports.");

	/**
	 * Deletes all incomplete reports.
	 */
	public static final LocalePhrase deleteHelpIncompleteDetails = new LocalePhrase(
			"deleteHelpIncompleteDetails",
			"Deletes all incomplete reports.");

	/**
	 * /report delete/remove <Player Name> [reported/sender]
	 */
	public final static LocalePhrase deleteHelpPlayer = new LocalePhrase(
			"deleteHelpPlayer",
			"/report delete/remove <Player Name> [reported/sender]");

	/**
	 * Deletes all reports where the given player is either reported or the submitter of the report.
	 */
	public final static LocalePhrase deleteHelpPlayerDetails = new LocalePhrase(
			"deleteHelpPlayerDetails",
			"Deletes all reports where the given player is either reported or the submitter of the report.");

	/**
	 * A total of %r report(s) have been deleted!
	 */
	public static final LocalePhrase deletedReportsTotal = new LocalePhrase(
			"deletedTotalReports",
			"A total of %r report(s) have been deleted!");
}
