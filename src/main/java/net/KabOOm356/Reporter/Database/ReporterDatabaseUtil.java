package net.KabOOm356.Reporter.Database;

import net.KabOOm356.Database.Connection.ConnectionPoolConfig;
import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Reporter.Database.Migrator.MigrateToVersion7;
import net.KabOOm356.Reporter.Database.Migrator.MigrateToVersion8;
import net.KabOOm356.Reporter.Database.Migrator.MigrateToVersion9;
import net.KabOOm356.Reporter.Reporter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * A class to help initialize and update Reporter's database.
 */
public class ReporterDatabaseUtil
{
	private static final Logger log = LogManager.getLogger(ReporterDatabaseUtil.class);

	/**
	 * Initializes the database.
	 * 
	 * @param configuration The {@link FileConfiguration} that will be used to get the database options.
	 * @param dataFolder A {@link File} to the directory where the data should be stored.
	 * 
	 * @return An initialized {@link ExtendedDatabaseHandler}.
	 *
	 * @throws IllegalArgumentException Thrown if the connection pool configuration is invalid.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public static ExtendedDatabaseHandler initDB(FileConfiguration configuration, File dataFolder) throws IllegalArgumentException, IOException, ClassNotFoundException, SQLException, InterruptedException
	{
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
		if(configuration.getString("database.type", DatabaseType.SQLITE.toString()).equalsIgnoreCase(DatabaseType.MYSQL.toString()))
		{
			try
			{
				log.log(Level.INFO, Reporter.getDefaultConsolePrefix() + "Connecting to MySQL server...");
				
				String host = configuration.getString("database.host", "localhost:3306");
				String database = configuration.getString("database.database", "Reporter");
				String username = configuration.getString("database.username", "root");
				String password = configuration.getString("database.password", "root");
				
				databaseHandler = new ExtendedDatabaseHandler(host, database, username, password, connectionPoolConfig);
				
				databaseHandler.openConnection();
				
				databaseHandler.checkTable("reports");
				
				initDatabaseTables(databaseHandler.getDatabase());
			}
			catch(final Exception e)
			{
				databaseHandler = null;
				fallbackToNextDB = true;
				log.log(Level.ERROR, Reporter.getDefaultConsolePrefix() + "Error connecting to MySQL server using SQLite.", e);
			}
			finally
			{
				databaseHandler.closeConnection();
			}
		}
		else
			fallbackToNextDB = true;
		
		// Attempt to initialize a SQLite database
		if(fallbackToNextDB)
		{
			String databaseName = configuration.getString("database.dbName", "reports.db");

			try {
				databaseHandler = new ExtendedDatabaseHandler(DatabaseType.SQLITE, dataFolder.getPath(), databaseName, connectionPoolConfig);
				initDatabaseTables(databaseHandler.getDatabase());
			} catch (final IOException e) {
				log.warn(Reporter.getDefaultConsolePrefix() + "Failed to initialize an SQLite database!");
				throw e;
			} catch (ClassNotFoundException e) {
				log.warn(Reporter.getDefaultConsolePrefix() + "Failed to initialize an SQLite database!");
				throw e;
			} catch (SQLException e) {
				log.warn(Reporter.getDefaultConsolePrefix() + "Failed to initialize an SQLite database!");
				throw e;
			} catch (InterruptedException e) {
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
	 * 
	 * @throws SQLException 
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	private static void initDatabaseTables(Database database) throws ClassNotFoundException, SQLException, InterruptedException
	{
		log.info(Reporter.getDefaultConsolePrefix() + "Checking " + database.getDatabaseType() + " tables...");
		
		try
		{
			if (needsToCreateTables(database))
			{	
				log.info(Reporter.getDefaultConsolePrefix() + "Creating " + database.getDatabaseType() + " tables...");

				createTables(database);
			}
			else
				log.info(Reporter.getDefaultConsolePrefix() + "Using existing " + database.getDatabaseType() + " tables.");
			
			if(migrateData(database) || updateTables(database))
			{
				log.info(Reporter.getDefaultConsolePrefix() +
						"The " + database.getDatabaseType() + " tables have been updated to version " + Reporter.getDatabaseVersion() + ".");
			}
		}
		finally
		{
			database.closeConnection();
		}
	}
	
	private static boolean migrateData(Database database)
	{
		boolean migrated = false;

		migrated = new MigrateToVersion7(database).migrate();
		migrated = migrated || new MigrateToVersion8(database).migrate();
		migrated = migrated || new MigrateToVersion9(database).migrate();
		
		return migrated;
	}

	/**
	 * Creates the tables in the given {@link Database}.
	 * 
	 * @param database The {@link Database} to create the tables in.
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	protected static void createTables(Database database) throws ClassNotFoundException, SQLException, InterruptedException
	{
		String primaryKey = "ID INTEGER PRIMARY KEY";
		
		// If the database is using MySQL, set the primary key to auto increment.
		// SQLite does this automatically for primary keys.
		if (database.getDatabaseType() == DatabaseType.MYSQL) {
			primaryKey += " AUTO_INCREMENT, ";
		} else {
			primaryKey += ", ";
		}

		createReportsTable(database);
		createModStatsTable(database, primaryKey);
		createPlayerStatsTable(database, primaryKey);
	}

	private static void createPlayerStatsTable(final Database database, final String primaryKey) throws SQLException, ClassNotFoundException, InterruptedException {
		final StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE IF NOT EXISTS PlayerStats (")
				.append(primaryKey)
				.append("Name VARCHAR(16) NOT NULL, ")
				.append("UUID VARCHAR(36) NOT NULL, ")
				.append("FirstReportDate VARCHAR(19) NOT NULL DEFAULT '', ")
				.append("LastReportDate VARCHAR(19) NOT NULL DEFAULT '', ")
				.append("ReportCount INTEGER NOT NULL DEFAULT '0', ")
				.append("FirstReportedDate VARCHAR(19) NOT NULL DEFAULT '', ")
				.append("LastReportedDate VARCHAR(19) NOT NULL DEFAULT '', ")
				.append("ReportedCount INTEGER NOT NULL DEFAULT '0');");

		database.updateQuery(query.toString());
	}

	private static void createReportsTable(final Database database) throws SQLException, ClassNotFoundException, InterruptedException {
		final StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE IF NOT EXISTS Reports (")
				.append("ID INTEGER PRIMARY KEY, ")
				.append("Date CHAR(19) NOT NULL DEFAULT 'N/A', ")
				.append("SenderUUID CHAR(36) DEFAULT '', ")
				.append("Sender VARCHAR(32), ")
				.append("ReportedUUID CHAR(36) DEFAULT '', ")
				.append("Reported VARCHAR(32) NOT NULL DEFAULT '* (Anonymous)', ")
				.append("Details VARCHAR(200) NOT NULL, ")
				.append("Priority TINYINT NOT NULL DEFAULT '0', ")
				.append("SenderWorld VARCHAR(100) DEFAULT '', ")
				.append("SenderX DOUBLE NOT NULL DEFAULT '0.0', ")
				.append("SenderY DOUBLE NOT NULL DEFAULT '0.0', ")
				.append("SenderZ DOUBLE NOT NULL DEFAULT '0.0', ")
				.append("ReportedWorld VARCHAR(100) DEFAULT '', ")
				.append("ReportedX DOUBLE DEFAULT '0.0', ")
				.append("ReportedY DOUBLE DEFAULT '0.0', ")
				.append("ReportedZ DOUBLE DEFAULT '0.0', ")
				.append("CompletionStatus BOOLEAN NOT NULL DEFAULT '0', ")
				.append("CompletedByUUID CHAR(36) DEFAULT '', ")
				.append("CompletedBy VARCHAR(32) DEFAULT '', ")
				.append("CompletionDate CHAR(19) DEFAULT '', ")
				.append("CompletionSummary VARCHAR(200) DEFAULT '', ")
				.append("ClaimStatus BOOLEAN NOT NULL DEFAULT '0', ")
				.append("ClaimDate CHAR(19) DEFAULT '', ")
				.append("ClaimedByUUID CHAR(36) DEFAULT '', ")
				.append("ClaimedBy VARCHAR(32) DEFAULT '', ")
				.append("ClaimPriority TINYINT DEFAULT '0');");

		database.updateQuery(query.toString());
	}

	private static void createModStatsTable(final Database database, final String primaryKey) throws SQLException, ClassNotFoundException, InterruptedException {
		final StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE IF NOT EXISTS ModStats (")
				.append(primaryKey)
				.append("ModName VARCHAR(16) NOT NULL, ")
				.append("ModUUID VARCHAR(36) NOT NULL, ")
				.append("AssignCount INTEGER NOT NULL DEFAULT '0', ")
				.append("ClaimedCount INTEGER NOT NULL DEFAULT '0', ")
				.append("CompletionCount INTEGER NOT NULL DEFAULT '0', ")
				.append("DeletionCount INTEGER NOT NULL DEFAULT '0', ")
				.append("MoveCount INTEGER NOT NULL DEFAULT '0', ")
				.append("RespondCount INTEGER NOT NULL DEFAULT '0', ")
				.append("UnassignCount INTEGER NOT NULL DEFAULT '0', ")
				.append("UnclaimCount INTEGER NOT NULL DEFAULT '0');");

		database.updateQuery(query.toString());
	}

	/**
	 * Checks if the tables need to be created in the {@link Database}.
	 * 
	 * @param database The {@link Database} to check if the tables exist in.
	 * 
	 * @return True if the tables do not exist, otherwise false.
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public static boolean needsToCreateTables(Database database) throws ClassNotFoundException, SQLException, InterruptedException
	{
		boolean createReportsTable = !database.checkTable("Reports");
		boolean createModStatsTable = !database.checkTable("ModStats");
		boolean createPlayerStatsTable = !database.checkTable("PlayerStats");
		
		return createReportsTable || createModStatsTable || createPlayerStatsTable;
	}
	
	/**
	 * Updates the Reporter database tables, if they need to be.
	 * 
	 * @param database The {@link Database} where the tables to update are.
	 * 
	 * @return True if one or more of the tables was updated, otherwise false.
	 */
	public static boolean updateTables(Database database)
	{
		boolean updated = false;
		
		try
		{
			updated = updateReportsTable(database);
			updated = updated || updateModStatsTable(database);
			updated = updated || updatePlayerStatsTable(database);
		}
		catch(final Exception e)
		{
			log.error("Error updating Reporter tables!", e);
		}
		finally
		{
			database.closeConnection();
		}
		
		return updated;
	}
	
