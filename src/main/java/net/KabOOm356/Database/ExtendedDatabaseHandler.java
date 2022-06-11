package net.KabOOm356.Database;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import net.KabOOm356.Database.Connection.ConnectionPoolConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** An class that extends the functionality of {@link DatabaseHandler}. */
public class ExtendedDatabaseHandler extends DatabaseHandler {
  private static final Logger log = LogManager.getLogger(ExtendedDatabaseHandler.class);

  /**
   * Constructor.
   *
   * @see DatabaseHandler#DatabaseHandler(String, String, String, String, ConnectionPoolConfig)
   */
  public ExtendedDatabaseHandler(
      final String host,
      final String database,
      final String username,
      final String password,
      final ConnectionPoolConfig connectionPoolConfig) {
    super(host, database, username, password, connectionPoolConfig);
  }

  /**
   * Constructor.
   *
   * @see DatabaseHandler#DatabaseHandler(DatabaseType, String, String, ConnectionPoolConfig)
   */
  public ExtendedDatabaseHandler(
      final DatabaseType type,
      final String path,
      final String name,
      final ConnectionPoolConfig connectionPoolConfig)
      throws IOException {
    super(type, path, name, connectionPoolConfig);
  }

  /**
   * Constructor.
   *
   * @see DatabaseHandler#DatabaseHandler(Database)
   */
  public ExtendedDatabaseHandler(final Database database) {
    super(database);
  }

  /**
   * Performs an SQL query on the database that returns a {@link SQLResultSet} containing the
   * results.
   *
   * @param query The query to send to the database.
   * @return A {@link SQLResultSet} containing the results
   * @throws ClassNotFoundException
   * @throws SQLException
   * @throws InterruptedException
   * @see net.KabOOm356.Database.DatabaseHandler#query(java.lang.String)
   */
  public SQLResultSet sqlQuery(final String query)
      throws ClassNotFoundException, SQLException, InterruptedException {
    ResultSet resultSet = null;

    try {
      resultSet = super.query(query);

      if (!usingSQLite() && !resultSet.isBeforeFirst()) {
        resultSet.beforeFirst();
      }

      return new SQLResultSet(resultSet);
    } finally {
      try {
        resultSet.close();
      } catch (final Exception e) {
        if (log.isDebugEnabled()) {
          log.log(Level.WARN, "Failed to close result set!", e);
        }
      }
      closeConnection();
    }
  }

  /**
   * Performs an SQL query on the database that returns a {@link SQLResultSet} containing the
   * results.
   *
   * @param query The query to send to the database.
   * @param params The parameters of the query.
   * @return A {@link SQLResultSet} containing the results
   * @throws ClassNotFoundException
   * @throws SQLException
   * @throws InterruptedException
   * @see net.KabOOm356.Database.DatabaseHandler#preparedQuery(String, List)
   */
  public SQLResultSet preparedSQLQuery(final String query, final List<String> params)
      throws ClassNotFoundException, SQLException, InterruptedException {
    ResultSet resultSet = null;

    try {
      resultSet = super.preparedQuery(query, params);

      if (!usingSQLite() && !resultSet.isBeforeFirst()) {
        resultSet.beforeFirst();
      }

      return new SQLResultSet(resultSet);
    } finally {
      try {
        resultSet.close();
      } catch (final Exception e) {
        if (log.isDebugEnabled()) {
          log.log(Level.WARN, "Failed to close result set!", e);
        }
      }
      closeConnection();
    }
  }

  /**
   * Returns the {@link Database}'s column meta data.
   *
   * @param table The name of the table to get the meta data for.
   * @return An {@link SQLResultSet} containing the database's column meta data.
   * @throws ClassNotFoundException
   * @throws SQLException
   * @throws InterruptedException
   */
  public SQLResultSet getSQLColumnMetaData(final String table)
      throws ClassNotFoundException, SQLException, InterruptedException {
    ResultSet resultSet = null;

    try {
      resultSet = super.getColumnMetaData(table);

      if (!usingSQLite() && !resultSet.isBeforeFirst()) {
        resultSet.beforeFirst();
      }

      return new SQLResultSet(resultSet);
    } finally {
      try {
        resultSet.close();
      } catch (final Exception e) {
        if (log.isDebugEnabled()) {
          log.log(Level.WARN, "Failed to close result set!", e);
        }
      }
      closeConnection();
    }
  }

  /**
   * Performs an SQL query on the database that returns a {@link SQLResultSet} containing the
   * results.
   *
   * @param connectionId The id of the connection to execute on.
   * @param query The query to send to the database.
   * @return A {@link SQLResultSet} containing the results
   * @throws SQLException
   * @see net.KabOOm356.Database.DatabaseHandler#query(Integer, String)
   */
  public SQLResultSet sqlQuery(final int connectionId, final String query) throws SQLException {
    ResultSet resultSet = null;
    try {
      resultSet = super.query(connectionId, query);

      if (!usingSQLite() && !resultSet.isBeforeFirst()) {
        resultSet.beforeFirst();
      }

      return new SQLResultSet(resultSet);
    } finally {
      try {
        resultSet.close();
      } catch (final Exception e) {
        if (log.isDebugEnabled()) {
          log.log(Level.WARN, "Failed to close result set!", e);
        }
      }
    }
  }

  /**
   * Performs an SQL query on the database that returns a {@link SQLResultSet} containing the
   * results.
   *
   * @param connectionId The id of the connection to execute on.
   * @param query The query to send to the database.
   * @param params The parameters of the query.
   * @return A {@link SQLResultSet} containing the results
   * @throws SQLException
   * @see net.KabOOm356.Database.DatabaseHandler#preparedQuery(Integer, String, List)
   */
  public SQLResultSet preparedSQLQuery(
      final int connectionId, final String query, final List<String> params) throws SQLException {
    ResultSet resultSet = null;
    try {
      resultSet = super.preparedQuery(connectionId, query, params);

      if (!usingSQLite() && !resultSet.isBeforeFirst()) {
        resultSet.beforeFirst();
      }

      return new SQLResultSet(resultSet);
    } finally {
      try {
        resultSet.close();
      } catch (final Exception e) {
        if (log.isDebugEnabled()) {
          log.log(Level.WARN, "Failed to close result set!", e);
        }
      }
    }
  }

  /**
   * Returns the {@link Database}'s column meta data.
   *
   * @param connectionId The id of the connection to execute on.
   * @param table The name of the table to get the meta data for.
   * @return An {@link SQLResultSet} containing the database's column meta data.
   * @throws SQLException
   */
  public SQLResultSet getSQLColumnMetaData(final int connectionId, final String table)
      throws SQLException {
    ResultSet resultSet = null;
    try {
      resultSet = super.getColumnMetaData(connectionId, table);

      if (!usingSQLite() && !resultSet.isBeforeFirst()) {
        resultSet.beforeFirst();
      }

      return new SQLResultSet(resultSet);
    } finally {
      try {
        resultSet.close();
      } catch (final Exception e) {
        if (log.isDebugEnabled()) {
          log.log(Level.WARN, "Failed to close result set!", e);
        }
      }
    }
  }
}
