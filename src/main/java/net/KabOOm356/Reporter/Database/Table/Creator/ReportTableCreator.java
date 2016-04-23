package net.KabOOm356.Reporter.Database.Table.Creator;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.DatabaseTableCreator;

public class ReportTableCreator extends DatabaseTableCreator {
	public ReportTableCreator(final Database database, final String databaseVersion, final String tableName) {
		super(database, databaseVersion, tableName);
	}

	@Override
	protected String getTableCreationQuery() {
		return "CREATE TABLE IF NOT EXISTS Reports (" +
				"ID INTEGER PRIMARY KEY, " +
				"Date CHAR(19) NOT NULL DEFAULT 'N/A', " +
				"SenderUUID CHAR(36) DEFAULT '', " +
				"Sender VARCHAR(32), " +
				"ReportedUUID CHAR(36) DEFAULT '', " +
				"Reported VARCHAR(32) NOT NULL DEFAULT '* (Anonymous)', " +
				"Details VARCHAR(200) NOT NULL, " +
				"Priority TINYINT NOT NULL DEFAULT '0', " +
				"SenderWorld VARCHAR(100) DEFAULT '', " +
				"SenderX DOUBLE NOT NULL DEFAULT '0.0', " +
				"SenderY DOUBLE NOT NULL DEFAULT '0.0', " +
				"SenderZ DOUBLE NOT NULL DEFAULT '0.0', " +
				"ReportedWorld VARCHAR(100) DEFAULT '', " +
				"ReportedX DOUBLE DEFAULT '0.0', " +
				"ReportedY DOUBLE DEFAULT '0.0', " +
				"ReportedZ DOUBLE DEFAULT '0.0', " +
				"CompletionStatus BOOLEAN NOT NULL DEFAULT '0', " +
				"CompletedByUUID CHAR(36) DEFAULT '', " +
				"CompletedBy VARCHAR(32) DEFAULT '', " +
				"CompletionDate CHAR(19) DEFAULT '', " +
				"CompletionSummary VARCHAR(200) DEFAULT '', " +
				"ClaimStatus BOOLEAN NOT NULL DEFAULT '0', " +
				"ClaimDate CHAR(19) DEFAULT '', " +
				"ClaimedByUUID CHAR(36) DEFAULT '', " +
				"ClaimedBy VARCHAR(32) DEFAULT '', " +
				"ClaimPriority TINYINT DEFAULT '0');";
	}
}
