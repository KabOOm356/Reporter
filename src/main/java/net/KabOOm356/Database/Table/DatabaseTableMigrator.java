package net.KabOOm356.Database.Table;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionMigrator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public abstract class DatabaseTableMigrator extends DatabaseTableUpdateHandler {
	private static final Logger log = LogManager.getLogger(DatabaseTableMigrator.class);

	protected DatabaseTableMigrator(final Database database, final String databaseVersion, final String tableName) {
		super(database, databaseVersion, tableName);
	}

	public void migrate() throws InterruptedException, SQLException, ClassNotFoundException {
		try {
			migrateTable();
		} catch (final InterruptedException e) {
			log.warn(String.format("Failed to migrate table [%s]!", getTableName()));
			throw e;
		} catch (final SQLException e) {
			log.warn(String.format("Failed to migrate table [%s]!", getTableName()));
			throw e;
		} catch (final ClassNotFoundException e) {
			log.warn(String.format("Failed to migrate table [%s]!", getTableName()));
			throw e;
		}
	}

	private void migrateTable() throws InterruptedException, SQLException, ClassNotFoundException {
		for (final DatabaseTableVersionMigrator versionMigrator : getDatabaseTableVersionMigrators()) {
			versionMigrator.migrate();
		}
	}

	public abstract List<DatabaseTableVersionMigrator> getDatabaseTableVersionMigrators();
}
