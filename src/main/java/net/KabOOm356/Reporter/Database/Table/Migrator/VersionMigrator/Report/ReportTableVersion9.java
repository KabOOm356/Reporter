package net.KabOOm356.Reporter.Database.Table.Migrator.VersionMigrator.Report;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Database.Table.DatabaseTableCreator;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionMigrator;
import net.KabOOm356.Util.DatabaseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportTableVersion9 extends DatabaseTableVersionMigrator {
	private static final Logger log = LogManager.getLogger(ReportTableVersion9.class);
	private static final String version = "8";

	private final ReportTableVersion9Creator creator;

	public ReportTableVersion9(final Database database, final String tableName) {
		super(database, version, tableName);

		creator = new ReportTableVersion9Creator(getDatabase(), getDatabaseVersion(), getTableName());
	}

	@Override
	protected String getCreateTemporaryTableQuery() {
		return "CREATE TABLE IF NOT EXISTS Version9Temporary (" +
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
	}

	@Override
	protected String getDropTemporaryTableQuery() {
		return "DROP TABLE IF EXISTS Version9Temporary";
	}

	@Override
	protected String getDropTableQuery() {
		return "DROP TABLE IF EXISTS Reports";
	}

	@Override
	protected DatabaseTableCreator getCreator() {
		return creator;
	}

	@Override
	protected String getMigrateTableQuery() {
		return "INSERT INTO Reports SELECT * FROM Version9Temporary";
	}

	@Override
	protected String getPopulateTemporaryTableQuery() {
		return "INSERT INTO Version9Temporary SELECT * FROM Reports";
	}

	@Override
	public boolean needsMigration() throws ClassNotFoundException, SQLException, InterruptedException {
		startTransaction();
		if (getDatabase().checkTable(getConnectionId(), "Reports")) {
			final SQLResultSet columns = getColumnMetaData();

			final String columnName = "COLUMN_NAME";
			final String columnSize = DatabaseUtil.getColumnsSizeName(getDatabase());

			final ResultRow senderRow = columns.get(columnName, "Sender");
			final ResultRow reportedRow = columns.get(columnName, "Reported");
			final ResultRow claimedByRow = columns.get(columnName, "ClaimedBy");
			final ResultRow completedByRow = columns.get(columnName, "CompletedByRow");
			if (senderRow != null && reportedRow != null && claimedByRow != null && completedByRow != null) {
				final boolean sender = senderRow.getString(columnSize).contains("32");
				final boolean reported = reportedRow.getString(columnSize).contains("32");
				final boolean claimedBy = claimedByRow.getString(columnSize).contains("32");
				final boolean completedBy = completedByRow.getString(columnSize).contains("32");
				return !sender && !reported && !claimedBy && !completedBy;
			} else {
				return false;
			}
		}

		return false;
	}

	private SQLResultSet getColumnMetaData() throws SQLException {
		ResultSet rs = null;

		try {
			rs = getDatabase().getColumnMetaData(getConnectionId(), "Reports");

			return new SQLResultSet(rs);
		} catch (final SQLException e) {
			log.warn("Failed to get column meta data!");
			throw e;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (final SQLException e) {
				if (log.isDebugEnabled()) {
					log.warn("Failed to close ResultSet!", e);
				}
			}
		}
	}

	private static final class ReportTableVersion9Creator extends DatabaseTableCreator {
		private ReportTableVersion9Creator(final Database database, final String databaseVersion, final String tableName) {
			super(database, databaseVersion, tableName);
		}

		@Override
		protected String getTableCreationQuery() {
			return "CREATE TABLE IF NOT EXISTS Reports (" +
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
		}
	}
}
