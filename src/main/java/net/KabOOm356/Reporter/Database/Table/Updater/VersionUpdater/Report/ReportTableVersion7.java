package net.KabOOm356.Reporter.Database.Table.Updater.VersionUpdater.Report;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionUpdater;

import java.sql.SQLException;
import java.util.List;

public class ReportTableVersion7 extends DatabaseTableVersionUpdater {
	private static final String version = "7";

	public ReportTableVersion7(final Database database, final String tableName) {
		super(database, version, tableName);
	}

	@Override
	public boolean needsToUpdate() throws InterruptedException, SQLException, ClassNotFoundException {
		startTransaction();
		final List<String> columns = getColumns();
		return !columns.contains("Priority") || !columns.contains("ClaimStatus") || !columns.contains("ClaimDate") || !columns.contains("ClaimedBy") || !columns.contains("ClaimPriority");
	}

	@Override
	protected void apply() throws SQLException, ClassNotFoundException, InterruptedException {
		startTransaction();
		final List<String> columns = getColumns();

		// Version 7 (Claiming Update)
		if (!columns.contains("Priority")) {
			addQueryToTransaction("ALTER TABLE Reports ADD Priority TINYINT NOT NULL DEFAULT '0'");
		}
		if (!columns.contains("ClaimStatus")) {
			addQueryToTransaction("ALTER TABLE Reports ADD ClaimStatus BOOLEAN NOT NULL DEFAULT '0'");
		}
		if (!columns.contains("ClaimDate")) {
			addQueryToTransaction("ALTER TABLE Reports ADD ClaimDate CHAR(19)");
		}
		if (!columns.contains("ClaimedBy")) {
			addQueryToTransaction("ALTER TABLE Reports ADD ClaimedBy VARCHAR(32)");
		}
		if (!columns.contains("ClaimPriority")) {
			addQueryToTransaction("ALTER TABLE Reports ADD ClaimPriority TINYINT");
		}
	}
}
