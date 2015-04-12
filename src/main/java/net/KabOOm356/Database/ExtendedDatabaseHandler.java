package net.KabOOm356.Database;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An class that extends the functionality of {@link DatabaseHandler}.
 */
public class ExtendedDatabaseHandler extends DatabaseHandler
{
	private static final Logger log = LogManager.getLogger(ExtendedDatabaseHandler.class);
	
	/**
	 * Constructor.
	 * 
	 * @see DatabaseHandler#DatabaseHandler(String, String, String, String)
	 */
	public ExtendedDatabaseHandler(String host, String database, String username, String password)
	{
		super(host, database, username, password);
	}
	
	/**
	 * Constructor.
	 * 
	 * @see DatabaseHandler#DatabaseHandler(DatabaseType, String, String)
	 */
	public ExtendedDatabaseHandler(DatabaseType type, String path, String name) throws IOException
	{
		super(type, path, name);
	}
	
	/**
	 * Constructor.
	 * 
	 * @see DatabaseHandler#DatabaseHandler(Database)
	 */
	public ExtendedDatabaseHandler(Database database)
	{
		super(database);
	}
	
	/**
	 * Performs an SQL query on the database that returns a {@link SQLResultSet} containing the results.
	 * 
	 * @param query The query to send to the database.
	 * 
	 * @return A {@link SQLResultSet} containing the results
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * 
	 * @see net.KabOOm356.Database.DatabaseHandler#query(java.lang.String)
	 */
	public SQLResultSet sqlQuery(String query) throws ClassNotFoundException, SQLException
	{
		ResultSet resultSet = null;
		
		try
		{
			resultSet = super.query(query);
			
			if(!usingSQLite() && !resultSet.isBeforeFirst())
				resultSet.beforeFirst();
			
			return new SQLResultSet(resultSet);
		}
		finally
		{
			try {
				resultSet.close();
			} catch(final Exception e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to close result set!", e);
				}
			}
			closeConnection();
		}
	}
	
	/**
	 * Performs an SQL query on the database that returns a {@link SQLResultSet} containing the results.
	 * 
	 * @param query The query to send to the database.
	 * @param params The parameters of the query.
	 * 
	 * @return A {@link SQLResultSet} containing the results
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * 
	 * @see net.KabOOm356.Database.DatabaseHandler#preparedQuery(String, ArrayList)
	 */
	public SQLResultSet preparedSQLQuery(String query, ArrayList<String> params) throws ClassNotFoundException, SQLException
	{
		ResultSet resultSet = null;
		
		try
		{
			resultSet = super.preparedQuery(query, params);
			
			if(!usingSQLite() && !resultSet.isBeforeFirst())
				resultSet.beforeFirst();
			
			return new SQLResultSet(resultSet);
		}
		finally
		{
			try {
				resultSet.close();
			} catch(final Exception e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to close result set!", e);
				}
			}
			closeConnection();
		}
	}
	
	/**
	 * Returns the {@link Database}'s column meta data.
	 * 
	 * @param table The name of the table to get the meta data for.
	 * 
	 * @return An {@link SQLResultSet} containing the database's column meta data.
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public SQLResultSet getSQLColumnMetaData(String table) throws ClassNotFoundException, SQLException
	{
		ResultSet resultSet = null;
		
		try
		{
			resultSet = super.getColumnMetaData(table);
			
			if(!usingSQLite() && !resultSet.isBeforeFirst())
				resultSet.beforeFirst();
			
			return new SQLResultSet(resultSet);
		}
		finally
		{
			try {
				resultSet.close();
			} catch(final Exception e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to close result set!", e);
				}
			}
			closeConnection();
		}
	}
}
