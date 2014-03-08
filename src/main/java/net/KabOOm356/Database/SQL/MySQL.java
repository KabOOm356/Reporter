package net.KabOOm356.Database.SQL;

import java.sql.SQLException;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Util.FormattingUtil;


/**
 * A simple class to handle MySQL database connections using JDBC.
 */
public class MySQL extends Database
{
	/**The username to connect to the database with.*/
	private String username;
	/**The password to connect to the database with.*/
	private String password;

	/**
	 * Main constructor.
	 * 
	 * @param host The hostname that the database is on.
	 * @param database The name of the table to connect to.
	 * @param username The username to connect to the database with.
	 * @param password The password to connect to the database with.
	 */
	public MySQL(String host, String database, String username, String password)
	{
		super(DatabaseType.MYSQL, "com.mysql.jdbc.Driver", "jdbc:mysql://" + host + "/" + database);
		
		this.username = username;
		this.password = password;
	}
	
	/**
	 * Attempts to open a connection to the database.
	 * 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	@Override
	public void openConnection() throws ClassNotFoundException, SQLException
	{
		super.openConnection(username, password);
	}
	
	/**
	 * Returns a String representation of a MySQL object.
	 */
	@Override
	public String toString()
	{
		String toString = "Database Type: MySQL\n";
		toString += "Database Username: " + username;
		toString += "\n" + super.toString();
		
		toString = FormattingUtil.addTabsToNewLines(toString, 1);
		
		return toString;
	}
}
