package net.KabOOm356.Util;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import org.apache.commons.lang.Validate;

public final class DatabaseUtil {
	private DatabaseUtil() {
	}

	public static String getAutoIncrementingPrimaryKeyQuery(final Database database, final String columnName) {
		Validate.notNull(database);
		Validate.notNull(columnName);
		Validate.notEmpty(columnName);

		return columnName + " INTEGER PRIMARY KEY" +
				getAutoIncrementingPrimaryKeySuffix(database);
	}

	public static String getAutoIncrementingPrimaryKeySuffix(final Database database) {
		// If the database is using MySQL, set the primary key to auto increment.
		// SQLite does this automatically for primary keys.
		return (database.getDatabaseType() == DatabaseType.MYSQL) ? " AUTO_INCREMENT" : "";
	}

	public static String getColumnsSizeName(final Database database) {
		return (database.getDatabaseType() == DatabaseType.SQLITE) ? "TYPE_NAME" : "COLUMN_SIZE";
	}
}
