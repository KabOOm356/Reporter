package net.KabOOm356.File.AbstractFiles;

/**
 * A {@link NetworkFile} that is a readable index for other files or entries, in either XML or RSS format.
 */
public class UpdateSite extends NetworkFile {
	/**
	 * The type of this UpdateSite.
	 */
	private final Type type;

	/**
	 * UpdateSite Constructor.
	 *
	 * @param url  The URL location of this file on the network.
	 * @param type The {@link Type} of this UpdateSite.
	 */
	public UpdateSite(String url, Type type) {
		super(url);

		this.type = type;
	}

	/**
	 * @return The type of this UpdateSite.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String string = super.toString();
		string += "\nType: " + type;

		return string;
	}

	/**
	 * Defines the type of UpdateSite.
	 */
	public enum Type {
		/**
		 * RSS UpdateSite Type.
		 */
		RSS("RSS"),
		/**
		 * XML UpdateSite Type.
		 */
		XML("XML");

		private final String name;

		Type(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}
}
