package net.KabOOm356.Reporter.Database.Table.Updater.VersionUpdater.Report;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionUpdater;

import java.sql.SQLException;
import java.util.List;

public class ReportTableVersion8 extends DatabaseTableVersionUpdater {
	private static final String version = "8";

	public ReportTableVersion8(final Database database, final String tableName) {
		super(database, version, tableName);
	}

	@Override
	public boolean needsToUpdate() throws InterruptedException, SQLException, ClassNotFoundException {
		startTransaction();
		final List<String> columns = getColumns();
		return !columns.contains("SenderUUID") || !columns.contains("ReportedUUID") || !columns.contains("CompletedByUUID") || !columns.contains("ClaimedByUUID");
	}

	@Override
	protected void apply() throws SQLException, ClassNotFoundException, InterruptedException {
		startTransaction();
		final List<String> columns = getColumns();

		// Version 8 (UUID Update)
		if (!columns.contains("SenderUUID")) {
			addQueryToTransaction("ALTER TABLE Reports ADD SenderUUID CHAR(36) DEFAULT ''");
		}
		if (!columns.contains("ReportedUUID")) {
			addQueryToTransaction("ALTER TABLE Reports ADD ReportedUUID CHAR(36) DEFAULT ''");
		}
		if (!columns.contains("CompletedByUUID")) {
			addQueryToTransaction("ALTER TABLE Reports ADD CompletedByUUID CHAR(36) DEFAULT ''");
		}
		if (!columns.contains("ClaimedByUUID")) {
			addQueryToTransaction("ALTER TABLE Reports ADD ClaimedByUUID CHAR(36) DEFAULT ''");
		}
	}
}
