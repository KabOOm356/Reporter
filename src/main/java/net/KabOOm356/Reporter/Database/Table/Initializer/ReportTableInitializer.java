package net.KabOOm356.Reporter.Database.Table.Initializer;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.DatabaseTableCreator;
import net.KabOOm356.Database.Table.DatabaseTableInitializer;
import net.KabOOm356.Database.Table.DatabaseTableMigrator;
import net.KabOOm356.Database.Table.DatabaseTableUpdater;
import net.KabOOm356.Reporter.Database.Table.Creator.ReportTableCreator;
import net.KabOOm356.Reporter.Database.Table.Migrator.ReportTableMigrator;
import net.KabOOm356.Reporter.Database.Table.Updater.ReportTableUpdater;

public class ReportTableInitializer extends DatabaseTableInitializer {
  private static final String tableName = "Reports";

  private final ReportTableCreator creator;
  private final ReportTableMigrator migrator;
  private final ReportTableUpdater updater;

  public ReportTableInitializer(final Database database, final String databaseVersion) {
    super();

    this.creator = new ReportTableCreator(database, databaseVersion, tableName);
    this.migrator = new ReportTableMigrator(database, databaseVersion, tableName);
    this.updater = new ReportTableUpdater(database, databaseVersion, tableName);
  }

  @Override
  protected DatabaseTableCreator getCreator() {
    return creator;
  }

  @Override
  protected DatabaseTableMigrator getMigrator() {
    return migrator;
  }

  @Override
  protected DatabaseTableUpdater getUpdater() {
    return updater;
  }
}
