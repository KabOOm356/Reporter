package net.KabOOm356.Locale.Entry;

import net.KabOOm356.Reporter.Reporter;

/**
 * A {@link LocaleEntry} that has a 'info' prefix.
 */
public class LocaleInfo extends LocaleEntry {
	public static final LocaleInfo language = new LocaleInfo("language", "English");
	public static final LocaleInfo version = new LocaleInfo("version", Reporter.localeVersion);
	public static final LocaleInfo author = new LocaleInfo("author", "KabOOm 356");

	/**
	 * The prefix of the path.
	 */
	public static final String prefix = "info.";

	/**
	 * Constructor.
	 *
	 * @param path The path to the entry.
	 * @param def  The default for the entry.
	 */
	public LocaleInfo(String path, String def) {
		super(prefix + path, def);
	}
}
