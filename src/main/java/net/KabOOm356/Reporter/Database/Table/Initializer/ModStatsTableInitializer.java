package net.KabOOm356.Reporter.Database.Table.Initializer;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.DatabaseTableCreator;
import net.KabOOm356.Database.Table.DatabaseTableInitializer;
import net.KabOOm356.Database.Table.DatabaseTableMigrator;
import net.KabOOm356.Database.Table.DatabaseTableUpdater;
import net.KabOOm356.Reporter.Database.Table.Creator.ModStatsTableCreator;
import net.KabOOm356.Reporter.Database.Table.Updater.ModStatsTableUpdater;

public class ModStatsTableInitializer extends DatabaseTableInitializer {
  private static final String tableName = "ModStats";

  private final ModStatsTableCreator creator;
  private final ModStatsTableUpdater updater;

  public ModStatsTableInitializer(final Database database, final String databaseVersion) {
    super();

    this.creator = new ModStatsTableCreator(database, databaseVersion, tableName);
    this.updater = new ModStatsTableUpdater(database, databaseVersion, tableName);
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
