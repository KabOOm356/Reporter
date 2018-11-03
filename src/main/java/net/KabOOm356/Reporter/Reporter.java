package net.KabOOm356.Reporter;

import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.File.AbstractFiles.UpdateSite;
import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile.ReleaseLevel;
import net.KabOOm356.Listeners.ReporterPlayerListener;
import net.KabOOm356.Locale.Entry.LocaleInfo;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Permission.PermissionHandler;
import net.KabOOm356.Reporter.Configuration.ReporterConfigurationUtil;
import net.KabOOm356.Reporter.Database.ReporterDatabaseUtil;
import net.KabOOm356.Reporter.Locale.ReporterLocaleInitializer;
import net.KabOOm356.Service.Messager.PlayerMessages;
import net.KabOOm356.Service.ServiceModule;
import net.KabOOm356.Service.Store.StoreModule;
import net.KabOOm356.Service.Store.type.LastViewed;
import net.KabOOm356.Service.Store.type.PlayerReport;
import net.KabOOm356.Updater.PluginUpdater;
import net.KabOOm356.Util.ArrayUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * The main Reporter class.
 * <br /><br />
 * Homepage on BukkitDev: <a href="http://dev.bukkit.org/server-mods/reporter/">http://dev.bukkit.org/server-mods/reporter/</a>
 */
public class Reporter extends JavaPlugin {
	public static final String localeVersion = "11";
	public static final String configVersion = "15";
	public static final String databaseVersion = "10";
	public static final String anonymousPlayerName = "* (Anonymous)";
	public static final String console = "CONSOLE";
	private static final Logger log = LogManager.getLogger(Reporter.class);
	private static final String logPrefix = "[Reporter] ";
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final UpdateSite localeXMLUpdateSite = new UpdateSite("https://www.dropbox.com/s/m75q8xsvc1swys0/latest.xml?dl=1", UpdateSite.Type.XML);
	private static final String pluginUpdateAPI = "https://api.curseforge.com/servermods/files?projectIds=31347";
	private static String version;
	private static String defaultConsolePrefix;
	private static String versionString;
	private final Locale locale = new Locale();
	private ExtendedDatabaseHandler databaseHandler;
	private PermissionHandler permissionHandler;
	private ReporterPlayerListener playerListener;
	private ServiceModule serviceModule;
	private ReporterCommandManager commandManager;

	public Reporter() {
		version = getDescription().getVersion();
		versionString = 'v' + version + " - ";
		defaultConsolePrefix = logPrefix + versionString;
	}

	public static String getLogPrefix() {
		return logPrefix;
	}

	public static String getVersion() {
		return version;
	}

	public static String getVersionString() {
		return versionString;
	}

	public static String getDefaultConsolePrefix() {
		return defaultConsolePrefix;
	}

	public static boolean isCommandSenderSupported(final CommandSender cs) {
		return (cs instanceof org.bukkit.entity.Player) || (cs instanceof org.bukkit.command.ConsoleCommandSender) || (cs instanceof org.bukkit.command.RemoteConsoleCommandSender);
	}

	public static DateFormat getDateformat() {
		return dateFormat;
	}

	public static UpdateSite getLocaleXMLUpdateSite() {
		return localeXMLUpdateSite;
	}

	public static String getConfigurationVersion() {
		return configVersion;
	}

	public static String getDatabaseVersion() {
		return databaseVersion;
	}

