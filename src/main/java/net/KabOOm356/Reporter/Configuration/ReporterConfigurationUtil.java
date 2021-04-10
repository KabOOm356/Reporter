package net.KabOOm356.Reporter.Configuration;

import net.KabOOm356.Database.Connection.ConnectionPoolConfig;
import net.KabOOm356.Reporter.Reporter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.URL;
import java.util.Date;

/**
 * A class to help with initializing and updating the Reporter configuration file.
 */
public final class ReporterConfigurationUtil {
	private static final Logger log = LogManager.getLogger(ReporterConfigurationUtil.class);

	private ReporterConfigurationUtil() {
	}

	/**
	 * Initializes the Reporter configuration file by extracting the file if it does not exist and then attempts to load it.
	 *
	 * @param defaultConfigurationFile URL pointing to the default configuration file.
	 * @param dataFolder               The parent directory to save the configuration file.
	 * @param configuration            A {@link FileConfiguration} that will be loaded with the configuration file.
	 */
	public static void initConfiguration(final URL defaultConfigurationFile, final File dataFolder, final FileConfiguration configuration) {
		final File configFile = new File(dataFolder, "config.yml");

		if (!configFile.exists()) {
			BufferedReader input = null;
			BufferedWriter out = null;

			try {
				log.log(Level.INFO, Reporter.getDefaultConsolePrefix() + "Extracting default config file from the jar.");

				configFile.createNewFile();
				final InputStreamReader inputStreamReader = new InputStreamReader(defaultConfigurationFile.openStream());
				input = new BufferedReader(inputStreamReader);

				String line;

				final FileWriter fileWriter = new FileWriter(configFile);
				out = new BufferedWriter(fileWriter);

				while ((line = input.readLine()) != null) {
					if (line.contains("Version")) {
						out.write("# Plugin Version: " + Reporter.getVersion());
						out.newLine();
						out.write(line);
						out.newLine();
						out.write("# " + Reporter.getDateformat().format(new Date()));
                    } else {
						out.write(line);
                    }
                    out.newLine();
                }

				out.flush();
			} catch (final Exception ex) {
				log.log(Level.WARN, Reporter.getDefaultConsolePrefix() + "Error creating config file.", ex);
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (final IOException e) {
					if (log.isDebugEnabled()) {
						log.warn("Failed to close configuration writer!", e);
					}
				}

				try {
					if (input != null) {
						input.close();
					}
				} catch (final IOException e) {
					if (log.isDebugEnabled()) {
						log.warn("Failed to close configuration reader!", e);
					}
				}
			}
		}

		try {
			configuration.load(configFile);
		} catch (final Exception e) {
			log.log(Level.WARN, Reporter.getDefaultConsolePrefix() + "Error loading config file.", e);
			log.log(Level.WARN, Reporter.getDefaultConsolePrefix() + "Using default configuration.", e);
		}
	}

