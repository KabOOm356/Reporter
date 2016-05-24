package net.KabOOm356.Database;

import net.KabOOm356.Database.Connection.*;
import net.KabOOm356.Util.ArrayUtil;
import net.KabOOm356.Util.DatabaseUtil;
import net.KabOOm356.Util.FormattingUtil;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * A simple class to handle connections and queries to a database.
 */
public class Database implements DatabaseInterface, ConnectionPooledDatabaseInterface, ConnectionPoolManager {
	private static final Logger log = LogManager.getLogger(Database.class);
	/**
	 * A random number generator for generating connection ids.
	 */
	private static final Random idGenerator = new Random();

	/**
	 * The {@link DatabaseType} representation of the type of this database.
	 */
	private final DatabaseType databaseType;
	/**
	 * The database driver represented as a String.
	 */
	private final String databaseDriver;
	/**
	 * The URL to the database as a String.
	 */
	private final String connectionURL;
	/**
	 * The pool of connections.
	 */
	private final HashMap<Integer, ConnectionWrapper> connectionPool;
	private final ConnectionPoolConfig connectionPoolConfig;
	/**
	 * The id of a connection that is being accessed in a non-pooled manner.
	 */
	private Integer localConnectionId;

	/**
	 * Database Constructor.
	 *
	 * @param databaseType   The {@link DatabaseType} that is being constructed.
	 * @param databaseDriver The driver to use for the connection to the database.
	 * @param connectionURL  The URL of the database to connect to.
	 */
	public Database(final DatabaseType databaseType, final String databaseDriver, final String connectionURL, final ConnectionPoolConfig connectionPoolConfig) {
		Validate.notNull(connectionPoolConfig, "Parameter 'connectionPoolConfig' cannot be null!");
		this.databaseType = databaseType;
		this.databaseDriver = databaseDriver;
		this.connectionURL = connectionURL;
		this.connectionPoolConfig = connectionPoolConfig;
		localConnectionId = null;
		connectionPool = new HashMap<Integer, ConnectionWrapper>();
	}

	@Override
	public void openConnection() throws ClassNotFoundException, SQLException, InterruptedException {
		if (localConnectionId != null) {
			throw new IllegalStateException("There is already an open non-pooled connection in use!");
		}
		localConnectionId = openPooledConnection();
		if (log.isDebugEnabled()) {
			log.debug("New non-pooled connection created with id [" + localConnectionId + ']');
		}
	}

	@Override
	public synchronized int openPooledConnection() throws ClassNotFoundException, SQLException, InterruptedException {
		return openPooledConnection(null, null);
	}

	private boolean isConnectionSlotAvailable() {
		// A connection slot is available if the size of the pool is less than the max number of connections allowed
		final boolean isConnectionSlotAvailable = connectionPool.size() < connectionPoolConfig.getMaxConnections();
		if (log.isDebugEnabled()) {
			if (isConnectionSlotAvailable) {
				log.debug("New connection slot is available in the connection pool");
			} else {
				log.debug("No connection slot available in the connection pool");
			}
		}
		return isConnectionSlotAvailable;
	}

	/**
	 * Attempts to open a connection to the database using a username and
	 * password.
	 *
	 * @param username The username to login to the database with.
	 * @param password The password for the user associated with the username.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 * @throws IllegalStateException  Thrown if there is already a non-pooled connection open.
	 */
	protected void openConnection(final String username, final String password) throws SQLException, ClassNotFoundException, InterruptedException {
		if (localConnectionId != null) {
			throw new IllegalStateException("There is already an open connection in use!");
		}
		localConnectionId = openPooledConnection(username, password);
		if (log.isDebugEnabled()) {
			log.debug("New non-pooled connection created with id [" + localConnectionId + ']');
		}
	}