	@Override
	public void onEnable() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}

		final URL defaultConfigurationFile = getDefaultConfigurationFile();
		ReporterConfigurationUtil.initConfiguration(defaultConfigurationFile, getDataFolder(), getConfig());

		// If the configuration has been updated save and reload the configuration.
		// Which will get rid of all the comments/descriptions in the default configuration file.
		if (ReporterConfigurationUtil.updateConfiguration(getConfig())) {
			saveConfig();

			reloadConfig();
		}

		checkForPluginUpdate();
		initializeLocale();
		initializeDatabase();
		initializePermissions();

		playerListener = new ReporterPlayerListener(this);

		final LastViewed lastViewed = new LastViewed();
		final PlayerMessages playerMessages = new PlayerMessages();
		final PlayerReport playerReport = new PlayerReport();
		final StoreModule storeModule = new StoreModule(getConfig(), getDatabaseHandler(), getLocale(), getPermissionHandler(), lastViewed, playerMessages, playerReport);
		serviceModule = new ServiceModule(storeModule);
		commandManager = new ReporterCommandManager(this);

		setupCommands();

		getServer().getPluginManager().registerEvents(playerListener, this);

		log.info(defaultConsolePrefix + "Reporter enabled.");
	}

	@Override
	public void onDisable() {
		log.info(defaultConsolePrefix + "Stopping threads...");

		getServer().getScheduler().cancelTasks(this);

		if (databaseHandler != null) {
			log.info(defaultConsolePrefix + "Closing " + databaseHandler.getDatabaseType() + " connections...");
			databaseHandler.closeConnections();
		}
		log.info(defaultConsolePrefix + "Reporter disabled.");
	}

	private void setupCommands() {
		final String[] cmds = {"report", "rreport", "rep", "respond", "rrespond", "resp"};

		PluginCommand cmd = null;

		boolean error = false;

		for (final String currentCmd : cmds) {
			cmd = getCommand(currentCmd);

			if (cmd != null) {
				cmd.setExecutor(commandManager);
			} else {
				log.error(defaultConsolePrefix + "Unable to set executor for " + currentCmd + " command!");
				error = true;
			}
		}

		if (error) {
			log.warn(defaultConsolePrefix + "plugin.yml may have been altered!");
			log.warn(defaultConsolePrefix + "Please re-download the plugin from BukkitDev.");
		}
	}

	private void checkForPluginUpdate() {
		if (getConfig().getBoolean("plugin.updates.checkForUpdates", true)) {
			final ReleaseLevel pluginLevel = ReleaseLevel.getByName(getConfig().getString("plugin.updates.updateLevel", "RELEASE"));

			URL url = null;
			URLConnection connection = null;

			try {
				url = new URL(pluginUpdateAPI);

				connection = url.openConnection();
			} catch (final IOException e) {
				log.warn(Reporter.getDefaultConsolePrefix() +
						"Could not open a connection to the ServerMods API to check for plugin updates!", e);
				return;
			}

			final String apiKey = getConfig().getString("plugin.updates.api-key", "NO_KEY");

			// Set the X-API-Key if it is set in the configuration.
			if (apiKey != null && !apiKey.isEmpty() && !apiKey.equalsIgnoreCase("NO_KEY")) {
				connection.addRequestProperty("X-API-Key", apiKey);
			}

			final String name = getDescription().getName();
			final List<String> authors = getDescription().getAuthors();

			final String authorsString = ArrayUtil.indexesToString(authors);

			// userAgent = "Reporter v3.1.1 (By KabOOm356)"
			final String userAgent = name + " v" + version + " (By " + authorsString + ')';

			// Set User-Agent field, for connection to ServerMods API.
			connection.addRequestProperty("User-Agent", userAgent);

			final PluginUpdater update = new PluginUpdater(connection, name, version, pluginLevel);

			Bukkit.getScheduler().runTaskAsynchronously(this, update);
		}
	}

	private void initializeLocale() {
		final String localeName = getConfig().getString("locale.locale", "en_US");
		final boolean asynchronousUpdate = getConfig().getBoolean("locale.updates.asynchronousUpdate", true);
		final boolean autoDownload = getConfig().getBoolean("locale.updates.autoDownload", true);
		final boolean keepBackup = getConfig().getBoolean("locale.updates.keepBackup", false);
		final ReleaseLevel localeLevel = ReleaseLevel.getByName(getConfig().getString("locale.updates.releaseLevel", "RELEASE"));

		final ReporterLocaleInitializer localeInitializer =
				new ReporterLocaleInitializer(this, localeName, getDataFolder(), autoDownload, localeLevel, keepBackup);

		if (asynchronousUpdate) {
			this.getServer().getScheduler().runTaskAsynchronously(this, localeInitializer);
		} else {
			localeInitializer.initLocale();
			loadLocale();
		}
	}

	private void initializeDatabase() {
		try {
			databaseHandler = ReporterDatabaseUtil.initDB(getConfig(), getDataFolder());
		} catch (final Exception e) {
			log.fatal(Reporter.getDefaultConsolePrefix() + "Failed to initialize the database!", e);
		}

		if (databaseHandler == null) {
			log.fatal(Reporter.getDefaultConsolePrefix() + "Disabling plugin!");
			getServer().getPluginManager().disablePlugin(this);
		}
	}


	private void initializePermissions() {
		permissionHandler = new PermissionHandler();
	}

	public void loadLocale() {
		if (!setLocaleDefaults(locale)) {
			log.warn(Reporter.getDefaultConsolePrefix() + "Unable to set defaults for the locale!");
		}

		log.info(Reporter.getDefaultConsolePrefix() + "Language: " + locale.getString(LocaleInfo.language)
				+ " v" + locale.getString(LocaleInfo.version)
				+ " By " + locale.getString(LocaleInfo.author));
	}

	private boolean setLocaleDefaults(final Locale locale) {
		final Reader defaultLocaleReader = getTextResource("en_US.yml");

		if (defaultLocaleReader != null) {
			final YamlConfiguration defaultLocale = new YamlConfiguration();

			try {
				defaultLocale.load(defaultLocaleReader);
				locale.setDefaults(defaultLocale);
				return true;
			} catch (final Exception e) {
				log.warn(Reporter.getDefaultConsolePrefix() + "Unable to read the default locale file!", e);
			}
		} else {
			log.warn(Reporter.getDefaultConsolePrefix() + "Unable to find the default locale file!");
		}

		return false;
	}

	public Locale getLocale() {
		return locale;
	}

	public ExtendedDatabaseHandler getDatabaseHandler() {
		return databaseHandler;
	}

	public PermissionHandler getPermissionHandler() {
		return permissionHandler;
	}

	public ServiceModule getServiceModule() {
		return serviceModule;
	}

	public ReporterCommandManager getCommandManager() {
		return commandManager;
	}

	private URL getDefaultConfigurationFile() {
		return getClassLoader().getResource("config.yml");
	}
}
