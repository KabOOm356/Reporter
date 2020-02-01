package net.KabOOm356.File.AbstractFiles;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A {@link NetworkFile} that has a String GNU standard version associated with it.
 */
public class VersionedNetworkFile extends NetworkFile {
	/**
	 * The version of this {@link NetworkFile}.
	 */
	private String version;

	/**
	 * The versions split into indexes.
	 */
	private List<String> versions;

	/**
	 * The level of this release.
	 */
	private ReleaseLevel releaseLevel;

	/**
	 * The release version of the current level.
	 */
	private int releaseLevelVersion;

	/**
	 * VersionedNetworkFile Constructor.
	 *
	 * @param url The URL location of this file.
	 * @see NetworkFile#NetworkFile(String)
	 */
	public VersionedNetworkFile(final String url) {
		super(url);

		this.version = "";

		this.setVersion(version);
	}

	/**
	 * VersionedNetworkFile Constructor.
	 *
	 * @param fileName The fileName of this file.
	 * @param version  The version of this file.
	 * @param url      The URL location of this file.
	 * @see NetworkFile#NetworkFile(String, String)
	 */
	public VersionedNetworkFile(final String fileName, final String version, final String url) {
		super(fileName, url);

		this.setVersion(version);
	}

	/**
	 * VersionedNetworkFile Constructor.
	 *
	 * @param fileName The fileName of this file.
	 * @param url      The URL location of this file.
	 * @see NetworkFile#NetworkFile(String, String)
	 */
	public VersionedNetworkFile(final String fileName, final String url) {
		super(fileName, url);

		this.setVersion("");
	}

	/**
	 * VersionedNetworkFile Constructor.
	 *
	 * @param fileName The file name of this file.
	 * @param version  The version of this file.
	 * @param encoding A String representation of the encoding of this file.
	 * @param url      The URL location of the file.
	 */
	public VersionedNetworkFile(final String fileName, final String version, final String encoding, final String url) {
		super(fileName, encoding, url);

		this.setVersion(version);
	}

	/**
	 * VersionedNetworkFile Constructor.
	 *
	 * @param name             The name of this file.
	 * @param extension        The extension of this file.
	 * @param fileName         The file name of this file.
	 * @param modificationDate The last time this file was modified.
	 * @param url              The URL location of the file.
	 * @see NetworkFile#NetworkFile(String, String, String, Date, String)
	 */
	public VersionedNetworkFile(final String name, final String extension, final String fileName, final Date modificationDate, final String url) {
		super(name, extension, fileName, modificationDate, url);

		this.setVersion("");
	}

	/**
	 * VersionedNetworkFile Constructor.
	 *
	 * @param name             The name of this file.
	 * @param extension        The extension of this file.
	 * @param fileName         The file name of this file.
	 * @param version          The version of this file.
	 * @param modificationDate The last time this file was modified.
	 * @param url              The URL location of the file.
	 * @see NetworkFile#NetworkFile(String, String, String, Date, String)
	 */
	public VersionedNetworkFile(final String name, final String extension, final String fileName, final String version, final Date modificationDate, final String url) {
		super(name, extension, fileName, modificationDate, url);

		this.setVersion(version);
	}

	/**
	 * VersionedNetworkFile Constructor.
	 *
	 * @param name             The name of this file.
	 * @param extension        The extension of this file.
	 * @param fileName         The file name of this file.
	 * @param version          The version of this file.
	 * @param encoding         A String representation of the encoding of this file.
	 * @param modificationDate The last time this file was modified.
	 * @param url              The URL location of the file.
	 * @see NetworkFile#NetworkFile(String, String, String, String, Date, String)
	 */
	public VersionedNetworkFile(final String name, final String extension, final String fileName, final String version, final String encoding, final Date modificationDate, final String url) {
		super(name, extension, fileName, encoding, modificationDate, url);

		this.setVersion(version);
	}

	/**
	 * Separates the given version String into each version number.
	 * <br /><br />
	 * Separators are '.', '-', '_', and ' '.  These can be used in conjunction with each other.
	 * <br />
	 * EX. 1.2-4, is a valid version.
	 *
	 * @return An ArrayList of Strings with each version number in an index.
	 */
	private static List<String> separateVersion(final String version) {
		final List<String> list = new ArrayList<>();
		final String[] array = version.split("[\\.]|[-]|[_]|[ ]");

		for (final String str : array) {
			list.add(str.toLowerCase());
		}

		return list;
	}

