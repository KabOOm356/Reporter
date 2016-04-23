package net.KabOOm356.Database.Table;

import net.KabOOm356.Database.Database;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.sql.Statement;

public abstract class DatabaseTableUpdateHandler {
	private static final Logger log = LogManager.getLogger(DatabaseTableUpdateHandler.class);

	private final Database database;
	private final String databaseVersion;
	private final String tableName;

	private Integer connectionId;
	private Statement statement;

	private DatabaseTableUpdateHandler() {
		throw log.throwing(new InstantiationError("Empty constructor not supported!"));
	}

	protected DatabaseTableUpdateHandler(final Database database, final String databaseVersion, final String tableName) {
		this.database = database;
		this.databaseVersion = databaseVersion;
		this.tableName = tableName;
	}

	protected void startTransaction() throws InterruptedException, SQLException, ClassNotFoundException {
		if (!isTransactionInProgress()) {
			try {
				connectionId = getDatabase().openPooledConnection();
				statement = getDatabase().createStatement(connectionId);
			} catch (final InterruptedException e) {
				log.warn("Failed to start transaction!");
				terminateTransaction();
				endTransaction();
				throw e;
			} catch (final SQLException e) {
				log.warn("Failed to start transaction!");
				terminateTransaction();
				endTransaction();
				throw e;
			} catch (final ClassNotFoundException e) {
				log.warn("Failed to start transaction!");
				terminateTransaction();
				endTransaction();
				throw e;
			}
		}
	}

	protected void addQueryToTransaction(final String query) throws SQLException, IllegalStateException {
		if (log.isDebugEnabled()) {
			log.debug(String.format("Adding query [%s] to transaction!", query));
		}
		if (isTransactionInProgress()) {
			try {
				getStatement().addBatch(query);
			} catch (final SQLException e) {
				log.warn(String.format("Failed to add query [%s] to transaction!", query));
				// If an exception is thrown here, the transaction is still recoverable, do not terminate
				throw e;
			}
		} else {
			final IllegalStateException exception = new IllegalStateException(String.format("There is not a valid transaction started to add query [%s] to!", query));
			throw log.throwing(exception);
		}
	}

	protected void commitTransaction() throws SQLException, IllegalStateException {
		if (isTransactionInProgress()) {
			try {
				getStatement().executeBatch();
			} catch (final SQLException e) {
				log.warn("Failed to execute batch statement!");
				terminateTransaction();
				throw e;
			} finally {
				endTransaction();
			}
		}
	}

	protected Database getDatabase() {
		return database;
	}

	protected String getDatabaseVersion() {
		return databaseVersion;
	}

	protected Integer getConnectionId() {
		return connectionId;
	}

	protected Statement getStatement() {
		return statement;
	}

	public String getTableName() {
		return tableName;
	}

	public boolean isTransactionInProgress() {
		return connectionId != null || statement != null;
	}

	protected void terminateTransaction() {
		// TODO Rollback any changes that occurred during the transaction
		if (log.isDebugEnabled()) {
			log.info("Rolling back transaction!");
		}
	}

	protected void endTransaction() {
		if (isTransactionInProgress()) {
			try {
				if (getStatement() != null) {
					getStatement().close();
				}
			} catch (final SQLException e) {
				if (log.isDebugEnabled()) {
					log.debug("Suppressed exception while closing statement!", e);
				}
			}

			getDatabase().closeConnection(connectionId);
		}

		connectionId = null;
		statement = null;
	}
}
