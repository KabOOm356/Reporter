package net.KabOOm356.Reporter.Metrics;

import com.google.common.collect.ImmutableMap;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Locale.Entry.LocaleInfo;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Permission.PermissionType;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.FormattingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * A class to initialize and start plugin metrics reporting.
 */
public class MetricsInitializer implements Runnable {
	private static final Logger log = LogManager.getLogger(MetricsInitializer.class);

	private final JavaPlugin plugin;
	private final Locale locale;
	private final DatabaseType databaseType;
	private final PermissionType permissionType;

	public MetricsInitializer(final Reporter plugin) {
		this.plugin = plugin;
		this.locale = plugin.getLocale();
		this.databaseType = plugin.getDatabaseHandler().getDatabaseType();
		this.permissionType = plugin.getPermissionHandler().getPermissionType();
	}

	@Override
	public void run() {
		// If the locale has not been initialized, wait for a notification it has been initialized.
		synchronized (locale) {
			try {
				if (!locale.isInitialized()) {
					locale.wait();
				}
			} catch (final InterruptedException e) {
				if (log.isDebugEnabled()) {
					log.warn("Interrupted while waiting on locale initialization!", e);
				}
			}
		}

		// Initialize Metrics.
		final Metrics metrics = new Metrics(plugin);

		// Create a chart to track the locale language and locale version being used.
		metrics.addCustomChart(new Metrics.DrilldownPie("locale", new Callable<Map<String, Map<String, Integer>>>() {
			@Override
			public Map<String, Map<String, Integer>> call() {
				final String language = FormattingUtil.capitalizeFirstCharacter(locale.getString(LocaleInfo.language));
				final String version = "Version " + locale.getString(LocaleInfo.version);
				final Map<String, Integer> versionEntry = ImmutableMap.of(version, 1);
				return ImmutableMap.of(language, versionEntry);
			}
		}));

		// Create a chart to track the database engine being used.
		metrics.addCustomChart(new Metrics.SimplePie("database_engine", new Callable<String>() {
			@Override
			public String call() {
				return databaseType.toString();
			}
		}));

		metrics.addCustomChart(new Metrics.SimplePie("permission_manager", new Callable<String>() {
			@Override
			public String call() {
				return permissionType.toString();
			}
		}));
	}
}
