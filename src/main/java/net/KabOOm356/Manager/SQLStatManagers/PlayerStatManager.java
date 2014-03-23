package net.KabOOm356.Manager.SQLStatManagers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.bukkit.OfflinePlayer;

import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Manager.SQLStatManager;
import net.KabOOm356.Reporter.Reporter;

/**
 * A class to manage setting player statistics held in an SQL database.
 */
public class PlayerStatManager extends SQLStatManager
{
	/**
	 * A class that represents a statistic for a player.
	 */
	public static class PlayerStat extends SQLStat
	{
		/**
		 * The number of times this player has been reported.
		 */
		public static final PlayerStat REPORTED = new PlayerStat("ReportCount");
		/**
		 * The date this player was first reported.
		 */
		public static final PlayerStat FIRSTREPORTDATE = new PlayerStat("FirstReportDate");
		/**
		 * The date this player was last reported.
		 */
		public static final PlayerStat LASTREPORTDATE = new PlayerStat("LastReportDate");
		
		/**
		 * Constructor.
		 * 
		 * @param columnName The case-sensitive column name of the statistic.
		 */
		protected PlayerStat(String columnName)
		{
			super(columnName);
		}
	}
	
	/**
	 * The case-sensitive table name for the player statistics.
	 */
	public static final String tableName = "PlayerStats";
	/**
	 * The case-sensitive column name that should be used as an index.
	 */
	public static final String indexColumn = "NameRaw";
	
	/**
	 * Constructor.
	 * 
	 * @param database The database where the statistics are being stored.
	 */
	public PlayerStatManager(ExtendedDatabaseHandler database)
	{
		super(database, tableName, indexColumn);
	}

	@Override
	protected void addRow(OfflinePlayer player)
	{
		String query = "SELECT ID FROM " + tableName + " WHERE " + indexColumn + " = '" + player.getName() + "'";
		
		SQLResultSet rs = null;
		
		try
		{
			rs = getDatabase().sqlQuery(query);
			
			if(rs.isEmpty())
			{
				query = "INSERT INTO " + tableName + " "
						+ "(Name, NameRaw, FirstReportDate, LastReportDate) "
						+ "VALUES (?,?,?,?)";
				
				ArrayList<String> params = new ArrayList<String>();
				
				String date = Reporter.getDateformat().format(new Date());
				
				String name = player.getName();
				
				if(player.isOnline())
				{
					name = player.getPlayer().getDisplayName();
				}
				
				params.add(name);
				params.add(player.getName());
				params.add(date);
				params.add(date);
				
				getDatabase().preparedUpdateQuery(query, params);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				getDatabase().closeConnection();
			}
			catch (SQLException e)
			{
			}
		}
	}
}
