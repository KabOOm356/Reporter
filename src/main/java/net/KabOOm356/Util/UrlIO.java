package net.KabOOm356.Util;

import net.KabOOm356.File.AbstractFiles.NetworkFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;

/**
 * A class to help with {@link URL} based input/output operations.
 */
public final class UrlIO {
	/**
	 * The encoding to use when writing files.
	 */
	public static final Charset outputCharset = StandardCharsets.UTF_8;
	public static final String ALPHA = "alpha";
	public static final String BETA = "beta";
	public static final String RELEASE_CANDIDATE = "rc";
	private static final Logger log = LogManager.getLogger(UrlIO.class);

	private UrlIO() {
	}

	/**
	 * Returns the response code from the given URLConnection.
	 *
	 * @param connection The connection to get the response from.
	 * @return The response code from the given connection.
	 * @throws IOException
	 */
	public static int getResponse(final URLConnection connection) throws IOException {
		return ((HttpURLConnection) connection).getResponseCode();
	}

	/**
	 * Checks if the response from the given URLConnection is valid (200).
	 *
	 * @param connection The URLConnection to get check if the response is valid to.
	 * @return True if the response is valid (200), otherwise false.
	 * @throws IOException
	 */
	public static boolean isResponseValid(final URLConnection connection) throws IOException {
		return getResponse(connection) == HttpURLConnection.HTTP_OK;
	}

	/**
	 * Downloads the given {@link NetworkFile} to the given destination.
	 *
	 * @param abstractFile The {@link NetworkFile} to download.
	 * @param destination  The destination to download the {@link NetworkFile} to.
	 * @throws IOException
	 */
	public static void downloadFile(final NetworkFile abstractFile, final File destination) throws IOException {
		if (abstractFile == null && destination == null) {
			throw new IllegalArgumentException("Both the abstract file and destination file cannot be null!");
		} else if (abstractFile == null) {
			throw new IllegalArgumentException("The abstract file cannot be null!");
		} else if (destination == null) {
			throw new IllegalArgumentException("The destination file cannot be null!");
		}

		if (!destination.exists()) {
			destination.createNewFile();
		}

		final URL url = new URL(abstractFile.getURL());

		final URLConnection dlUrlConnection = url.openConnection();
		final HttpURLConnection connection = (HttpURLConnection) dlUrlConnection;

		if (!isResponseValid(connection)) {
			throw new IOException(String.format("Connection response [%d] is not valid!", connection.getResponseCode()));
		}

		BufferedReader in = null;
		BufferedWriter out = null;

		try {
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), abstractFile.getEncoding()));
			out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(destination.toPath()), outputCharset.name()));

			String line;

			while ((line = in.readLine()) != null) {
				out.write(line);
				out.newLine();
			}

			destination.setLastModified(new Date().getTime());
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (final IOException e) {
				if (log.isDebugEnabled()) {
					log.warn("Failed to close output!", e);
				}
			}

			try {
				if (in != null) {
					in.close();
				}
			} catch (final IOException e) {
				if (log.isDebugEnabled()) {
					log.warn("Failed to close input!", e);
				}
			}

			connection.disconnect();
		}
	}

	/**
	 * Returns a String representation of a file's version given it's file name without the extension.
	 *
	 * @param fileName The file name of the file without the extension to get the version from.
	 * @return A String representation of the version parsed from the given file name.
	 */
	public static String getVersion(String fileName) {
		if (fileName.contains(" v")) {
			return fileName.substring(fileName.lastIndexOf(" v") + 2);
		} else if (fileName.contains(" V")) {
			return fileName.substring(fileName.lastIndexOf(" V") + 2);
		} else if (fileName.contains(" ")) {
			final int lastIndex = fileName.lastIndexOf(' ');
			String version = fileName.substring(lastIndex + 1);
			fileName = fileName.substring(0, lastIndex);

			/*
			 * If the release level is appended behind the version separated with a space
			 * append it to the numeral version and re-parse for a space.
			 */
			if (version.equalsIgnoreCase(ALPHA) || version.equalsIgnoreCase(BETA) || version.equalsIgnoreCase(RELEASE_CANDIDATE)) {
				version = getVersion(fileName + '-' + version);
			}

			return version;
		}
		return "";
	}

	/**
	 * Returns the node value from the given {@link Element} with the given tag name.
	 *
	 * @param element The {@link Element} to get the given node value from.
	 * @param tagName The tag name to get from the given {@link Element}.
	 * @return The node value for the given tag name if it is set, otherwise null.
	 */
	public static String getNodeValue(final Element element, final String tagName) {
		return element.getElementsByTagName(tagName).item(0).getChildNodes().item(0).getNodeValue();
	}
}
