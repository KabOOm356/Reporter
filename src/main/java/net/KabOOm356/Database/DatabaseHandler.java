package net.KabOOm356.Database;

import java.io.File;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import net.KabOOm356.Database.Connection.ConnectionPoolConfig;
import net.KabOOm356.Database.Connection.ConnectionPooledDatabaseInterface;
import net.KabOOm356.Database.SQL.MySQL;
import net.KabOOm356.Database.SQL.SQLite;
import net.KabOOm356.Util.FormattingUtil;

/**
 * Handler for a {@link Database}.
 */
public class DatabaseHandler implements DatabaseInterface, ConnectionPooledDatabaseInterface {
	/** The {@link Database} the Handler will use. */
	private Database database;

	/**
	 * Connection-based database.
	 * 
	 * @param host
	 *            The host where the database is located.
	 * @param database
	 *            The database name.
	 * @param username
	 *            The user to connect with.
	 * @param password
	 *            The password for the user.
	 * @param connectionPoolConfig The configuration for the connection pool.
	 */
	public DatabaseHandler(final String host, final String database, final String username, final String password, final ConnectionPoolConfig connectionPoolConfig) {
		this.database = new MySQL(host, database, username, password, connectionPoolConfig);
	}

	/**
	 * File-based database.
	 * 
	 * @param type
	 *            The type of database.
	 * @param path
	 *            The path to the file.
	 * @param name
	 *            The name of the file.
	 * @param connectionPoolConfig The configuration for the connection pool.
	 * 
	 * @throws IOException
	 */
	public DatabaseHandler(DatabaseType type, String path, String name, final ConnectionPoolConfig connectionPoolConfig) throws IOException {
		if (name.contains("/") || name.contains("\\")) {
			name = name.substring(name.lastIndexOf("\\"));
			name = name.substring(name.lastIndexOf("/"));
		}

		File SQLFile = new File(path, name);

		SQLFile.createNewFile();

		if (type == DatabaseType.SQLITE) {
			database = new SQLite(SQLFile.getAbsolutePath(), connectionPoolConfig);
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param database
	 *            The {@link Database} to be used.
	 */
	public DatabaseHandler(Database database) {
		this.database = database;
	}

	/**
	 * @return The {@link Database} this DatabaseHandler is using.
	 */
	public Database getDatabase() {
		return database;
	}

	@Override
	public void openConnection() throws ClassNotFoundException, SQLException, InterruptedException {
		database.openConnection();
	}

	@Override
	public void closeConnection() {
		database.closeConnection();
	}
	
	@Override
	public void closeConnections() {
		database.closeConnections();
	}

	/**
	 * Returns if the current database type is SQLite.
	 * 
	 * @return True if the current database type is SQLite, otherwise false.
	 */
	public boolean usingSQLite() {
		return database.getDatabaseType() == DatabaseType.SQLITE;
	}

	/**
	 * Returns if the current database type is MySQL.
	 * 
	 * @return True if the current database type is MySQL, otherwise false.
	 */
	public boolean usingMySQL() {
		return database.getDatabaseType() == DatabaseType.MYSQL;
	}

	@Override
	public boolean checkTable(String table) throws ClassNotFoundException, SQLException, InterruptedException {
		return database.checkTable(table);
	}

	@Override
	public void updateQuery(String query) throws ClassNotFoundException, SQLException, InterruptedException {
		database.updateQuery(query);
	}

	@Override
	public ArrayList<String> getColumnNames(String table) throws SQLException, ClassNotFoundException, InterruptedException {
		return database.getColumnNames(table);
	}

	@Override
	public DatabaseMetaData getMetaData() throws ClassNotFoundException, SQLException, InterruptedException {
		return database.getMetaData();
	}

	@Override
	public ResultSet getColumnMetaData(String table) throws ClassNotFoundException, SQLException, InterruptedException {
		return database.getColumnMetaData(table);
	}

	@Override
	public ResultSet query(String query) throws ClassNotFoundException, SQLException, InterruptedException {
		return database.query(query);
	}

	@Override
	public ResultSet preparedQuery(String query, ArrayList<String> params) throws ClassNotFoundException, SQLException, InterruptedException {
		return database.preparedQuery(query, params);
	}

	@Override
	public void preparedUpdateQuery(String query, ArrayList<String> params) throws ClassNotFoundException, SQLException, InterruptedException {
		database.preparedUpdateQuery(query, params);
	}

	@Override
	public Statement createStatement() throws ClassNotFoundException, SQLException, InterruptedException {
		return database.createStatement();
	}

	@Override
	public PreparedStatement prepareStatement(String query) throws ClassNotFoundException, SQLException, InterruptedException {
		return database.prepareStatement(query);
	}

	@Override
	public DatabaseType getDatabaseType() {
		return database.getDatabaseType();
	}

	@Override
	public String toString() {
		String toString = "Database Handler:\n" + database;
		toString = FormattingUtil.addTabsToNewLines(toString, 1);
		return toString;
	}

	@Override
	public int openPooledConnection() throws ClassNotFoundException, SQLException, InterruptedException {
		return database.openPooledConnection();
	}

	@Override
	public void closeConnection(Integer connectionId) {
		database.closeConnection(connectionId);
	}

	@Override
	public ResultSet query(Integer connectionId, String query) throws SQLException {
		return database.query(connectionId, query);
	}

	@Override
	public void updateQuery(Integer connectionId, String query) throws SQLException {
		database.updateQuery(connectionId, query);
	}

	@Override
	public ResultSet preparedQuery(Integer connectionId, String query, ArrayList<String> params) throws SQLException {
		return database.preparedQuery(connectionId, query, params);
	}

	@Override
	public void preparedUpdateQuery(Integer connectionId, String query, ArrayList<String> params) throws SQLException {
		database.preparedUpdateQuery(connectionId, query, params);
	}

	@Override
	public boolean checkTable(Integer connectionId, String table) throws SQLException {
		return database.checkTable(connectionId, table);
	}

	@Override
	public ArrayList<String> getColumnNames(Integer connectionId, String table) throws SQLException {
		return database.getColumnNames(connectionId, table);
	}

	@Override
	public DatabaseMetaData getMetaData(Integer connectionId) throws SQLException {
		return database.getMetaData(connectionId);
	}

	@Override
	public ResultSet getColumnMetaData(Integer connectionId, String table) throws SQLException {
		return database.getColumnMetaData(connectionId, table);
	}

	@Override
	public Statement createStatement(Integer connectionId) throws SQLException {
		return database.createStatement(connectionId);
	}

	@Override
	public PreparedStatement prepareStatement(Integer connectionId, String query) throws SQLException {
		return database.prepareStatement(connectionId, query);
	}
}
