package net.KabOOm356.Command.Commands;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.DowngradePhrases;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * A {@link ReporterCommand} that will handle downgrading a report's priority.
 */
public class DowngradeCommand extends ReporterCommand {
	private static final Logger log = LogManager.getLogger(DowngradeCommand.class);

	private static final String name = "Downgrade";
	private final static String permissionNode = "reporter.move";
	private static int minimumNumberOfArguments = 1;

	/**
	 * Constructor.
	 *
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public DowngradeCommand(ReporterCommandManager manager) {
		super(manager, name, permissionNode, minimumNumberOfArguments);

		updateDocumentation();
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
	public void execute(CommandSender sender, ArrayList<String> args) {
		try {
			if (!hasRequiredPermission(sender))
				return;

			int index = Util.parseInt(args.get(0));

			if (args.get(0).equalsIgnoreCase("last")) {
				if (!hasRequiredLastViewed(sender))
					return;

				index = getLastViewed(sender);
			}

			if (!getManager().isReportIndexValid(sender, index))
				return;

			if (!getManager().canAlterReport(sender, index))
				return;

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

	private ModLevel getNextLowestPriorityLevel(int index) throws ClassNotFoundException, SQLException, InterruptedException {
		String query = "SELECT Priority FROM Reports WHERE ID=" + index;

		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId;
		// Log the error with more detail.
		try {
			connectionId = database.openPooledConnection();
		} catch (final ClassNotFoundException e) {
			log.error("Failed to open pooled connection to get the next lowest priority!");
			throw e;
		} catch (final SQLException e) {
			log.error("Failed to open pooled connection to get the next lowest priority!");
			throw e;
		}
		try {
			SQLResultSet result = database.sqlQuery(connectionId, query);
			int currentPriorityLevel = result.getInt("Priority");
			return ModLevel.getByLevel(currentPriorityLevel - 1);
		} catch (final SQLException e) {
			log.error(String.format("Failed to get the next lowest priorirty on connection [%d]!", connectionId));
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}
	}

	@Override
	public void updateDocumentation() {
		super.updateDocumentation(
				getManager().getLocale().getString(DowngradePhrases.downgradeHelp),
				getManager().getLocale().getString(DowngradePhrases.downgradeHelpDetails));
	}
}
