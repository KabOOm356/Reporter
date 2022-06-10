package net.KabOOm356.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/** A class to store and access information returned after a SQL query. */
public class SQLResultSet extends ArrayList<ResultRow> {
  /** Index of the first row (0). */
  public static final int FIRSTROW = 0;

  /** Index of the first column (0); */
  public static final int FIRSTCOLUMN = 0;

  /** Generated Serial-ID. */
  private static final long serialVersionUID = 2992528074740195473L;

  /** Constructor. */
  public SQLResultSet() {
    super();
  }

  /**
   * Constructor.
   *
   * @param resultSet The {@link ResultSet} to initialize this to.
   * @throws SQLException
   */
  public SQLResultSet(final ResultSet resultSet) throws SQLException {
    super();

    set(resultSet);
  }

  /**
   * Sets the contents of this to the contents of the given ResultSet. <br>
   * <br>
   * <b>NOTE:</b> The internal cursor of the given {@link ResultSet} should be set to just before
   * the first element.
   *
   * @param resultSet The {@link ResultSet}.
   * @throws SQLException
   */
  public void set(final ResultSet resultSet) throws SQLException {
    clear();

    try {
      resultSet.beforeFirst();
    } catch (final SQLException e) {
      // Don't care if this throws an exception.
      // The SQLite driver will always throw an exception.
    }

    while (resultSet.next()) {
      add(new ResultRow(resultSet));
    }
  }

  /**
   * Attempts to cast the contents of the given column to a String.
   *
   * @param row The row of result to access.
   * @param colName The column name to cast.
   * @return The contents of the given column cast to a String.
   */
  public String getString(final int row, final String colName) {
    return get(row).getString(colName);
  }

  /**
   * Attempts to cast the contents of the first row to a String.
   *
   * @param colName The name of the column to cast.
   * @return The contents of the first row cast to a String.
   * @see SQLResultSet#FIRSTROW
   */
  public String getString(final String colName) {
    return getString(FIRSTROW, colName);
  }

  /**
   * Attempts to cast the contents of the given column to a Boolean.
   *
   * @param row The row of result to access.
   * @param colName The column name to cast.
   * @return The contents of the given column cast to a Boolean.
   */
  public Boolean getBoolean(final int row, final String colName) {
    return get(row).getBoolean(colName);
  }

  /**
   * Attempts to cast the contents of the first row to a Boolean.
   *
   * @param colName The name of the column to cast.
   * @return The contents of the first row cast to a Boolean.
   * @see SQLResultSet#FIRSTROW
   */
  public Boolean getBoolean(final String colName) {
    return getBoolean(FIRSTROW, colName);
  }

  /**
   * Attempts to cast the contents of the given column to an Integer.
   *
   * @param row The row of result to access.
   * @param colName The column name to cast.
   * @return The contents of the given column cast to an Integer.
   */
  public Integer getInt(final int row, final String colName) {
    return get(row).getInt(colName);
  }

  /**
   * Attempts to cast the contents of the first row to an Integer.
   *
   * @param colName The name of the column to cast.
   * @return The contents of the first row cast to an Integer.
   * @see SQLResultSet#FIRSTROW
   */
  public Integer getInt(final String colName) {
    return getInt(FIRSTROW, colName);
  }

  /**
   * Attempts to cast the contents of the given column to a Double.
   *
   * @param row The row of result to access.
   * @param colName The column name to cast.
   * @return The contents of the given column cast to a Double.
   */
  public Double getDouble(final int row, final String colName) {
    return get(row).getDouble(colName);
  }

  /**
   * Attempts to cast the contents of the first row to a Double.
   *
   * @param colName The name of the column to cast.
   * @return The contents of the first row cast to a Double.
   * @see SQLResultSet#FIRSTROW
   */
  public Double getDouble(final String colName) {
    return getDouble(FIRSTROW, colName);
  }

  /**
   * Checks if this contains the given value in the given column.
   *
   * @param colName The name of the column to check.
   * @param value The value of the column.
   * @return True if the value is present in the given column, otherwise false.
   */
  public boolean contains(final String colName, final Object value) {
    return this.get(colName, value) != null;
  }

  /**
   * Returns a {@link ResultRow} where the given column equals the given value.
   *
   * @param colName The name of the column.
   * @param value The value.
   * @return If the given value exists in the given column a {@link ResultRow} is returned,
   *     otherwise null is returned.
   */
  public ResultRow get(final String colName, final Object value) {
    for (final ResultRow row : this) {
      final Object rowValue = row.get(colName);

      if (rowValue != null && rowValue.equals(value)) {
        return row;
      }
    }

    return null;
  }

  /**
   * Returns all {@link ResultRow}s where the given column equals the given value.
   *
   * @param colName The name of the column.
   * @param value The value.
   * @return Returns an containing all {@link ResultRow}s where the given column equals the given
   *     value.
   */
  public SQLResultSet getAll(final String colName, final Object value) {
    final SQLResultSet set = new SQLResultSet();

    for (final ResultRow row : this) {
      final Object rowValue = row.get(colName);

      if (rowValue != null && rowValue.equals(value)) {
        set.add(row);
      }
    }

    return set;
  }
}
