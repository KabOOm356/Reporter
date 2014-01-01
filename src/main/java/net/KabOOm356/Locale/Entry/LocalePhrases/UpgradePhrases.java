package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

/**
 * Class containing static {@link LocalePhrase}s that are used by 
 * {@link net.KabOOm356.Command.Commands.UpgradeCommand}.
 */
public abstract class UpgradePhrases
{
	/**
	 * The report at index %i is already at the highest priority!
	 */
	public static final LocalePhrase reportIsAtHighestPriority = new LocalePhrase(
			"reportIsAtHighestPriority",
			"The report at index %i is already at the highest priority!");
	
	/**
	 * /report upgrade Index/last
	 */
	public static final LocalePhrase upgradeHelp = new LocalePhrase(
			"upgradeHelp",
			"/report upgrade <Index/last>");
	
	/**
	 * Moves the specified report up to the next highest priority.
	 */
	public static final LocalePhrase upgradeHelpDetails = new LocalePhrase(
			"upgradeHelpDetails",
			"Moves the specified report up to the next highest priority.");
}
