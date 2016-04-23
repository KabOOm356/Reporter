package net.KabOOm356.Reporter.Database.Table.Updater.VersionUpdater.Report;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class ReportTableVersion2 extends DatabaseTableVersionUpdater {
	private static final Logger log = LogManager.getLogger(ReportTableVersion2.class);

	private static final String version = "2";

	public ReportTableVersion2(final Database database, final String tableName) {
		super(database, version, tableName);
	}

	@Override
	public boolean needsToUpdate() throws InterruptedException, SQLException, ClassNotFoundException {
		startTransaction();
		final List<String> columns = getColumns();
		return !columns.contains("SenderX") ||
				!columns.contains("SenderY") ||
				!columns.contains("SenderZ") ||
				!columns.contains("ReportedX") ||
				!columns.contains("ReportedY") ||
				!columns.contains("ReportedZ");
	}

	@Override
	protected void apply() throws SQLException, ClassNotFoundException, InterruptedException {
		startTransaction();
		final List<String> columns = getColumns();

		// Version 2 (Location Update)
		if (!columns.contains("SenderX")) {
			addQueryToTransaction("ALTER TABLE Reports ADD SenderX DOUBLE NOT NULL DEFAULT '0.0'");
		}
		if (!columns.contains("SenderY")) {
			addQueryToTransaction("ALTER TABLE Reports ADD SenderY DOUBLE NOT NULL DEFAULT '0.0'");
		}
		if (!columns.contains("SenderZ")) {
			addQueryToTransaction("ALTER TABLE Reports ADD SenderZ DOUBLE NOT NULL DEFAULT '0.0'");
		}
		if (!columns.contains("ReportedX")) {
			addQueryToTransaction("ALTER TABLE Reports ADD ReportedX DOUBLE DEFAULT '0.0'");
		}
		if (!columns.contains("ReportedY")) {
			addQueryToTransaction("ALTER TABLE Reports ADD ReportedX DOUBLE DEFAULT '0.0'");
		}
		if (!columns.contains("ReportedZ")) {
			addQueryToTransaction("ALTER TABLE Reports ADD ReportedX DOUBLE DEFAULT '0.0'");
		}
	}
}
