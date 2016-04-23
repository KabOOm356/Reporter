package net.KabOOm356.Reporter.Database.Table.Creator;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.DatabaseTableCreator;
import net.KabOOm356.Util.DatabaseUtil;

public class ModStatsTableCreator extends DatabaseTableCreator {
	public ModStatsTableCreator(final Database database, final String databaseVersion, final String tableName) {
		super(database, databaseVersion, tableName);
	}

	@Override
	protected String getTableCreationQuery() {
		return "CREATE TABLE IF NOT EXISTS ModStats (" +
				DatabaseUtil.getAutoIncrementingPrimaryKeyQuery(getDatabase(), "ID") + ", " +
				"ModName VARCHAR(16) NOT NULL, " +
				"ModUUID VARCHAR(36) NOT NULL, " +
				"AssignCount INTEGER NOT NULL DEFAULT '0', " +
				"ClaimedCount INTEGER NOT NULL DEFAULT '0', " +
				"CompletionCount INTEGER NOT NULL DEFAULT '0', " +
				"DeletionCount INTEGER NOT NULL DEFAULT '0', " +
				"MoveCount INTEGER NOT NULL DEFAULT '0', " +
				"RespondCount INTEGER NOT NULL DEFAULT '0', " +
				"UnassignCount INTEGER NOT NULL DEFAULT '0', " +
				"UnclaimCount INTEGER NOT NULL DEFAULT '0');";
	}
}
