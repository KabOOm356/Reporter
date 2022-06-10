package net.KabOOm356.Reporter.Database.Table.Updater.VersionUpdater.Report;

import java.sql.SQLException;
import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionUpdater;

public class ReportTableVersion5 extends DatabaseTableVersionUpdater {
  private static final String version = "5";

  public ReportTableVersion5(final Database database, final String tableName) {
    super(database, version, tableName);
  }

  @Override
  public boolean needsToUpdate() throws InterruptedException, SQLException, ClassNotFoundException {
    return false;
  }

  @Override
  protected void apply() throws SQLException, ClassNotFoundException, InterruptedException {
    // Version 5 (Request Update)
    // Removed in favor of the UUID player lookup.
  }
}
