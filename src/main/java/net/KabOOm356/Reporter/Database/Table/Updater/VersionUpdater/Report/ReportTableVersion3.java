package net.KabOOm356.Reporter.Database.Table.Updater.VersionUpdater.Report;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class ReportTableVersion3 extends DatabaseTableVersionUpdater {
	private static final Logger log = LogManager.getLogger(ReportTableVersion3.class);

	private static final String version = "3";

	public ReportTableVersion3(final Database database, final String tableName) {
		super(database, version, tableName);
	}

	@Override
	public boolean needsToUpdate() throws InterruptedException, SQLException, ClassNotFoundException {
		startTransaction();
		final List<String> columns = getColumns();
		return !columns.contains("SenderWorld") || !columns.contains("ReportedWorld");
	}

	@Override
	protected void apply() throws SQLException, ClassNotFoundException, InterruptedException {
		startTransaction();
		final List<String> columns = getColumns();

		// Version 3 (World Update)
		if (!columns.contains("SenderWorld")) {
			addQueryToTransaction("ALTER TABLE Reports ADD SenderWorld VARCHAR(100) DEFAULT ''");
		}
		if (!columns.contains("ReportedWorld")) {
			addQueryToTransaction("ALTER TABLE Reports ADD ReportedWorld VARCHAR(100) DEFAULT ''");
		}
	}
}
