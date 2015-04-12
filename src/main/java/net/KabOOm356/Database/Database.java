package net.KabOOm356.Database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.KabOOm356.Util.FormattingUtil;
import net.KabOOm356.Util.Util;


/**
 * A simple class to handle connections and queries to a database.
 */
public abstract class Database implements DatabaseInterface
{
	private static final Logger log = LogManager.getLogger(Database.class);
	
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
	
	public void openConnection() throws ClassNotFoundException, SQLException
	{
		if(connection != null)
			return;
		
		try {
			Class.forName(databaseDriver);
			connection = DriverManager.getConnection(connectionURL);
		} catch (final ClassNotFoundException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to open connection to database!");
			}
			throw e;
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to open connection to database!");
			}
			throw e;
		}
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
		
		try {
			Class.forName(databaseDriver);
			connection = DriverManager.getConnection(connectionURL, username, password);
		} catch (final ClassNotFoundException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to open connection to database!");
			}
			throw e;
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to open connection to database!");
			}
			throw e;
		}
	}
	
	public ResultSet query(String query) throws ClassNotFoundException, SQLException
	{
		openConnection();
		
		try {
			return createStatement().executeQuery(query);
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to execute query!");
			}
			throw e;
		}
	}
	
	public void updateQuery(String query) throws ClassNotFoundException, SQLException
	{
		openConnection();
		
		Statement statement = null;
		
		try
		{
			statement = createStatement();
			try {
				statement.executeUpdate(query);
			} catch (final SQLException e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to execute update query!");
				}
				throw e;
			}
		}
		finally
		{
			try {
				statement.close();
			} catch(final Exception e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to close statement!", e);
				}
			}
			
			try {
				closeConnection();
			} catch(final Exception e) {}
		}
	}
	
	public ResultSet preparedQuery(String query, ArrayList<String> params) throws ClassNotFoundException, SQLException
	{
		int numberOfOccurences = Util.countOccurrences(query, '?');
		
		if(params.size() != numberOfOccurences)
		{
			final StringBuilder builder = new StringBuilder();
			builder.append("Required number of parameters: ");
			builder.append(params.size());
			builder.append(" got: ");
			builder.append(Integer.toString(numberOfOccurences));
			final IllegalArgumentException e = new IllegalArgumentException(builder.toString());
			if (log.isDebugEnabled()) {
				log.throwing(Level.WARN, e);
			}
			throw e;
		}
		else
		{
			openConnection();
			
			PreparedStatement preparedStatement = prepareStatement(query);
				
			try {
				for(int LCV = 0; LCV < params.size(); LCV++) {
					preparedStatement.setString(LCV+1, params.get(LCV));
				}
			} catch (final SQLException e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to set parameter to prepared query!");
				}
				throw e;
			}
			
			try {
				return preparedStatement.executeQuery();
			} catch (final SQLException e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to execute query!");
				}
				throw e;
			}
		}
	}
	
	public void preparedUpdateQuery(String query, ArrayList<String> params) throws ClassNotFoundException, SQLException
	{
		int numberOfOccurences = Util.countOccurrences(query, '?');
		
		if(params.size() == numberOfOccurences)
		{
			PreparedStatement preparedStatement = null;
			
			try
			{
				openConnection();
				
				preparedStatement = prepareStatement(query);
				
				try {
					for(int LCV = 0; LCV < params.size(); LCV++) {
						preparedStatement.setString(LCV+1, params.get(LCV));
					}
				} catch (final SQLException e) {
					if (log.isDebugEnabled()) {
						log.log(Level.WARN, "Failed to set parameter to prepared query!");
					}
					throw e;
				}
				
				try {
					preparedStatement.executeUpdate();
				} catch (final SQLException e) {
					if (log.isDebugEnabled()) {
						log.log(Level.WARN, "Failed to excecute prepared query!");
					}
					throw e;
				}
			} finally {
				try {
					preparedStatement.close();
				} catch(final Exception e) {
					if (log.isDebugEnabled()) {
						log.log(Level.WARN, "Failed to close prepared statement!", e);
					}
				}
				closeConnection();
			}
		}
		else 
		{
			final StringBuilder builder = new StringBuilder();
			builder.append("Required number of parameters: ");
			builder.append(params.size());
			builder.append(" got: ");
			builder.append(Integer.toString(numberOfOccurences));
			final IllegalArgumentException e = new IllegalArgumentException(builder.toString());
			if (log.isDebugEnabled()) {
				log.throwing(Level.WARN, e);
			}
			throw e;
		}
	}
	
	public boolean checkTable(String table) throws ClassNotFoundException, SQLException
	{
		ResultSet tables = null;
		
		try
		{
			openConnection();
			
			final DatabaseMetaData dbm;
			
			try {
				dbm = connection.getMetaData();
			} catch (final SQLException e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to get connection meta data!");
				}
				throw e;
			}
			
			try {
				tables = dbm.getTables(null, null, table, null);
			} catch (final SQLException e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to get tables from connection meta data!");
				}
				throw e;
			}

			if (tables.next())
				return true;
			else
				return false;
		} catch (final ClassNotFoundException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to check table!");
			}
			throw e;
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to check table!");
			}
			throw e;
		}
		finally {
			try {
				tables.close();
			} catch (final Exception e) {
				if (log.isDebugEnabled()) {
					log.log(Level.DEBUG, "Failed to close ResultSet!");
				}
			}
			closeConnection();
		}
	}
	
	public ArrayList<String> getColumnNames(String table) throws SQLException, ClassNotFoundException
	{
		openConnection();
		
		ArrayList<String> col = new ArrayList<String>();
		
		ResultSet rs = null;
		
		try
		{
			rs = getColumnMetaData(table);

			while(rs.next())
				col.add(rs.getString("COLUMN_NAME"));

			return col;
		}
		finally {
			try {
				rs.close();
			} catch (final Exception e) {
				if (log.isDebugEnabled()) {
					log.log(Level.DEBUG, "Failed to close ResultSet!", e);
				}
			}
			closeConnection();
		}
	}
	
	public DatabaseMetaData getMetaData() throws ClassNotFoundException, SQLException
	{
		openConnection();
		
		try {
			return connection.getMetaData();
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.DEBUG, "Failed to get meta data from the connection!");
			}
			throw e;
		}
	}
	
	public ResultSet getColumnMetaData(String table) throws ClassNotFoundException, SQLException
	{
		openConnection();
		
		try {
			return getMetaData().getColumns(null, null, table, null);
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.DEBUG, "Failed to get table columns!");
			}
			throw e;
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
	
	public void closeConnection()
	{
		if(connection != null)
		{
			try {
				connection.close();
			} catch (final SQLException e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to close database connection!", e);
				}
			}
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
	
	public Statement createStatement() throws SQLException, ClassNotFoundException
	{
		openConnection();
		
		try {
			return connection.createStatement();
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to create statement!");
			}
			throw e;
		}
	}
	
	public PreparedStatement prepareStatement(String query) throws SQLException, ClassNotFoundException
	{
		openConnection();
		
		try {
			return connection.prepareStatement(query);
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to prepare statement!");
			}
			throw e;
		}
	}
	
	public DatabaseType getDatabaseType()
	{
		return databaseType;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder toString = new StringBuilder();
		toString.append("Database Type: ").append(databaseType.toString());
		toString.append("\nDatabase Driver: ").append(databaseDriver);
		toString.append("\nConnection URL: ").append(connectionURL);
		toString.append("\nConnection Status: ");
		if(isConnectionOpen()) {
			toString.append("Open\n");
		} else {
			toString.append("Closed\n");
		}
		
		return FormattingUtil.addTabsToNewLines(toString.toString(), 1);
	}
}
