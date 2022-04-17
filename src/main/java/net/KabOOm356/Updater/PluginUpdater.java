package net.KabOOm356.Updater;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile;
import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile.ReleaseLevel;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.UrlIO;
import net.KabOOm356.Util.Util;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URLConnection;
import java.util.List;

/**
 * A {@link Updater} to update the plugin from the ServerMods API.
 */
public class PluginUpdater extends Updater {
	public static final String PLUGIN_FILE_EXTENSION = ".jar";
	private static final Logger log = LogManager.getLogger(PluginUpdater.class);

	/**
	 * Constructor.
	 *
	 * @param connection   The connection to the url to parse for updates.
	 * @param name         The name of the file to parse for.
	 * @param localVersion The local version.
	 * @param lowestLevel  The lowest {@link ReleaseLevel} to consider.
	 */
	public PluginUpdater(final URLConnection connection, final String name,
						 final String localVersion, final ReleaseLevel lowestLevel) {
		super(connection, name, localVersion, lowestLevel);
	}

	/**
	 * Finds the latest plugin release from the ServerMods API.
	 *
	 * @return A {@link VersionedNetworkFile} representation of the latest plugin file found.
	 * @throws FileNotFoundException        Thrown if no files match the file name.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	@Override
	protected VersionedNetworkFile findLatestFile() throws SAXException, IOException, ParserConfigurationException {
		final URLConnection connection = getConnection();

		final String name = getName();
		final ReleaseLevel lowestLevel = getLowestLevel();

		if (connection == null && UrlIO.isResponseValid(connection)) {
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("Both the connection and the name cannot be null!");
			}
			throw new IllegalArgumentException("The connection cannot be null!");
		} else if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("File name to search for cannot be null!");
		}

		BufferedReader in = null;
		String list;

		try {
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			list = in.readLine();
		} finally {
			if (in != null) {
				in.close();
			}
		}

		final Type listType = new TypeToken<List<PluginUpdateMetadata>>(){}.getType();
		final List<PluginUpdateMetadata> pluginUpdateMetadataList = new Gson().fromJson(list, listType);

		VersionedNetworkFile latestFile = null;

		for (final PluginUpdateMetadata metadata : pluginUpdateMetadataList) {
			final String currentName = metadata.getName();

			// Check if the current item matches what we are looking for.
			if (Util.startsWithIgnoreCase(currentName, name)) {
				// Parse the version from the name of the item.
				final String version = UrlIO.getVersion(currentName);
				final String link = metadata.getDownloadUrl();

				final VersionedNetworkFile file = new VersionedNetworkFile(name + PLUGIN_FILE_EXTENSION, version, link);

				// The release level must be greater than or equal to the lowest release level specified.
				if (file.getReleaseLevel().compareToByValue(lowestLevel) >= 0) {
					// If latestFile is not initialized, set latestFile to the current file.
					// If the current file's version is greater than the latestFile's version, set latestFile to the current file.
					if (latestFile == null || latestFile.compareVersionTo(file) < 0) {
						latestFile = file;
					}
				}
			}
		}

		if (latestFile == null) {
			throw new FileNotFoundException("File " + name + " could not be found!");
		}

		return latestFile;
	}

	@Override
	public void run() {
		try {
			final VersionedNetworkFile latestFile = checkForUpdates();

			if (latestFile == null) {
				log.log(Level.INFO, Reporter.getDefaultConsolePrefix() + "Reporter is up to date!");
			} else {
				if (latestFile.getVersion() != null) {
					log.log(Level.WARN, Reporter.getDefaultConsolePrefix() + "There is a new update available on BukkitDev: Version " + latestFile.getVersion());
				} else {
					log.log(Level.WARN, Reporter.getDefaultConsolePrefix() + "There is a new update available on BukkitDev!");
				}
			}
		} catch (final Exception e) {
			log.log(Level.FATAL, Reporter.getDefaultConsolePrefix() + "Plugin update thread failed!", e);
		}
	}
}
