package net.KabOOm356.Reporter.Database;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Reporter.Reporter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.file.FileConfiguration;

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
	 */
	public static ExtendedDatabaseHandler initDB(FileConfiguration configuration, File dataFolder)
	{
		ExtendedDatabaseHandler databaseHandler = null;
		
		/* If an error occurs attempting to initialize a database
		 * the plugin will attempt to use the next less "complicated" database
		 * 
		 * Attempt order:
		 * MySQL -> SQLite -> Flatfile (When completed)
		 */
		boolean fallbackToNextDB = false;
		
		// Attempt to initialize a MySQL database
		if(configuration.getString("database.type", "sqlite").equalsIgnoreCase("mysql"))
		{
			try
			{
				log.log(Level.INFO, Reporter.getDefaultConsolePrefix() + "Connecting to MySQL server...");
				
				String host = configuration.getString("database.host", "localhost:3306");
				String database = configuration.getString("database.database", "Reporter");
				String username = configuration.getString("database.username", "root");
				String password = configuration.getString("database.password", "root");
				
				databaseHandler = new ExtendedDatabaseHandler(host, database, username, password);
				
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
			try
			{
				String databaseName = configuration.getString("database.dbName", "reports.db");
				
				databaseHandler = new ExtendedDatabaseHandler(DatabaseType.SQLITE, dataFolder.getPath(), databaseName);
				
				initDatabaseTables(databaseHandler.getDatabase());
			}
			catch(final Exception e)
			{
				databaseHandler = null;
				log.fatal(Reporter.getDefaultConsolePrefix() + "A severe error occurred connecting to the database file!", e);
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
	 */
	private static void initDatabaseTables(Database database) throws ClassNotFoundException, SQLException
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
		
		migrated = MigrateToVersion7.migrateToVersion7(database);
		migrated = migrated || MigrateToVersion8.migrateToVersion8(database);
		migrated = migrated || MigrateToVersion9.migrateToVersion9(database);
		
		return migrated;
	}

	/**
	 * Creates the tables in the given {@link Database}.
	 * 
	 * @param database The {@link Database} to create the tables in.
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	protected static void createTables(Database database) throws ClassNotFoundException, SQLException
	{
		String query = "CREATE TABLE IF NOT EXISTS Reports (" +
				"ID INTEGER PRIMARY KEY, " +
				"Date CHAR(19) NOT NULL DEFAULT 'N/A', " +
				"SenderUUID CHAR(36) DEFAULT '', " +
				"Sender VARCHAR(32), " +
				"ReportedUUID CHAR(36) DEFAULT '', " +
				"Reported VARCHAR(32) NOT NULL DEFAULT '* (Anonymous)', " +
				"Details VARCHAR(200) NOT NULL, " +
				"Priority TINYINT NOT NULL DEFAULT '0', " +
				"SenderWorld VARCHAR(100) DEFAULT '', " +
				"SenderX DOUBLE NOT NULL DEFAULT '0.0', " +
				"SenderY DOUBLE NOT NULL DEFAULT '0.0', " +
				"SenderZ DOUBLE NOT NULL DEFAULT '0.0', " +
				"ReportedWorld VARCHAR(100) DEFAULT '', " +
				"ReportedX DOUBLE DEFAULT '0.0', " +
				"ReportedY DOUBLE DEFAULT '0.0', " +
				"ReportedZ DOUBLE DEFAULT '0.0', " +
				"CompletionStatus BOOLEAN NOT NULL DEFAULT '0', " +
				"CompletedByUUID CHAR(36) DEFAULT '', " +
				"CompletedBy VARCHAR(32) DEFAULT '', " +
				"CompletionDate CHAR(19) DEFAULT '', " +
				"CompletionSummary VARCHAR(200) DEFAULT '', " +
				"ClaimStatus BOOLEAN NOT NULL DEFAULT '0', " +
				"ClaimDate CHAR(19) DEFAULT '', " +
				"ClaimedByUUID CHAR(36) DEFAULT '', " +
				"ClaimedBy VARCHAR(32) DEFAULT '', " +
				"ClaimPriority TINYINT DEFAULT '0');";
		
		database.updateQuery(query);
		
		String primaryKey = "ID INTEGER PRIMARY KEY";
		
		// If the database is using MySQL, set the primary key to auto increment.
		// SQLite does this automatically for primary keys.
		if(database.getDatabaseType() == DatabaseType.MYSQL)
		{
			primaryKey += " AUTO_INCREMENT, ";
		}
		else
		{
			primaryKey += ", ";
		}
		
		query = "CREATE TABLE IF NOT EXISTS ModStats ("
				+ primaryKey
				+ "ModName VARCHAR(16) NOT NULL, "
				+ "ModUUID VARCHAR(36) NOT NULL, "
				+ "AssignCount INTEGER NOT NULL DEFAULT '0', "
				+ "ClaimedCount INTEGER NOT NULL DEFAULT '0', "
				+ "CompletionCount INTEGER NOT NULL DEFAULT '0', "
				+ "DeletionCount INTEGER NOT NULL DEFAULT '0', "
				+ "MoveCount INTEGER NOT NULL DEFAULT '0', "
				+ "RespondCount INTEGER NOT NULL DEFAULT '0', "
				+ "UnassignCount INTEGER NOT NULL DEFAULT '0', "
				+ "UnclaimCount INTEGER NOT NULL DEFAULT '0');";
		
		database.updateQuery(query);
		
		query = "CREATE TABLE IF NOT EXISTS PlayerStats ("
				+ primaryKey
				+ "Name VARCHAR(16) NOT NULL, "
				+ "UUID VARCHAR(36) NOT NULL, "
				+ "FirstReportDate VARCHAR(19) NOT NULL DEFAULT '', "
				+ "LastReportDate VARCHAR(19) NOT NULL DEFAULT '', "
				+ "ReportCount INTEGER NOT NULL DEFAULT '0', "
				+ "FirstReportedDate VARCHAR(19) NOT NULL DEFAULT '', "
				+ "LastReportedDate VARCHAR(19) NOT NULL DEFAULT '', "
				+ "ReportedCount INTEGER NOT NULL DEFAULT '0');";
		
		database.updateQuery(query);
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
	 */
	public static boolean needsToCreateTables(Database database) throws ClassNotFoundException, SQLException
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
	 */
	public static boolean updateReportsTable(Database database) throws ClassNotFoundException, SQLException
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
			
			// Version 9
			
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
	 */
	public static boolean updateModStatsTable(Database database) throws ClassNotFoundException, SQLException
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
	 */
	public static boolean updatePlayerStatsTable(Database database) throws ClassNotFoundException, SQLException
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
	
	/**
	 * Drops the reports, Reports, ModStats and PlayerStats tables, if they exist.
	 * 
	 * @param database The database to drop the tables from.
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	protected static void dropTables(Database database) throws ClassNotFoundException, SQLException
	{
		database.updateQuery("DROP TABLE IF EXISTS reports");
		database.updateQuery("DROP TABLE IF EXISTS Reports");
		database.updateQuery("DROP TABLE IF EXISTS ModStats");
		database.updateQuery("DROP TABLE IF EXISTS PlayerStats");
	}
}
