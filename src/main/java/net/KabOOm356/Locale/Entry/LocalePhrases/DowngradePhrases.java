package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

/**
 * Class containing static {@link LocalePhrase}s that are used by 
 * {@link net.KabOOm356.Command.Commands.DowngradeCommand}.
 */
public abstract class DowngradePhrases
{
	/**
	 * The report at index %i is already at the lowest priority!
	 */
	public static final LocalePhrase reportIsAtLowestPriority = new LocalePhrase(
			"reportIsAtLowestPriority",
			"The report at index %i is already at the lowest priority!");
	
	/**
	 * /report downgrade Index/last
	 */
	public static final LocalePhrase downgradeHelp = new LocalePhrase(
			"downgradeHelp",
			"/report downgrade <Index/last>");
	
	/**
	 * Moves the specified report down to the next lowest priority.
	 */
	public static final LocalePhrase downgradeHelpDetails = new LocalePhrase(
			"downgradeHelpDetails",
			"Moves the specified report down to the next lowest priority.");
}
