package net.KabOOm356.Database;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * A {@link HashMap} that represents a row returned from a SQL query.
 */
public class ResultRow extends HashMap<String, Object> {
	private static final Logger log = LogManager.getLogger(ResultRow.class);

	/**
	 * Generated Serial-ID.
	 */
	private static final long serialVersionUID = -1489657675159738791L;

	/**
	 * Constructor.
	 */
	public ResultRow() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param result The {@link ResultSet} to initialize this to.
	 * @throws SQLException
	 */
	public ResultRow(final ResultSet result) throws SQLException {
		super();

		set(result);
	}

	/**
	 * Sets the contents of this to the current row of the given ResultSet.
	 *
	 * @param result The {@link ResultSet}.
	 * @throws SQLException
	 */
	public void set(final ResultSet result) throws SQLException {
		try {
			final ResultSetMetaData metaData = result.getMetaData();
			final int columns = metaData.getColumnCount();
			clear();
			for (int LCV = 1; LCV <= columns; LCV++) {
				put(metaData.getColumnName(LCV), result.getObject(LCV));
			}
		} catch (final SQLException e) {
			if (log.isDebugEnabled()) {
				log.log(Level.WARN, "Failed to set ResultRow contents!");
			}
			throw e;
		}
	}

	/**
	 * Attempts to cast the contents of the given column to a String.
	 *
	 * @param colName The name of the column.
	 * @return The column cast to a String if the column exists, otherwise null.
	 */
	public String getString(final String colName) {
		if (get(colName) == null) {
			return null;
		}
		return get(colName).toString();
	}

	/**
	 * Attempts to cast the contents of the given column to a Boolean.
	 *
	 * @param colName The name of the column.
	 * @return The column cast to a Boolean if the column exists, otherwise null.
	 */
	public Boolean getBoolean(final String colName) {
        if (get(colName) == null) {
            return null;
        }

        // Try to parse for the Boolean, only returns true if the string value is "true".
        boolean value = Boolean.parseBoolean(getString(colName));
        // Tries to parse for the Boolean by Integer value, true if the string value is "1".
        value = value || getString(colName).equals("1");

        return value;
    }

	/**
	 * Attempts to cast the contents of the given column to an Integer.
	 *
	 * @param colName The name of the column.
	 * @return The column cast to an Integer if the column exists, otherwise null.
	 */
	public Integer getInt(final String colName) {
		if (get(colName) == null) {
			return null;
		}
		return Integer.parseInt(getString(colName));
	}

	/**
	 * Attempts to cast the contents of the given column to a Double.
	 *
	 * @param colName The name of the column.
	 * @return The column cast to a Double if the column exists, otherwise null.
	 */
	public Double getDouble(final String colName) {
		if (get(colName) == null) {
			return null;
		}
		return Double.parseDouble(getString(colName));
	}
}
