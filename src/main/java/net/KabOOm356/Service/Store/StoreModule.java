package net.KabOOm356.Service.Store;

import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Permission.PermissionHandler;
import net.KabOOm356.Service.Messager.PlayerMessages;
import net.KabOOm356.Service.Store.type.LastViewed;
import net.KabOOm356.Service.Store.type.PlayerReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.Configuration;

public class StoreModule {
	private static final Logger log = LogManager.getLogger(StoreModule.class);

	private final Store<Configuration> configurationStore;
	private final Store<ExtendedDatabaseHandler> databaseStore;
	private final Store<Locale> localeStore;
	private final Store<PermissionHandler> permissionStore;
	private final Store<LastViewed> lastViewedStore;
	private final Store<PlayerMessages> playerMessagesStore;
	private final Store<PlayerReport> playerReportStore;

	public StoreModule(final Configuration configuration, final ExtendedDatabaseHandler database, final Locale locale, final PermissionHandler permission, final LastViewed lastViewed, final PlayerMessages playerMessages, final PlayerReport playerReport) {
		if (log.isDebugEnabled()) {
			log.info("Initializing service store...");
		}
		configurationStore = new Store<>(configuration);
		databaseStore = new Store<>(database);
		localeStore = new Store<>(locale);
		permissionStore = new Store<>(permission);
		lastViewedStore = new Store<>(lastViewed);
		playerMessagesStore = new Store<>(playerMessages);
		playerReportStore = new Store<>(playerReport);
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

	public Store<LastViewed> getLastViewedStore() {
		return lastViewedStore;
	}

	public Store<PlayerMessages> getPlayerMessagesStore() {
		return playerMessagesStore;
	}

	public Store<PlayerReport> getPlayerReportStore() {
		return playerReportStore;
	}
}
