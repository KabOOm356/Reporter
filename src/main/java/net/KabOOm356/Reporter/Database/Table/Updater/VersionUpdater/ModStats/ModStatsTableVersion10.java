package net.KabOOm356.Reporter.Database.Table.Updater.VersionUpdater.ModStats;

import java.sql.SQLException;
import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionUpdater;
import net.KabOOm356.Util.DatabaseUtil;

public class ModStatsTableVersion10 extends DatabaseTableVersionUpdater {
  private static final String version = "10";

  public ModStatsTableVersion10(final Database database, final String tableName) {
    super(database, version, tableName);
  }

  @Override
  public boolean needsToUpdate() throws InterruptedException, SQLException, ClassNotFoundException {
    startTransaction();
    return !getColumns().contains("ID")
        || !getColumns().contains("ModName")
        || !getColumns().contains("ModUUID")
        || !getColumns().contains("AssignCount")
        || !getColumns().contains("ClaimedCount")
        || !getColumns().contains("CompletionCount")
        || !getColumns().contains("DeletionCount")
        || !getColumns().contains("MoveCount")
        || !getColumns().contains("RespondCount")
        || !getColumns().contains("UnassignCount")
        || !getColumns().contains("UnclaimCount");
  }

  @Override
  protected void apply() throws SQLException, ClassNotFoundException, InterruptedException {
    startTransaction();

    // Version 10 (Initial Table Version)
    if (!getColumns().contains("ID")) {
      addQueryToTransaction(
          "ALTER TABLE ModStats ADD ID INTEGER PRIMARY KEY"
              + DatabaseUtil.getAutoIncrementingPrimaryKeySuffix(getDatabase()));
    }
    if (!getColumns().contains("ModName")) {
      addQueryToTransaction("ALTER TABLE ModStats ADD ModName VARCHAR(16) NOT NULL");
    }
    if (!getColumns().contains("ModUUID")) {
      addQueryToTransaction("ALTER TABLE ModStats ADD ModUUID VARCHAR(36) NOT NULL");
    }
    if (!getColumns().contains("AssignCount")) {
      addQueryToTransaction("ALTER TABLE ModStats ADD AssignCount INTEGER NOT NULL DEFAULT '0'");
    }
    if (!getColumns().contains("ClaimedCount")) {
      addQueryToTransaction("ALTER TABLE ModStats ADD ClaimedCount INTEGER NOT NULL DEFAULT '0'");
    }
    if (!getColumns().contains("CompletionCount")) {
      addQueryToTransaction(
          "ALTER TABLE ModStats ADD CompletionCount INTEGER NOT NULL DEFAULT '0'");
    }
    if (!getColumns().contains("DeletionCount")) {
      addQueryToTransaction("ALTER TABLE ModStats ADD DeletionCount INTEGER NOT NULL DEFAULT '0'");
    }
    if (!getColumns().contains("MoveCount")) {
      addQueryToTransaction("ALTER TABLE ModStats ADD MoveCount INTEGER NOT NULL DEFAULT '0'");
    }
    if (!getColumns().contains("RespondCount")) {
      addQueryToTransaction("ALTER TABLE ModStats ADD RespondCount INTEGER NOT NULL DEFAULT '0'");
    }
    if (!getColumns().contains("UnassignCount")) {
      addQueryToTransaction("ALTER TABLE ModStats ADD UnassignCount INTEGER NOT NULL DEFAULT '0'");
    }
    if (!getColumns().contains("UnclaimCount")) {
      addQueryToTransaction("ALTER TABLE ModStats ADD UnclaimCount INTEGER NOT NULL DEFAULT '0'");
    }
  }
}
