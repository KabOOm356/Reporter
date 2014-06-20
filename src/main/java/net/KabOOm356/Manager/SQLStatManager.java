package net.KabOOm356.Manager;

import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.OfflinePlayer;

import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;

/**
 * A class to manage setting statistics held in an SQL database.
 */
public abstract class SQLStatManager
{
	/**
	 * A class that represents a statistic column in an SQL database.
	 */
	public static class SQLStat
	{
		/**
		 * Represents all statistic columns in the database.
		 */
		public static final SQLStat ALL = new SQLStat("*");
		
		/**
		 * The case-sensitive column name of the statistic this SQLStat represents.
		 */
		private String columnName;
		
		/**
		 * Constructor.
		 * 
		 * @param columnName The case-sensitive column name of the statistic.
		 */
		protected SQLStat(String columnName)
		{
			this.columnName = columnName;
		}
		
		@Override
		public String toString()
		{
			return columnName;
		}
		
		/**
		 * Checks if this SQLStat's column name equals the given column name.
		 * 
		 * @param column The name of the column to check.
		 * 
		 * @return True if this SQLStat's column name equals the given column name, otherwise false.
		 */
		public boolean equals(String column)
		{
			return columnName.equals(column);
		}
	}
	
	/**
	 * The database where the statistics are being stored.
	 */
	private ExtendedDatabaseHandler database;
	/**
	 * The name of the table the statistics are being stored.
	 */
	private String tableName;
	/**
	 * The case-sensitive name of the column that should be used as the index.
	 */
	private String indexColumn;
	
	/**
	 * Constructor.
	 * 
	 * @param database The database where the statistics are being stored.
	 * @param tableName The name of the table the statistics are being stored.
	 * @param indexColumn The case-sensitive name of the column that should be used as the index.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the parameters are null.
	 */
	public SQLStatManager(ExtendedDatabaseHandler database, String tableName, String indexColumn)
	{
		if(database == null)
		{
			throw new IllegalArgumentException("Parameter 'database' cannot be null!");
		}
		
		if(tableName == null)
		{
			throw new IllegalArgumentException("Parameter 'tableName' cannot be null!");
		}
		
		if(indexColumn == null)
		{
			throw new IllegalArgumentException("Parameter 'indexColumn' cannot be null!");
		}
		
		this.database = database;
		this.tableName = tableName;
		this.indexColumn = indexColumn;
	}
	
	/**
	 * Increments a statistic by one (1).
	 * 
	 * @param player The player to increment the statistic for.
	 * @param stat The SQLStat to increment.
	 */
	public void incrementStat(OfflinePlayer player, SQLStat stat)
	{
		addRow(player);
		
		String statColumn = stat.toString();
		
		String query = "UPDATE " + tableName + " "
				+ "SET " + statColumn + " = " + statColumn + " + 1 "
				+ "WHERE " + indexColumn + " = '" + player.getUniqueId() + "'";
		
		try
		{
			getDatabase().updateQuery(query);
		}
		catch (Exception e)
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
	
	/**
	 * Decrements a statistic by one (1).
	 * 
	 * @param player The player to decrement the statistic for.
	 * @param stat The SQLStat to decrement.
	 */
	public void decrementStat(OfflinePlayer player, SQLStat stat)
	{
		addRow(player);
		
		String statColumn = stat.toString();
		
		String query = "UPDATE " + tableName + " "
				+ "SET " + statColumn + " = " + statColumn + " - 1 "
				+ "WHERE " + indexColumn + " = '" + player.getUniqueId() + "'";
		
		try
		{
			getDatabase().updateQuery(query);
		}
		catch (Exception e)
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
	
	/**
	 * Sets a statistic.
	 * <br /> <br />
	 * NOTE: This does not create a column for the statistic, it solely sets a value to an already existing column.
	 * 
	 * @param player The player to set the statistic for.
	 * @param stat The statistic to set.
	 * @param value The value to set the statistic to.
	 */
	public void setStat(OfflinePlayer player, SQLStat stat, String value)
	{
		addRow(player);
		
		String statColumn = stat.toString();
		
		String query = "UPDATE " + tableName + " "
				+ "SET " + statColumn + " = ? "
				+ "WHERE " + indexColumn + " = ?";
		
		ArrayList<String> params = new ArrayList<String>();
		
		params.add(value);
		params.add(player.getUniqueId().toString());
		
		try
		{
			getDatabase().preparedUpdateQuery(query, params);
		}
		catch (Exception e)
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
	
	/**
	 * Gets a statistic.
	 * 
	 * @param player The player to get the statistic for.
	 * @param stat The statistic to get.
	 * 
	 * @return A {@link ResultRow} containing the statistic requested.
	 * If an exception is thrown while querying the database, null is returned.
	 */
	public ResultRow getStat(OfflinePlayer player, SQLStat stat)
	{
		String statColumn = stat.toString();
		
		String query = "SELECT " + statColumn
				+ " FROM " + tableName
				+ " WHERE " + indexColumn + " = ?";
		
		ArrayList<String> params = new ArrayList<String>();
		
		params.add(player.getUniqueId().toString());
		
		ResultRow resultRow = null;
		
		try
		{
			SQLResultSet result = getDatabase().preparedSQLQuery(query, params);
			
			resultRow = result.get(SQLResultSet.FIRSTROW);
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
		
		return resultRow;
	}
	
	/**
	 * Gets the values for the statistics passed.
	 * 
	 * @param player The player to get the statistics for.
	 * @param stats The SQLStats to get.
	 * 
	 * @return A {@link ResultRow} containing the values for the statistics passed.
	 * If an exception occurs while getting a statistic it is omitted from the returned {@link ResultRow}.
	 */
	public ResultRow getStat(OfflinePlayer player, Iterable<SQLStat> stats)
	{
		ResultRow returnedRow = new ResultRow();
		
		for(SQLStat s : stats)
		{
			ResultRow result = getStat(player, s);
			
			if(result != null)
			{
				returnedRow.putAll(result);
			}
		}
		
		return returnedRow;
	}
	
	/**
	 * Creates a new row for the player, if one does not exist.
	 * 
	 * @param player The player to create a new row for, if one does not already exist.
	 */
	protected abstract void addRow(OfflinePlayer player);
	
	/**
	 * Returns the database the statistics are being stored.
	 * 
	 * @return The database the statistics are being stored.
	 */
	protected ExtendedDatabaseHandler getDatabase()
	{
		return database;
	}
}
