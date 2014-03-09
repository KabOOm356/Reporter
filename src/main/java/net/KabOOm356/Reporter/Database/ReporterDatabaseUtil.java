package net.KabOOm356.Reporter.Database;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Reporter.Reporter;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * A class to help initialize and update Reporter's database.
 */
public class ReporterDatabaseUtil
{
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
				Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Connecting to MySQL server...");
				
				String host = configuration.getString("database.host", "localhost:3306");
				String database = configuration.getString("database.database", "Reporter");
				String username = configuration.getString("database.username", "root");
				String password = configuration.getString("database.password", "root");
				
				databaseHandler = new ExtendedDatabaseHandler(host, database, username, password);
				
				databaseHandler.openConnection();
				
				databaseHandler.checkTable("reports");
				
				initDatabaseTables(databaseHandler.getDatabase());
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				databaseHandler = null;
				fallbackToNextDB = true;
				Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Error connecting to MySQL server using SQLite.");
			}
			finally
			{
				try
				{
					databaseHandler.closeConnection();
				}
				catch(Exception ex)
				{
				}
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
			catch(Exception ex)
			{
				ex.printStackTrace();
				
				databaseHandler = null;
				
				Reporter.getLog().severe(Reporter.getDefaultConsolePrefix() + "A severe error occurred connecting to the database file!");
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
		Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Checking " + database.getDatabaseType() + " tables...");
		
		try
		{
			if (needsToCreateTables(database))
			{	
				Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Creating " + database.getDatabaseType() + " tables...");

				createTables(database);
			}
			else
				Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Using existing " + database.getDatabaseType() + " tables.");
			
			if(migrateData(database) || updateTables(database))
				Reporter.getLog().info(Reporter.getDefaultConsolePrefix() +
						"The " + database.getDatabaseType() + " tables have been updated to version " + Reporter.getDatabaseVersion() + ".");
		}
		finally
		{
			try
			{
				database.closeConnection();
			}
			catch(Exception ex)
			{
			}
		}
	}
	
	private static boolean migrateData(Database database)
	{
		boolean migrated = false;
		
		migrated = MigrateToVersion7.migrateToVersion7(database);
		
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
				"Date VARCHAR(19) NOT NULL DEFAULT 'N/A', " +
				"Sender VARCHAR(50) NOT NULL, " +
				"SenderRaw VARCHAR(16) NOT NULL, " +
				"Reported VARCHAR(50) NOT NULL DEFAULT '* (Anonymous)', " +
				"ReportedRaw VARCHAR(16) NOT NULL DEFAULT '* (Anonymous)', " +
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
				"CompletedBy VARCHAR(50) DEFAULT '', " +
				"CompletedByRaw VARCHAR(16) DEFAULT '', " +
				"CompletionDate VARCHAR(19) DEFAULT '', " +
				"CompletionSummary VARCHAR(200) DEFAULT '', " +
				"ClaimStatus BOOLEAN NOT NULL DEFAULT '0', " +
				"ClaimDate VARCHAR(19) DEFAULT '', " +
				"ClaimedBy VARCHAR(50) DEFAULT '', " +
				"ClaimedByRaw VARCHAR(16) DEFAULT '', " +
				"ClaimPriority TINYINT DEFAULT '0');";
		
		database.updateQuery(query);
		
		query = "CREATE TABLE IF NOT EXISTS ModStats ("
				+ "ID INTEGER PRIMARY KEY, "
				+ "ModName VARCHAR(50) NOT NULL, "
				+ "ModNameRaw VARCHAR(16) NOT NULL, "
				+ "ModLevel TINYINT NOT NULL DEFAULT '0', "
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
				+ "ID INTEGER PRIMARY KEY, "
				+ "Name VARCHAR(50) NOT NULL, "
				+ "NameRaw VARCHAR(16) NOT NULL, "
				+ "FirstReportDate VARCHAR(19) NOT NULL, "
				+ "LastReportDate VARCHAR(19) NOT NULL, "
				+ "ReportCount INTEGER NOT NULL DEFAULT '0');";
		
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
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				database.closeConnection();
			}
			catch(Exception e)
			{
			}
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
			ArrayList<String> cols = database.getColumns("Reports");
			
			statement = database.createStatement();
			
			// Version 1 (Initial Version)
			if(!cols.contains("ID"))
			{
				statement.addBatch("ALTER TABLE Reports ADD ID INTEGER PRIMARY KEY");
				updated = true;
			}
			if(!cols.contains("Sender"))
			{
				statement.addBatch("ALTER TABLE Reports ADD Sender VARCHAR(50) NOT NULL");
				updated = true;
			}
			if(!cols.contains("Reported"))
			{
				statement.addBatch("ALTER TABLE Reports ADD Reported VARCHAR(50) NOT NULL DEFAULT '* (Anonymous)'");
				updated = true;
			}
			if(!cols.contains("Details"))
			{
				statement.addBatch("ALTER TABLE Reports ADD Details VARCHAR(200) NOT NULL");
				updated = true;
			}
			if(!cols.contains("Date"))
			{
				statement.addBatch("ALTER TABLE Reports ADD Date VARCHAR(19) NOT NULL DEFAULT 'N/A'");
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
				statement.addBatch("ALTER TABLE Reports ADD CompletedBy VARCHAR(50) DEFAULT ''");
				updated = true;
			}
			if(!cols.contains("CompletionDate"))
			{
				statement.addBatch("ALTER TABLE Reports ADD CompletionDate VARCHAR(19) DEFAULT ''");
				updated = true;
			}
			if(!cols.contains("CompletionSummary"))
			{
				statement.addBatch("ALTER TABLE Reports ADD CompletionSummary VARCHAR(200) DEFAULT ''");
				updated = true;
			}
			
			// Version 5 (Request Update)
			if(!cols.contains("ReportedRaw"))
			{
				statement.addBatch("ALTER TABLE Reports ADD ReportedRaw VARCHAR(19) NOT NULL DEFAULT '* (Anonymous)'");
				updated = true;
			}
			
			// Version 6 (Viewing Update)
			if(!cols.contains("SenderRaw"))
			{
				statement.addBatch("ALTER TABLE Reports ADD SenderRaw VARCHAR(50) NOT NULL DEFAULT ''");
				updated = true;
			}
			
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
				statement.addBatch("ALTER TABLE Reports ADD ClaimDate VARCHAR(19)");
				updated = true;
			}
			if(!cols.contains("ClaimedBy"))
			{
				statement.addBatch("ALTER TABLE Reports ADD ClaimedBy VARCHAR(50)");
				updated = true;
			}
			if(!cols.contains("ClaimedByRaw"))
			{
				statement.addBatch("ALTER TABLE Reports ADD ClaimedByRaw VARCHAR(16)");
				updated = true;
			}
			if(!cols.contains("ClaimPriority"))
			{
				statement.addBatch("ALTER TABLE Reports ADD ClaimPriority TINYINT");
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
	 */
	public static boolean updateModStatsTable(Database database) throws ClassNotFoundException, SQLException
	{
		Statement statement = null;
		boolean updated = false;
		
		try
		{
			ArrayList<String> cols = database.getColumns("ModStats");
			
			statement = database.createStatement();
			
			// Version 8 (Initial Table Version)
			if(!cols.contains("ID"))
			{
				statement.addBatch("ALTER TABLE ModStats ADD ID INTEGER PRIMARY KEY");
				updated = true;
			}
			if(!cols.contains("ModName"))
			{
				statement.addBatch("ALTER TABLE ModStats ADD ModName VARCHAR(50) NOT NULL");
				updated = true;
			}
			if(!cols.contains("ModNameRaw"))
			{
				statement.addBatch("ALTER TABLE ModStats ADD ModNameRaw VARCHAR(16) NOT NULL");
				updated = true;
			}
			if(!cols.contains("ModLevel"))
			{
				statement.addBatch("ALTER TABLE ModStats ADD ModLevel TINYINT NOT NULL DEFAULT '0'");
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
			ArrayList<String> cols = database.getColumns("PlayerStats");
			
			statement = database.createStatement();
			
			// Version 8 (Initial Table Version)
			if(!cols.contains("ID"))
			{
				statement.addBatch("ALTER TABLE PlayerStats ADD ID INTEGER PRIMARY KEY");
				updated = true;
			}
			if(!cols.contains("Name"))
			{
				statement.addBatch("ALTER TABLE PlayerStats ADD Name VARCHAR(50) NOT NULL");
				updated = true;
			}
			if(!cols.contains("NameRaw"))
			{
				statement.addBatch("ALTER TABLE PlayerStats ADD NameRaw VARCHAR(16) NOT NULL");
				updated = true;
			}
			if(!cols.contains("FirstReportDate"))
			{
				statement.addBatch("ALTER TABLE PlayerStats ADD FirstReportDate VARCHAR(19) NOT NULL");
				updated = true;
			}
			if(!cols.contains("LastReportDate"))
			{
				statement.addBatch("ALTER TABLE PlayerStats ADD LastReportDate VARCHAR(19) NOT NULL");
				updated = true;
			}
			if(!cols.contains("ReportCount"))
			{
				statement.addBatch("ALTER TABLE PlayerStats ADD ReportCount INTEGER NOT NULL DEFAULT '0'");
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
