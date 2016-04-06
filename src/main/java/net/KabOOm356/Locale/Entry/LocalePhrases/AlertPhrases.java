package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

public abstract class AlertPhrases
{
	/**
	 * %r from your claimed report %i has logged in!
	 */
	public static LocalePhrase alertClaimedPlayerLogin = new LocalePhrase(
			"alertClaimedPlayerLogin",
			"%r from your claimed report %i has logged in!");
	
	/**
	 * %r from the unclaimed report(s) %i has logged in!
	 */
	public static LocalePhrase alertUnclaimedPlayerLogin = new LocalePhrase(
			"alertUnclaimedPlayerLogin",
			"%r from the unclaimed report(s) %i has logged in!");

	/**
	 * %r from report(s) %i has logged in!
	 */
	public static LocalePhrase alertConsoleReportedPlayerLogin = new LocalePhrase(
			"alertConsoleReportedPlayerLogin",
			"%r from report(s) %i has logged in!"
	);
}
