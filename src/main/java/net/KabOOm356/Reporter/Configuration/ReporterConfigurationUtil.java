package net.KabOOm356.Reporter.Configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;

import net.KabOOm356.Reporter.Reporter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * A class to help with initializing and updating the Reporter configuration file.
 */
public class ReporterConfigurationUtil
{
	private static final Logger log = LogManager.getLogger(ReporterConfigurationUtil.class);
	
	/**
	 * Initializes the Reporter configuration file by extracting the file if it does not exist and then attempts to load it.
	 * 
	 * @param dataFolder The parent directory to save the configuration file.
	 * @param configuration A {@link FileConfiguration} that will be loaded with the configuration file.
	 */
	public static void initConfiguration(File dataFolder, FileConfiguration configuration)
	{
		File configFile = new File(dataFolder, "config.yml");
		
		if(!configFile.exists())
		{
			BufferedReader input = null;
			BufferedWriter out = null;
			
			try
			{
				log.log(Level.INFO, Reporter.getDefaultConsolePrefix() + "Extracting default config file from the jar.");

				configFile.createNewFile();

				URL fileURL = Reporter.class.getClassLoader().getResource(configFile.getName());

				input = new BufferedReader(new InputStreamReader(fileURL.openStream()));

				String line;

				out = new BufferedWriter(new FileWriter(configFile));
				
				while((line = input.readLine()) != null)
				{
					if(line.contains("Version"))
					{
						out.write("# Plugin Version: " + Reporter.getVersion());
						out.newLine();
						out.write(line);
						out.newLine();
						out.write("# " + Reporter.getDateformat().format(new Date()));
						out.newLine();
					}
					else
					{
						out.write(line);
						out.newLine();
					}
				}

				out.flush();
			}
			catch(Exception ex)
			{
				log.log(Level.WARN, Reporter.getDefaultConsolePrefix() + "Error creating config file.", ex);
			}
			finally
			{
				try
				{
					out.close();
				}
				catch (IOException e)
				{
				}
				
				try
				{
					input.close();
				}
				catch(Exception ex)
				{
				}
			}
		}
		
		try
		{
			configuration.load(configFile);
		}
		catch (Exception e)
		{
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
	 * 
	 * @return True if the configuration was updated, otherwise false.
	 */
	public static boolean updateConfiguration(FileConfiguration configuration)
	{
		configuration.setDefaults(new YamlConfiguration());
		
		boolean updated = false;
		
		// Version 1
		if(!configuration.isSet("database.type"))
		{
			configuration.set("database.type", "sqlite");
			updated = true;
		}
		if(!configuration.isSet("database.host"))
		{
			configuration.set("database.host", "localhost:3306");
			updated = true;
		}
		if(!configuration.isSet("database.database"))
		{
			configuration.set("database.database", "Reporter");
			updated = true;
		}
		if(!configuration.isSet("database.username"))
		{
			configuration.set("database.username", "root");
			updated = true;
		}
		if(!configuration.isSet("database.password"))
		{
			configuration.set("database.password", "root");
			updated = true;
		}
		
		// Version 2
		if(!configuration.isSet("database.dbName"))
		{
			configuration.set("database.dbName", "reports.db");
			updated = true;
		}
		if(!configuration.isSet("general.messaging.listOnLogin"))
		{
			configuration.set("general.messaging.listOnLogin", true);
			updated = true;
		}
		
		// Version 3
		if(configuration.isSet("general.messaging.reportList"))
		{
			configuration.set("general.messaging.reportList", null);
			updated = true;
		}
		if(configuration.isSet("general.messaging.broadcast"))
		{	
			configuration.set("general.messaging.broadcast", null);
			updated = true;
		}
		
		// Version 4
		if(!configuration.isSet("general.canViewSubmittedReports"))
		{	
			configuration.set("general.canViewSubmittedReports", true);
			updated = true;
		}
		
		// Version 5
		if(!configuration.isSet("general.canCompleteWithoutSummary"))
		{	
			configuration.set("general.canCompleteWithoutSummary", false);
			updated = true;
		}
		
		// Version 6
		if(!configuration.isSet("locale.locale"))
		{
			configuration.set("locale.locale", configuration.getString("general.locale", "en_US"));
			updated = true;
		}
		if(configuration.isSet("general.locale"))
		{
			configuration.set("general.locale", null);
			updated = true;
		}
		if(configuration.isSet("general.localeAutoDownload"))
		{
			configuration.set("general.localeAutoDownload", null);
			updated = true;
		}
		
		// Version 7
		if(!configuration.isSet("general.messaging.listOnLogin.listOnLogin"))
		{
			configuration.set("general.messaging.listOnLogin.listOnLogin", configuration.getBoolean("general.messaging.listOnLogin", true));
			updated = true;
		}
		if(!configuration.isSet("general.messaging.listOnLogin.useDelay"))
		{
			configuration.set("general.messaging.listOnLogin.useDelay", true);
			updated = true;
		}
		if(!configuration.isSet("general.messaging.listOnLogin.delay"))
		{
			configuration.set("general.messaging.listOnLogin.delay", 5);
			updated = true;
		}
		
		// Version 8
		if(!configuration.isSet("general.viewing.displayLocation"))
		{
			configuration.set("general.viewing.displayLocation", true);
			updated = true;
		}
		if(!configuration.isSet("general.reporting.limitNumberOfReports"))
		{
			configuration.set("general.reporting.limitNumberOfReports", true);
			updated = true;
		}
		if(!configuration.isSet("general.reporting.limitNumber"))
		{
			configuration.set("general.reporting.limitNumber", 5);
			updated = true;
		}
		if(!configuration.isSet("general.reporting.limitTime"))
		{
			configuration.set("general.reporting.limitTime", 600);
			updated = true;
		}
		
		// Version 9
		if(!configuration.isSet("general.viewing.displayRealName"))
		{
			configuration.set("general.viewing.displayRealName", false);
			updated = true;
		}
		if(!configuration.isSet("general.messaging.completedMessageOnLogin.completedMessageOnLogin"))
		{
			configuration.set("general.messaging.completedMessageOnLogin.completedMessageOnLogin", true);
			updated = true;
		}
		if(!configuration.isSet("general.messaging.completedMessageOnLogin.useDelay"))
		{
			configuration.set("general.messaging.completedMessageOnLogin.useDelay", true);
			updated = true;
		}
		if(!configuration.isSet("general.messaging.completedMessageOnLogin.delay"))
		{
			configuration.set("general.messaging.completedMessageOnLogin.delay", 5);
			updated = true;
		}
		
		// Version 10
		if(!configuration.isSet("plugin.updates.checkForUpdates"))
		{
			configuration.set("plugin.updates.checkForUpdates", configuration.getBoolean("general.checkForUpdates", true));
			updated = true;
		}
		if(!configuration.isSet("plugin.updates.releaseLevel"))
		{
			configuration.set("plugin.updates.releaseLevel", "RELEASE");
			updated = true;
		}
		if(!configuration.isSet("locale.updates.autoDownload"))
		{
			configuration.set("locale.updates.autoDownload", configuration.getBoolean("locale.localeAutoDownload", true));
			updated = true;
		}
		if(!configuration.isSet("locale.updates.keepBackup"))
		{
			configuration.set("locale.updates.keepBackup", configuration.getBoolean("locale.keepLocaleBackupFile", false));
			updated = true;
		}
		if(!configuration.isSet("locale.updates.releaseLevel"))
		{
			configuration.set("locale.updates.releaseLevel", "RELEASE");
			updated = true;
		}
		if(!configuration.isSet("locale.updates.asynchronousUpdate"))
		{
			configuration.set("locale.updates.asynchronousUpdate", true);
			updated = true;
		}
		if(configuration.isSet("general.checkForUpdates"))
		{
			configuration.set("general.checkForUpdates", null);
			updated = true;
		}
		if(configuration.isSet("general.checkForDevUpdates"))
		{
			configuration.set("general.checkForDevUpdates", null);
			updated = true;
		}
		if(configuration.isSet("locale.localeAutoDownload"))
		{
			configuration.set("locale.localeAutoDownload", null);
			updated = true;
		}
		if(configuration.isSet("locale.keepLocaleBackupFile"))
		{
			configuration.set("locale.keepLocaleBackupFile", null);
			updated = true;
		}
		
		// Version 11
		if(!configuration.isSet("general.permissions.opsHaveAllPermissions"))
		{
			configuration.set("general.permissions.opsHaveAllPermissions", true);
			updated = true;
		}
		if(!configuration.isSet("general.reporting.limitReportsAgainstPlayers"))
		{
			configuration.set("general.reporting.limitReportsAgainstPlayers", false);
			updated = true;
		}
		if(!configuration.isSet("general.reporting.limitNumberAgainstPlayers"))
		{
			configuration.set("general.reporting.limitNumberAgainstPlayers", 2);
			updated = true;
		}
		if(!configuration.isSet("general.reporting.alerts.toConsole.limitAgainstPlayerReached"))
		{
			configuration.set("general.reporting.alerts.toConsole.limitAgainstPlayerReached", true);
			updated = true;
		}
		if(!configuration.isSet("general.reporting.alerts.toConsole.allowedToReportPlayerAgain"))
		{
			configuration.set("general.reporting.alerts.toConsole.allowedToReportPlayerAgain", true);
			updated = true;
		}
		if(!configuration.isSet("general.reporting.alerts.toPlayer.allowedToReportAgain"))
		{
			configuration.set("general.reporting.alerts.toPlayer.allowedToReportAgain", true);
			updated = true;
		}
		if(!configuration.isSet("general.reporting.alerts.toPlayer.allowedToReportPlayerAgain"))
		{
			configuration.set("general.reporting.alerts.toPlayer.allowedToReportPlayerAgain", true);
			updated = true;
		}
		if(!configuration.isSet("general.reporting.alerts.toConsole.limitReached"))
		{
			boolean oldValue = configuration.getBoolean("general.reporting.alerts.limitReached", true);
			configuration.set("general.reporting.alerts.toConsole.limitReached", oldValue);
			updated = true;
		}
		if(!configuration.isSet("general.reporting.alerts.toConsole.allowedToReportAgain"))
		{
			boolean oldValue = configuration.getBoolean("general.reporting.alerts.allowedToReportAgain", true);
			configuration.set("general.reporting.alerts.toConsole.allowedToReportAgain", oldValue);
			updated = true;
		}
		if(configuration.isSet("general.reporting.alerts.limitReached"))
		{
			configuration.set("general.reporting.alerts.limitReached", null);
			updated = true;
		}
		if(configuration.isSet("general.reporting.alerts.allowedToReportAgain"))
		{
			configuration.set("general.reporting.alerts.allowedToReportAgain", null);
			updated = true;
		}
		
		// Version 12
		if(!configuration.isSet("plugin.updates.api-key"))
		{
			configuration.set("plugin.updates.api-key", "NO_KEY");
			updated = true;
		}
		
		// Version 13
		if(!configuration.isSet("general.matchPartialOfflineUsernames"))
		{
			configuration.set("general.matchPartialOfflineUsernames", true);
			updated = true;
		}
		
		// Version 14
		if(!configuration.isSet("plugin.statistics.opt-out"))
		{
			configuration.set("plugin.statistics.opt-out", false);
			updated = true;
		}
		
		if(updated)
		{
			configuration.options().header("Reporter Configuration File\n" +
					"Plugin Version: " + Reporter.getVersion() + "\n" +
					"Config Version: " + Reporter.getConfigurationVersion() + "\n" +
					Reporter.getDateformat().format(new Date()));
			
			log.log(Level.INFO, Reporter.getDefaultConsolePrefix() +
					"Updating the config file to version " + Reporter.getConfigurationVersion());
		}
		
		return updated;
	}
}
