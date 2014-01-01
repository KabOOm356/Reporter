package net.KabOOm356.Database;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import net.KabOOm356.Database.SQL.MySQL;
import net.KabOOm356.Database.SQL.SQLite;


/**
 * Handler for a {@link Database}.
 */
public class DatabaseHandler
{
	/** The {@link Database} the Handler will use. */
	private Database database;
	
	/**
	 * Connection-based database.
	 * 
	 * @param host The host where the database is located.
	 * @param database The database name.
	 * @param username The user to connect with.
	 * @param password The password for the user.
	 */
	public DatabaseHandler(String host, String database, String username, String password)
	{
		this.database = new MySQL(host, database, username, password);
	}
	
	/**
	 * File-based database.
	 * 
	 * @param type The type of database.
	 * @param path The path to the file.
	 * @param name The name of the file.
	 * 
	 * @throws IOException 
	 */
	public DatabaseHandler(DatabaseType type, String path, String name) throws IOException
	{
		if(name.contains("/") || name.contains("\\"))
		{
			name = name.substring(name.lastIndexOf("\\"));
			name = name.substring(name.lastIndexOf("/"));
		}
		
		File SQLFile = new File(path, name);
		
		SQLFile.createNewFile();
		
		if(type == DatabaseType.SQLITE)
			database = new SQLite(SQLFile.getAbsolutePath());
	}
	
	/**
	 * @return The {@link Database} this DatabaseHandler is using.
	 */
	public Database getDatabase()
	{
		return database;
	}

	/**
	 * @see Database#openConnection()
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public void openConnection() throws ClassNotFoundException, SQLException
	{
		database.openConnection();
	}
	
	/**
	 * @see Database#closeConnection()
	 * 
	 * @throws SQLException
	 */
	public void closeConnection() throws SQLException
	{
		database.closeConnection();
	}
	
	/**
	 * Returns if the current database type is SQLite.
	 * 
	 * @return True if the current database type is SQLite, otherwise false.
	 */
	public boolean usingSQLite()
	{
		return database.getDatabaseType() == DatabaseType.SQLITE;
	}
	
	/**
	 * Returns if the current database type is MySQL.
	 * 
	 * @return True if the current database type is MySQL, otherwise false.
	 */
	public boolean usingMySQL()
	{
		return database.getDatabaseType() == DatabaseType.MYSQL;
	}

	/**
	 * @see Database#checkTable(String)
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public boolean checkTable(String table) throws ClassNotFoundException, SQLException
	{
		return database.checkTable(table);
	}

	/**
	 * @see Database#updateQuery(String)
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public void updateQuery(String query) throws ClassNotFoundException, SQLException
	{
		database.updateQuery(query);
	}
	
	/**
	 * @see Database#getColumns(String)
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public ArrayList<String> getColumns(String table) throws SQLException, ClassNotFoundException
	{
		return database.getColumns(table);
	}

	/**
	 * @see Database#query(String)
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public ResultSet query(String query) throws ClassNotFoundException, SQLException
	{
		return database.query(query);
	}

	/**
	 * @see Database#preparedQuery(String, ArrayList)
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public ResultSet preparedQuery(String query, ArrayList<String> params) throws ClassNotFoundException, SQLException
	{
		return database.preparedQuery(query, params);
	}

	/**
	 * @see Database#preparedUpdateQuery(String, ArrayList)
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public void preparedUpdateQuery(String query, ArrayList<String> params) throws ClassNotFoundException, SQLException
	{
		database.preparedUpdateQuery(query, params);
	}
	
	/**
	 * @see Database#createStatement()
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Statement createStatement() throws ClassNotFoundException, SQLException
	{
		return database.createStatement();
	}
	
	/**
	 * @see Database#prepareStatement(String)
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public PreparedStatement prepareStatement(String query) throws ClassNotFoundException, SQLException
	{
		return database.prepareStatement(query);
	}
	
	/**
	 * @see Database#getDatabaseType()
	 */
	public DatabaseType getDatabaseType()
	{
		return database.getDatabaseType();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		String toString = "Database Handler:\n";
		toString += "\t" + database;
		
		return toString;
	}
}