package net.KabOOm356.Command.Commands;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Locale.Entry.LocalePhrases.ClaimPhrases;
import net.KabOOm356.Manager.SQLStatManagers.ModeratorStatManager.ModeratorStat;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.Util;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

/**
 * A {@link ReporterCommand} to handle players claiming reports.
 */
public class ClaimCommand extends ReporterCommand {
	private static final Logger log = LogManager.getLogger(ClaimCommand.class);

	private static final String name = "Claim";
	private static final int minimumNumberOfArguments = 1;
	private final static String permissionNode = "reporter.claim";

	/**
	 * Constructor.
	 *
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public ClaimCommand(ReporterCommandManager manager) {
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

			claimReport(sender, index);
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to claim report!", e);
			sender.sendMessage(getErrorMessage());
		}
	}

	private void claimReport(CommandSender sender, int index) throws ClassNotFoundException, SQLException, InterruptedException {
		ArrayList<String> params = new ArrayList<String>();

		params.add("1");
		params.add(BukkitUtil.getUUIDString(sender));
		params.add(sender.getName());
		params.add(Integer.toString(getManager().getModLevel(sender).getLevel()));
		params.add(Reporter.getDateformat().format(new Date()));
		params.add(Integer.toString(index));

		String query = "UPDATE Reports " +
				"SET ClaimStatus=?, ClaimedByUUID=?, ClaimedBy=?, ClaimPriority=?, ClaimDate=? " +
				"WHERE ID=?";

		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			database.preparedUpdateQuery(connectionId, query, params);
		} catch (final SQLException e) {
			log.error(String.format("Failed to execute claim query on connection [%d]!", connectionId));
		} finally {
			database.closeConnection(connectionId);
		}

		String output = getManager().getLocale().getString(ClaimPhrases.reportClaimSuccess);

		output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);

		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);

		if (BukkitUtil.isOfflinePlayer(sender)) {
			OfflinePlayer senderPlayer = (OfflinePlayer) sender;

			getManager().getModStatsManager().incrementStat(senderPlayer, ModeratorStat.CLAIMED);
		}
	}

	/**
	 * Updates the documentation for the command.
	 * <br/>
	 * This should be called after the locale has changed.
	 */
	public void updateDocumentation() {
		super.updateDocumentation(
				getManager().getLocale().getString(ClaimPhrases.claimHelp),
				getManager().getLocale().getString(ClaimPhrases.claimHelpDetails));
	}
}
