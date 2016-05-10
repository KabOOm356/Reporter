package net.KabOOm356.Database.SQL;

import net.KabOOm356.Database.Connection.ConnectionPoolConfig;
import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Util.FormattingUtil;

import java.sql.SQLException;


/**
 * A simple class to handle MySQL database connections using JDBC.
 */
public class MySQL extends Database {
	/**
	 * The username to connect to the database with.
	 */
	private final String username;
	/**
	 * The password to connect to the database with.
	 */
	private final String password;

	/**
	 * Main constructor.
	 *
	 * @param host                 The hostname that the database is on.
	 * @param database             The name of the table to connect to.
	 * @param username             The username to connect to the database with.
	 * @param password             The password to connect to the database with.
	 * @param connectionPoolConfig The configuration for the connection pool.
	 */
	public MySQL(final String host, final String database, final String username, final String password, final ConnectionPoolConfig connectionPoolConfig) {
		super(DatabaseType.MYSQL, "com.mysql.jdbc.Driver", "jdbc:mysql://" + host + "/" + database, connectionPoolConfig);

		this.username = username;
		this.password = password;
	}

	/**
	 * Attempts to open a connection to the database.
	 *
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	@Override
	public void openConnection() throws ClassNotFoundException, SQLException, InterruptedException {
		super.openConnection(username, password);
	}

	@Override
	public int openPooledConnection() throws ClassNotFoundException, SQLException, InterruptedException {
		return super.openPooledConnection(username, password);
	}

	/**
	 * Returns a String representation of a MySQL object.
	 */
	@Override
	public String toString() {
		String toString = "Database Type: MySQL\n";
		toString += "Database Username: " + username;
		toString += "\n" + super.toString();

		toString = FormattingUtil.addTabsToNewLines(toString, 1);

		return toString;
	}
}
