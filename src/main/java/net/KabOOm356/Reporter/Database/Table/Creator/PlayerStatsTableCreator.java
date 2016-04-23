package net.KabOOm356.Reporter.Database.Table.Creator;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.DatabaseTableCreator;
import net.KabOOm356.Util.DatabaseUtil;

public class PlayerStatsTableCreator extends DatabaseTableCreator {
	public PlayerStatsTableCreator(final Database database, final String databaseVersion, final String tableName) {
		super(database, databaseVersion, tableName);
	}

	@Override
	protected String getTableCreationQuery() {
		return "CREATE TABLE IF NOT EXISTS PlayerStats (" +
				DatabaseUtil.getAutoIncrementingPrimaryKeyQuery(getDatabase(), "ID") + ", " +
				"Name VARCHAR(16) NOT NULL, " +
				"UUID VARCHAR(36) NOT NULL, " +
				"FirstReportDate VARCHAR(19) NOT NULL DEFAULT '', " +
				"LastReportDate VARCHAR(19) NOT NULL DEFAULT '', " +
				"ReportCount INTEGER NOT NULL DEFAULT '0', " +
				"FirstReportedDate VARCHAR(19) NOT NULL DEFAULT '', " +
				"LastReportedDate VARCHAR(19) NOT NULL DEFAULT '', " +
				"ReportedCount INTEGER NOT NULL DEFAULT '0');";
	}
}
