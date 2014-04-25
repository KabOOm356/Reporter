package net.KabOOm356.Reporter.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
				addColumns(database);
				
				migrateTable(database);
				
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
	
	protected static void addColumns(Database database) throws ClassNotFoundException, SQLException
	{
		ArrayList<String> cols = database.getColumns("Reports");
		
		Statement statement = database.createStatement();
		
		if(!cols.contains("SenderUUID"))
			statement.addBatch("ALTER TABLE Reports ADD SenderUUID VARCHAR(36)");
		if(!cols.contains("ReportedUUID"))
			statement.addBatch("ALTER TABLE Reports ADD ReportedUUID VARCHAR(36)");
		if(!cols.contains("ClaimedByUUID"))
			statement.addBatch("ALTER TABLE Reports ADD ClaimedByUUID VARCHAR(36)");
		if(!cols.contains("CompletedByUUID"))
			statement.addBatch("ALTER TABLE Reports ADD CompletedByUUID VARCHAR(36)");
		
		statement.executeBatch();
		
		statement.close();
	}

	protected static boolean needsMigration(Database database) throws ClassNotFoundException, SQLException
	{
		if(database.checkTable("Reports"))
		{
			ArrayList<String> cols = null;
			
			cols = database.getColumns("Reports");
			
			if(!cols.contains("SenderUUID") && !cols.contains("ReportedUUID")
					&& !cols.contains("ClaimedByUUID") && !cols.contains("CompletedByUUID"))
				return true;
		}
		
		return false;
	}
	
	private static void migrateTable(Database database) throws ClassNotFoundException, SQLException
	{
		String query = "SELECT ID, SenderRaw, ReportedRaw, ClaimedByRaw, CompletedByRaw FROM Reports";
		
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
				+ "SET SenderUUID=?, ReportedUUID=?, ClaimedByUUID=?, CompletedByUUID=? "
				+ "WHERE ID=?";
		
		PreparedStatement statement = null;
		
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
				
				OfflinePlayer player = null;
				UUID uuid = null;
				
				if(!senderName.equals("CONSOLE"))
				{
					player = Bukkit.getServer().getOfflinePlayer(senderName);
					
					uuid = player.getUniqueId();
					
					statement.setString(1, uuid.toString());
				}
				else
				{
					statement.setString(1, "");
				}
				
				if(!reportedName.equalsIgnoreCase("* (Anonymous)"))
				{
					player = Bukkit.getServer().getOfflinePlayer(reportedName);
					
					uuid = player.getUniqueId();
					
					statement.setString(2, uuid.toString());
				}
				else
				{
					statement.setString(2, "");
				}
				
				if(!claimedBy.isEmpty() && !claimedBy.equals("CONSOLE"))
				{
					player = Bukkit.getServer().getOfflinePlayer(claimedBy);
					
					uuid = player.getUniqueId();
					
					statement.setString(3, uuid.toString());
				}
				else
				{
					statement.setString(3, "");
				}
				
				if(!completedBy.isEmpty() && !completedBy.equals("CONSOLE"))
				{
					player = Bukkit.getServer().getOfflinePlayer(completedBy);
					
					uuid = player.getUniqueId();
					
					statement.setString(4, uuid.toString());
				}
				else
				{
					statement.setString(4, "");
				}
				
				statement.setInt(5, id);
				
				statement.executeUpdate();
				statement.clearParameters();
			}
		}
		finally
		{
			statement.close();
		}
	}
}
