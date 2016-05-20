package net.KabOOm356.Locale.Entry;

/**
 * An entry in the locale.
 */
public class LocaleEntry extends Entry {
	/**
	 * The prefix of the path.
	 */
	public static final String prefix = "locale.";

	/**
	 * Constructor.
	 *
	 * @param path The path to the entry.
	 * @param def  The default for the entry.
	 */
	public LocaleEntry(final String path, final String def) {
		super(prefix + path, def);
	}
}
