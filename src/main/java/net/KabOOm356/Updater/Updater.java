package net.KabOOm356.Updater;

import net.KabOOm356.File.AbstractFiles.UpdateSite;
import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile;
import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile.ReleaseLevel;
import net.KabOOm356.Util.UrlIO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;

/**
 * A class that checks a remote site to see if a file is up-to-date.
 */
public abstract class Updater implements Runnable {
	private static final Logger log = LogManager.getLogger(Updater.class);
	/**
	 * The URL to the location to parse for updates.
	 */
	private final URL url;
	/**
	 * A connection to the URL to parse for updates.
	 */
	private final URLConnection connection;
	/**
	 * The name of the file to parse for.
	 */
	private String name;
	/**
	 * The local version of the file.
	 */
	private String localVersion;
	/**
	 * The lowest {@link ReleaseLevel} to be considered for downloading.
	 */
	private ReleaseLevel lowestLevel;

	/**
	 * Constructor.
	 *
	 * @param updateSite   The {@link UpdateSite} to parse for updates.
	 * @param name         The name of the file to parse for.
	 * @param localVersion The local version.
	 * @param lowestLevel  The lowest {@link ReleaseLevel} to consider.
	 * @throws IOException
	 */
	protected Updater(UpdateSite updateSite, String name, String localVersion, ReleaseLevel lowestLevel) throws IOException {
		if (updateSite == null)
			throw new IllegalArgumentException("The update site cannot be null!");
		if (updateSite.getURL() == null)
			throw new IllegalArgumentException("The url from the update site cannot be null!");

		this.name = name;
		this.localVersion = localVersion;
		this.lowestLevel = lowestLevel;

		this.url = new URL(updateSite.getURL());

		connection = url.openConnection();
	}

	/**
	 * Constructor.
	 *
	 * @param connection   The connection to the url to parse for updates.
	 * @param name         The name of the file to parse for.
	 * @param localVersion The local version.
	 * @param lowestLevel  The lowest {@link ReleaseLevel} to consider.
	 */
	protected Updater(URLConnection connection, String name, String localVersion, ReleaseLevel lowestLevel) {
		if (connection == null)
			throw new IllegalArgumentException("Connection cannot be null!");

		this.name = name;
		this.localVersion = localVersion;
		this.lowestLevel = lowestLevel;

		this.connection = connection;

		this.url = connection.getURL();
	}

	/**
	 * Finds the latest file released.
	 *
	 * @return A {@link VersionedNetworkFile} representation of the latest file found.
	 * @throws FileNotFoundException        Thrown if no files match the file name.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws ParseException
	 */
	protected abstract VersionedNetworkFile findLatestFile() throws SAXException, IOException, ParserConfigurationException, ParseException;

	public VersionedNetworkFile checkForUpdates() throws IOException, SAXException, ParserConfigurationException, ParseException {
		if (connection != null && UrlIO.isResponseValid(connection)) {
			VersionedNetworkFile remoteFile = findLatestFile();

			if (VersionedNetworkFile.compareVersionTo(localVersion, remoteFile.getVersion()) < 0)
				return remoteFile;
		}

		return null;
	}

	@Override
	public void run() {
		try {
			final VersionedNetworkFile latestFile = checkForUpdates();

			if (latestFile == null)
				System.out.println(name + " is up to date!");
			else {
				if (latestFile.getVersion() != null)
					System.out.println("There is a new update available for " + name + ": Version " + latestFile.getVersion());
				else
					System.out.println("There is a new update available for " + name + "!");
			}
		} catch (final Exception e) {
			log.log(Level.FATAL, name + " update thread failed!", e);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocalVersion() {
		return localVersion;
	}

	public void setLocalVersion(String localVersion) {
		this.localVersion = localVersion;
	}

	public ReleaseLevel getLowestLevel() {
		return lowestLevel;
	}

	public void setLowestLevel(ReleaseLevel lowestLevel) {
		this.lowestLevel = lowestLevel;
	}

	public URL getUrl() {
		return url;
	}

	public URLConnection getConnection() {
		return connection;
	}
}
