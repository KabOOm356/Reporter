package net.KabOOm356.Reporter.Database.Table.Migrator.VersionMigrator.Report;

import java.sql.SQLException;
import java.util.List;
import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.DatabaseTableCreator;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionMigrator;

public class ReportTableVersion7 extends DatabaseTableVersionMigrator {
  private static final String version = "7";

  private final DatabaseTableCreator creator;

  public ReportTableVersion7(final Database database, final String tableName) {
    super(database, version, tableName);

    this.creator =
        new ReportTableVersion7Creator(getDatabase(), getDatabaseVersion(), getTableName());
  }

  @Override
  protected String getCreateTemporaryTableQuery() {
    return "CREATE TABLE IF NOT EXISTS Version7Temporary ("
        + "ID INTEGER PRIMARY KEY, "
        + "Date VARCHAR(19) NOT NULL DEFAULT 'N/A', "
        + "Sender VARCHAR(50) NOT NULL, "
        + "SenderRaw VARCHAR(16) NOT NULL, "
        + "Reported VARCHAR(50) NOT NULL DEFAULT '* (Anonymous)', "
        + "ReportedRaw VARCHAR(16) NOT NULL DEFAULT '* (Anonymous)', "
        + "Details VARCHAR(200) NOT NULL, "
        + "Priority TINYINT NOT NULL DEFAULT '0', "
        + "SenderWorld VARCHAR(100) DEFAULT '', "
        + "SenderX DOUBLE NOT NULL DEFAULT '0.0', "
        + "SenderY DOUBLE NOT NULL DEFAULT '0.0', "
        + "SenderZ DOUBLE NOT NULL DEFAULT '0.0', "
        + "ReportedWorld VARCHAR(100) DEFAULT '', "
        + "ReportedX DOUBLE DEFAULT '0.0', "
        + "ReportedY DOUBLE DEFAULT '0.0', "
        + "ReportedZ DOUBLE DEFAULT '0.0', "
        + "CompletionStatus BOOLEAN NOT NULL DEFAULT '0', "
        + "CompletedBy VARCHAR(50), "
        + "CompletedByRaw VARCHAR(16), "
        + "CompletionDate VARCHAR(19), "
        + "CompletionSummary VARCHAR(200), "
        + "ClaimStatus BOOLEAN NOT NULL DEFAULT '0', "
        + "ClaimDate VARCHAR(19), "
        + "ClaimedBy VARCHAR(50), "
        + "ClaimedByRaw VARCHAR(16), "
        + "ClaimPriority TINYINT);";
  }

  @Override
  protected String getDropTemporaryTableQuery() {
    return "DROP TABLE IF EXISTS Version7Temporary";
  }

  @Override
  protected String getDropTableQuery() {
    return "DROP TABLE IF EXISTS reports";
  }

  @Override
  public boolean needsMigration()
      throws ClassNotFoundException, SQLException, InterruptedException {
    startTransaction();
    if (getDatabase().checkTable(getConnectionId(), "reports")) {
      final List<String> columnNames = getDatabase().getColumnNames(getConnectionId(), "reports");

      return !columnNames.contains("ClaimStatus")
          && !columnNames.contains("ClaimDate")
          && !columnNames.contains("ClaimedBy")
          && !columnNames.contains("ClaimedByRaw")
          && !columnNames.contains("ClaimedPriority");
    }

    return false;
  }

  @Override
  protected DatabaseTableCreator getCreator() {
    return creator;
  }

  @Override
  protected String getMigrateTableQuery() {
    return "INSERT INTO Reports "
        + "(ID, Date, Sender, SenderRaw, Reported, ReportedRaw, Details, SenderWorld, SenderX, SenderY, SenderZ, ReportedWorld, ReportedX, ReportedY, ReportedZ, CompletionStatus, CompletedBy, CompletedByRaw, CompletionDate, CompletionSummary) "
        + "SELECT "
        + "ID, Date, Sender, SenderRaw, Reported, ReportedRaw, Details, SenderWorld, SenderX, SenderY, SenderZ, ReportedWorld, ReportedX, ReportedY, ReportedZ, CompletionStatus, CompletedBy, CompletedByRaw, CompletionDate, CompletionSummary "
        + "FROM Version7Temporary";
  }

  @Override
  protected String getPopulateTemporaryTableQuery() {
    return "INSERT INTO Version7Temporary "
        + "(ID, Date, Sender, SenderRaw, Reported, ReportedRaw, Details, SenderWorld, SenderX, SenderY, SenderZ, ReportedWorld, ReportedX, ReportedY, ReportedZ, CompletionStatus, CompletedBy, CompletedByRaw, CompletionDate, CompletionSummary) "
        + "SELECT "
        + "id, date, sender, SenderRaw, reported, ReportedRaw, details, SenderWorld, SenderX, SenderY, SenderZ, ReportedWorld, ReportedX, ReportedY, ReportedZ, CompletionStatus, CompletedBy, CompletedBy, CompletionDate, CompletionSummary "
        + "FROM reports";
  }

  private static final class ReportTableVersion7Creator extends DatabaseTableCreator {
    private ReportTableVersion7Creator(
        final Database database, final String databaseVersion, final String tableName) {
      super(database, databaseVersion, tableName);
    }

    @Override
    protected String getTableCreationQuery() {
      return "CREATE TABLE IF NOT EXISTS Reports ("
          + "ID INTEGER PRIMARY KEY, "
          + "Date VARCHAR(19) NOT NULL DEFAULT 'N/A', "
          + "Sender VARCHAR(50) NOT NULL, "
          + "SenderRaw VARCHAR(16) NOT NULL, "
          + "Reported VARCHAR(50) NOT NULL DEFAULT '* (Anonymous)', "
          + "ReportedRaw VARCHAR(16) NOT NULL DEFAULT '* (Anonymous)', "
          + "Details VARCHAR(200) NOT NULL, "
          + "Priority TINYINT NOT NULL DEFAULT '0', "
          + "SenderWorld VARCHAR(100) DEFAULT '', "
          + "SenderX DOUBLE NOT NULL DEFAULT '0.0', "
          + "SenderY DOUBLE NOT NULL DEFAULT '0.0', "
          + "SenderZ DOUBLE NOT NULL DEFAULT '0.0', "
          + "ReportedWorld VARCHAR(100) DEFAULT '', "
          + "ReportedX DOUBLE DEFAULT '0.0', "
          + "ReportedY DOUBLE DEFAULT '0.0', "
          + "ReportedZ DOUBLE DEFAULT '0.0', "
          + "CompletionStatus BOOLEAN NOT NULL DEFAULT '0', "
          + "CompletedBy VARCHAR(50), "
          + "CompletedByRaw VARCHAR(16), "
          + "CompletionDate VARCHAR(19), "
          + "CompletionSummary VARCHAR(200), "
          + "ClaimStatus BOOLEAN NOT NULL DEFAULT '0', "
          + "ClaimDate VARCHAR(19), "
          + "ClaimedBy VARCHAR(50), "
          + "ClaimedByRaw VARCHAR(16), "
          + "ClaimPriority TINYINT);";
    }
  }
}
