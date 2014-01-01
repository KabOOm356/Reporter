package net.KabOOm356.Database.SQL;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;

/**
 * A simple class to handle SQLite database connections using JDBC.
 */
public class SQLite extends Database
{
	/**
	 * Main Constructor.
	 * 
	 * @param SQLFile The string URL of the SQL file to use.
	 */
	public SQLite(String SQLFile)
	{
		super(DatabaseType.SQLITE, "org.sqlite.JDBC", "jdbc:sqlite:" + SQLFile);
	}
}