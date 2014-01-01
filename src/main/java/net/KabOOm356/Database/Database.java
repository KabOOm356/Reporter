package net.KabOOm356.Database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import net.KabOOm356.Util.Util;


/**
 * A simple class to handle connections and queries to a database.
 */
public abstract class Database
{
	/** The {@link DatabaseType} representation of the type of this database. */
	private DatabaseType databaseType;
	/** The connection to the database. */
	private Connection connection;
	/** The database driver represented as a String. */
	private String databaseDriver;
	/** The URL to the database as a String. */
	private String connectionURL;
	
	/**
	 * Database Constructor.
	 * 
	 * @param databaseType The {@link DatabaseType} that is being constructed.
	 * @param databaseDriver The driver to use for the connection to the database.
	 * @param connectionURL	The URL of the database to connect to.
	 */
	public Database(DatabaseType databaseType, String databaseDriver, String connectionURL)
	{
		this.databaseType = databaseType;
		this.databaseDriver = databaseDriver;
		this.connectionURL = connectionURL;
		connection = null;
	}
	
	/**
	 * Attempts to open a connection to the database.
	 * 
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public void openConnection() throws ClassNotFoundException, SQLException
	{
		if(connection != null)
			return;
		
		Class.forName(databaseDriver);
		connection = DriverManager.getConnection(connectionURL);
	}
	
	/**
	 * Attempts to open a connection to the database using a username and password.
	 * 
	 * @param username	The username to login to the database with.
	 * @param password	The password for the user associated with the username.
	 * 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public void openConnection(String username, String password) throws SQLException, ClassNotFoundException
	{
		if(connection != null)
			return;
		
		Class.forName(databaseDriver);
		connection = DriverManager.getConnection(connectionURL, username, password);
	}
	
	/**
	 * Attempts to perform a query on the database and returns a ResultSet of data.
	 * <br /><br />
	 * <b>NOTE:</b> closeConnection() should be called after calling this method.
	 * 
	 * @param query	The string to query the database with.
	 * 
	 * @return	A {@link ResultSet} of information returned from the database.
	 * 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public ResultSet query(String query) throws ClassNotFoundException, SQLException
	{
		openConnection();
		
		return connection.createStatement().executeQuery(query);
	}
	
	/**
	 * Attempts to perform a query on the database that returns no data.
	 * 
	 * @param query	The string to query the database with.
	 * 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public void updateQuery(String query) throws ClassNotFoundException, SQLException
	{
		openConnection();
		
		Statement statement = null;
		
		try
		{
			statement = connection.createStatement();
			statement.executeUpdate(query);
		}
		finally
		{
			statement.close();
			closeConnection();
		}
	}
	
	/**
	 * Attempts to perform a prepared query on the database that returns a ResultSet of data.
	 * <br /><br />
	 * <b>NOTE:</b> closeConnection() should be called after calling this method.
	 * 
	 * @param query The string to query the database with.
	 * @param params The parameters of the query.
	 * 
	 * @return Returns a {@link ResultSet} of data if there are enough entries in params.
	 * 
	 * @throws SQLException 
	 * @throws ClassNotFoundException
	 * @throws IllegalArgumentException If the number of parameters given do not match the number of parameters required. 
	 */
	public ResultSet preparedQuery(String query, ArrayList<String> params) throws ClassNotFoundException, SQLException
	{
		int numberOfOccurances = Util.countOccurrences(query, '?');
		
		if(params.size() != Util.countOccurrences(query, '?'))
		{
			throw new IllegalArgumentException(
					"Required number of parameters: " + params.size() +
					" got: " + Integer.toString(numberOfOccurances) + "!");
		}
		else
		{
			openConnection();
			
			PreparedStatement preparedStatement = connection.prepareStatement(query);
				
			for(int LCV = 0; LCV < params.size(); LCV++)
				preparedStatement.setString(LCV+1, params.get(LCV));
			
			return preparedStatement.executeQuery();
		}
	}
	
