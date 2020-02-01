package net.KabOOm356.Service;

import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Util.BukkitUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportInformationService extends Service {
	protected ReportInformationService(final ServiceModule module) {
		super(module);
	}

	/**
	 * Returns the report indexes of all reports the given {@link CommandSender} can view.
	 *
	 * @param sender The {@link CommandSender}
	 * @return A {@link List} of integers that contains all the report indexes the {@link CommandSender} can view.
	 * @throws InterruptedException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public List<Integer> getViewableReports(final CommandSender sender) throws InterruptedException, SQLException, ClassNotFoundException {
		final String query;
		final List<String> params = new ArrayList<>();

		if (BukkitUtil.isPlayer(sender)) {
			final OfflinePlayer player = OfflinePlayer.class.cast(sender);
			query = "SELECT ID FROM Reports WHERE SenderUUID=?";
			params.add(player.getUniqueId().toString());
		} else {
			query = "SELECT ID FROM Reports WHERE Sender=?";
			params.add(sender.getName());
		}

		final List<Integer> indexes = new ArrayList<>();
		final ExtendedDatabaseHandler database = getDatabase();
		final int connectionId = database.openPooledConnection();

		try {
			final SQLResultSet result = database.preparedSQLQuery(connectionId, query, params);

			for (final ResultRow row : result) {
				indexes.add(row.getInt("ID"));
			}
		} finally {
			database.closeConnection(connectionId);
		}

		return indexes;
	}

	/**
	 * Returns the indexes of the completed reports in the database.
	 *
	 * @return A {@link List} of integers containing the indexes to all the completed reports in the database.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public List<Integer> getCompletedReportIndexes() throws SQLException, ClassNotFoundException, InterruptedException {
		final List<Integer> indexes = new ArrayList<>();
		final ExtendedDatabaseHandler database = getDatabase();
		final int connectionId = database.openPooledConnection();
		try {
			final String query = "SELECT ID FROM Reports WHERE CompletionStatus=1";
			final SQLResultSet result = database.sqlQuery(connectionId, query);

			for (final ResultRow row : result) {
				indexes.add(row.getInt("ID"));
			}
		} finally {
			database.closeConnection(connectionId);
		}

		return indexes;
	}

	/**
	 * Returns the indexes of the incomplete reports in the database.
	 *
	 * @return A {@link List} of integers containing the indexes to all the incomplete reports in the database.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public List<Integer> getIncompleteReportIndexes() throws ClassNotFoundException, SQLException, InterruptedException {
		final List<Integer> indexes = new ArrayList<>();
		final ExtendedDatabaseHandler database = getDatabase();
		final int connectionId = database.openPooledConnection();
		try {
			final String query = "SELECT ID FROM Reports WHERE CompletionStatus=0";

			final SQLResultSet result = database.sqlQuery(connectionId, query);

			for (final ResultRow row : result) {
				indexes.add(row.getInt("ID"));
			}
		} finally {
			database.closeConnection(connectionId);
		}

		return indexes;
	}

	/**
	 * Gets the indexes of the reports with a given {@link ModLevel} priority.
	 *
	 * @param level The {@link ModLevel} priority of the reports to get the indexes for.
	 * @return The indexes of the reports with the given {@link ModLevel} priority.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public List<Integer> getIndexesOfPriority(final ModLevel level) throws ClassNotFoundException, SQLException, InterruptedException {
		final List<Integer> indexes = new ArrayList<>();
		final String query = "SELECT ID FROM Reports WHERE Priority = " + level.getLevel();

		final ExtendedDatabaseHandler database = getDatabase();
		final int connectionId = database.openPooledConnection();
		try {
			final SQLResultSet result = database.sqlQuery(connectionId, query);

			for (final ResultRow row : result) {
				indexes.add(row.getInt("ID"));
			}
		} finally {
			database.closeConnection(connectionId);
		}

		return indexes;
	}

	/**
	 * Gets the priority of the report at the given index.
	 *
	 * @param index The index of the report to get the priority for.
	 * @return The priority for the report at the given index if one exists, otherwise {@link ModLevel#UNKNOWN} is returned.
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	public ModLevel getReportPriority(final int index) throws ClassNotFoundException, InterruptedException, SQLException {
		final String query = "SELECT Priority FROM Reports WHERE ID=" + index;
		final ExtendedDatabaseHandler database = getDatabase();
		Integer connectionId = null;
		try {
			connectionId = database.openPooledConnection();
			final SQLResultSet result = database.sqlQuery(connectionId, query);
			final int level = result.getInt("Priority");
			return ModLevel.getByLevel(level);
		} finally {
			database.closeConnection(connectionId);
		}
	}

	private ExtendedDatabaseHandler getDatabase() {
		return getStore().getDatabaseStore().get();
	}
}
