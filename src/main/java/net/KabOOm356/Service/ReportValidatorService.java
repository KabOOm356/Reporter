package net.KabOOm356.Service;

import net.KabOOm356.Throwable.IndexOutOfRangeException;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class ReportValidatorService extends Service {
	protected ReportValidatorService(final ServiceModule module) {
		super(module);
	}

	/**
	 * Checks if the report index is valid.  If it is not an {@link IndexOutOfRangeException} is thrown.
	 *
	 * @param index The report index to check is it is valid.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 * @throws IndexOutOfRangeException Thrown if the report index is not valid.
	 */
	public void requireReportIndexValid(final int index) throws ClassNotFoundException, SQLException, InterruptedException, IndexOutOfRangeException {
		final int count = getCount();
		if (index < 1 || index > count) {
			final String message = String.format("The requested index [%d] is out side of range [1-%d]", index, count);
			throw new IndexOutOfRangeException(message);
		}
	}

	/**
	 * Checks if the given report index is valid.  Silent version of {@link #requireReportIndexValid(int)} that will not throw a corresponding exception for the reason why the index is not valid.
	 * <br/>If the report index is not valid, the the given {@link CommandSender} will be alerted.
	 *
	 * @param index The report index to check if it is valid.
	 * @return True if the report index is valid, otherwise false.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public boolean isReportIndexValid(final int index) throws InterruptedException, SQLException, ClassNotFoundException {
		try {
			requireReportIndexValid(index);
			return true;
		} catch (final IndexOutOfRangeException e) {
			return false;
		}
	}

	private int getCount() throws InterruptedException, SQLException, ClassNotFoundException {
		return getModule().getReportCountService().getCount();
	}
}
