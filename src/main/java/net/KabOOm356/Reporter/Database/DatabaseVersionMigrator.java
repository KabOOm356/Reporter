package net.KabOOm356.Reporter.Database;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Reporter.Reporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

public abstract class DatabaseVersionMigrator {
	private final Database database;
	private final String upgradeVersion;
	private Logger log = LogManager.getLogger(DatabaseVersionMigrator.class);

	protected DatabaseVersionMigrator(final Database database, final String upgradeVersion) {
		this.database = database;
		this.upgradeVersion = upgradeVersion;
	}

	/**
	 * Creates the temporary table that will be used to migrate the data.
	 *
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	protected abstract void createTemporaryTable() throws ClassNotFoundException, SQLException, InterruptedException;

	/**
	 * Drops the temporary table that was used to migrate the data.
	 *
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	protected abstract void dropTemporaryTable() throws ClassNotFoundException, SQLException, InterruptedException;

	/**
	 * Drops the main table that is being migrated.
	 *
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	protected abstract void dropTable() throws ClassNotFoundException, SQLException, InterruptedException;

	protected Database getDatabase() {
		return database;
	}

	public boolean migrate() {
		boolean updated = false;

		try {
			if (needsMigration()) {
				createTemporaryTable();
				dropTable();
				ReporterDatabaseUtil.createTables(database);
				migrateTable();
				dropTemporaryTable();

				updated = true;
			}
		} catch (final Exception ex) {
			log.fatal(Reporter.getDefaultConsolePrefix() + "An error occurred while upgrading database data to version " + upgradeVersion + "!", ex);
			log.fatal(Reporter.getDefaultConsolePrefix() + "If you receive more errors, you may have to delete your database!");
		} finally {
			database.closeConnection();
		}

		return updated;
	}

	/**
	 * Does the logic to migrate the data from the temporary table to the new main table.
	 *
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	protected abstract void migrateTable() throws ClassNotFoundException, SQLException, InterruptedException;

	/**
	 * Performs the logic to check if the table needs to be migrated.
	 *
	 * @return True if the table needs to be migrated, otherwise false.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	protected abstract boolean needsMigration() throws ClassNotFoundException, SQLException, InterruptedException;
}
