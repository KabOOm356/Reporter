package net.KabOOm356.Database.Table.Version;

import java.sql.SQLException;
import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.DatabaseTableCreator;
import net.KabOOm356.Database.Table.DatabaseTableUpdateHandler;
import net.KabOOm356.Reporter.Reporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DatabaseTableVersionMigrator extends DatabaseTableUpdateHandler {
  private static final Logger log = LogManager.getLogger(DatabaseTableVersionMigrator.class);

  protected DatabaseTableVersionMigrator(
      final Database database, final String databaseVersion, final String tableName) {
    super(database, databaseVersion, tableName);
  }

  public void migrate() throws InterruptedException, SQLException, ClassNotFoundException {
    try {
      startTransaction();
      if (needsMigration()) {
        createTemporaryTable();
        populateTemporaryTable();
        dropTable();
        getCreator().create();
        migrateTable();
        dropTemporaryTable();
      }
    } catch (final InterruptedException | ClassNotFoundException | SQLException e) {
      log.error(
          Reporter.getDefaultConsolePrefix()
              + String.format(
                  "An error occurred while migrating the database data of table [%s] to version [%s]!",
                  getTableName(), getDatabaseVersion()));
      log.error(
          Reporter.getDefaultConsolePrefix()
              + "If you receive more errors, you may have to recover or delete your database!");
      throw e;
    } finally {
      try {
        commitTransaction();
      } catch (final IllegalStateException | SQLException e) {
        log.error(
            String.format(
                "Failed to commit transaction while migrating table [%s] to version [%s]!",
                getTableName(), getDatabaseVersion()));
        throw e;
      }
    }
  }

  protected void dropTemporaryTable()
      throws SQLException, ClassNotFoundException, InterruptedException {
    startTransaction();
    addQueryToTransaction(getDropTemporaryTableQuery());
  }

  protected void migrateTable() throws SQLException, ClassNotFoundException, InterruptedException {
    startTransaction();
    addQueryToTransaction(getMigrateTableQuery());
  }

  protected void dropTable() throws SQLException, ClassNotFoundException, InterruptedException {
    startTransaction();
    addQueryToTransaction(getDropTableQuery());
  }

  protected void populateTemporaryTable()
      throws SQLException, ClassNotFoundException, InterruptedException {
    startTransaction();
    addQueryToTransaction(getPopulateTemporaryTableQuery());
  }

  protected void createTemporaryTable()
      throws SQLException, ClassNotFoundException, InterruptedException {
    startTransaction();
    addQueryToTransaction(getCreateTemporaryTableQuery());
  }

  /** Gets the query that will create the temporary table that will be used to migrate the data. */
  protected abstract String getCreateTemporaryTableQuery();

  /** Gets the query that will drop the temporary table that was used to migrate the data. */
  protected abstract String getDropTemporaryTableQuery();

  /** Gets the query that will drop the main table that is being migrated. */
  protected abstract String getDropTableQuery();

  protected abstract DatabaseTableCreator getCreator();

  /** Gets the query to migrate the data from the temporary table to the new main table. */
  protected abstract String getMigrateTableQuery();

  /** Gets the query to populate the temporary table with data. */
  protected abstract String getPopulateTemporaryTableQuery();

  /**
   * Performs the logic to check if the table needs to be migrated.
   *
   * @return True if the table needs to be migrated, otherwise false.
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  public abstract boolean needsMigration()
      throws ClassNotFoundException, SQLException, InterruptedException;
}
