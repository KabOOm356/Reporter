package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

/**
 * Class containing static {@link LocalePhrase}s that are used by 
 * {@link net.KabOOm356.Command.Commands.RequestCommand}.
 */
public abstract class RequestPhrases
{
	/**
	 * %p was found in the following reports: %i
	 */
	public static final LocalePhrase reqFI = new LocalePhrase(
			"reqFI",
			"%p was found in the following reports: %i");
	
	/**
	 * %p was not found in any reports.
	 */
	public static final LocalePhrase reqNF = new LocalePhrase(
			"reqNF",
			"%p was not found in any reports.");
	
	/**
	 * /report request Player Name
	 */
	public static final LocalePhrase requestHelp = new LocalePhrase(
			"requestHelp",
			"/report request <Player Name>");
	
	/**
	 * Prints a list of indexes where the specified player was reported.
	 */
	public static final LocalePhrase requestHelpDetails = new LocalePhrase(
			"requestHelpDetails",
			"Prints a list of indexes where the specified player was reported.");
	
	/**
	 * There are %n report(s) against player(s): %p!
	 */
	public static final LocalePhrase numberOfReportsAgainst = new LocalePhrase(
			"numberOfReportsAgainst",
			"There are %n report(s) against player(s): %p!");
	
	/**
	 * Displays the player or players with the most reports against them.
	 */
	public static final LocalePhrase requestMostHelpDetails = new LocalePhrase(
			"requestMostHelpDetails",
			"Displays the player or players with the most reports against them.");
}
