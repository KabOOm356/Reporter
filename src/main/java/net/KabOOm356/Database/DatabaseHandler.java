package net.KabOOm356.Database;

import java.io.File;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import net.KabOOm356.Database.SQL.MySQL;
import net.KabOOm356.Database.SQL.SQLite;
import net.KabOOm356.Util.FormattingUtil;


/**
 * Handler for a {@link Database}.
 */
public class DatabaseHandler implements DatabaseInterface
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
	 * Constructor.
	 * 
	 * @param database The {@link Database} to be used.
	 */
	public DatabaseHandler(Database database)
	{
		this.database = database;
	}
	
	/**
	 * @return The {@link Database} this DatabaseHandler is using.
	 */
	public Database getDatabase()
	{
		return database;
	}
	
	public void openConnection() throws ClassNotFoundException, SQLException
	{
		database.openConnection();
	}
	
	public void closeConnection()
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
	
	public boolean checkTable(String table) throws ClassNotFoundException, SQLException
	{
		return database.checkTable(table);
	}
	
	public void updateQuery(String query) throws ClassNotFoundException, SQLException
	{
		database.updateQuery(query);
	}
	
	public ArrayList<String> getColumnNames(String table) throws SQLException, ClassNotFoundException
	{
		return database.getColumnNames(table);
	}
	
	public DatabaseMetaData getMetaData() throws ClassNotFoundException, SQLException
	{
		return database.getMetaData();
	}
	
	public ResultSet getColumnMetaData(String table) throws ClassNotFoundException, SQLException
	{
		return database.getColumnMetaData(table);
	}
	
	public ResultSet query(String query) throws ClassNotFoundException, SQLException
	{
		return database.query(query);
	}
	
	public ResultSet preparedQuery(String query, ArrayList<String> params) throws ClassNotFoundException, SQLException
	{
		return database.preparedQuery(query, params);
	}
	
	public void preparedUpdateQuery(String query, ArrayList<String> params) throws ClassNotFoundException, SQLException
	{
		database.preparedUpdateQuery(query, params);
	}
	
	public Statement createStatement() throws ClassNotFoundException, SQLException
	{
		return database.createStatement();
	}
	
	public PreparedStatement prepareStatement(String query) throws ClassNotFoundException, SQLException
	{
		return database.prepareStatement(query);
	}
	
	public DatabaseType getDatabaseType()
	{
		return database.getDatabaseType();
	}
	
	@Override
	public String toString()
	{
		String toString = "Database Handler:\n" + database;
		
		toString = FormattingUtil.addTabsToNewLines(toString, 1);
		
		return toString;
	}
}
