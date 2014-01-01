package net.KabOOm356.Locale.Entry;

/**
 * An entry in the locale.
 */
public class LocaleEntry
{
	/**
	 * The prefix of the path.
	 */
	public static final String prefix = "locale.";
	
	/**
	 * The complete path to the entry.
	 */
	private String path;
	
	/**
	 * The default value to use if the entry is not present.
	 */
	private String def;
	
	/**
	 * Constructor.
	 * 
	 * @param path The path to the entry.
	 * @param def The default for the entry.
	 */
	public LocaleEntry(String path, String def)
	{
		this.path = prefix + path;
		this.def = def;
	}
	
	/**
	 * Returns the path to this entry.
	 * 
	 * @return The path to this entry.
	 */
	public String getPath()
	{
		return path;
	}
	
	/**
	 * Returns the default value for this entry.
	 * 
	 * @return The default value for this entry.
	 */
	public String getDefault()
	{
		return def;
	}
	
	@Override
	public String toString()
	{
		String str;
		
		str = "Path: " + getPath();
		str += "\nDefault: " + getDefault();
		
		return str;
	}
}
