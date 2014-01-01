package net.KabOOm356.Reporter;

import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.File.AbstractFiles.UpdateSite;
import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile.ReleaseLevel;
import net.KabOOm356.Listeners.ReporterPlayerListener;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Locale.Entry.LocaleInfo;
import net.KabOOm356.Reporter.Configuration.ReporterConfigurationUtil;
import net.KabOOm356.Reporter.Database.ReporterDatabaseUtil;
import net.KabOOm356.Reporter.Locale.ReporterLocaleUtil;
import net.KabOOm356.Runnable.ReporterLocaleInitializer;
import net.KabOOm356.Runnable.UpdateThread;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main Reporter class.
 * <br /><br />
 * Homepage on BukkitDev: <a href="http://dev.bukkit.org/server-mods/reporter/">http://dev.bukkit.org/server-mods/reporter/</a>
 */
public class Reporter extends JavaPlugin
{
	private static final Logger log = Logger.getLogger("Minecraft");
	private static final String logPrefix = "[Reporter] ";
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static UpdateSite localeXMLUpdateSite = new UpdateSite("https://dl.dropbox.com/u/21577960/Reporter/Locale%20Files/latest.xml", UpdateSite.Type.XML);
	private static UpdateSite bukkitDevRSSUpdateSite = new UpdateSite("http://dev.bukkit.org/server-mods/reporter/files.rss", UpdateSite.Type.RSS);
	
	public static final String localeVersion = "9";
	public static final String configVersion = "11";
	public static final String databaseVersion = "7";
	
	private static String version;
	private static String defaultConsolePrefix;
	private static String versionString;
	
	private Locale locale;
	private ExtendedDatabaseHandler databaseHandler;
	private ReporterPlayerListener playerListener;
	private ReporterCommandManager commandManager;
	
	@Override
	public void onEnable()
	{
		version = getDescription().getVersion();
		
		versionString = "v" + version + " - ";
		
		defaultConsolePrefix = logPrefix + versionString;
		
		if(!getDataFolder().exists())
			getDataFolder().mkdir();

		ReporterConfigurationUtil.initConfiguration(getDataFolder(), getConfig());
		
		// If the configuration has been updated save and reload the configuration.
		// Which will get rid of all the comments/descriptions in the default configuration file.
		if(ReporterConfigurationUtil.updateConfiguration(getConfig()))
		{
			saveConfig();
			
			reloadConfig();
		}
		
		checkForPluginUpdate();
		
		initializeLocale();

		initializeDatabase();
		
		playerListener = new ReporterPlayerListener(this);
		commandManager = new ReporterCommandManager(this);
		
		setupCommands();
		
		getServer().getPluginManager().registerEvents(playerListener, this);

		log.info(defaultConsolePrefix + "Reporter enabled.");
	}
	