	/**
	 * Compares the difference between two versions.
	 * <br /><br />
	 * A returned value of zero mean both versions are equal, positive mean the first version is greater, negative means the second version is greater.
	 *
	 * @param comp1 The first version string.
	 * @param comp2 The second version string.
	 * @return The difference between version.
	 * @see #compareVersionTo(String)
	 */
	public static int compareVersionTo(final String comp1, final String comp2) {
		if (comp1 == null) {
			if (comp2 == null) {
				throw new IllegalArgumentException("Both versions to compare cannot be null!");
			} else {
				throw new IllegalArgumentException("First version cannot be null!");
			}
		} else if (comp2 == null) {
			throw new IllegalArgumentException("Second version cannot be null!");
		}

		final VersionedNetworkFile f = new VersionedNetworkFile("temporary.tmp");
		f.setVersion(comp1);

		return f.compareVersionTo(comp2);
	}

	/**
	 * @return The version of this file.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version of this file to the given version.
	 *
	 * @param version The new version to set this file to.
	 */
	public void setVersion(final String version) {
		this.version = version;

		final List<String> separatedVersions = separateVersion();

		final int index;
		final int lastIndex = separatedVersions.size() - 1;

		if (separatedVersions.contains("alpha")) {
			this.releaseLevel = ReleaseLevel.ALPHA;

			index = separatedVersions.indexOf("alpha");

			if (index == lastIndex - 1) {
				this.releaseLevelVersion = parseInt(separatedVersions.remove(lastIndex));
			}

			separatedVersions.remove(index);
		} else if (separatedVersions.contains("beta")) {
			this.releaseLevel = ReleaseLevel.BETA;

			index = separatedVersions.indexOf("beta");

			if (index == lastIndex - 1) {
				this.releaseLevelVersion = parseInt(separatedVersions.remove(lastIndex));
			}

			separatedVersions.remove(index);
		} else if (separatedVersions.contains("rc")) {
			this.releaseLevel = ReleaseLevel.RC;

			index = separatedVersions.indexOf("rc");

			if (index == lastIndex - 1) {
				this.releaseLevelVersion = parseInt(separatedVersions.remove(lastIndex));
			}

			separatedVersions.remove(index);
		} else {
			this.releaseLevel = ReleaseLevel.RELEASE;
			this.releaseLevelVersion = 0;
		}

		this.versions = separatedVersions;
	}

	/**
	 * @return The major version number of this file.
	 * @see #getVersion(int)
	 */
	public int getMajorVersion() {
		return getVersion(0);
	}

	/**
	 * @return The major version number of this file.
	 * @see #getVersion(int)
	 */
	public int getMinorVersion() {
		return getVersion(1);
	}

	/**
	 * @return The fix version number of this file.
	 * @see #getVersion(int)
	 */
	public int getFixVersion() {
		return getVersion(2);
	}

	/**
	 * Returns the release level version of this file.
	 *
	 * @return The release level version of this file.
	 */
	public int getReleaseLevelVersion() {
		return releaseLevelVersion;
	}

	/**
	 * Returns the version number at the given depth, if there is no version number at the given depth zero is returned.
	 *
	 * @param depth The depth of the version number to get, starting at 0.
	 * @return The version number at the given depth, or zero if there is not a number at the given depth.
	 */
	public int getVersion(final int depth) {
		final String versionNumber;

		if (versions.size() > depth) {
			versionNumber = versions.get(depth);
		} else {
			versionNumber = "0";
		}

		return parseInt(versionNumber);
	}

	/**
	 * @return The release level of this file.
	 */
	public ReleaseLevel getReleaseLevel() {
		return releaseLevel;
	}

	/**
	 * Sets the release level of this file to the given {@link ReleaseLevel}.
	 *
	 * @param level The {@link ReleaseLevel} to this file to.
	 */
	public void setReleaseLevel(final ReleaseLevel level) {
		this.releaseLevel = level;
	}

	/**
	 * @return The separated version numbers in each index.
	 */
	public List<String> getVersions() {
		return versions;
	}

	/**
	 * Separates this {@link VersionedNetworkFile}'s version String into each version number.
	 * <br /><br />
	 * Separators are '.', '-', '_', and ' '.  These can be used in conjunction with each other.
	 * <br />
	 * EX. 1.2-4, is a valid version.
	 *
	 * @return An ArrayList of Strings with each version number in an index.
	 * @see #separateVersion(String)
	 */
	private List<String> separateVersion() {
		return separateVersion(version);
	}

