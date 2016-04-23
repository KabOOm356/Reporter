package net.KabOOm356.Database.Table.Version;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.DatabaseTableUpdateHandler;
import net.KabOOm356.Reporter.Reporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public abstract class DatabaseTableVersionUpdater extends DatabaseTableUpdateHandler {
	private static final Logger log = LogManager.getLogger(DatabaseTableVersionUpdater.class);

	private List<String> columns;

	public DatabaseTableVersionUpdater(final Database database, final String version, final String tableName) {
		super(database, version, tableName);
	}

	public void update() throws InterruptedException, SQLException, ClassNotFoundException {
		try {
			startTransaction();
			if (needsToUpdate()) {
				log.info(Reporter.getDefaultConsolePrefix() + String.format("Updating table [%s] to version [%s]...", getTableName(), getDatabaseVersion()));
				apply();
			} else {
				log.info(String.format(Reporter.getDefaultConsolePrefix() + "Table [%s] is up-to-date with version [%s].", getTableName(), getDatabaseVersion()));
			}
		} catch (final InterruptedException e) {
			log.warn(String.format("Failed to update table [%s] to version [%s]!", getTableName(), getDatabaseVersion()));
			throw e;
		} catch (final SQLException e) {
			log.warn(String.format("Failed to update table [%s] to version [%s]!", getTableName(), getDatabaseVersion()));
			throw e;
		} catch (final ClassNotFoundException e) {
			log.warn(String.format("Failed to update table [%s] to version [%s]!", getTableName(), getDatabaseVersion()));
			throw e;
		} finally {
			try {
				commitTransaction();
			} catch (final IllegalStateException e) {
				log.warn(String.format("Failed to commit transaction while updating table [%s] to version [%s]!", getTableName(), getDatabaseVersion()));
				throw e;
			} catch (final SQLException e) {
				log.warn(String.format("Failed to commit transaction while updating table [%s] to version [%s]!", getTableName(), getDatabaseVersion()));
				throw e;
			}
		}
	}

	@Override
	protected void startTransaction() throws InterruptedException, SQLException, ClassNotFoundException {
		super.startTransaction();
		columns = getDatabase().getColumnNames(getConnectionId(), getTableName());
	}

	protected List<String> getColumns() {
		return columns;
	}

	protected abstract boolean needsToUpdate() throws InterruptedException, SQLException, ClassNotFoundException;

	protected abstract void apply() throws SQLException, ClassNotFoundException, InterruptedException;
}
