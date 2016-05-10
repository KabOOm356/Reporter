package net.KabOOm356.Manager.SQLStatManagers;

import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Manager.SQLStatManager;

/**
 * A class to manage setting player statistics held in an SQL database.
 */
public class PlayerStatManager extends SQLStatManager {
	/**
	 * The case-sensitive table name for the player statistics.
	 */
	public static final String tableName = "PlayerStats";
	/**
	 * The case-sensitive column name that should be used as an index.
	 */
	public static final String indexColumn = "UUID";
	/**
	 * The case-sensitive column name that should be used as a secondary index.
	 */
	public static final String secondaryIndexColumn = "Name";

	/**
	 * Constructor.
	 *
	 * @param database The database where the statistics are being stored.
	 */
	public PlayerStatManager(ExtendedDatabaseHandler database) {
		super(database, tableName, indexColumn, secondaryIndexColumn);
	}

	/**
	 * A class that represents a statistic for a player.
	 */
	public static class PlayerStat extends SQLStat {
		/**
		 * The number of times this player has reported someone else.
		 */
		public static final PlayerStat REPORTCOUNT = new PlayerStat("Report", "ReportCount");
		/**
		 * The date this player first reported someone else.
		 */
		public static final PlayerStat FIRSTREPORTDATE = new PlayerStat("FirstReport", "FirstReportDate");
		/**
		 * The date this player last reported someone else.
		 */
		public static final PlayerStat LASTREPORTDATE = new PlayerStat("LastReport", "LastReportDate");
		/**
		 * The number of times this player has been reported.
		 */
		public static final PlayerStat REPORTED = new PlayerStat("Reported", "ReportedCount");
		/**
		 * The date this player was first reported.
		 */
		public static final PlayerStat FIRSTREPORTEDDATE = new PlayerStat("FirstReported", "FirstReportedDate");
		/**
		 * The date this player was last reported.
		 */
		public static final PlayerStat LASTREPORTEDDATE = new PlayerStat("LastReported", "LastReportedDate");

		/**
		 * Constructor.
		 *
		 * @param name       The name of the statistic.
		 * @param columnName The case-sensitive column name of the statistic.
		 */
		protected PlayerStat(String name, String columnName) {
			super(name, columnName);
		}
	}
}
