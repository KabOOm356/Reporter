package net.KabOOm356.Manager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.OfflinePlayer;

import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Manager.SQLStatManagers.ModeratorStatManager.ModeratorStat;
import net.KabOOm356.Manager.SQLStatManagers.PlayerStatManager.PlayerStat;
import net.KabOOm356.Util.BukkitUtil;

/**
 * A class to manage setting statistics held in an SQL database.
 */
public class SQLStatManager
{
	/**
	 * A class that represents a statistic column in an SQL database.
	 */
	public static class SQLStat
	{
		/**
		 * Represents all statistic columns in the database.
		 */
		public static final SQLStat ALL = new SQLStat("All", "*");
		
		/**
		 * The case-sensitive column name of the statistic this SQLStat represents.
		 */
		private String columnName;
		
		/**
		 * The name of this statistic.
		 */
		private String name;
		
		/**
		 * Constructor.
		 * 
		 * @param name The name of the statistic.
		 * @param columnName The case-sensitive column name of the statistic.
		 */
		protected SQLStat(String name, String columnName)
		{
			this.name = name;
			this.columnName = columnName;
		}
		
		/**
		 * Returns the column name of this SQLStat.
		 * 
		 * @return The column name of this SQLStat.
		 */
		public String getColumnName()
		{
			return columnName;
		}
		
