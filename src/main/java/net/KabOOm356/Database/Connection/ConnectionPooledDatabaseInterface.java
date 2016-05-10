package net.KabOOm356.Database.Connection;

import java.sql.*;
import java.util.ArrayList;

/**
 * A database that can have multiple connections in use at the same time.
 */
public interface ConnectionPooledDatabaseInterface {
	int openPooledConnection() throws ClassNotFoundException, SQLException, InterruptedException;

	/**
	 * Closes a pooled connection.
	 *
	 * @param connectionId The id of the connection to close.
	 */
	void closeConnection(final Integer connectionId);

	/**
	 * Closes all currently open connections.
	 * <br/><br/>
	 * This should only be called when the database is being closed.
	 */
	void closeConnections();

	/**
	 * Attempts to perform a query on the database and returns a ResultSet of data.
	 * <br /><br />
	 * <b>NOTE:</b> closeConnection() should be called after calling this method.
	 *
	 * @param connectionId The id of the connection to execute on.
	 * @param query        The string to query the database with.
	 * @return A {@link ResultSet} of information returned from the database.
	 * @throws SQLException
	 */
	ResultSet query(Integer connectionId, String query) throws SQLException;

	/**
	 * Attempts to perform a query on the database that returns no data.
	 *
	 * @param connectionId The id of the connection to execute on.
	 * @param query        The string to query the database with.
	 * @throws SQLException
	 */
	void updateQuery(Integer connectionId, String query) throws SQLException;

	/**
	 * Attempts to perform a prepared query on the database that returns a ResultSet of data.
	 * <br /><br />
	 * <b>NOTE:</b> closeConnection() should be called after calling this method.
	 *
	 * @param connectionId The id of the connection to execute on.
	 * @param query        The string to query the database with.
	 * @param params       The parameters of the query.
	 * @return Returns a {@link ResultSet} of data if there are enough entries in params.
	 * @throws SQLException
	 * @throws IllegalArgumentException If the number of parameters given do not match the number of parameters required.
	 */
	ResultSet preparedQuery(Integer connectionId, String query, ArrayList<String> params) throws SQLException;

	/**
	 * Attempts to perform a query on the database that returns no data.
	 *
	 * @param connectionId The id of the connection to execute on.
	 * @param query        The string to query the database with.
	 * @param params       The parameters of the query.
	 * @throws SQLException
	 * @throws IllegalArgumentException If the number of parameters given do not match the number of parameters required.
	 */
	void preparedUpdateQuery(Integer connectionId, String query, ArrayList<String> params) throws SQLException;

	/**
	 * Checks the database if a table exists.
	 *
	 * @param connectionId The id of the connection to execute on.
	 * @param table        The name of the table to check for.
	 * @return If the table exists then returns true, otherwise false.
	 * @throws SQLException
	 */
	boolean checkTable(Integer connectionId, String table) throws SQLException;

	/**
	 * Returns the columns in a table.
	 *
	 * @param connectionId The id of the connection to execute on.
	 * @param table        The name of the table to get the columns from.
	 * @return An {@link ArrayList} containing the names of the columns.
	 * @throws SQLException
	 */
	ArrayList<String> getColumnNames(Integer connectionId, String table) throws SQLException;

	/**
	 * Returns the database's meta data.
	 * <br /><br />
	 * <b>NOTE:</b> closeConnection() should be called after calling this method.
	 *
	 * @param connectionId The id of the connection to execute on.
	 * @return A {@link DatabaseMetaData} object.
	 * @throws SQLException
	 */
	DatabaseMetaData getMetaData(Integer connectionId) throws SQLException;

	/**
	 * Returns the database's column meta data.
	 * <br /><br />
	 * <b>NOTE:</b> closeConnection() should be called after calling this method.
	 *
	 * @param connectionId The id of the connection to execute on.
	 * @param table        The name of the table to get the meta data for.
	 * @return A {@link ResultSet} containing the database's column meta data.
	 * @throws SQLException
	 */
	ResultSet getColumnMetaData(Integer connectionId, String table) throws SQLException;

	/**
	 * Returns a Statement from the database connection.
	 *
	 * @param connectionId The id of the connection to execute on.
	 * @return A Statement from the database.
	 * @throws SQLException
	 */
	Statement createStatement(Integer connectionId) throws SQLException;

	/**
	 * Returns a PreparedStatement from the database connection.
	 *
	 * @param connectionId The id of the connection to execute on.
	 * @param query        The SQL query to create the PreparedStatement from.
	 * @return A PreparedStatement created from the given query.
	 * @throws SQLException
	 */
	PreparedStatement prepareStatement(Integer connectionId, String query) throws SQLException;
}
