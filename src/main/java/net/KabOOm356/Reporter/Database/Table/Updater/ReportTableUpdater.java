package net.KabOOm356.Reporter.Database.Table.Updater;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.DatabaseTableUpdater;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionUpdater;
import net.KabOOm356.Reporter.Database.Table.Updater.VersionUpdater.Report.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ReportTableUpdater extends DatabaseTableUpdater {
	private static final Logger log = LogManager.getLogger(ReportTableUpdater.class);

	private final List<DatabaseTableVersionUpdater> versionUpdaters = new ArrayList<>();

	public ReportTableUpdater(final Database database, final String updateVersion, final String tableName) {
		super(database, updateVersion, tableName);

		versionUpdaters.add(new ReportTableVersion1(database, getTableName()));
		versionUpdaters.add(new ReportTableVersion2(database, getTableName()));
		versionUpdaters.add(new ReportTableVersion3(database, getTableName()));
		versionUpdaters.add(new ReportTableVersion4(database, getTableName()));
		versionUpdaters.add(new ReportTableVersion5(database, getTableName()));
		versionUpdaters.add(new ReportTableVersion6(database, getTableName()));
		versionUpdaters.add(new ReportTableVersion7(database, getTableName()));
		versionUpdaters.add(new ReportTableVersion8(database, getTableName()));
	}

	@Override
	public List<DatabaseTableVersionUpdater> getDatabaseTableVersionUpdaters() {
		return versionUpdaters;
	}
}
