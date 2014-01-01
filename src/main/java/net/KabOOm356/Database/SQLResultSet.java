package net.KabOOm356.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * A class to store and access information returned after a SQL query.
 */
public class SQLResultSet extends ArrayList<ResultRow>
{
	/**
	 * Index of the first row (0).
	 */
	public static final int FIRSTROW = 0;
	
	/**
	 * Index of the first column (0);
	 */
	public static final int FIRSTCOLUMN = 0;
	
	/**
	 * Generated Serial-ID.
	 */
	private static final long serialVersionUID = 2992528074740195473L;

	/**
	 * Constructor.
	 */
	public SQLResultSet()
	{
		super();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param resultSet The {@link ResultSet} to initialize this {@link SQLResultSet} to.
	 * 
	 * @throws SQLException
	 */
	public SQLResultSet(ResultSet resultSet) throws SQLException
	{
		super();
		
		set(resultSet);
	}
	
	/**
	 * Sets the contents of this {@link SQLResultSet} to the contents of the given ResultSet.
	 * <br /><br />
	 * <b>NOTE:</b> The internal cursor of the given {@link ResultSet} should be set to just before the first element.
	 * 
	 * @param resultSet The {@link ResultSet}.
	 * 
	 * @throws SQLException
	 */
	public void set(ResultSet resultSet) throws SQLException
	{
		clear();
		
		try
		{
			resultSet.beforeFirst();
		}
		catch(SQLException e)
		{}
		
		while(resultSet.next())
			add(new ResultRow(resultSet));
	}
	
	/**
	 * Attempts to cast the contents of the given column to a String.
	 * 
	 * @param row The row of result to access.
	 * @param colName The column name to cast.
	 * 
	 * @return The contents of the given column cast to a String.
	 */
	public String getString(int row, String colName)
	{
		return get(row).getString(colName);
	}
	
	/**
	 * Attempts to cast the contents of the first row to a String. 
	 * 
	 * @param colName The name of the column to cast.
	 * 
	 * @return The contents of the first row cast to a String.
	 * 
	 * @see SQLResultSet#FIRSTROW
	 */
	public String getString(String colName)
	{
		return getString(FIRSTROW, colName);
	}
	
	/**
	 * Attempts to cast the contents of the given column to a Boolean.
	 * 
	 * @param row The row of result to access.
	 * @param colName The column name to cast.
	 * 
	 * @return The contents of the given column cast to a Boolean.
	 */
	public Boolean getBoolean(int row, String colName)
	{
		return get(row).getBoolean(colName);
	}
	
	/**
	 * Attempts to cast the contents of the first row to a Boolean. 
	 * 
	 * @param colName The name of the column to cast.
	 * 
	 * @return The contents of the first row cast to a Boolean.
	 * 
	 * @see SQLResultSet#FIRSTROW
	 */
	public Boolean getBoolean(String colName)
	{
		return getBoolean(FIRSTROW, colName);
	}
	
	/**
	 * Attempts to cast the contents of the given column to an Integer.
	 * 
	 * @param row The row of result to access.
	 * @param colName The column name to cast.
	 * 
	 * @return The contents of the given column cast to an Integer.
	 */
	public Integer getInt(int row, String colName)
	{
		return get(row).getInt(colName);
	}
	
	/**
	 * Attempts to cast the contents of the first row to an Integer. 
	 * 
	 * @param colName The name of the column to cast.
	 * 
	 * @return The contents of the first row cast to an Integer.
	 * 
	 * @see SQLResultSet#FIRSTROW
	 */
	public Integer getInt(String colName)
	{
		return getInt(FIRSTROW, colName);
	}
	
	/**
	 * Attempts to cast the contents of the given column to a Double.
	 * 
	 * @param row The row of result to access.
	 * @param colName The column name to cast.
	 * 
	 * @return The contents of the given column cast to a Double.
	 */
	public Double getDouble(int row, String colName)
	{
		return get(row).getDouble(colName);
	}
	
	/**
	 * Attempts to cast the contents of the first row to a Double. 
	 * 
	 * @param colName The name of the column to cast.
	 * 
	 * @return The contents of the first row cast to a Double.
	 * 
	 * @see SQLResultSet#FIRSTROW
	 */
	public Double getDouble(String colName)
	{
		return getDouble(FIRSTROW, colName);
	}
}
