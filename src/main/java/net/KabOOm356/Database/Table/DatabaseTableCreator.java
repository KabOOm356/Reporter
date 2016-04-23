package net.KabOOm356.Database.Table;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Reporter.Reporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

public abstract class DatabaseTableCreator extends DatabaseTableUpdateHandler {
	private static final Logger log = LogManager.getLogger(DatabaseTableCreator.class);

	public DatabaseTableCreator(final Database database, final String databaseVersion, final String tableName) {
		super(database, databaseVersion, tableName);
	}

	public void create() throws InterruptedException, SQLException, ClassNotFoundException {
		try {
			startTransaction();
			if (needsToCreateTable()) {
				log.info(Reporter.getDefaultConsolePrefix() + "Creating " + getTableName() + " table...");
				createTable();
			} else {
				log.info(Reporter.getDefaultConsolePrefix() + "Using existing " + getTableName() + " table.");
			}
		} catch (final InterruptedException e) {
			log.error(String.format("Failed to create table [%s]!", getTableName()));
			throw e;
		} catch (final SQLException e) {
			log.error(String.format("Failed to create table [%s]!", getTableName()));
			throw e;
		} catch (final ClassNotFoundException e) {
			log.error(String.format("Failed to create table [%s]!", getTableName()));
			throw e;
		} finally {
			try {
				commitTransaction();
			} catch (final IllegalStateException e) {
				log.warn(String.format("Failed to commit transaction while creating table [%s]!", getTableName()));
				throw e;
			} catch (final SQLException e) {
				log.warn(String.format("Failed to commit transaction while creating table [%s]!", getTableName()));
				throw e;
			}
		}
	}

	private void createTable() throws InterruptedException, SQLException, ClassNotFoundException {
		final String query = getTableCreationQuery();
		if (log.isDebugEnabled()) {
			log.trace(String.format("Creating table [%s].", getTableName()), query);
		}
		addQueryToTransaction(query);
	}

	private boolean needsToCreateTable() throws InterruptedException, SQLException, ClassNotFoundException {
		if (log.isDebugEnabled()) {
			log.trace(String.format("Checking if table [%s] exists.", getTableName()));
		}
		return !getDatabase().checkTable(getConnectionId(), getTableName());
	}

	protected abstract String getTableCreationQuery();
}
