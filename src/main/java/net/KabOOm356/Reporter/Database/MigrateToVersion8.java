package net.KabOOm356.Reporter.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Reporter.Reporter;

/**
 * A class to help migrate the database tables to version 8, if they need to be.
 */
public class MigrateToVersion8
{
	protected static boolean migrateToVersion8(Database database)
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
			Reporter.getLog().severe(Reporter.getDefaultConsolePrefix() + "An error occured while upgrading database data to version 8!");
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
		database.updateQuery("DROP TABLE IF EXISTS Version8Temporary");
	}

	private static void createTemporaryTable(Database database) throws ClassNotFoundException, SQLException
	{
		String query = "CREATE TABLE IF NOT EXISTS Version8Temporary (" +
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
		
		database.updateQuery("INSERT INTO Version8Temporary " +
				"SELECT * FROM Reports");
	}

	protected static boolean needsMigration(Database database) throws ClassNotFoundException, SQLException
	{
		if(database.checkTable("Reports"))
		{
			ArrayList<String> cols = null;
			
			cols = database.getColumns("Reports");
			
			if(!cols.contains("SenderUUID") &&
					!cols.contains("ReportedUUID") &&
					!cols.contains("ClaimedByUUID") &&
					!cols.contains("CompletedByUUID") &&
					cols.contains("SenderRaw") &&
					cols.contains("ReportedRaw") &&
					cols.contains("ClaimedByRaw") &&
					cols.contains("CompletedByRaw"))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private static void migrateTable(Database database) throws ClassNotFoundException, SQLException
	{
		String query = "INSERT INTO Reports SELECT * FROM Version8Temporary";
		
		database.updateQuery(query);
		
		query = "SELECT * FROM Version8Temporary";
		
		ResultSet rs = null;
		SQLResultSet resultSet = new SQLResultSet();
		
		try
		{
			rs = database.query(query);
			
			resultSet.set(rs);
		}
		finally
		{
			if(rs != null)
			{
				rs.close();
			}
		}
		
		query = "UPDATE Reports "
				+ "SET SenderUUID=?, Sender=?, ReportedUUID=?, Reported=?, ClaimedByUUID=?, ClaimedBy=?, CompletedByUUID=?, CompletedBy=? "
				+ "WHERE ID=?";
		
		PreparedStatement statement = null;
		
		HashMap<String, UUID> players = new HashMap<String, UUID>();
		
		try
		{
			statement = database.prepareStatement(query);
			
			for(ResultRow row : resultSet)
			{
				int id = row.getInt("ID");
				String senderName = row.getString("SenderRaw");
				String reportedName = row.getString("ReportedRaw");
				String claimedBy = row.getString("ClaimedByRaw");
				String completedBy = row.getString("CompletedByRaw");
				
				UUID uuid = null;
				
				if(!senderName.equals("CONSOLE"))
				{
					uuid = getPlayerUUID(senderName, players);
					
					statement.setString(1, uuid.toString());
				}
				else
				{
					statement.setString(1, "");
				}
				
				statement.setString(2, senderName);
				
				if(!reportedName.equalsIgnoreCase("* (Anonymous)"))
				{
					uuid = getPlayerUUID(reportedName, players);
					
					statement.setString(3, uuid.toString());
				}
				else
				{
					statement.setString(3, "");
				}
				
				statement.setString(4, reportedName);
				
				if(!claimedBy.isEmpty() && !claimedBy.equals("CONSOLE"))
				{
					uuid = getPlayerUUID(claimedBy, players);
					
					statement.setString(5, uuid.toString());
				}
				else
				{
					statement.setString(5, "");
				}
				
				statement.setString(6, claimedBy);
				
				if(!completedBy.isEmpty() && !completedBy.equals("CONSOLE"))
				{
					uuid = getPlayerUUID(completedBy, players);
					
					statement.setString(7, uuid.toString());
				}
				else
				{
					statement.setString(7, "");
				}
				
				statement.setString(8, completedBy);
				
				statement.setInt(9, id);
				
				statement.executeUpdate();
				statement.clearParameters();
			}
		}
		finally
		{
			statement.close();
		}
	}
	
	private static UUID getPlayerUUID(String playerName, HashMap<String, UUID> players)
	{
		UUID uuid = null;
		
		if(players.containsKey(playerName))
		{
			uuid = players.get(playerName);
		}
		else
		{
			OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(playerName);
			
			uuid = player.getUniqueId();
			
			players.put(playerName, uuid);
		}
		
		return uuid;
	}
}
