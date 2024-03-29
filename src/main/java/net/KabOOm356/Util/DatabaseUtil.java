package net.KabOOm356.Util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Throwable.WrongNumberOfSQLParametersException;
import org.apache.commons.lang.Validate;

public final class DatabaseUtil {
  public static final char queryParameter = '?';

  private DatabaseUtil() {}

  public static String getAutoIncrementingPrimaryKeyQuery(
      final Database database, final String columnName) {
    Validate.notNull(database);
    Validate.notNull(columnName);
    Validate.notEmpty(columnName);

    return columnName + " INTEGER PRIMARY KEY" + getAutoIncrementingPrimaryKeySuffix(database);
  }

  public static String getAutoIncrementingPrimaryKeySuffix(final Database database) {
    // If the database is using MySQL, set the primary key to auto increment.
    // SQLite does this automatically for primary keys.
    return (database.getDatabaseType() == DatabaseType.MYSQL) ? " AUTO_INCREMENT" : "";
  }

  public static String getTableCreationMetadata(final Database database) {
    return getTableCreationStorageEngine(database)
        + getTableCreationCharacterSet(database)
        + getTableCreationCollation(database);
  }

  public static String getTableCreationStorageEngine(final Database database) {
    return (database.getDatabaseType() == DatabaseType.MYSQL) ? " ENGINE = INNODB" : "";
  }

  public static String getTableCreationCharacterSet(final Database database) {
    return (database.getDatabaseType() == DatabaseType.MYSQL) ? " CHARACTER SET utf8mb4" : "";
  }

  public static String getTableCreationCollation(final Database database) {
    return (database.getDatabaseType() == DatabaseType.MYSQL) ? " COLLATE utf8mb4_bin" : "";
  }

  public static String getColumnsSizeName(final Database database) {
    return (database.getDatabaseType() == DatabaseType.SQLITE) ? "TYPE_NAME" : "COLUMN_SIZE";
  }

  public static boolean checkPreparedStatementParameters(
      final String query, final List<String> parameters) {
    final int numberOfOccurrences = Util.countOccurrences(query, queryParameter);
    return parameters.size() == numberOfOccurrences;
  }

  public static void bindParametersToPreparedStatement(
      final PreparedStatement preparedStatement, final String query, final List<String> parameters)
      throws SQLException {
    if (checkPreparedStatementParameters(query, parameters)) {
      for (int LCV = 0; LCV < parameters.size(); LCV++) {
        preparedStatement.setString(LCV + 1, parameters.get(LCV));
      }
    } else {
      final int numberOfOccurrences = Util.countOccurrences(query, queryParameter);
      final String exceptionMessage =
          "Required number of parameters: " + parameters.size() + " got: " + numberOfOccurrences;
      throw new WrongNumberOfSQLParametersException(exceptionMessage);
    }
  }
}
