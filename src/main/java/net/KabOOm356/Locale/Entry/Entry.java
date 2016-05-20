package net.KabOOm356.Locale.Entry;

public abstract class Entry {
	/**
	 * The complete path to the entry.
	 */
	private final String path;

	/**
	 * The default value to use if the entry is not present.
	 */
	private final String def;

	public Entry(final String path, final String def) {
		this.path = path;
		this.def = def;
	}

	/**
	 * Returns the path to this entry.
	 *
	 * @return The path to this entry.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Returns the default value for this entry.
	 *
	 * @return The default value for this entry.
	 */
	public String getDefault() {
		return def;
	}

	@Override
	public String toString() {
		return "Path: " + getPath() + "\nDefault: " + getDefault();
	}
}