	/**
	 * Attempts to open a pooled connection to the database using a username and
	 * password.
	 *
	 * @param username The username to login to the database with.
	 * @param password The password for the user associated with the username.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	protected synchronized int openPooledConnection(final String username, final String password) throws ClassNotFoundException, SQLException, InterruptedException {
		try {
			synchronized (connectionPool) {
				final long startWaitTime = System.currentTimeMillis();
				boolean isWaiting = false;
				int updateCount = 0;
				long currentWaitTime = 0;
				// While a connection slot is unavailable
				while (!isConnectionSlotAvailable()) {
					if (!isWaiting) {
						if (log.isDebugEnabled()) {
							log.warn("Thread has begun waiting on new connection to become available");
						}
					} else {
						currentWaitTime = System.currentTimeMillis() - startWaitTime;
						if (log.isDebugEnabled()) {
							log.warn(String.format("Thread has been waiting for a new connection for [%dms] this is update [%d]; possible bottleneck!", currentWaitTime, updateCount));
						}
						if (connectionPoolConfig.isConnectionPoolLimited() && updateCount >= connectionPoolConfig.getMaxAttemptsForConnection()) {
							log.warn("Thread has reached the max number of updates! Cancelling operation!");
							throw new InterruptedException(String.format("Thread has reached the cycle limit [%d] after waiting for [%dms] for a new connection!", connectionPoolConfig.getMaxAttemptsForConnection(), currentWaitTime));
						}
					}
					connectionPool.wait(connectionPoolConfig.getWaitTimeBeforeUpdate());
					isWaiting = true;
					updateCount++;
				}
				currentWaitTime = System.currentTimeMillis() - startWaitTime;
				if (isWaiting && log.isDebugEnabled()) {
					log.debug(String.format("A connection is now available in the connection pool after waiting [%dms]... proceeding!", currentWaitTime));
				}
			}
		} catch (final InterruptedException e) {
			if (log.isDebugEnabled()) {
				log.warn("Waiting for available connection was interrupted!");
			}
			throw e;
		}
		try {
			Class.forName(databaseDriver);
			final Connection connection;
			if (username == null || password == null) {
				connection = DriverManager.getConnection(connectionURL);
			} else {
				connection = DriverManager.getConnection(connectionURL, username, password);
			}
			int connectionId;
			do {
				connectionId = idGenerator.nextInt();
			} while (connectionPool.containsKey(connectionId));
			final ConnectionWrapper ConnectionWrapper = new AlertingPooledConnection(this, connectionId, connection);
			connectionPool.put(connectionId, ConnectionWrapper);
			if (log.isDebugEnabled()) {
				log.debug("New pooled connection created with id [" + connectionId + ']');
				log.debug("Connection pool size [" + connectionPool.size() + ']');
			}
			return connectionId;
		} catch (final ClassNotFoundException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to open connection to database!");
			}
			throw e;
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to open connection to database!");
			}
			throw e;
		}
	}

	@Override
	public ResultSet query(final String query) throws ClassNotFoundException, SQLException, InterruptedException {
		openNonPooledConnection();
		return query(localConnectionId, query);
	}

	@Override
	public void updateQuery(final String query) throws ClassNotFoundException, SQLException, InterruptedException {
		openNonPooledConnection();
		updateQuery(localConnectionId, query);
	}

	@Override
	public ResultSet preparedQuery(final String query, final ArrayList<String> params) throws ClassNotFoundException, SQLException, InterruptedException {
		openNonPooledConnection();
		return preparedQuery(localConnectionId, query, params);
	}

	@Override
	public void preparedUpdateQuery(final String query, final ArrayList<String> params) throws ClassNotFoundException, SQLException, InterruptedException {
		openNonPooledConnection();
		preparedUpdateQuery(localConnectionId, query, params);
	}

	@Override
	public boolean checkTable(final String table) throws ClassNotFoundException, SQLException, InterruptedException {
		openNonPooledConnection();
		return checkTable(localConnectionId, table);
	}

	@Override
	public ArrayList<String> getColumnNames(final String table) throws SQLException, ClassNotFoundException, InterruptedException {
		openNonPooledConnection();
		return getColumnNames(localConnectionId, table);
	}

	@Override
	public DatabaseMetaData getMetaData() throws ClassNotFoundException, SQLException, InterruptedException {
		openNonPooledConnection();
		return getMetaData(localConnectionId);
	}

	@Override
	public ResultSet getColumnMetaData(final String table) throws ClassNotFoundException, SQLException, InterruptedException {
		openNonPooledConnection();
		return getColumnMetaData(localConnectionId, table);
	}

	@Override
	public void closeConnection() {
		if (localConnectionId != null) {
			if (log.isDebugEnabled()) {
				log.debug("Closing non-pooled connection with id [" + localConnectionId + ']');
			}
			closeConnection(localConnectionId);
			localConnectionId = null;
		}
	}

	@Override
	public void closeConnection(final Integer connectionId) {
		if (doesConnectionExist(connectionId)) {
			final ConnectionWrapper connection = getConnection(connectionId);
			try {
				if (!connection.isClosed()) {
					if (log.isDebugEnabled()) {
						log.debug("Closing pooled connection with id [" + connectionId + ']');
					}
					connection.close();
				}
			} catch (final SQLException e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to close connection with id [" + connectionId + "]!", e);
				}
			}
		} else {
			if (log.isDebugEnabled()) {
				log.warn("Connection with id [" + connectionId + "] is not in the connection pool!");
			}
		}
	}

	@Override
	public void closeConnections() {
		if (log.isDebugEnabled()) {
			log.info("Closing all connections!");
			log.info("Current connection pool size: " + connectionPool.size());
		}
		// Close the local connection
		closeConnection();
		// Start closing all connections in the pool
		Integer[] connectionIds = new Integer[connectionPool.size()];
		connectionIds = connectionPool.keySet().toArray(connectionIds);
		for (final Integer connectionId : connectionIds) {
			closeConnection(connectionId);
		}
		if (log.isDebugEnabled()) {
			log.info("All connections closed!");
		}
	}

	@Override
	public Statement createStatement() throws SQLException, ClassNotFoundException, InterruptedException {
		openNonPooledConnection();
		return createStatement(localConnectionId);
	}

	@Override
	public PreparedStatement prepareStatement(final String query) throws SQLException, ClassNotFoundException, InterruptedException {
		openNonPooledConnection();
		return prepareStatement(localConnectionId, query);
	}

	@Override
	public DatabaseType getDatabaseType() {
		return databaseType;
	}

	@Override
	public void connectionClosed(final Integer connectionId) {
		if (log.isDebugEnabled()) {
			log.debug("Connection close detected for connection with id [" + connectionId + ']');
		}
		removeConnectionFromPool(connectionId);
	}

	@Override
	public ResultSet query(final Integer connectionId, final String query) throws SQLException {
		try {
			return createStatement(connectionId).executeQuery(query);
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to execute query!");
			}
			throw e;
		}
	}

	@Override
	public void updateQuery(final Integer connectionId, final String query) throws SQLException {
		Statement statement = null;
		try {
			statement = createStatement(connectionId);
			try {
				statement.executeUpdate(query);
			} catch (final SQLException e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to execute update query!");
				}
				throw e;
			}
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (final Exception e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to close statement!", e);
				}
			}
			closeConnection(connectionId);
		}
	}

	@Override
	public ResultSet preparedQuery(final Integer connectionId, final String query, final ArrayList<String> params) throws SQLException {
		final PreparedStatement preparedStatement = prepareStatement(connectionId, query);
		bindParametersToPreparedStatement(preparedStatement, query, params);

		try {
			return preparedStatement.executeQuery();
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to execute query!");
			}
			throw e;
		}
	}

	@Override
	public void preparedUpdateQuery(final Integer connectionId, final String query, final ArrayList<String> params) throws SQLException {
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = prepareStatement(connectionId, query);
			bindParametersToPreparedStatement(preparedStatement, query, params);

			try {
				preparedStatement.executeUpdate();
			} catch (final SQLException e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to execute prepared query!");
				}
				throw e;
			}
		} finally {
			try {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (final Exception e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to close prepared statement!", e);
				}
			}
			closeConnection(connectionId);
		}
	}

	private void bindParametersToPreparedStatement(final PreparedStatement preparedStatement, final String query, final List<String> parameters) throws SQLException {
		try {
			DatabaseUtil.bindParametersToPreparedStatement(preparedStatement, query, parameters);
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.warn("Failed to bind parameters to prepared statement!");
			}
			throw e;
		}
	}

	@Override
	public boolean checkTable(final Integer connectionId, final String table) throws SQLException {
		ResultSet tables = null;

		try {
			final DatabaseMetaData dbm;
			final ConnectionWrapper connection = getConnection(connectionId);
			try {
				dbm = connection.getMetaData();
			} catch (final SQLException e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to get connection meta data!");
				}
				throw e;
			}

			try {
				tables = dbm.getTables(null, null, table, null);
			} catch (final SQLException e) {
				if (log.isDebugEnabled()) {
					log.log(Level.WARN, "Failed to get tables from connection meta data!");
				}
				throw e;
			}

			return tables.next();
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to check table!");
			}
			throw e;
		} finally {
			try {
				if (tables != null) {
					tables.close();
				}
			} catch (final Exception e) {
				if (log.isDebugEnabled()) {
					log.log(Level.DEBUG, "Failed to close ResultSet!");
				}
			}
		}
	}

	@Override
	public ArrayList<String> getColumnNames(final Integer connectionId, final String table) throws SQLException {
		final ArrayList<String> col = new ArrayList<String>();
		ResultSet rs = null;
		try {
			rs = getColumnMetaData(connectionId, table);
			while (rs.next()) {
				col.add(rs.getString("COLUMN_NAME"));
			}
			return col;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (final Exception e) {
				if (log.isDebugEnabled()) {
					log.log(Level.DEBUG, "Failed to close ResultSet!", e);
				}
			}
		}
	}

	@Override
	public DatabaseMetaData getMetaData(final Integer connectionId) throws SQLException {
		final Connection connection = getConnection(connectionId);
		try {
			return connection.getMetaData();
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.DEBUG, "Failed to get meta data from the connection!");
			}
			throw e;
		}
	}

	@Override
	public ResultSet getColumnMetaData(final Integer connectionId, final String table) throws SQLException {
		try {
			return getMetaData(connectionId).getColumns(null, null, table, null);
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.DEBUG, "Failed to get table columns!");
			}
			throw e;
		}
	}

	@Override
	public Statement createStatement(final Integer connectionId) throws SQLException {
		final Connection connection = getConnection(connectionId);
		try {
			return connection.createStatement();
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to create statement!");
			}
			throw e;
		}
	}

	@Override
	public PreparedStatement prepareStatement(final Integer connectionId, final String query) throws SQLException {
		final Connection connection = getConnection(connectionId);
		try {
			return connection.prepareStatement(query);
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to prepare statement!");
			}
			throw e;
		}
	}

	private boolean doesConnectionExist(final Integer connectionId) {
		return connectionPool.containsKey(connectionId);
	}

	private ConnectionWrapper getConnection(final Integer connectionId) {
		Validate.notNull(connectionId, "Connection id cannot be null!");
		if (doesConnectionExist(connectionId)) {
			return connectionPool.get(connectionId);
		}
		throw new IllegalArgumentException("Connection with connection id [" + connectionId + "] does not exist!");
	}

	private void removeConnectionFromPool(final int connectionId) {
		if (log.isDebugEnabled()) {
			log.debug("Removing connection with id [" + connectionId + "] from connection pool");
		}
		if (localConnectionId != null && connectionId == localConnectionId) {
			localConnectionId = null;
		}
		synchronized (connectionPool) {
			connectionPool.remove(connectionId);
			// Notify any threads waiting to open a new connection.
			connectionPool.notify();
		}
		if (log.isDebugEnabled()) {
			log.debug("Current connection pool size [" + connectionPool.size() + ']');
		}
	}

	private void openNonPooledConnection() throws ClassNotFoundException, SQLException, InterruptedException {
		if (localConnectionId == null) {
			openConnection();
		}
	}

	@Override
	public String toString() {
		final String toString = "Database Type: " + databaseType.toString() +
				"\nDatabase Driver: " + databaseDriver +
				"\nConnection URL: " + connectionURL +
				"\nConnection Pool Size: " + connectionPool.size() +
				"\nConnection Pool: " +
				'\n' + ArrayUtil.indexesToString(connectionPool.keySet());
		return FormattingUtil.addTabsToNewLines(toString, 1);
	}
}
