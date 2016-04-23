package net.KabOOm356.Reporter.Database.Table.Updater;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.DatabaseTableUpdater;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionUpdater;
import net.KabOOm356.Reporter.Database.Table.Updater.VersionUpdater.PlayerStats.PlayerStatsTableVersion10;

import java.util.ArrayList;
import java.util.List;

public class PlayerStatsTableUpdater extends DatabaseTableUpdater {
	private final List<DatabaseTableVersionUpdater> versionUpdaters = new ArrayList<DatabaseTableVersionUpdater>();

	public PlayerStatsTableUpdater(final Database database, final String updateVersion, final String tableName) {
		super(database, updateVersion, tableName);

		versionUpdaters.add(new PlayerStatsTableVersion10(getDatabase(), getTableName()));
	}

	@Override
	public List<DatabaseTableVersionUpdater> getDatabaseTableVersionUpdaters() {
		return versionUpdaters;
	}
}
