package net.KabOOm356.Locale.Entry;

/**
 * A {@link LocaleEntry} that has a 'phrases' prefix.
 */
public class LocalePhrase extends LocaleEntry
{
	/**
	 * The prefix of the path.
	 */
	public static final String prefix = "phrases.";
	
	/**
	 * Constructor.
	 * 
	 * @param path The path to the entry.
	 * @param def The default for the entry.
	 */
	public LocalePhrase(String path, String def)
	{
		super(prefix + path, def);
	}
}