	/**
	 * Attempts to perform a query on the database that returns no data.
	 * 
	 * @param query The string to query the database with.
	 * @param params The parameters of the query.
	 * 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalArgumentException If the number of parameters given do not match the number of parameters required.
	 */
	public void preparedUpdateQuery(String query, ArrayList<String> params) throws ClassNotFoundException, SQLException
	{
		int numberOfOccurances = Util.countOccurrences(query, '?');
		
		if(params.size() == numberOfOccurances)
		{
			PreparedStatement preparedStatement = null;
			
			try
			{
				openConnection();
				
				preparedStatement = connection.prepareStatement(query);
				
				for(int LCV = 0; LCV < params.size(); LCV++)
					preparedStatement.setString(LCV+1, params.get(LCV));
				
				preparedStatement.executeUpdate();
			}
			finally
			{
				preparedStatement.close();
				closeConnection();
			}
		}
		else 
		{
			throw new IllegalArgumentException(
					"Required number of parameters: " + params.size() +
					" got: " + Integer.toString(numberOfOccurances) + "!");
		}
	}
	
	/**
	 * Checks the database if a table exists.
	 * 
	 * @param table The name of the table to check for.
	 * 
	 * @return If the table exists then returns true, otherwise false.
	 * 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public boolean checkTable(String table) throws ClassNotFoundException, SQLException
	{
		ResultSet tables = null;
		
		try
		{
			openConnection();
			
			DatabaseMetaData dbm = connection.getMetaData();
			tables = dbm.getTables(null, null, table, null);

			if (tables.next())
				return true;
			else
				return false;
		}
		finally
		{
			tables.close();
			closeConnection();
		}
	}
	
	/**
	 * Returns the columns in a table.
	 * 
	 * @param table	The name of the table to get the columns from.
	 * 
	 * @return An {@link ArrayList} containing the names of the columns.
	 * 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public ArrayList<String> getColumns(String table) throws SQLException, ClassNotFoundException
	{
		openConnection();
		
		ArrayList<String> col = new ArrayList<String>();
		
		ResultSet rs = null;
		
		try
		{
			DatabaseMetaData d = connection.getMetaData();
			rs = d.getColumns(null,null,table,null);

			while(rs.next())
				col.add(rs.getString("COLUMN_NAME"));

			return col;
		}
		finally
		{
			rs.close();
			closeConnection();
		}
	}
	
	/**
	 * Checks if the connection is open.
	 * 
	 * @return If the connection is open returns true, otherwise false.
	 */
	public boolean isConnectionOpen()
	{
		return connection != null;
	}
	
	/**
	 * Closes the connection to the database.
	 * 
	 * @throws SQLException 
	 */
	public void closeConnection() throws SQLException
	{
		if(connection != null)
		{
			connection.close();
			connection = null;
		}
	}
	
	/**
	 * Returns the connection to the database.
	 * 
	 * @return The connection to the database.
	 */
	public Connection getConnection()
	{
		return connection;
	}
	
	/**
	 * Returns a Statement from the database connection.
	 * 
	 * @return A Statement from the database.
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	public Statement createStatement() throws SQLException, ClassNotFoundException
	{
		openConnection();
		
		return connection.createStatement();
	}
	
	/**
	 * Returns a PreparedStatement from the database connection.
	 * 
	 * @param query The SQL query to create the PreparedStatement from.
	 * 
	 * @return A PreparedStatement created from the given query.
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	public PreparedStatement prepareStatement(String query) throws SQLException, ClassNotFoundException
	{
		openConnection();
		
		return connection.prepareStatement(query);
	}
	
	/**
	 * Returns the {@link DatabaseType} of this database.
	 * 
	 * @return The {@link DatabaseType} of this database.
	 */
	public DatabaseType getDatabaseType()
	{
		return databaseType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		String toString = "Database Type: " + databaseType.toString();
		toString += "\n\tDatabase Driver: " + databaseDriver;
		toString += "\n\tConnection URL: " + connectionURL;
		toString += "\n\tConnection Status: ";
		if(isConnectionOpen())
			toString += "Open\n";
		else
			toString += "Closed\n";
		return toString;
	}
}
