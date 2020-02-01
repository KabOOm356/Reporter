package net.KabOOm356.Command.Commands;

import net.KabOOm356.Command.Help.Usage;
import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.DowngradePhrases;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Throwable.IndexNotANumberException;
import net.KabOOm356.Throwable.IndexOutOfRangeException;
import net.KabOOm356.Throwable.NoLastViewedReportException;
import net.KabOOm356.Util.ArrayUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * A {@link ReporterCommand} that will handle downgrading a report's priority.
 */
public class DowngradeCommand extends ReporterCommand {
	private static final Logger log = LogManager.getLogger(DowngradeCommand.class);

	private static final String name = "Downgrade";
	private final static String permissionNode = "reporter.move";
	private static final int minimumNumberOfArguments = 1;

	private static final List<Usage> usages = Collections.unmodifiableList(ArrayUtil.arrayToList(new Usage[]{new Usage(DowngradePhrases.downgradeHelp, DowngradePhrases.downgradeHelpDetails)}));
	private static final List<String> aliases = Collections.emptyList();

	/**
	 * Constructor.
	 *
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public DowngradeCommand(final ReporterCommandManager manager) {
		super(manager, name, permissionNode, minimumNumberOfArguments);
	}

	/**
	 * Returns the name of this command.
	 *
	 * @return The name of this command.
	 */
	public static String getCommandName() {
		return name;
	}

	/**
	 * Returns the permission node of this command.
	 *
	 * @return The permission node of this command.
	 */
	public static String getCommandPermissionNode() {
		return permissionNode;
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) throws NoLastViewedReportException, IndexOutOfRangeException, IndexNotANumberException {
		try {
			if (!hasRequiredPermission(sender)) {
				return;
			}

			final int index = getServiceModule().getLastViewedReportService().getIndexOrLastViewedReport(sender, args.get(0));

			if (!getServiceModule().getReportValidatorService().isReportIndexValid(index)) {
				return;
			}

			if (!getServiceModule().getReportPermissionService().canAlterReport(sender, index)) {
				return;
			}

			final ModLevel newPriority = getNextLowestPriorityLevel(index);
			if (newPriority == ModLevel.UNKNOWN) {
				String output = getManager().getLocale().getString(DowngradePhrases.reportIsAtLowestPriority);
				output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.RED);
				sender.sendMessage(ChatColor.RED + output);
			} else {
				// Get MoveCommand and let it take care of moving the report to the new priority.
				final MoveCommand move = (MoveCommand) getManager().getCommand("Move");
				move.moveReport(sender, index, newPriority);
			}
		} catch (final Exception e) {
			log.error("Failed to downgrade report priority!", e);
			sender.sendMessage(getErrorMessage());
		}
	}

	@Override
	public List<Usage> getUsages() {
		return usages;
	}

	@Override
	public List<String> getAliases() {
		return aliases;
	}

	private ModLevel getNextLowestPriorityLevel(final int index) throws ClassNotFoundException, SQLException, InterruptedException {
		final String query = "SELECT Priority FROM Reports WHERE ID=" + index;

		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId;
		// Log the error with more detail.
		try {
			connectionId = database.openPooledConnection();
		} catch (final ClassNotFoundException | SQLException e) {
			log.error("Failed to open pooled connection to get the next lowest priority!");
			throw e;
		}
		try {
			final SQLResultSet result = database.sqlQuery(connectionId, query);
			final int currentPriorityLevel = result.getInt("Priority");
			return ModLevel.getByLevel(currentPriorityLevel - 1);
		} catch (final SQLException e) {
			log.error(String.format("Failed to get the next lowest priority on connection [%d]!", connectionId));
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}
	}
}