		/**
		 * Returns the name of this SQLStat.
		 * 
		 * @return The name of this SQLStat.
		 */
		public String getName()
		{
			return name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		
		/**
		 * Gets an {@link SQLStat}, {@link PlayerStat}, or a {@link ModeratorStat} by the given name.
		 * 
		 * @param name The name of the {@link SQLStat} to return.
		 * 
		 * @return An {@link SQLStat} if one matches the given name, otherwise null.
		 */
		public static SQLStat getByName(String name)
		{
			if(name.equalsIgnoreCase("all"))
			{
				return SQLStat.ALL;
			}
			else
			{
				ArrayList<SQLStat> stats = SQLStat.getAll(ModeratorStat.class);
				stats.addAll(SQLStat.getAll(PlayerStat.class));
				
				for(SQLStat stat : stats)
				{
					if(name.equalsIgnoreCase(stat.getName()))
					{
						return stat;
					}
				}
			}
			
			return null;
		}
		
		/**
		 * Returns all {@link SQLStat}s that are static fields of the given Class.
		 * 
		 * @param clazz The Class to get the static {@link SQLStat} fields from.
		 * 
		 * @return An {@link ArrayList} containing all the {@link SQLStat}s from the given class.
		 */
		public static <T extends SQLStat> ArrayList<SQLStat> getAll(Class<T> clazz)
		{
			ArrayList<SQLStat> stats = new ArrayList<SQLStat>();
			
			for(Field f : clazz.getDeclaredFields())
			{
				try
				{
					// Only care if the field is static and an instance of a SQLStat.
					if(Modifier.isStatic(f.getModifiers()) && f.get(null) instanceof SQLStat)
					{
						SQLStat stat = (SQLStat) f.get(null);
						
						stats.add(stat);
					}
				}
				catch(IllegalAccessException e)
				{
					e.printStackTrace();
				}
			}
			
			return stats;
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
	 * The case-sensitive column name that should be used as a secondary index.
	 */
	private String secondaryIndexColumn;
	
	/**
	 * Constructor.
	 * 
	 * @param database The database where the statistics are being stored.
	 * @param tableName The name of the table the statistics are being stored.
	 * @param indexColumn The case-sensitive name of the column that should be used as the index.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the parameters are null.
	 */
	public SQLStatManager(ExtendedDatabaseHandler database, String tableName, String indexColumn, String secondaryIndexColumn)
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
		
		if(secondaryIndexColumn == null)
		{
			throw new IllegalArgumentException("Parameter 'secondaryIndexColumn' cannot be null!");
		}
		
		this.database = database;
		this.tableName = tableName;
		this.indexColumn = indexColumn;
		this.secondaryIndexColumn = secondaryIndexColumn;
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
		
		String statColumn = stat.getColumnName();
		
		String query = "UPDATE " + tableName + " "
				+ "SET " + statColumn + " = " + statColumn + " + 1 "
				+ "WHERE " + indexColumn + " = ? OR " + secondaryIndexColumn + " = ? "
				+ "LIMIT 1";
		
		ArrayList<String> params = new ArrayList<String>();
		
		params.add(player.getUniqueId().toString());
		params.add(player.getName());
		
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
	 * Decrements a statistic by one (1).
	 * 
	 * @param player The player to decrement the statistic for.
	 * @param stat The SQLStat to decrement.
	 */
	public void decrementStat(OfflinePlayer player, SQLStat stat)
	{
		addRow(player);
		
		String statColumn = stat.getColumnName();
		
		String query = "UPDATE " + tableName + " "
				+ "SET " + statColumn + " = " + statColumn + " - 1 "
				+ "WHERE " + indexColumn + " = ? OR " + secondaryIndexColumn + " = ? "
				+ "LIMIT 1";
		
		ArrayList<String> params = new ArrayList<String>();
		
		params.add(player.getUniqueId().toString());
		params.add(player.getName());
		
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
		
		String statColumn = stat.getColumnName();
		
		String query = "UPDATE " + tableName + " "
				+ "SET " + statColumn + " = ? "
				+ "WHERE " + indexColumn + " = ? OR " + secondaryIndexColumn + " = ? "
				+ "LIMIT 1";
		
		ArrayList<String> params = new ArrayList<String>();
		
		params.add(value);
		params.add(player.getUniqueId().toString());
		params.add(player.getName());
		
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
	 * @return An {@link ResultRow} containing the statistic requested.
	 * An empty {@link ResultRow} is returned if there is not an entry for the given player.
	 * If an exception is thrown while querying the database, null is returned.
	 */
	public ResultRow getStat(OfflinePlayer player, SQLStat stat)
	{
		String statColumn = stat.getColumnName();
		
		String query = "SELECT " + statColumn
				+ " FROM " + tableName
				+ " WHERE " + indexColumn + " = ? OR " + secondaryIndexColumn + " = ? "
				+ "LIMIT 1";
		
		ArrayList<String> params = new ArrayList<String>();
		
		params.add(player.getUniqueId().toString());
		params.add(player.getName());
		
		ResultRow resultRow = new ResultRow();
		
		try
		{
			SQLResultSet result = getDatabase().preparedSQLQuery(query, params);
			
			if(result != null && !result.isEmpty())
			{
				resultRow = result.get(SQLResultSet.FIRSTROW);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
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
	protected void addRow(OfflinePlayer player)
	{
		try
		{
			SQLResultSet rs = getIndex(player);
			
			if(rs.isEmpty())
			{
				String query = "INSERT INTO " + tableName + " "
						+ "(" + indexColumn + "," + secondaryIndexColumn + ") "
						+ "VALUES (?,?)";
				
				ArrayList<String> params = new ArrayList<String>();
				
				if(BukkitUtil.isPlayerValid(player))
				{
					params.add(player.getUniqueId().toString());
				}
				else
				{
					params.add("");
				}
				
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
	
	/**
	 * Gets the SQL index for the given player.
	 * 
	 * @param player The {@link OfflinePlayer} to get the index for.
	 * 
	 * @return An {@link SQLResultSet} that either contains the ID for the given player, or is empty.
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private SQLResultSet getIndex(OfflinePlayer player) throws ClassNotFoundException, SQLException
	{
		String query = "SELECT ID "
				+ "FROM " + tableName + " "
				+ "WHERE " + indexColumn + " = ? OR " + secondaryIndexColumn + " = ? "
				+ "LIMIT 1";
		
		ArrayList<String> params = new ArrayList<String>();
		
		params.add(player.getUniqueId().toString());
		params.add(player.getName());
		
		return database.preparedSQLQuery(query, params);
	}
	
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
