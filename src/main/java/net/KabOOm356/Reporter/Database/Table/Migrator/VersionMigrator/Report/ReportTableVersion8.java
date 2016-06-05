package net.KabOOm356.Reporter.Database.Table.Migrator.VersionMigrator.Report;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Database.Table.DatabaseTableCreator;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionMigrator;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.BukkitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.OfflinePlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ReportTableVersion8 extends DatabaseTableVersionMigrator {
	private static final Logger log = LogManager.getLogger(ReportTableVersion8.class);
	private static final String version = "8";

	private final ReportTableVersion8Creator creator;

	public ReportTableVersion8(final Database database, final String tableName) {
		super(database, version, tableName);

		creator = new ReportTableVersion8Creator(getDatabase(), getDatabaseVersion(), getTableName());
	}

	private static String getPlayerUUID(final String playerName) {
		if (playerName.equalsIgnoreCase(Reporter.console) || playerName.equalsIgnoreCase(Reporter.anonymousPlayerName)) {
			return "";
		}
		final OfflinePlayer player = BukkitUtil.getOfflinePlayer(null, playerName);
		return BukkitUtil.getUUIDString(player);
	}

	@Override
	protected String getCreateTemporaryTableQuery() {
		return "CREATE TABLE IF NOT EXISTS Version8Temporary (" +
				"ID INTEGER PRIMARY KEY, " +
				"Date VARCHAR(19) NOT NULL DEFAULT 'N/A', " +
				"Sender VARCHAR(50) NOT NULL, " +
				"SenderRaw VARCHAR(16) NOT NULL, " +
				"Reported VARCHAR(50) NOT NULL DEFAULT '* (Anonymous)', " +
				"ReportedRaw VARCHAR(16) NOT NULL DEFAULT '* (Anonymous)', " +
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
				"CompletedBy VARCHAR(50), " +
				"CompletedByRaw VARCHAR(16), " +
				"CompletionDate VARCHAR(19), " +
				"CompletionSummary VARCHAR(200), " +
				"ClaimStatus BOOLEAN NOT NULL DEFAULT '0', " +
				"ClaimDate VARCHAR(19), " +
				"ClaimedBy VARCHAR(50), " +
				"ClaimedByRaw VARCHAR(16), " +
				"ClaimPriority TINYINT);";
	}

	@Override
	protected String getDropTemporaryTableQuery() {
		return "DROP TABLE IF EXISTS Version8Temporary";
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
		return "INSERT INTO Reports SELECT * FROM Version8Temporary";
	}

	@Override
	protected String getPopulateTemporaryTableQuery() {
		return "INSERT INTO Version8Temporary SELECT * FROM Reports";
	}

	@Override
	public boolean needsMigration() throws ClassNotFoundException, SQLException, InterruptedException {
		startTransaction();
		final Integer connectionId = getConnectionId();
		if (getDatabase().checkTable(connectionId, "Reports")) {
			final List<String> columns = getDatabase().getColumnNames(connectionId, "Reports");

			return !columns.contains("SenderUUID") &&
					!columns.contains("ReportedUUID") &&
					!columns.contains("ClaimedByUUID") &&
					!columns.contains("CompletedByUUID") &&
					columns.contains("SenderRaw") &&
					columns.contains("ReportedRaw") &&
					columns.contains("ClaimedByRaw") &&
					columns.contains("CompletedByRaw");
		}

		return false;
	}

	@Override
	protected void populateTemporaryTable() throws InterruptedException, SQLException, ClassNotFoundException {
		try {
			startTransaction();
			prePopulateTemporaryTable();
			// We need all prior DDL to be committed at this point.
			// So we can begin DML on a separate statement.
			commitTransaction();
			convertData();
			startTransaction();
		} catch (final InterruptedException e) {
			log.warn("Failed to populate temporary table!");
			throw e;
		} catch (final SQLException e) {
			log.warn("Failed to populate temporary table!");
			throw e;
		} catch (final ClassNotFoundException e) {
			log.warn("Failed to populate temporary table!");
			throw e;
		}
	}

	private void prePopulateTemporaryTable() throws SQLException {
		try {
			addQueryToTransaction("INSERT INTO Version8Temporary" +
					"SELECT ID, Date, Details, Priority, " +
					"SenderWorld, SenderX, SenderY, SenderZ, ReportedWorld, ReportedX, ReportedY, ReportedZ, " +
					"CompletionStatus, CompletionDate, CompletionSummary, " +
					"ClaimStatus, ClaimDate, ClaimPriority" +
					"FROM Reports");
		} catch (final SQLException e) {
			log.warn("Failed to pre-populate temporary table!");
			throw e;
		}
	}

	private void convertData() throws SQLException {
		try {
			final SQLResultSet conversionData = getConversionData();
			final PreparedStatement preparedStatement = getDatabase().prepareStatement(getConnectionId(),
					"UPDATE Reports " +
							"SET SenderUUID=?, Sender=?, ReportedUUID=?, Reported=?, ClaimedByUUID=?, ClaimedBy=?, CompletedByUUID=?, CompletedBy=? " +
							"WHERE ID=?");
			for (final ResultRow row : conversionData) {
				final int id = row.getInt("ID");

				final String sender = row.getString("SenderRaw");
				final String reported = row.getString("ReportedRaw");
				final String claimedBy = row.getString("ClaimedByRaw");
				final String completedBy = row.getString("CompletedByRaw");

				final String senderUUID = getPlayerUUID(sender);
				final String reportedUUID = getPlayerUUID(reported);
				final String claimedByUUID = getPlayerUUID(claimedBy);
				final String completedByUUID = getPlayerUUID(completedBy);

				preparedStatement.setString(1, senderUUID);
				preparedStatement.setString(2, sender);
				preparedStatement.setString(3, reportedUUID);
				preparedStatement.setString(4, reported);
				preparedStatement.setString(5, claimedByUUID);
				preparedStatement.setString(6, claimedBy);
				preparedStatement.setString(7, completedByUUID);
				preparedStatement.setString(8, completedBy);
				preparedStatement.setInt(9, id);

				preparedStatement.addBatch();
			}
			preparedStatement.executeBatch();
		} catch (final SQLException e) {
			log.warn("Failed to convert data to temporary table!");
			throw e;
		}
	}

	private SQLResultSet getConversionData() throws SQLException {
		final SQLResultSet sqlResultSet = new SQLResultSet();
		ResultSet resultSet = null;
		try {
			resultSet = getDatabase().query(getConnectionId(), "SELECT ID, SenderRaw, ReportedRaw, ClaimedByRaw, CompletedByRaw FROM Reports");
			sqlResultSet.set(resultSet);
		} catch (final SQLException e) {
			log.warn("Failed to execute query to get conversion data!");
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
		}
		return sqlResultSet;
	}

	private static final class ReportTableVersion8Creator extends DatabaseTableCreator {
		public ReportTableVersion8Creator(final Database database, final String databaseVersion, final String tableName) {
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
