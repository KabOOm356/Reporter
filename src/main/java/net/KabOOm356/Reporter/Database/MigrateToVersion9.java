package net.KabOOm356.Reporter.Database;


import java.sql.ResultSet;
import java.sql.SQLException;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Reporter.Reporter;

public class MigrateToVersion9
{
	protected static boolean migrateToVersion9(Database database)
	{
		boolean updated = false;
		
		try
		{
			if(needsMigration(database))
			{
				createTemporaryTable(database);
				
				database.updateQuery("DROP TABLE IF EXISTS Reports");
				
				ReporterDatabaseUtil.createTables(database);
				
				migrateTable(database);
				
				deleteTemporaryTable(database);
				
				updated = true;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			Reporter.getLog().severe(Reporter.getDefaultConsolePrefix() + "An error occured while upgrading database data to version 9!");
			Reporter.getLog().severe(Reporter.getDefaultConsolePrefix() + "If you receive more errors, you may have to delete your database!");
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
	
	private static void deleteTemporaryTable(Database database) throws ClassNotFoundException, SQLException
	{
		database.updateQuery("DROP TABLE IF EXISTS Version9Temporary");
	}

	private static void createTemporaryTable(Database database) throws ClassNotFoundException, SQLException
	{
		String query = "CREATE TABLE IF NOT EXISTS Version9Temporary (" +
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
		
		database.updateQuery("INSERT INTO Version9Temporary " +
				"SELECT * FROM Reports");
	}

	protected static boolean needsMigration(Database database) throws ClassNotFoundException, SQLException
	{
		if(database.checkTable("Reports"))
		{
			String columnName = "COLUMN_NAME";
			String columnSize = "COLUMN_SIZE";
			
			if(database.getDatabaseType() == DatabaseType.SQLITE)
				columnSize = "TYPE_NAME";
			
			SQLResultSet cols = new SQLResultSet();
			ResultSet rs = null;
			
			try
			{
				rs = database.getColumnMetaData("Reports");
				
				cols.set(rs);
			}
			finally
			{
				try
				{
					rs.close();
					database.closeConnection();
				}
				catch(SQLException e)
				{
				}
			}
			
			boolean sender = cols.get(columnName, "Sender").getString(columnSize).contains("32");
			boolean reported = cols.get(columnName, "Reported").getString(columnSize).contains("32");
			boolean claimedBy = cols.get(columnName, "ClaimedBy").getString(columnSize).contains("32");
			boolean completedBy = cols.get(columnName, "CompletedBy").getString(columnSize).contains("32");
			
			if(!sender && !reported && !claimedBy && !completedBy)
			{
				return true;
			}
		}
		
		return false;
	}
	
	private static void migrateTable(Database database) throws ClassNotFoundException, SQLException
	{
		String query = "INSERT INTO Reports SELECT * FROM Version9Temporary";
		
		database.updateQuery(query);
	}
}