	@Override
	public void onDisable()
	{
		log.info(defaultConsolePrefix + "Stopping threads...");
		
		getServer().getScheduler().cancelTasks(this);
		
		log.info(defaultConsolePrefix + "Closing " + databaseHandler.getDatabaseType() + " connection...");
		
		try
		{
			databaseHandler.closeConnection();
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
		
		databaseHandler = null;
		locale = null;
		
		log.info(defaultConsolePrefix + "Reporter disabled.");
	}
	
	private void setupCommands()
	{
		String[] cmds = {"report", "rreport", "rep", "respond", "rrespond", "resp"};
		
		PluginCommand cmd = null;
		
		boolean error = false;
		
		for(String currentCmd : cmds)
		{
			cmd = getCommand(currentCmd);
			
			if(cmd != null)
				cmd.setExecutor(commandManager);
			else
			{
				getLog().warning(defaultConsolePrefix + "Unable to set executor for " + currentCmd + " command!");
				error = true;
			}
		}
		
		if(error)
		{
			getLog().warning(defaultConsolePrefix + "plugin.yml may have been altered!");
			getLog().warning(defaultConsolePrefix + "Please re-download the plugin from BukkitDev.");
		}
	}
	
	private void checkForPluginUpdate()
	{
		ReleaseLevel pluginLevel = ReleaseLevel.getByName(getConfig().getString("plugin.updates.updateLevel", "RELEASE"));
		
		if(getConfig().getBoolean("plugin.updates.checkForUpdates", true))
			Bukkit.getScheduler().runTaskAsynchronously(this, 
					new UpdateThread(bukkitDevRSSUpdateSite, version, pluginLevel));
	}
	
	private void initializeLocale()
	{
		String localeName = getConfig().getString("locale.locale", "en_US");
		boolean asynchronousUpdate = getConfig().getBoolean("locale.updates.asynchronousUpdate", true);
		boolean autoDownload = getConfig().getBoolean("locale.updates.autoDownload", true);
		boolean keepBackup = getConfig().getBoolean("locale.updates.keepBackup", false);
		ReleaseLevel localeLevel = ReleaseLevel.getByName(getConfig().getString("locale.updates.releaseLevel", "RELEASE"));
		
		locale = new Locale();
		
		if(asynchronousUpdate)
		{
			ReporterLocaleInitializer localeInitilizer = 
					new ReporterLocaleInitializer(this, localeName, getDataFolder(), autoDownload, localeLevel, keepBackup);
			
			this.getServer().getScheduler().runTaskAsynchronously(this, localeInitilizer);
		}
		else
			setLocale(ReporterLocaleUtil.initLocale(localeName, getDataFolder(), autoDownload, localeLevel, keepBackup));
	}
	
	private void initializeDatabase()
	{
		databaseHandler = ReporterDatabaseUtil.initDB(getConfig(), getDataFolder());

		if(databaseHandler == null)
		{
			Reporter.getLog().severe(Reporter.getDefaultConsolePrefix() + "Disabling plugin!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
	}
	
	public static Logger getLog()
	{
		return log;
	}

	public static String getLogPrefix()
	{
		return logPrefix;
	}

	public static String getVersion()
	{
		return version;
	}
	
	public static String getVersionString()
	{
		return versionString;
	}

	public static String getDefaultConsolePrefix()
	{
		return defaultConsolePrefix;
	}
	
	public static boolean isCommandSenderSupported(CommandSender cs)
	{
		return (cs instanceof org.bukkit.entity.Player) || (cs instanceof org.bukkit.command.ConsoleCommandSender) || (cs instanceof org.bukkit.command.RemoteConsoleCommandSender);
	}
	
	public void setLocale(Locale locale)
	{
		this.locale = locale;
		
		if(!setLocaleDefaults(locale))
		{
			log.warning(Reporter.getDefaultConsolePrefix() + "Unable to set defaults for the locale!");
		}
		
		log.info(Reporter.getDefaultConsolePrefix() + "Language: " + locale.getString(LocaleInfo.language)
				+ " v" + locale.getString(LocaleInfo.version) 
				+ " By " + locale.getString(LocaleInfo.author));
	}
	
	private boolean setLocaleDefaults(Locale locale)
	{
		InputStream defaultLocaleStream = getResource("en_US.yml");
		
		if(defaultLocaleStream != null)
		{
			YamlConfiguration defaultLocale = new YamlConfiguration();
			
			try
			{
				defaultLocale.load(defaultLocaleStream);
				locale.setDefaults(defaultLocale);
				return true;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				log.warning(Reporter.getDefaultConsolePrefix() + "Unable to read the default locale file!");
			}
		}
		else
		{
			log.warning(Reporter.getDefaultConsolePrefix() + "Unable to find the default locale file!");
		}
		
		return false;
	}
	
	public void updateLocale(Locale locale)
	{
		setLocale(locale);
		
		// Update the documentation for all the commands
		commandManager.updateDocumentation();
	}

	public Locale getLocale()
	{
		return locale;
	}
	
	public ExtendedDatabaseHandler getDatabaseHandler()
	{
		return databaseHandler;
	}

	public static DateFormat getDateformat()
	{
		return dateFormat;
	}

	public ReporterCommandManager getCommandManager()
	{
		return commandManager;
	}

	public static UpdateSite getBukkitDevRSSUpdateSite()
	{
		return bukkitDevRSSUpdateSite;
	}
	
	public static UpdateSite getLocaleXMLUpdateSite()
	{
		return localeXMLUpdateSite;
	}

	public static String getConfigurationVersion()
	{
		return configVersion;
	}

	public static String getDatabaseVersion()
	{
		return databaseVersion;
	}
}