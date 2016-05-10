package net.KabOOm356.Command.Commands;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.UpgradePhrases;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * A {@link ReporterCommand} that will handle upgrading a report's priority.
 */
public class UpgradeCommand extends ReporterCommand {
	private static final Logger log = LogManager.getLogger(UpgradeCommand.class);

	private static final String name = "Upgrade";
	private final static String permissionNode = "reporter.move";
	private static final int minimumNumberOfArguments = 1;

	/**
	 * Constructor.
	 *
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public UpgradeCommand(final ReporterCommandManager manager) {
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
	public void execute(final CommandSender sender, final ArrayList<String> args) {
		try {
			if (!hasRequiredPermission(sender)) {
				return;
			}

			int index = Util.parseInt(args.get(0));

			if (args.get(0).equalsIgnoreCase("last")) {
				if (!hasRequiredLastViewed(sender)) {
					return;
				}

				index = getLastViewed(sender);
			}

			if (!getManager().isReportIndexValid(sender, index)) {
				return;
			}

			if (!getManager().canAlterReport(sender, index)) {
				return;
			}

			final ModLevel newPriority = getNextPriorityLevel(index);
			if (newPriority == ModLevel.UNKNOWN) {
				String output = getManager().getLocale().getString(UpgradePhrases.reportIsAtHighestPriority);
				output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.RED);
				sender.sendMessage(ChatColor.RED + output);
				return;
			}

			// Get MoveCommand and let it take care of moving the report to the new priority.
			final MoveCommand move = (MoveCommand) getManager().getCommand("Move");
			move.moveReport(sender, index, newPriority);
		} catch (final Exception e) {
			log.error("Failed to upgrade report priority!", e);
			sender.sendMessage(getErrorMessage());
		}
	}

	private ModLevel getNextPriorityLevel(final int index) throws ClassNotFoundException, SQLException, InterruptedException {
		final String query = "SELECT Priority FROM Reports WHERE ID=" + index;

		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId;
		try {
			connectionId = database.openPooledConnection();
		} catch (final ClassNotFoundException e) {
			log.error("Failed to open pooled connection to get next highest priority!");
			throw e;
		} catch (final SQLException e) {
			log.error("Failed to open pooled connection to get next highest priority!");
			throw e;
		}
		try {
			final SQLResultSet result = database.sqlQuery(connectionId, query);
			final int currentPriorityLevel = result.getInt("Priority");
			return ModLevel.getByLevel(currentPriorityLevel + 1);
		} catch (final SQLException e) {
			log.error(String.format("Failed to execute query to get next highest priority on connection [%s]!", connectionId));
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}
	}

	@Override
	public void updateDocumentation() {
		super.updateDocumentation(
				getManager().getLocale().getString(UpgradePhrases.upgradeHelp),
				getManager().getLocale().getString(UpgradePhrases.upgradeHelpDetails));
	}
}