	/**
	 * Checks the configuration for updates and then updates if needed.
	 * <br /><br />
	 * <b>NOTE:</b> This does not save the {@link FileConfiguration}.
	 *
	 * @param configuration The {@link FileConfiguration} to update.
	 * @return True if the configuration was updated, otherwise false.
	 */
	public static boolean updateConfiguration(final FileConfiguration configuration) {
		configuration.setDefaults(new YamlConfiguration());

		boolean updated = false;

		// Version 1
		updated = set(configuration, "database.type", "sqlite");
		updated = set(configuration, "database.host", "localhost:3306") || updated;
		updated = set(configuration, "database.database", "Reporter") || updated;
		updated = set(configuration, "database.username", "root") || updated;
		updated = set(configuration, "database.password", "root") || updated;

		// Version 2
		updated = set(configuration, "database.dbName", "reports.db") || updated;
		updated = set(configuration, "general.messaging.listOnLogin", true) || updated;

		// Version 3
		if (configuration.isSet("general.messaging.reportList")) {
			configuration.set("general.messaging.reportList", null);
			updated = true;
		}
		if (configuration.isSet("general.messaging.broadcast")) {
			configuration.set("general.messaging.broadcast", null);
			updated = true;
		}

		// Version 4
		updated = set(configuration, "general.canViewSubmittedReports", true) || updated;

		// Version 5
		updated = set(configuration, "general.canCompleteWithoutSummary", false) || updated;

		// Version 6
		updated = set(configuration, "locale.locale", configuration.getString("general.locale", "en_US")) || updated;
		if (configuration.isSet("general.locale")) {
			configuration.set("general.locale", null);
			updated = true;
		}
		if (configuration.isSet("general.localeAutoDownload")) {
			configuration.set("general.localeAutoDownload", null);
			updated = true;
		}

		// Version 7
		updated = set(configuration, "general.messaging.listOnLogin.listOnLogin", configuration.getBoolean("general.messaging.listOnLogin", true)) || updated;
		updated = set(configuration, "general.messaging.listOnLogin.useDelay", true) || updated;
		updated = set(configuration, "general.messaging.listOnLogin.delay", 5) || updated;

		// Version 8
		updated = set(configuration, "general.viewing.displayLocation", true) || updated;
		updated = set(configuration, "general.reporting.limitNumberOfReports", true) || updated;
		updated = set(configuration, "general.reporting.limitNumber", 5) || updated;
		updated = set(configuration, "general.reporting.limitTime", 600) || updated;

		// Version 9
		updated = set(configuration, "general.viewing.displayRealName", false) || updated;
		updated = set(configuration, "general.messaging.completedMessageOnLogin.completedMessageOnLogin", true) || updated;
		updated = set(configuration, "general.messaging.completedMessageOnLogin.useDelay", true) || updated;
		updated = set(configuration, "general.messaging.completedMessageOnLogin.delay", 5) || updated;

		// Version 10
		updated = set(configuration, "plugin.updates.checkForUpdates", configuration.getBoolean("general.checkForUpdates", true)) || updated;
		updated = set(configuration, "plugin.updates.releaseLevel", "RELEASE") || updated;
		updated = set(configuration, "locale.updates.autoDownload", configuration.getBoolean("locale.localeAutoDownload", true)) || updated;
		updated = set(configuration, "locale.updates.keepBackup", configuration.getBoolean("locale.keepLocaleBackupFile", false)) || updated;
		updated = set(configuration, "locale.updates.releaseLevel", "RELEASE") || updated;
		updated = set(configuration, "locale.updates.asynchronousUpdate", true) || updated;
		if (configuration.isSet("general.checkForUpdates")) {
			configuration.set("general.checkForUpdates", null);
			updated = true;
		}
		if (configuration.isSet("general.checkForDevUpdates")) {
			configuration.set("general.checkForDevUpdates", null);
			updated = true;
		}
		if (configuration.isSet("locale.localeAutoDownload")) {
			configuration.set("locale.localeAutoDownload", null);
			updated = true;
		}
		if (configuration.isSet("locale.keepLocaleBackupFile")) {
			configuration.set("locale.keepLocaleBackupFile", null);
			updated = true;
		}

		// Version 11
		updated = set(configuration, "general.permissions.opsHaveAllPermissions", true) || updated;
		updated = set(configuration, "general.reporting.limitReportsAgainstPlayers", false) || updated;
		updated = set(configuration, "general.reporting.limitNumberAgainstPlayers", 2) || updated;
		updated = set(configuration, "general.reporting.alerts.toConsole.limitAgainstPlayerReached", true) || updated;
		updated = set(configuration, "general.reporting.alerts.toConsole.allowedToReportPlayerAgain", true) || updated;
		updated = set(configuration, "general.reporting.alerts.toPlayer.allowedToReportAgain", true) || updated;
		updated = set(configuration, "general.reporting.alerts.toPlayer.allowedToReportPlayerAgain", true) || updated;
		updated = set(configuration, "general.reporting.alerts.toConsole.limitReached", configuration.getBoolean("general.reporting.alerts.limitReached", true)) || updated;
		updated = set(configuration, "general.reporting.alerts.toConsole.allowedToReportAgain", configuration.getBoolean("general.reporting.alerts.allowedToReportAgain", true)) || updated;
		if (configuration.isSet("general.reporting.alerts.limitReached")) {
			configuration.set("general.reporting.alerts.limitReached", null);
			updated = true;
		}
		if (configuration.isSet("general.reporting.alerts.allowedToReportAgain")) {
			configuration.set("general.reporting.alerts.allowedToReportAgain", null);
			updated = true;
		}

		// Version 12
		updated = set(configuration, "plugin.updates.api-key", "NO_KEY") || updated;

		// Version 13
		updated = set(configuration, "general.matchPartialOfflineUsernames", true) || updated;

		// Version 14
//		updated = set(configuration, "plugin.statistics.opt-out", false) || updated;

		// Version 15 (Database Connection Pooling)
		updated = set(configuration, "database.connectionPool.enableLimiting", ConnectionPoolConfig.defaultInstance.isConnectionPoolLimited()) || updated;
		updated = set(configuration, "database.connectionPool.maxNumberOfConnections", ConnectionPoolConfig.defaultInstance.getMaxConnections()) || updated;
		updated = set(configuration, "database.connectionPool.maxNumberOfAttemptsForConnection", ConnectionPoolConfig.defaultInstance.getMaxAttemptsForConnection()) || updated;
		updated = set(configuration, "database.connectionPool.waitTimeBeforeUpdate", ConnectionPoolConfig.defaultInstance.getWaitTimeBeforeUpdate()) || updated;
		updated = set(configuration, "general.messaging.alerts.reportedPlayerLogin.enabled", true) || updated;
		updated = set(configuration, "general.messaging.alerts.reportedPlayerLogin.toPlayer", true) || updated;
		updated = set(configuration, "general.messaging.alerts.reportedPlayerLogin.toConsole", true) || updated;

		// Version 16 (Update Statistics)
		updated = unset(configuration, "plugin.statistics.opt-out") || updated;

		if (updated) {
			configuration.options().header("Reporter Configuration File\n" +
					"Plugin Version: " + Reporter.getVersion() + '\n' +
					"Config Version: " + Reporter.getConfigurationVersion() + '\n' +
					Reporter.getDateformat().format(new Date()));

			log.log(Level.INFO, Reporter.getDefaultConsolePrefix() +
					"Updating the config file to version " + Reporter.getConfigurationVersion());
		}

		return updated;
	}

	private static boolean set(final FileConfiguration configuration, final String key, final Object value) {
		if (!configuration.isSet(key)) {
			configuration.set(key, value);
			return true;
		}
		return false;
	}

	private static boolean unset(final FileConfiguration configuration, final String key) {
		if (configuration.isSet(key)) {
			configuration.set(key, null);
			return true;
		}
		return false;
	}
}
