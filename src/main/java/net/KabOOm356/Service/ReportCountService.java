package net.KabOOm356.Service;

import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Permission.ModLevel;

import java.sql.SQLException;
import java.util.List;

public class ReportCountService extends Service {
	protected ReportCountService(final ServiceModule module) {
		super(module);
	}

	/**
	 * Returns the current number of reports in the database.
	 *
	 * @return The current number of reports in the database.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public int getCount() throws InterruptedException, SQLException, ClassNotFoundException {
		final ExtendedDatabaseHandler database = getDatabase();
		final int connectionId = database.openPooledConnection();
		try {
			final String query = "SELECT COUNT(*) AS Count FROM Reports";
			final SQLResultSet result = database.sqlQuery(connectionId, query);
			return result.getInt("Count");
		} finally {
			database.closeConnection(connectionId);
		}
	}

	/**
	 * Returns the current number of incomplete reports in the database.
	 *
	 * @return The current number of incomplete reports in the database.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public int getIncompleteReports() throws ClassNotFoundException, SQLException, InterruptedException {
		return getIncompleteReportIndexes().size();
	}

	/**
	 * Returns the current number of complete reports in the database.
	 *
	 * @return The current number of complete reports in the database.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public int getCompletedReports() throws ClassNotFoundException, SQLException, InterruptedException {
		return getCompletedReportIndexes().size();
	}

	/**
	 * Gets the number of reports with a given {@link ModLevel} priority.
	 *
	 * @param level The {@link ModLevel} to get the number of reports for.
	 * @return The number of reports with the given {@link ModLevel} priority.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public int getNumberOfPriority(final ModLevel level) throws SQLException, ClassNotFoundException, InterruptedException {
		return getIndexesOfPriority(level).size();
	}

	private ExtendedDatabaseHandler getDatabase() {
		return getStore().getDatabaseStore().get();
	}

	private List<Integer> getIncompleteReportIndexes() throws InterruptedException, SQLException, ClassNotFoundException {
		return getModule().getReportInformationService().getIncompleteReportIndexes();
	}

	private List<Integer> getCompletedReportIndexes() throws InterruptedException, SQLException, ClassNotFoundException {
		return getModule().getReportInformationService().getCompletedReportIndexes();
	}

	private List<Integer> getIndexesOfPriority(final ModLevel level) throws InterruptedException, SQLException, ClassNotFoundException {
		return getModule().getReportInformationService().getIndexesOfPriority(level);
	}
}
