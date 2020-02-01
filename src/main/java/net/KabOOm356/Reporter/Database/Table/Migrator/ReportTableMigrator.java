package net.KabOOm356.Reporter.Database.Table.Migrator;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.DatabaseTableMigrator;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionMigrator;
import net.KabOOm356.Reporter.Database.Table.Migrator.VersionMigrator.Report.ReportTableVersion7;
import net.KabOOm356.Reporter.Database.Table.Migrator.VersionMigrator.Report.ReportTableVersion8;
import net.KabOOm356.Reporter.Database.Table.Migrator.VersionMigrator.Report.ReportTableVersion9;

import java.util.ArrayList;
import java.util.List;

public class ReportTableMigrator extends DatabaseTableMigrator {
	private final List<DatabaseTableVersionMigrator> versionMigrators = new ArrayList<>();

	public ReportTableMigrator(final Database database, final String databaseVersion, final String tableName) {
		super(database, databaseVersion, tableName);

		versionMigrators.add(new ReportTableVersion7(getDatabase(), getTableName()));
		versionMigrators.add(new ReportTableVersion8(getDatabase(), getTableName()));
		versionMigrators.add(new ReportTableVersion9(getDatabase(), getTableName()));
	}

	@Override
	public List<DatabaseTableVersionMigrator> getDatabaseTableVersionMigrators() {
		return versionMigrators;
	}
}
