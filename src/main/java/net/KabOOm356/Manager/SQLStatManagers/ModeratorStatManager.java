package net.KabOOm356.Manager.SQLStatManagers;

import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.OfflinePlayer;

import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Manager.SQLStatManager;

/**
 * A class to manage tracking statistics for moderators.
 */
public class ModeratorStatManager extends SQLStatManager
{
	/**
	 * A class that represents a statistic for a moderator.
	 */
	public static class ModeratorStat extends SQLStat
	{
		/**
		 * The number of times a moderator has used the assign command.
		 */
		public static final ModeratorStat ASSIGNED = new ModeratorStat("AssignCount");
		/**
		 * The number of times a moderator has used the claim command.
		 */
		public static final ModeratorStat CLAIMED = new ModeratorStat("ClaimedCount");
		/**
		 * The number of times a moderator has used the complete command.
		 */
		public static final ModeratorStat COMPLETED = new ModeratorStat("CompletionCount");
		/**
		 * The number of times a moderator has used the delete command.
		 */
		public static final ModeratorStat DELETED = new ModeratorStat("DeletionCount");
		/**
		 * The number of times a moderator has used the move command.
		 */
		public static final ModeratorStat MOVED = new ModeratorStat("MoveCount");
		/**
		 * The number of times a moderator has used the respond command.
		 */
		public static final ModeratorStat RESPONDED = new ModeratorStat("RespondCount");
		/**
		 * The number of times a moderator has used the unassign command.
		 */
		public static final ModeratorStat UNASSIGNED = new ModeratorStat("UnassignCount");
		/**
		 * The number of times a moderator has used the unclaim command.
		 */
		public static final ModeratorStat UNCLAIMED = new ModeratorStat("UnassignCount");
		
		/**
		 * Constructor.
		 * 
		 * @param columnName The case-sensitive column name of the statistic.
		 */
		protected ModeratorStat(String columnName)
		{
			super(columnName);
		}
	}
	
	/**
	 * The case-sensitive table name for the player statistics.
	 */
	public static final String tableName = "ModStats";
	/**
	 * The case-sensitive column name that should be used as an index.
	 */
	public static final String indexColumn = "ModNameRaw";
	
	/**
	 * Constructor.
	 * 
	 * @param database The database where the statistics are being stored.
	 */
	public ModeratorStatManager(ExtendedDatabaseHandler database)
	{
		super(database, tableName, indexColumn);
	}

	@Override
	protected void addRow(OfflinePlayer player)
	{
		String query = "SELECT ID FROM ModStats WHERE ModNameRaw = '" + player.getName() + "'";
		
		SQLResultSet rs = null;
		
		try
		{
			rs = getDatabase().sqlQuery(query);
			
			if(rs.isEmpty())
			{
				query = "INSERT INTO ModStats "
						+ "(ModName, ModNameRaw) "
						+ "VALUES (?,?)";
				
				ArrayList<String> params = new ArrayList<String>();
				
				String name = player.getName();
				
				if(player.isOnline())
				{
					name = player.getPlayer().getDisplayName();
				}
				
				params.add(name);
				params.add(player.getName());
				
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
