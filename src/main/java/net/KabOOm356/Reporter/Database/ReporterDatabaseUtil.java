package net.KabOOm356.Reporter.Database;

import net.KabOOm356.Database.Connection.ConnectionPoolConfig;
import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Reporter.Database.Table.Initializer.ModStatsTableInitializer;
import net.KabOOm356.Reporter.Database.Table.Initializer.PlayerStatsTableTableInitializer;
import net.KabOOm356.Reporter.Database.Table.Initializer.ReportTableInitializer;
import net.KabOOm356.Reporter.Reporter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * A class to help initialize and update Reporter's database.
 */
public class ReporterDatabaseUtil {
	private static final Logger log = LogManager.getLogger(ReporterDatabaseUtil.class);

	/**
	 * Initializes the database.
	 *
	 * @param configuration The {@link FileConfiguration} that will be used to get the database options.
	 * @param dataFolder    A {@link File} to the directory where the data should be stored.
	 * @return An initialized {@link ExtendedDatabaseHandler}.
	 * @throws IllegalArgumentException Thrown if the connection pool configuration is invalid.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public static ExtendedDatabaseHandler initDB(final FileConfiguration configuration, final File dataFolder) throws IllegalArgumentException, IOException, ClassNotFoundException, SQLException, InterruptedException {
		ExtendedDatabaseHandler databaseHandler = null;

		final boolean connectionPoolLimit = configuration.getBoolean("database.connectionPool.enableLimiting", ConnectionPoolConfig.defaultInstance.isConnectionPoolLimited());
		final int maxNumberOfConnections = configuration.getInt("database.connectionPool.maxNumberOfConnections", ConnectionPoolConfig.defaultInstance.getMaxConnections());
		final int maxNumberOfAttemptsForConnection = configuration.getInt("database.connectionPool.maxNumberOfAttemptsForConnection", ConnectionPoolConfig.defaultInstance.getMaxAttemptsForConnection());
		final long waitTimeBeforeUpdate = configuration.getLong("database.connectionPool.waitTimeBeforeUpdate", ConnectionPoolConfig.defaultInstance.getWaitTimeBeforeUpdate());

		final ConnectionPoolConfig connectionPoolConfig;

		try {
			connectionPoolConfig = new ConnectionPoolConfig(connectionPoolLimit, maxNumberOfConnections, waitTimeBeforeUpdate, maxNumberOfAttemptsForConnection);
		} catch (final IllegalArgumentException e) {
			log.warn("Failed to configure connection pool!");
			throw e;
		}

		/* If an error occurs attempting to initialize a database
		 * the plugin will attempt to use the next less "complicated" database
		 * 
		 * Attempt order:
		 * MySQL -> SQLite -> Flatfile (When completed)
		 */
		boolean fallbackToNextDB = false;

		// Attempt to initialize a MySQL database
		if (configuration.getString("database.type", DatabaseType.SQLITE.toString()).equalsIgnoreCase(DatabaseType.MYSQL.toString())) {
			try {
				log.log(Level.INFO, Reporter.getDefaultConsolePrefix() + "Connecting to MySQL server...");

				final String host = configuration.getString("database.host", "localhost:3306");
				final String database = configuration.getString("database.database", "Reporter");
				final String username = configuration.getString("database.username", "root");
				final String password = configuration.getString("database.password", "root");

				databaseHandler = new ExtendedDatabaseHandler(host, database, username, password, connectionPoolConfig);
				checkConnection(databaseHandler.getDatabase());
				initDatabaseTables(databaseHandler.getDatabase());
			} catch (final Exception e) {
				databaseHandler = null;
				fallbackToNextDB = true;
				log.log(Level.ERROR, Reporter.getDefaultConsolePrefix() + "Error connecting to MySQL server using SQLite.", e);
			}
		} else {
			fallbackToNextDB = true;
		}

		// Attempt to initialize a SQLite database
		if (fallbackToNextDB) {
			final String databaseName = configuration.getString("database.dbName", "reports.db");

			try {
				databaseHandler = new ExtendedDatabaseHandler(DatabaseType.SQLITE, dataFolder.getPath(), databaseName, connectionPoolConfig);
				initDatabaseTables(databaseHandler.getDatabase());
			} catch (final IOException e) {
				log.warn(Reporter.getDefaultConsolePrefix() + "Failed to initialize an SQLite database!");
				throw e;
			} catch (final ClassNotFoundException e) {
				log.warn(Reporter.getDefaultConsolePrefix() + "Failed to initialize an SQLite database!");
				throw e;
			} catch (final SQLException e) {
				log.warn(Reporter.getDefaultConsolePrefix() + "Failed to initialize an SQLite database!");
				throw e;
			} catch (final InterruptedException e) {
				log.warn(Reporter.getDefaultConsolePrefix() + "Failed to initialize an SQLite database!");
				throw e;
			}
		}

		return databaseHandler;
	}

	/**
	 * Creates the Reporter database tables, if they do not exist.
	 *
	 * @param database The {@link Database} to create the tables in.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	private static void initDatabaseTables(final Database database) throws ClassNotFoundException, SQLException, InterruptedException {
		log.info(Reporter.getDefaultConsolePrefix() + "Checking " + database.getDatabaseType() + " tables...");

		final String databaseVersion = Reporter.getDatabaseVersion();
		new ReportTableInitializer(database, databaseVersion).initialize();
		new ModStatsTableInitializer(database, databaseVersion).initialize();
		new PlayerStatsTableTableInitializer(database, databaseVersion).initialize();
	}

	private static void checkConnection(final Database database) throws InterruptedException, SQLException, ClassNotFoundException {
		Integer connectionId = null;
		try {
			connectionId = database.openPooledConnection();
			database.checkTable(connectionId, "Reports");
		} catch (final InterruptedException e) {
			log.warn("Failed to check connection!");
			throw e;
		} catch (final SQLException e) {
			log.warn("Failed to check connection!");
			throw e;
		} catch (final ClassNotFoundException e) {
			log.warn("Failed to check connection!");
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}
	}
}
