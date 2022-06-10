package net.KabOOm356.Reporter.Database.Table.Creator;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Database.Table.DatabaseTableCreator;
import net.KabOOm356.Util.DatabaseUtil;

public class ReportTableCreator extends DatabaseTableCreator {
  public ReportTableCreator(
      final Database database, final String databaseVersion, final String tableName) {
    super(database, databaseVersion, tableName);
  }

  @Override
  protected String getTableCreationQuery() {
    final StringBuilder createQuery = new StringBuilder();
    createQuery
        .append("CREATE TABLE IF NOT EXISTS Reports (")
        .append("ID INTEGER PRIMARY KEY, ")
        .append("Date CHAR(19) NOT NULL DEFAULT 'N/A', ")
        .append("SenderUUID CHAR(36) DEFAULT '', ")
        .append("Sender VARCHAR(32), ")
        .append("ReportedUUID CHAR(36) DEFAULT '', ")
        .append("Reported VARCHAR(32) NOT NULL DEFAULT '* (Anonymous)', ")
        .append("Details VARCHAR(200) NOT NULL, ")
        .append("Priority TINYINT NOT NULL DEFAULT '0', ")
        .append("SenderWorld VARCHAR(100) DEFAULT '', ")
        .append("SenderX DOUBLE NOT NULL DEFAULT '0.0', ")
        .append("SenderY DOUBLE NOT NULL DEFAULT '0.0', ")
        .append("SenderZ DOUBLE NOT NULL DEFAULT '0.0', ")
        .append("ReportedWorld VARCHAR(100) DEFAULT '', ")
        .append("ReportedX DOUBLE DEFAULT '0.0', ")
        .append("ReportedY DOUBLE DEFAULT '0.0', ")
        .append("ReportedZ DOUBLE DEFAULT '0.0', ")
        .append("CompletionStatus BOOLEAN NOT NULL DEFAULT '0', ")
        .append("CompletedByUUID CHAR(36) DEFAULT '', ")
        .append("CompletedBy VARCHAR(32) DEFAULT '', ")
        .append("CompletionDate CHAR(19) DEFAULT '', ")
        .append("CompletionSummary VARCHAR(200) DEFAULT '', ")
        .append("ClaimStatus BOOLEAN NOT NULL DEFAULT '0', ")
        .append("ClaimDate CHAR(19) DEFAULT '', ")
        .append("ClaimedByUUID CHAR(36) DEFAULT '', ")
        .append("ClaimedBy VARCHAR(32) DEFAULT '', ")
        .append("ClaimPriority TINYINT DEFAULT '0')");
    if (getDatabase().getDatabaseType() == DatabaseType.MYSQL) {
      createQuery.append(DatabaseUtil.getTableCreationMetadata(getDatabase()));
    }
    createQuery.append(';');
    return createQuery.toString();
  }
}