	/**
	 * Updates/repairs the Reports table in the database.
	 * 
	 * @param database The database.
	 * 
	 * @return True if the table was updated/repaired, otherwise false.
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public static boolean updateReportsTable(Database database) throws ClassNotFoundException, SQLException, InterruptedException
	{
		Statement statement = null;
		boolean updated = false;
		
		try
		{
			ArrayList<String> cols = database.getColumnNames("Reports");
			
			statement = database.createStatement();
			
			// Version 1 (Initial Version)
			if(!cols.contains("ID"))
			{
				statement.addBatch("ALTER TABLE Reports ADD ID INTEGER PRIMARY KEY");
				updated = true;
			}
			if(!cols.contains("Sender"))
			{
				statement.addBatch("ALTER TABLE Reports ADD Sender VARCHAR(32)");
				updated = true;
			}
			if(!cols.contains("Reported"))
			{
				statement.addBatch("ALTER TABLE Reports ADD Reported VARCHAR(32) NOT NULL DEFAULT '* (Anonymous)'");
				updated = true;
			}
			if(!cols.contains("Details"))
			{
				statement.addBatch("ALTER TABLE Reports ADD Details VARCHAR(200) NOT NULL");
				updated = true;
			}
			if(!cols.contains("Date"))
			{
				statement.addBatch("ALTER TABLE Reports ADD Date CHAR(19) NOT NULL DEFAULT 'N/A'");
				updated = true;
			}
			
			// Version 2 (Location Update)
			if(!cols.contains("SenderX"))
			{
				statement.addBatch("ALTER TABLE Reports ADD SenderX DOUBLE NOT NULL DEFAULT '0.0'");
				updated = true;
			}
			if(!cols.contains("SenderY"))
			{
				statement.addBatch("ALTER TABLE Reports ADD SenderY DOUBLE NOT NULL DEFAULT '0.0'");
				updated = true;
			}
			if(!cols.contains("SenderZ"))
			{
				statement.addBatch("ALTER TABLE Reports ADD SenderZ DOUBLE NOT NULL DEFAULT '0.0'");
				updated = true;
			}
			if(!cols.contains("ReportedX"))
			{
				statement.addBatch("ALTER TABLE Reports ADD ReportedX DOUBLE DEFAULT '0.0'");
				updated = true;
			}
			if(!cols.contains("ReportedY"))
			{
				statement.addBatch("ALTER TABLE Reports ADD ReportedX DOUBLE DEFAULT '0.0'");
				updated = true;
			}
			if(!cols.contains("ReportedZ"))
			{
				statement.addBatch("ALTER TABLE Reports ADD ReportedX DOUBLE DEFAULT '0.0'");
				updated = true;
			}
			
			// Version 3 (World Update)
			if(!cols.contains("SenderWorld"))
			{
				statement.addBatch("ALTER TABLE Reports ADD SenderWorld VARCHAR(100) DEFALUT ''");
				updated = true;
			}
			if(!cols.contains("ReportedWorld"))
			{
				statement.addBatch("ALTER TABLE Reports ADD ReportedWorld VARCHAR(100) DEFAULT ''");
				updated = true;
			}
			
			// Version 4 (Completion Update)
			if(!cols.contains("CompletionStatus"))
			{
				statement.addBatch("ALTER TABLE Reports ADD CompletionStatus BOOLEAN DEFALUT '0'");
				updated = true;
			}
			if(!cols.contains("CompletedBy"))
			{
				statement.addBatch("ALTER TABLE Reports ADD CompletedBy VARCHAR(32) DEFAULT ''");
				updated = true;
			}
			if(!cols.contains("CompletionDate"))
			{
				statement.addBatch("ALTER TABLE Reports ADD CompletionDate CHAR(19) DEFAULT ''");
				updated = true;
			}
			if(!cols.contains("CompletionSummary"))
			{
				statement.addBatch("ALTER TABLE Reports ADD CompletionSummary VARCHAR(200) DEFAULT ''");
				updated = true;
			}
			
			// Version 5 (Request Update)
			// Removed in favor of the UUID player lookup.
			
			// Version 6 (Viewing Update)
			// Removed in favor of the UUID player lookup.
			
			// Version 7 (Claiming Update)
			if(!cols.contains("Priority"))
			{
				statement.addBatch("ALTER TABLE Reports ADD Priority TINYINT NOT NULL DEFAULT '0'");
				updated = true;
			}
			if(!cols.contains("ClaimStatus"))
			{
				statement.addBatch("ALTER TABLE Reports ADD ClaimStatus BOOLEAN NOT NULL DEFAULT '0'");
				updated = true;
			}
			if(!cols.contains("ClaimDate"))
			{
				statement.addBatch("ALTER TABLE Reports ADD ClaimDate CHAR(19)");
				updated = true;
			}
			if(!cols.contains("ClaimedBy"))
			{
				statement.addBatch("ALTER TABLE Reports ADD ClaimedBy VARCHAR(32)");
				updated = true;
			}
			if(!cols.contains("ClaimPriority"))
			{
				statement.addBatch("ALTER TABLE Reports ADD ClaimPriority TINYINT");
				updated = true;
			}
			
			// Version 8 (UUID Update)
			if(!cols.contains("SenderUUID"))
			{
				statement.addBatch("ALTER TABLE Reports ADD SenderUUID CHAR(36) DEFAULT ''");
				updated = true;
			}
			if(!cols.contains("ReportedUUID"))
			{
				statement.addBatch("ALTER TABLE Reports ADD ReportedUUID CHAR(36) DEFAULT ''");
				updated = true;
			}
			if(!cols.contains("CompletedByUUID"))
			{
				statement.addBatch("ALTER TABLE Reports ADD CompletedByUUID CHAR(36) DEFAULT ''");
				updated = true;
			}
			if(!cols.contains("ClaimedByUUID"))
			{
				statement.addBatch("ALTER TABLE Reports ADD ClaimedByUUID CHAR(36) DEFAULT ''");
				updated = true;
			}
			
			if(updated)
			{
				statement.executeBatch();
			}
		}
		finally
		{
			if(statement != null)
			{
				statement.close();
			}
		}
		
		return updated;
	}
	
	/**
	 * Updates/repairs the ModStats table in the database.
	 * 
	 * @param database The database.
	 * 
	 * @return True if the table was updated/repaired, otherwise false.
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public static boolean updateModStatsTable(Database database) throws ClassNotFoundException, SQLException, InterruptedException
	{
		Statement statement = null;
		boolean updated = false;
		
		try
		{
			ArrayList<String> cols = database.getColumnNames("ModStats");
			
			statement = database.createStatement();
			
			// Version 10 (Initial Table Version)
			if(!cols.contains("ID"))
			{
				String query = "ALTER TABLE ModStats ADD ID INTEGER PRIMARY KEY";
				
				// If the database is using MySQL, set the primary key to auto increment.
				// SQLite does this automatically for primary keys.
				if(database.getDatabaseType() == DatabaseType.MYSQL)
				{
					query += " AUTO_INCREMENT";
				}
				
				statement.addBatch(query);
				updated = true;
			}
			if(!cols.contains("ModName"))
			{
				statement.addBatch("ALTER TABLE ModStats ADD ModName VARCHAR(16) NOT NULL");
				updated = true;
			}
			if(!cols.contains("ModUUID"))
			{
				statement.addBatch("ALTER TABLE ModStats ADD ModUUID VARCHAR(36) NOT NULL");
				updated = true;
			}
			if(!cols.contains("AssignCount"))
			{
				statement.addBatch("ALTER TABLE ModStats ADD AssignCount INTEGER NOT NULL DEFAULT '0'");
				updated = true;
			}
			if(!cols.contains("ClaimedCount"))
			{
				statement.addBatch("ALTER TABLE ModStats ADD ClaimedCount INTEGER NOT NULL DEFAULT '0'");
				updated = true;
			}
			if(!cols.contains("CompletionCount"))
			{
				statement.addBatch("ALTER TABLE ModStats ADD CompletionCount INTEGER NOT NULL DEFAULT '0'");
				updated = true;
			}
			if(!cols.contains("DeletionCount"))
			{
				statement.addBatch("ALTER TABLE ModStats ADD DeletionCount INTEGER NOT NULL DEFAULT '0'");
				updated = true;
			}
			if(!cols.contains("MoveCount"))
			{
				statement.addBatch("ALTER TABLE ModStats ADD MoveCount INTEGER NOT NULL DEFAULT '0'");
				updated = true;
			}
			if(!cols.contains("RespondCount"))
			{
				statement.addBatch("ALTER TABLE ModStats ADD RespondCount INTEGER NOT NULL DEFAULT '0'");
				updated = true;
			}
			if(!cols.contains("UnassignCount"))
			{
				statement.addBatch("ALTER TABLE ModStats ADD UnassignCount INTEGER NOT NULL DEFAULT '0'");
				updated = true;
			}
			if(!cols.contains("UnclaimCount"))
			{
				statement.addBatch("ALTER TABLE ModStats ADD UnclaimCount INTEGER NOT NULL DEFAULT '0'");
				updated = true;
			}
			
			if(updated)
			{
				statement.executeBatch();
			}
		}
		finally
		{
			if(statement != null)
			{
				statement.close();
			}
		}
		
		return updated;
	}
	
	/**
	 * Updates/repairs the PlayerStats table in the database.
	 * 
	 * @param database The database.
	 * 
	 * @return True if the table was updated/repaired, otherwise false.
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public static boolean updatePlayerStatsTable(Database database) throws ClassNotFoundException, SQLException, InterruptedException
	{
		Statement statement = null;
		boolean updated = false;
		
		try
		{
			ArrayList<String> cols = database.getColumnNames("PlayerStats");
			
			statement = database.createStatement();
			
			// Version 10 (Initial Table Version)
			if(!cols.contains("ID"))
			{
				String query = "ALTER TABLE PlayerStats ADD ID INTEGER PRIMARY KEY";
				
				// If the database is using MySQL, set the primary key to auto increment.
				// SQLite does this automatically for primary keys.
				if(database.getDatabaseType() == DatabaseType.MYSQL)
				{
					query += " AUTO_INCREMENT";
				}
				
				statement.addBatch(query);
				updated = true;
			}
			if(!cols.contains("Name"))
			{
				statement.addBatch("ALTER TABLE PlayerStats ADD Name VARCHAR(16) NOT NULL");
				updated = true;
			}
			if(!cols.contains("UUID"))
			{
				statement.addBatch("ALTER TABLE PlayerStats ADD UUID VARCHAR(36) NOT NULL");
				updated = true;
			}
			if(!cols.contains("FirstReportDate"))
			{
				statement.addBatch("ALTER TABLE PlayerStats ADD FirstReportDate VARCHAR(19) NOT NULL DEFAULT ''");
				updated = true;
			}
			if(!cols.contains("LastReportDate"))
			{
				statement.addBatch("ALTER TABLE PlayerStats ADD LastReportDate VARCHAR(19) NOT NULL DEFAULT ''");
				updated = true;
			}
			if(!cols.contains("ReportCount"))
			{
				statement.addBatch("ALTER TABLE PlayerStats ADD ReportCount INTEGER NOT NULL DEFAULT '0'");
				updated = true;
			}
			if(!cols.contains("FirstReportedDate"))
			{
				statement.addBatch("ALTER TABLE PlayerStats ADD FirstReportedDate VARCHAR(19) NOT NULL DEFAULT ''");
				updated = true;
			}
			if(!cols.contains("LastReportedDate"))
			{
				statement.addBatch("ALTER TABLE PlayerStats ADD LastReportedDate VARCHAR(19) NOT NULL DEFAULT ''");
				updated = true;
			}
			if(!cols.contains("ReportedCount"))
			{
				statement.addBatch("ALTER TABLE PlayerStats ADD ReportedCount INTEGER NOT NULL DEFAULT '0'");
				updated = true;
			}
			
			if(updated)
			{
				statement.executeBatch();
			}
		}
		finally
		{
			if(statement != null)
			{
				statement.close();
			}
		}
		
		return updated;
	}
}
