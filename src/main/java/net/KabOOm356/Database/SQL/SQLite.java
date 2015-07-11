package net.KabOOm356.Database.SQL;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Database.Connection.ConnectionPoolConfig;

/**
 * A simple class to handle SQLite database connections using JDBC.
 */
public class SQLite extends Database
{
	/**
	 * Main Constructor.
	 * 
	 * @param SQLFile The string URL of the SQL file to use.
	 * @param connectionPoolConfig The configuration for the connection pool.
	 */
	public SQLite(final String SQLFile, final ConnectionPoolConfig connectionPoolConfig)
	{
		super(DatabaseType.SQLITE, "org.sqlite.JDBC", "jdbc:sqlite:" + SQLFile, connectionPoolConfig);
	}
}
