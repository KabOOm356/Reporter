package net.KabOOm356.Reporter.Database;

import java.sql.SQLException;
import java.util.ArrayList;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Reporter.Reporter;

/**
 * A class to help migrate the database tables to version 7, if they need to be.
 */
public class MigrateToVersion7
{
	protected static boolean migrateToVersion7(Database database)
	{
		boolean updated = false;
		
		try
		{
			if(needsMigration(database))
			{
				createTemporaryTable(database);
				
				database.updateQuery("DROP TABLE IF EXISTS reports");
				
				ReporterDatabaseUtil.createTables(database);
				
				migrateTable(database);
				
				deleteTemporaryTable(database);
				
				updated = true;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			Reporter.getLog().severe(Reporter.getDefaultConsolePrefix() + "An error occured while upgrading database data to version 7!");
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
		database.updateQuery("DROP TABLE IF EXISTS Version7Temporary");
	}

	private static void createTemporaryTable(Database database) throws ClassNotFoundException, SQLException
	{
		String query = "CREATE TABLE IF NOT EXISTS Version7Temporary (" +
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
				"CompletedBy VARCHAR(50), " +
				"CompletedByRaw VARCHAR(16), " +
				"CompletionDate VARCHAR(19), " +
				"CompletionSummary VARCHAR(200), " +
				"ClaimStatus BOOLEAN NOT NULL DEFAULT '0', " +
				"ClaimDate VARCHAR(19), " +
				"ClaimedBy VARCHAR(50), " +
				"ClaimedByRaw VARCHAR(16), " +
				"ClaimPriority TINYINT);";
		
		database.updateQuery(query);
		
		database.updateQuery("INSERT INTO Version7Temporary " +
				"(ID, Date, Sender, SenderRaw, Reported, ReportedRaw, Details, SenderWorld, SenderX, SenderY, SenderZ, ReportedWorld, ReportedX, ReportedY, ReportedZ, CompletionStatus, CompletedBy, CompletedByRaw, CompletionDate, CompletionSummary) " +
				"SELECT " +
				"id, date, sender, SenderRaw, reported, ReportedRaw, details, SenderWorld, SenderX, SenderY, SenderZ, ReportedWorld, ReportedX, ReportedY, ReportedZ, CompletionStatus, CompletedBy, CompletedBy, CompletionDate, CompletionSummary " +
				"FROM reports");
	}

	protected static boolean needsMigration(Database database) throws ClassNotFoundException, SQLException
	{
		if(database.checkTable("reports"))
		{
			ArrayList<String> cols = null;
			
			cols = database.getColumnNames("reports");
			
			if(!cols.contains("ClaimStatus") && 
					!cols.contains("ClaimDate") && 
					!cols.contains("ClaimedBy") && 
					!cols.contains("ClaimedByRaw") && 
					!cols.contains("ClaimedPriority"))
				return true;
		}
		
		return false;
	}
	
	private static void migrateTable(Database database) throws ClassNotFoundException, SQLException
	{
		database.updateQuery("INSERT INTO Reports " +
				"(ID, Date, Sender, SenderRaw, Reported, ReportedRaw, Details, SenderWorld, SenderX, SenderY, SenderZ, ReportedWorld, ReportedX, ReportedY, ReportedZ, CompletionStatus, CompletedBy, CompletedByRaw, CompletionDate, CompletionSummary) " +
				"SELECT " +
				"ID, Date, Sender, SenderRaw, Reported, ReportedRaw, Details, SenderWorld, SenderX, SenderY, SenderZ, ReportedWorld, ReportedX, ReportedY, ReportedZ, CompletionStatus, CompletedBy, CompletedByRaw, CompletionDate, CompletionSummary " +
				"FROM Version7Temporary");
	}
}