	/**
	 * Compares this {@link VersionedNetworkFile}'s version to the given {@link VersionedNetworkFile}'s version.
	 * <br /><br />
	 * A returned value of zero mean both versions are equal, positive mean this version is greater, negative means the given version is greater.
	 *
	 * @param comp The {@link VersionedNetworkFile} to compare versions with.
	 * @return The difference between version of the two objects.
	 */
	public int compareVersionTo(final VersionedNetworkFile comp) {
		if (comp == null) {
			throw new IllegalArgumentException("Object to compare to cannot be null!");
		}

		int difference = 0;

		// Get the length of the longest version number sequence.
		final int length = (this.versions.size() > comp.getVersions().size()) ? this.versions.size() : comp.getVersions().size();

		// Calculate the difference between the two versions.
		for (int LCV = length - 1; LCV >= 0; LCV--) {
			difference += ((length - LCV) + Math.abs(difference)) * (this.getVersion(LCV) - comp.getVersion(LCV));
		}

		// Take release level into account if the two versions are the same.
		if (difference == 0) {
			difference = this.getReleaseLevel().value - comp.getReleaseLevel().value;
		}

		// Take the release level version into account if the two versions are the same.
		if (difference == 0) {
			difference = this.getReleaseLevelVersion() - comp.getReleaseLevelVersion();
		}

		return difference;
	}

	/**
	 * Compares this {@link VersionedNetworkFile}'s version to the given String version.
	 *
	 * @param compVersion The version to compare to.
	 * @return The difference between the two versions.
	 * @see #compareVersionTo(VersionedNetworkFile)
	 */
	public int compareVersionTo(final String compVersion) {
		if (compVersion == null) {
			throw new IllegalArgumentException("Object to compare to cannot be null!");
		}

		final VersionedNetworkFile comp = new VersionedNetworkFile("temporary.tmp");

		comp.setVersion(compVersion);

		return this.compareVersionTo(comp);
	}

	private int parseInt(final String str) {
		try {
			return Integer.parseInt(str);
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public String toString() {
		String string = super.toString();
		string += "\nVersion: " + version;
		string += "\nRelease Level: " + releaseLevel;

		if (releaseLevelVersion != 0) {
			string += " " + releaseLevelVersion;
		}

		return string;
	}

	/**
	 * An enumerated type to express levels of releases of a project.
	 */
	public enum ReleaseLevel {
		/**
		 * Full Release Level.
		 */
		RELEASE("RELEASE", 4),
		/**
		 * Release Candidate Level.
		 */
		RC("RC", 3),
		/**
		 * Beta Release Level.
		 */
		BETA("BETA", 2),
		/**
		 * Alpha Release Level.
		 */
		ALPHA("ALPHA", 1),
		/**
		 * Any Level
		 */
		ANY("ANY", 0);

		/**
		 * The name of the ReleaseLevel.
		 */
		final String name;

		/**
		 * An integer value that is associated with release levels.
		 * <br /><br />
		 * <p>
		 * Full Release = 4.
		 * <br />
		 * Release Candidate = 3.
		 * <br />
		 * Beta Release = 2.
		 * <br />
		 * Alpha Release = 1.
		 * <br />
		 * Any Release = 0.
		 */
		final int value;

		/**
		 * VersionednetworkFile Constructor.
		 *
		 * @param name  The name of the ReleaseLevel.
		 * @param value The value associated with the ReleaseLevel.
		 */
		ReleaseLevel(final String name, final int value) {
			this.name = name;
			this.value = value;
		}

		/**
		 * Returns the {@link ReleaseLevel} associated with the given name.
		 * <br /><br />
		 * If the given name does not match any of the names of the Objects, RELEASE will be returned.
		 *
		 * @param name The name of the {@link ReleaseLevel} that will be returned.
		 * @return The {@link ReleaseLevel} associated with the given name.
		 */
		public static ReleaseLevel getByName(final String name) {
			if (name.equalsIgnoreCase(ANY.getName())) {
				return ANY;
			} else if (name.equalsIgnoreCase(ALPHA.getName())) {
				return ALPHA;
			} else if (name.equalsIgnoreCase(BETA.getName())) {
				return BETA;
			} else if (name.equalsIgnoreCase(RC.getName())) {
				return RC;
			}
			return RELEASE;
		}

		/**
		 * @return The name of the ReleaseLevel.
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return The value associated with the ReleaseLevel
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Returns a comparison between this ReleaseLevel's value and the given Object.
		 * <br /><br />
		 * A returned value of zero mean the both Object's values are equal,
		 * positive means this ReleaseLevel's value is greater,
		 * negative means the given ReleaseLevel's value is greater.
		 *
		 * @param level The given {@link ReleaseLevel} to compare to.
		 * @return The difference between the two ReleaseLevels.
		 */
		public int compareToByValue(final ReleaseLevel level) {
			return this.value - level.value;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
