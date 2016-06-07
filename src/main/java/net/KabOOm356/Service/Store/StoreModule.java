package net.KabOOm356.Service.Store;

import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Permission.PermissionHandler;
import net.KabOOm356.Service.Messager.PlayerMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;

import java.util.Map;

public class StoreModule {
	private static final Logger log = LogManager.getLogger(StoreModule.class);

	private final Store<Configuration> configurationStore;
	private final Store<ExtendedDatabaseHandler> databaseStore;
	private final Store<Locale> localeStore;
	private final Store<PermissionHandler> permissionStore;
	private final Store<Map<CommandSender, Integer>> lastViewedStore;
	private final Store<PlayerMessages> playerMessagesStore;

	public StoreModule(final Configuration configuration, final ExtendedDatabaseHandler database, final Locale locale, final PermissionHandler permission, final Map<CommandSender, Integer> lastViewed, final PlayerMessages playerMessages) {
		if (log.isDebugEnabled()) {
			log.info("Initializing service store...");
		}
		configurationStore = new Store<Configuration>(configuration);
		databaseStore = new Store<ExtendedDatabaseHandler>(database);
		localeStore = new Store<Locale>(locale);
		permissionStore = new Store<PermissionHandler>(permission);
		lastViewedStore = new Store<Map<CommandSender, Integer>>(lastViewed);
		playerMessagesStore = new Store<PlayerMessages>(playerMessages);
	}

	public Store<Configuration> getConfigurationStore() {
		return configurationStore;
	}

	public Store<ExtendedDatabaseHandler> getDatabaseStore() {
		return databaseStore;
	}

	public Store<Locale> getLocaleStore() {
		return localeStore;
	}

	public Store<PermissionHandler> getPermissionStore() {
		return permissionStore;
	}

	public Store<Map<CommandSender, Integer>> getLastViewedStore() {
		return lastViewedStore;
	}

	public Store<PlayerMessages> getPlayerMessagesStore() {
		return playerMessagesStore;
	}
}
