package net.KabOOm356.Database;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;

/**
 * A {@link LinkedHashMap} that represents a row returned from a SQL query.
 */
public class ResultRow extends LinkedHashMap<String, Object>
{
	/**
	 * Generated Serial-ID.
	 */
	private static final long serialVersionUID = -1489657675159738791L;

	/**
	 * Constructor.
	 */
	public ResultRow()
	{
		super(16, 0.75f, false);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param result The {@link ResultSet} to initialize this {@link ResultRow} to.
	 * 
	 * @throws SQLException
	 */
	public ResultRow(ResultSet result) throws SQLException
	{
		super(16, 0.75f, false);
		
		set(result);
	}
	
	/**
	 * Sets the contents of this {@link ResultRow} to the current row of the given ResultSet.
	 * 
	 * @param result The {@link ResultSet}.
	 * 
	 * @throws SQLException
	 */
	public void set(ResultSet result) throws SQLException
	{
		clear();
		
		ResultSetMetaData metaData = result.getMetaData();
		
		int columns = metaData.getColumnCount();
		
		for(int LCV = 1; LCV <= columns; LCV++)
			put(metaData.getColumnName(LCV), result.getObject(LCV));
	}
	
	/**
	 * Attempts to cast the contents of the given column to a String.
	 * 
	 * @param colName The name of the column.
	 * 
	 * @return The column cast to a String if the column exists, otherwise null.
	 */
	public String getString(String colName)
	{
		if(get(colName) == null)
			return null;
		return get(colName).toString();
	}
	
	/**
	 * Attempts to cast the contents of the given column to a Boolean.
	 * 
	 * @param colName The name of the column.
	 * 
	 * @return The column cast to a Boolean if the column exists, otherwise null.
	 */
	public Boolean getBoolean(String colName)
	{
		if(get(colName) == null)
			return null;
		
		// Try to parse for the Boolean, only returns true if the string value is "true".
		Boolean value = Boolean.parseBoolean(getString(colName));
		// Tries to parse for the Boolean by Integer value, true if the string value is "1".
		value = value || getString(colName).equals("1");
		
		return value;
	}
	
	/**
	 * Attempts to cast the contents of the given column to an Integer.
	 * 
	 * @param colName The name of the column.
	 * 
	 * @return The column cast to an Integer if the column exists, otherwise null.
	 */
	public Integer getInt(String colName)
	{
		if(get(colName) == null)
			return null;
		return Integer.parseInt(getString(colName));
	}
	
	/**
	 * Attempts to cast the contents of the given column to a Double.
	 * 
	 * @param colName The name of the column.
	 * 
	 * @return The column cast to a Double if the column exists, otherwise null.
	 */
	public Double getDouble(String colName)
	{
		if(get(colName) == null)
			return null;
		return Double.parseDouble(getString(colName));
	}
}