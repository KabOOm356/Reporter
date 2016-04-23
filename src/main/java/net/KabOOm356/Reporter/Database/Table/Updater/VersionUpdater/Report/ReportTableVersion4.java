package net.KabOOm356.Reporter.Database.Table.Updater.VersionUpdater.Report;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionUpdater;

import java.sql.SQLException;
import java.util.List;

public class ReportTableVersion4 extends DatabaseTableVersionUpdater {
	private static final String version = "4";

	public ReportTableVersion4(final Database database, final String tableName) {
		super(database, version, tableName);
	}

	@Override
	public boolean needsToUpdate() throws InterruptedException, SQLException, ClassNotFoundException {
		startTransaction();
		final List<String> columns = getColumns();
		return !columns.contains("CompletionStatus") || !columns.contains("CompletedBy") || !columns.contains("CompletionDate") || !columns.contains("CompletionSummary");
	}

	@Override
	protected void apply() throws SQLException, ClassNotFoundException, InterruptedException {
		startTransaction();
		final List<String> columns = getColumns();

		// Version 4 (Completion Update)
		if (!columns.contains("CompletionStatus")) {
			addQueryToTransaction("ALTER TABLE Reports ADD CompletionStatus BOOLEAN DEFAULT '0'");
		}
		if (!columns.contains("CompletedBy")) {
			addQueryToTransaction("ALTER TABLE Reports ADD CompletedBy VARCHAR(32) DEFAULT ''");
		}
		if (!columns.contains("CompletionDate")) {
			addQueryToTransaction("ALTER TABLE Reports ADD CompletionDate CHAR(19) DEFAULT ''");
		}
		if (!columns.contains("CompletionSummary")) {
			addQueryToTransaction("ALTER TABLE Reports ADD CompletionSummary VARCHAR(200) DEFAULT ''");
		}
	}
}
