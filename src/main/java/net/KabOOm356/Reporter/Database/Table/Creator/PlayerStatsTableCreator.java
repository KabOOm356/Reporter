package net.KabOOm356.Reporter.Database.Table.Creator;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Database.Table.DatabaseTableCreator;
import net.KabOOm356.Util.DatabaseUtil;

public class PlayerStatsTableCreator extends DatabaseTableCreator {
  public PlayerStatsTableCreator(
      final Database database, final String databaseVersion, final String tableName) {
    super(database, databaseVersion, tableName);
  }

  @Override
  protected String getTableCreationQuery() {
    final StringBuilder createQuery = new StringBuilder();
    createQuery
        .append("CREATE TABLE IF NOT EXISTS PlayerStats (")
        .append(DatabaseUtil.getAutoIncrementingPrimaryKeyQuery(getDatabase(), "ID"))
        .append(", ")
        .append("Name VARCHAR(16) NOT NULL, ")
        .append("UUID VARCHAR(36) NOT NULL, ")
        .append("FirstReportDate VARCHAR(19) NOT NULL DEFAULT '', ")
        .append("LastReportDate VARCHAR(19) NOT NULL DEFAULT '', ")
        .append("ReportCount INTEGER NOT NULL DEFAULT '0', ")
        .append("FirstReportedDate VARCHAR(19) NOT NULL DEFAULT '', ")
        .append("LastReportedDate VARCHAR(19) NOT NULL DEFAULT '', ")
        .append("ReportedCount INTEGER NOT NULL DEFAULT '0')");
    if (getDatabase().getDatabaseType() == DatabaseType.MYSQL) {
      createQuery.append(DatabaseUtil.getTableCreationMetadata(getDatabase()));
    }
    createQuery.append(';');
    return createQuery.toString();
  }
}
