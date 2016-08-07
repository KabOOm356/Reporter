package net.KabOOm356.Configuration;

public class ConstantEntry<T> extends Entry<T> {
	private static final String path = "";

	/**
	 * Constructor.
	 *
	 * @param constant The string that will be returned every time this entry is looked up.
	 */
	public ConstantEntry(final T constant) {
		super(path, constant);
	}
}
