package net.KabOOm356.Reporter.Database.Table.Updater.VersionUpdater.PlayerStats;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionUpdater;
import net.KabOOm356.Util.DatabaseUtil;

import java.sql.SQLException;

public class PlayerStatsTableVersion10 extends DatabaseTableVersionUpdater {
	private static final String version = "10";

	public PlayerStatsTableVersion10(final Database database, final String tableName) {
		super(database, version, tableName);
	}

	@Override
	public boolean needsToUpdate() throws InterruptedException, SQLException, ClassNotFoundException {
		startTransaction();
		return !getColumns().contains("ID") ||
				!getColumns().contains("Name") ||
				!getColumns().contains("UUID") ||
				!getColumns().contains("FirstReportDate") ||
				!getColumns().contains("LastReportDate") ||
				!getColumns().contains("ReportCount") ||
				!getColumns().contains("FirstReportedDate") ||
				!getColumns().contains("LastReportedDate") ||
				!getColumns().contains("ReportedCount");
	}

	@Override
	protected void apply() throws SQLException, ClassNotFoundException, InterruptedException {
		startTransaction();

		// Version 10 (Initial Table Version)
		if (!getColumns().contains("ID")) {
			addQueryToTransaction("ALTER TABLE PlayerStats ADD ID INTEGER PRIMARY KEY" + DatabaseUtil.getAutoIncrementingPrimaryKeySuffix(getDatabase()));
		}
		if (!getColumns().contains("Name")) {
			addQueryToTransaction("ALTER TABLE PlayerStats ADD Name VARCHAR(16) NOT NULL");
		}
		if (!getColumns().contains("UUID")) {
			addQueryToTransaction("ALTER TABLE PlayerStats ADD UUID VARCHAR(36) NOT NULL");
		}
		if (!getColumns().contains("FirstReportDate")) {
			addQueryToTransaction("ALTER TABLE PlayerStats ADD FirstReportDate VARCHAR(19) NOT NULL DEFAULT ''");
		}
		if (!getColumns().contains("LastReportDate")) {
			addQueryToTransaction("ALTER TABLE PlayerStats ADD LastReportDate VARCHAR(19) NOT NULL DEFAULT ''");
		}
		if (!getColumns().contains("ReportCount")) {
			addQueryToTransaction("ALTER TABLE PlayerStats ADD ReportCount INTEGER NOT NULL DEFAULT '0'");
		}
		if (!getColumns().contains("FirstReportedDate")) {
			addQueryToTransaction("ALTER TABLE PlayerStats ADD FirstReportedDate VARCHAR(19) NOT NULL DEFAULT ''");
		}
		if (!getColumns().contains("LastReportedDate")) {
			addQueryToTransaction("ALTER TABLE PlayerStats ADD LastReportedDate VARCHAR(19) NOT NULL DEFAULT ''");
		}
		if (!getColumns().contains("ReportedCount")) {
			addQueryToTransaction("ALTER TABLE PlayerStats ADD ReportedCount INTEGER NOT NULL DEFAULT '0'");
		}
	}
}
