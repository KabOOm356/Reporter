package net.KabOOm356.Locale.Entry;

public class ConstantEntry extends Entry {
	private static final String path = "";

	/**
	 * Constructor.
	 *
	 * @param constant The string that will be returned every time this entry is looked up.
	 */
	public ConstantEntry(final String constant) {
		super(path, constant);
	}
}
