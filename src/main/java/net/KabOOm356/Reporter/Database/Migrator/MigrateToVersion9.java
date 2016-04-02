package net.KabOOm356.Reporter.Database.Migrator;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Reporter.Database.DatabaseVersionMigrator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MigrateToVersion9 extends DatabaseVersionMigrator {
	private static final Logger log = LogManager.getLogger(MigrateToVersion9.class);
	private static final String upgradeVersion = "9";

	public MigrateToVersion9(final Database database) {
		super(database, upgradeVersion);
	}

	@Override
	protected void createTemporaryTable() throws ClassNotFoundException, SQLException, InterruptedException {
		// @formatter:off
		final String query = "CREATE TABLE IF NOT EXISTS Version9Temporary (" +
				"ID INTEGER PRIMARY KEY, " +
				"Date CHAR(19) NOT NULL DEFAULT 'N/A', " +
				"SenderUUID CHAR(36) DEFAULT '', " +
				"Sender VARCHAR(32), " +
				"ReportedUUID CHAR(36) DEFAULT '', " +
				"Reported VARCHAR(32) NOT NULL DEFAULT '* (Anonymous)', " +
				"Details VARCHAR(200) NOT NULL, " +
				"Priority TINYINT NOT NULL DEFAULT '0', " +
				"SenderWorld VARCHAR(100) DEFAULT '', " +
				"SenderX DOUBLE NOT NULL DEFAULT '0.0', " +
				"SenderY DOUBLE NOT NULL DEFAULT '0.0', " +
				"SenderZ DOUBLE NOT NULL DEFAULT '0.0', " +
				"ReportedWorld VARCHAR(100) DEFAULT '', " +
				"ReportedX DOUBLE DEFAULT '0.0', " +
				"ReportedY DOUBLE DEFAULT '0.0', " +
				"ReportedZ DOUBLE DEFAULT '0.0', " +
				"CompletionStatus BOOLEAN NOT NULL DEFAULT '0', " +
				"CompletedByUUID CHAR(36) DEFAULT '', " +
				"CompletedBy VARCHAR(32) DEFAULT '', " +
				"CompletionDate CHAR(19) DEFAULT '', " +
				"CompletionSummary VARCHAR(200) DEFAULT '', " +
				"ClaimStatus BOOLEAN NOT NULL DEFAULT '0', " +
				"ClaimDate CHAR(19) DEFAULT '', " +
				"ClaimedByUUID CHAR(36) DEFAULT '', " +
				"ClaimedBy VARCHAR(32) DEFAULT '', " +
				"ClaimPriority TINYINT DEFAULT '0');";
		// @formatter:on

		getDatabase().updateQuery(query);

		getDatabase().updateQuery("INSERT INTO Version9Temporary SELECT * FROM Reports");
	}

	@Override
	protected void dropTemporaryTable() throws ClassNotFoundException, SQLException, InterruptedException {
		getDatabase().updateQuery("DROP TABLE IF EXISTS Version9Temporary");
	}

	@Override
	protected void dropTable() {
	}

	@Override
	protected void migrateTable() throws ClassNotFoundException, SQLException, InterruptedException {
		final String query = "INSERT INTO Reports SELECT * FROM Version9Temporary";
		getDatabase().updateQuery(query);
	}

	@Override
	protected boolean needsMigration() throws ClassNotFoundException, SQLException, InterruptedException {
		final Database database = getDatabase();
		if (database.checkTable("Reports")) {
			String columnName = "COLUMN_NAME";
			String columnSize = "COLUMN_SIZE";

			if (database.getDatabaseType() == DatabaseType.SQLITE) {
				columnSize = "TYPE_NAME";
			}

			SQLResultSet cols = new SQLResultSet();
			ResultSet rs = null;

			try {
				rs = database.getColumnMetaData("Reports");

				cols.set(rs);
			} finally {
				try {
					rs.close();
				} catch (final SQLException e) {
					if (log.isDebugEnabled()) {
						log.log(Level.WARN, "Failed to close ResultSet!", e);
					}
				}

				database.closeConnection();
			}

			boolean sender = cols.get(columnName, "Sender").getString(columnSize).contains("32");
			boolean reported = cols.get(columnName, "Reported").getString(columnSize).contains("32");
			boolean claimedBy = cols.get(columnName, "ClaimedBy").getString(columnSize).contains("32");
			boolean completedBy = cols.get(columnName, "CompletedBy").getString(columnSize).contains("32");

			if (!sender && !reported && !claimedBy && !completedBy) {
				return true;
			}
		}

		return false;
	}
}