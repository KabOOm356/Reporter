package net.KabOOm356.File.AbstractFiles;

import net.KabOOm356.File.AbstractFile;

import java.util.Date;

/**
 * An {@link AbstractFile} that resides somewhere on the network, given by a URL.
 */
public class NetworkFile extends AbstractFile {
	/**
	 * The URL address to the location of the file.
	 */
	private String url;

	/**
	 * NetworkFile Constructor
	 *
	 * @param url The URL location of the file on the network.
	 * @see AbstractFile#AbstractFile(String)
	 */
	public NetworkFile(final String url) {
		super(getFileName(url));
		this.url = url;
	}

	/**
	 * NetworkFile Constructor
	 *
	 * @param fileName The file name of the file.
	 * @param encoding A String that represents the encoding of this file.
	 * @param url      The URL location of the file on the network.
	 */
	public NetworkFile(final String fileName, final String encoding, final String url) {
		super(fileName, encoding);
		this.url = url;
	}

	/**
	 * NetworkFile Constructor
	 *
	 * @param fileName The file name of the file.
	 * @param url      The URL location of the file on the network.
	 * @see AbstractFile#AbstractFile(String)
	 */
	public NetworkFile(final String fileName, final String url) {
		super(fileName);
		this.url = url;
	}

	/**
	 * NetworkFile Constructor.
	 *
	 * @param name      The name of the file.
	 * @param extension The extension of the file.
	 * @param fileName  The file name of the file.
	 * @param url       The URL location of the file on the network.
	 * @see AbstractFile#AbstractFile(String, String, String)
	 */
	public NetworkFile(final String name, final String extension, final String fileName, final String url) {
		super(name, extension, fileName);
		this.url = url;
	}

	/**
	 * NetworkFile Constructor.
	 *
	 * @param name      The name of the file.
	 * @param extension The extension of the file.
	 * @param fileName  The file name of the file.
	 * @param encoding  A String that represents the encoding of this file.
	 * @param url       The URL location of this file.
	 * @see AbstractFile#AbstractFile(String, String, String, String)
	 */
	public NetworkFile(final String name, final String extension, final String fileName, final String encoding, final String url) {
		super(name, extension, fileName, encoding);
		this.url = url;
	}

	/**
	 * NetworkFile Constructor.
	 *
	 * @param name             The name of the file.
	 * @param extension        The extension of the file.
	 * @param fileName         The file name of the file.
	 * @param modificationDate The last time the file was modified.
	 * @param url              The URL location of the file.
	 * @see AbstractFile#AbstractFile(String, String, String, Date)
	 */
	public NetworkFile(final String name, final String extension, final String fileName, final Date modificationDate, final String url) {
		super(name, extension, fileName, modificationDate);
		this.url = url;
	}

	/**
	 * NetworkFile Constructor.
	 *
	 * @param name             The name of the file.
	 * @param extension        The extension of the file.
	 * @param fileName         The file name of the file.
	 * @param modificationDate The last time the file was modified.
	 * @see AbstractFile#AbstractFile(String, String, String, Date)
	 */
	public NetworkFile(final String name, final String extension, final String fileName, final Date modificationDate) {
		super(name, extension, fileName, modificationDate);
		this.url = null;
	}

	/**
	 * NetworkFile Constructor.
	 *
	 * @param name             The name of the file.
	 * @param extension        The extension of the file.
	 * @param fileName         The file name of the file.
	 * @param encoding         A String representation of the encoding used in this file.
	 * @param modificationDate The last time the file was modified.
	 * @param url              The URL location of the file.
	 * @see AbstractFile#AbstractFile(String, String, String, String, Date)
	 */
	public NetworkFile(final String name, final String extension, final String fileName, final String encoding, final Date modificationDate, final String url) {
		super(name, extension, fileName, encoding, modificationDate);
		this.url = url;
	}

	/**
	 * Returns the file name parsed from a URL.
	 *
	 * @param url The URL to parse the file name from.
	 * @return The file name parsed from the URL.
	 */
	private static String getFileName(final String url) {
		final int index = url.lastIndexOf('/');
		if (index != -1 && index + 1 < url.length()) {
			return url.substring(index);
		}
		return url;
	}

	/**
	 * @return The current URL location to this file.
	 */
	public String getURL() {
		return url;
	}

	/**
	 * Sets the URL of this file to a new URL.
	 *
	 * @param url The new URL to this file.
	 */
	public void setUrl(final String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		String string = super.toString();
		string += "\nURL: " + url;

		return string;
	}
}
