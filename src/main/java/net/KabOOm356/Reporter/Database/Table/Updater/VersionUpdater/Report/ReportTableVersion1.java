package net.KabOOm356.Reporter.Database.Table.Updater.VersionUpdater.Report;

import java.sql.SQLException;
import java.util.List;
import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReportTableVersion1 extends DatabaseTableVersionUpdater {
  private static final Logger log = LogManager.getLogger(ReportTableVersion1.class);

  private static final String version = "1";

  public ReportTableVersion1(final Database database, final String tableName) {
    super(database, version, tableName);
  }

  @Override
  public boolean needsToUpdate() throws InterruptedException, SQLException, ClassNotFoundException {
    startTransaction();
    final List<String> columns = getColumns();
    return !columns.contains("ID")
        || !columns.contains("Sender")
        || !columns.contains("Reported")
        || !columns.contains("Details")
        || !columns.contains("Date");
  }

  @Override
  protected void apply() throws InterruptedException, SQLException, ClassNotFoundException {
    startTransaction();
    final List<String> columns = getColumns();

    try {
      // Version 1 (Initial Version)
      if (!columns.contains("ID")) {
        addQueryToTransaction("ALTER TABLE Reports ADD ID INTEGER PRIMARY KEY");
      }
      if (!columns.contains("Sender")) {
        addQueryToTransaction("ALTER TABLE Reports ADD Sender VARCHAR(32)");
      }
      if (!columns.contains("Reported")) {
        addQueryToTransaction(
            "ALTER TABLE Reports ADD Reported VARCHAR(32) NOT NULL DEFAULT '* (Anonymous)'");
      }
      if (!columns.contains("Details")) {
        addQueryToTransaction("ALTER TABLE Reports ADD Details VARCHAR(200) NOT NULL");
      }
      if (!columns.contains("Date")) {
        addQueryToTransaction("ALTER TABLE Reports ADD Date CHAR(19) NOT NULL DEFAULT 'N/A'");
      }
    } catch (final SQLException e) {
      log.warn(
          String.format(
              "Failed to update table [%s] to version [%s]!",
              getTableName(), getDatabaseVersion()));
      throw e;
    }
  }
}
