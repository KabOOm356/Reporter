package net.KabOOm356.Manager.SQLStatManagers;

import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Manager.SQLStatManager;

/**
 * A class to manage tracking statistics for moderators.
 */
public class ModeratorStatManager extends SQLStatManager {
	/**
	 * The case-sensitive table name for the player statistics.
	 */
	public static final String tableName = "ModStats";
	/**
	 * The case-sensitive column name that should be used as an index.
	 */
	public static final String indexColumn = "ModUUID";
	/**
	 * The case-sensitive column name that should be used as a secondary index.
	 */
	public static final String secondaryIndexColumn = "ModName";

	/**
	 * Constructor.
	 *
	 * @param database The database where the statistics are being stored.
	 */
	public ModeratorStatManager(ExtendedDatabaseHandler database) {
		super(database, tableName, indexColumn, secondaryIndexColumn);
	}

	/**
	 * A class that represents a statistic for a moderator.
	 */
	public static class ModeratorStat extends SQLStat {
		/**
		 * The number of players assigned to reports by this moderator.
		 */
		public static final ModeratorStat ASSIGNED = new ModeratorStat("Assigned", "AssignCount");
		/**
		 * The number of reports that have been claimed by this moderator.
		 */
		public static final ModeratorStat CLAIMED = new ModeratorStat("Claimed", "ClaimedCount");
		/**
		 * The number of reports claimed by this moderator.
		 */
		public static final ModeratorStat COMPLETED = new ModeratorStat("Completed", "CompletionCount");
		/**
		 * The number of reports deleted by this moderator.
		 */
		public static final ModeratorStat DELETED = new ModeratorStat("Deleted", "DeletionCount");
		/**
		 * The number of reports moved to a different priority by this moderator.
		 * This includes upgrading and downgrading report priorities.
		 */
		public static final ModeratorStat MOVED = new ModeratorStat("Moved", "MoveCount");
		/**
		 * The number of reports this moderator has responded to.
		 */
		public static final ModeratorStat RESPONDED = new ModeratorStat("Responded", "RespondCount");
		/**
		 * The number of players unassigned from reports by this moderator.
		 */
		public static final ModeratorStat UNASSIGNED = new ModeratorStat("Unassigned", "UnassignCount");
		/**
		 * The number of reports unclaimed by this moderator.
		 */
		public static final ModeratorStat UNCLAIMED = new ModeratorStat("Unclaimed", "UnclaimCount");

		/**
		 * Constructor.
		 *
		 * @param name       The name of the statistic.
		 * @param columnName The case-sensitive column name of the statistic.
		 */
		protected ModeratorStat(String name, String columnName) {
			super(name, columnName);
		}
	}
}
