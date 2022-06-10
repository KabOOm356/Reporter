package net.KabOOm356.Reporter.Database.Table.Updater.VersionUpdater.ModStats;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionUpdater;

public class ModStatsTableVersion11 extends DatabaseTableVersionUpdater {
  private static final String version = "11";

  public ModStatsTableVersion11(final Database database, final String tableName) {
    super(database, version, tableName);
  }

  @Override
  protected boolean needsToUpdate()
      throws InterruptedException, SQLException, ClassNotFoundException {
    startTransaction();
    if (getDatabase().getDatabaseType() == DatabaseType.MYSQL) {
      final ResultSet resultSet =
          getStatement().executeQuery("SHOW TABLE STATUS WHERE Name = '" + getTableName() + "';");
      resultSet.first();
      return !resultSet.getString("Collation").contains("utf8");
    }
    return false;
  }

  @Override
  protected void apply() throws SQLException, ClassNotFoundException, InterruptedException {
    startTransaction();
    addQueryToTransaction(
        "ALTER TABLE " + getTableName() + " CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;");
  }
}
