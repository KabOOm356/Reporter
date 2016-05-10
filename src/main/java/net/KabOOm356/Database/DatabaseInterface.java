package net.KabOOm356.Database;

import java.sql.*;
import java.util.ArrayList;

public interface DatabaseInterface {
	public void openConnection() throws ClassNotFoundException, SQLException, InterruptedException;

	/**
	 * Attempts to perform a query on the database and returns a ResultSet of data.
	 * <br /><br />
	 * <b>NOTE:</b> closeConnection() should be called after calling this method.
	 *
	 * @param query The string to query the database with.
	 * @return A {@link ResultSet} of information returned from the database.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public ResultSet query(String query) throws ClassNotFoundException, SQLException, InterruptedException;

	/**
	 * Attempts to perform a query on the database that returns no data.
	 *
	 * @param query The string to query the database with.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public void updateQuery(String query) throws ClassNotFoundException, SQLException, InterruptedException;

	/**
	 * Attempts to perform a prepared query on the database that returns a ResultSet of data.
	 * <br /><br />
	 * <b>NOTE:</b> closeConnection() should be called after calling this method.
	 *
	 * @param query  The string to query the database with.
	 * @param params The parameters of the query.
	 * @return Returns a {@link ResultSet} of data if there are enough entries in params.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 * @throws IllegalArgumentException If the number of parameters given do not match the number of parameters required.
	 */
	public ResultSet preparedQuery(String query, ArrayList<String> params) throws ClassNotFoundException, SQLException, InterruptedException;

	/**
	 * Attempts to perform a query on the database that returns no data.
	 *
	 * @param query  The string to query the database with.
	 * @param params The parameters of the query.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 * @throws IllegalArgumentException If the number of parameters given do not match the number of parameters required.
	 */
	public void preparedUpdateQuery(String query, ArrayList<String> params) throws ClassNotFoundException, SQLException, InterruptedException;

	/**
	 * Checks the database if a table exists.
	 *
	 * @param table The name of the table to check for.
	 * @return If the table exists then returns true, otherwise false.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public boolean checkTable(String table) throws ClassNotFoundException, SQLException, InterruptedException;

	/**
	 * Returns the columns in a table.
	 *
	 * @param table The name of the table to get the columns from.
	 * @return An {@link ArrayList} containing the names of the columns.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public ArrayList<String> getColumnNames(String table) throws SQLException, ClassNotFoundException, InterruptedException;

	/**
	 * Returns the database's meta data.
	 * <br /><br />
	 * <b>NOTE:</b> closeConnection() should be called after calling this method.
	 *
	 * @return A {@link DatabaseMetaData} object.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public DatabaseMetaData getMetaData() throws ClassNotFoundException, SQLException, InterruptedException;

	/**
	 * Returns the database's column meta data.
	 * <br /><br />
	 * <b>NOTE:</b> closeConnection() should be called after calling this method.
	 *
	 * @param table The name of the table to get the meta data for.
	 * @return A {@link ResultSet} containing the database's column meta data.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public ResultSet getColumnMetaData(String table) throws ClassNotFoundException, SQLException, InterruptedException;

	/**
	 * Closes the connection to the database.
	 */
	public void closeConnection();

	/**
	 * Returns a Statement from the database connection.
	 *
	 * @return A Statement from the database.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public Statement createStatement() throws SQLException, ClassNotFoundException, InterruptedException;

	/**
	 * Returns a PreparedStatement from the database connection.
	 *
	 * @param query The SQL query to create the PreparedStatement from.
	 * @return A PreparedStatement created from the given query.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public PreparedStatement prepareStatement(String query) throws SQLException, ClassNotFoundException, InterruptedException;

	/**
	 * Returns the {@link DatabaseType} of this database.
	 *
	 * @return The {@link DatabaseType} of this database.
	 */
	public DatabaseType getDatabaseType();
}
