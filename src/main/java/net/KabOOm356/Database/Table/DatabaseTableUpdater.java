package net.KabOOm356.Database.Table;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionUpdater;
import net.KabOOm356.Reporter.Reporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public abstract class DatabaseTableUpdater extends DatabaseTableUpdateHandler {
	private static final Logger log = LogManager.getLogger(DatabaseTableUpdater.class);

	public DatabaseTableUpdater(final Database database, final String updateVersion, final String tableName) {
		super(database, updateVersion, tableName);
	}

	public void update() throws InterruptedException, SQLException, ClassNotFoundException {
		try {
			log.info(Reporter.getDefaultConsolePrefix() + String.format("Updating table [%s]...", getTableName()));
			updateTable();
		} catch (final InterruptedException e) {
			log.warn(String.format("Failed to update table [%s]!", getTableName()));
			throw e;
		} catch (final SQLException e) {
			log.warn(String.format("Failed to update table [%s]!", getTableName()));
			throw e;
		} catch (final ClassNotFoundException e) {
			log.warn(String.format("Failed to update table [%s]!", getTableName()));
			throw e;
		} finally {
			try {
				commitTransaction();
			} catch (final IllegalStateException e) {
				log.error("Failed to commit update transaction!");
				throw e;
			} catch (final SQLException e) {
				log.error("Failed to commit update transaction!");
				throw e;
			}
		}
	}

	private void updateTable() throws InterruptedException, SQLException, ClassNotFoundException {
		for (final DatabaseTableVersionUpdater versionUpdater : getDatabaseTableVersionUpdaters()) {
			versionUpdater.update();
		}
	}

	public abstract List<DatabaseTableVersionUpdater> getDatabaseTableVersionUpdaters();
}
