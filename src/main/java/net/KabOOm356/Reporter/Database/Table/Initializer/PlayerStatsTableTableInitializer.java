package net.KabOOm356.Reporter.Database.Table.Initializer;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.DatabaseTableCreator;
import net.KabOOm356.Database.Table.DatabaseTableInitializer;
import net.KabOOm356.Database.Table.DatabaseTableMigrator;
import net.KabOOm356.Database.Table.DatabaseTableUpdater;
import net.KabOOm356.Reporter.Database.Table.Creator.PlayerStatsTableCreator;
import net.KabOOm356.Reporter.Database.Table.Updater.PlayerStatsTableUpdater;

public class PlayerStatsTableTableInitializer extends DatabaseTableInitializer {
	private static final String tableName = "PlayerStats";

	private final PlayerStatsTableCreator creator;
	private final PlayerStatsTableUpdater updater;

	public PlayerStatsTableTableInitializer(final Database database, final String databaseVersion) {
		super();

		this.creator = new PlayerStatsTableCreator(database, databaseVersion, tableName);
		this.updater = new PlayerStatsTableUpdater(database, databaseVersion, tableName);
	}

	@Override
	protected DatabaseTableCreator getCreator() {
		return creator;
	}

	@Override
	protected DatabaseTableMigrator getMigrator() {
		return null;
	}

	@Override
	protected DatabaseTableUpdater getUpdater() {
		return updater;
	}
}
