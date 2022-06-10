package net.KabOOm356.Reporter.Database.Table.Creator;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Database.Table.DatabaseTableCreator;
import net.KabOOm356.Util.DatabaseUtil;

public class ModStatsTableCreator extends DatabaseTableCreator {
  public ModStatsTableCreator(
      final Database database, final String databaseVersion, final String tableName) {
    super(database, databaseVersion, tableName);
  }

  @Override
  protected String getTableCreationQuery() {
    final StringBuilder createQuery = new StringBuilder();
    createQuery
        .append("CREATE TABLE IF NOT EXISTS ModStats (")
        .append(DatabaseUtil.getAutoIncrementingPrimaryKeyQuery(getDatabase(), "ID"))
        .append(", ")
        .append("ModName VARCHAR(16) NOT NULL, ")
        .append("ModUUID VARCHAR(36) NOT NULL, ")
        .append("AssignCount INTEGER NOT NULL DEFAULT '0', ")
        .append("ClaimedCount INTEGER NOT NULL DEFAULT '0', ")
        .append("CompletionCount INTEGER NOT NULL DEFAULT '0', ")
        .append("DeletionCount INTEGER NOT NULL DEFAULT '0', ")
        .append("MoveCount INTEGER NOT NULL DEFAULT '0', ")
        .append("RespondCount INTEGER NOT NULL DEFAULT '0', ")
        .append("UnassignCount INTEGER NOT NULL DEFAULT '0', ")
        .append("UnclaimCount INTEGER NOT NULL DEFAULT '0')");
    if (getDatabase().getDatabaseType() == DatabaseType.MYSQL) {
      createQuery.append(DatabaseUtil.getTableCreationMetadata(getDatabase()));
    }
    createQuery.append(';');
    return createQuery.toString();
  }
}
