package net.KabOOm356.Command.Commands;

import net.KabOOm356.Command.Help.Usage;
import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.UnassignPhrases;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Service.SQLStatServices.ModeratorStatService.ModeratorStat;
import net.KabOOm356.Throwable.IndexNotANumberException;
import net.KabOOm356.Throwable.IndexOutOfRangeException;
import net.KabOOm356.Throwable.NoLastViewedReportException;
import net.KabOOm356.Util.ArrayUtil;
import net.KabOOm356.Util.BukkitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A {@link ReporterCommand} that will handle unassigning players from reports..
 */
public class UnassignCommand extends ReporterCommand {
	private static final Logger log = LogManager.getLogger(UnassignCommand.class);

	private static final String name = "Unassign";
	private static final int minimumNumberOfArguments = 1;
	private static final String permissionNode = "reporter.unassign";

	private static final List<Usage> usages = Collections.unmodifiableList(ArrayUtil.arrayToArrayList(new Usage[]{new Usage(UnassignPhrases.unassignHelp, UnassignPhrases.unassignHelpDetails)}));
	private static final List<String> aliases = Collections.emptyList();

	/**
	 * Constructor.
	 *
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public UnassignCommand(final ReporterCommandManager manager) {
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
	public void execute(final CommandSender sender, final ArrayList<String> args) throws NoLastViewedReportException, IndexOutOfRangeException, IndexNotANumberException {
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

			unassignReport(sender, index);
		} catch (final Exception e) {
			log.error("Failed to unassign report!", e);
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

	private void unassignReport(final CommandSender sender, final int index) throws ClassNotFoundException, SQLException, InterruptedException {
		StringBuilder query = new StringBuilder();
		query.append("SELECT ClaimedByUUID, ClaimedBy FROM Reports WHERE ID=").append(index);
		String claimedByUUID, claimedBy;

		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			final SQLResultSet result = database.sqlQuery(connectionId, query.toString());

			claimedByUUID = result.getString("ClaimedByUUID");
			claimedBy = result.getString("ClaimedBy");

			query = new StringBuilder();
			query.append("UPDATE Reports ");
			query.append("SET ");
			query.append("ClaimStatus=0, ClaimedByUUID='', ClaimedBy='', ClaimPriority=0, ClaimDate='' ");
			query.append("WHERE ID=").append(index);

			database.updateQuery(connectionId, query.toString());
		} catch (final SQLException e) {
			log.error(String.format("Failed to execute unassign query on connection [%s]!", connectionId));
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}

		String playerName = claimedBy;
		OfflinePlayer claimingPlayer = null;

		if (!claimedByUUID.isEmpty()) {
			final UUID uuid = UUID.fromString(claimedByUUID);

			claimingPlayer = Bukkit.getOfflinePlayer(uuid);

			playerName = BukkitUtil.formatPlayerName(claimingPlayer);
		}

		String output = getManager().getLocale().getString(UnassignPhrases.reportUnassignSuccess);

		output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);
		output = output.replaceAll("%p", ChatColor.GOLD + playerName + ChatColor.WHITE);

		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);

		if (BukkitUtil.isOfflinePlayer(sender)) {
			final OfflinePlayer senderPlayer = (OfflinePlayer) sender;

			getServiceModule().getModStatsService().incrementStat(senderPlayer, ModeratorStat.UNASSIGNED);
		}

		if (claimingPlayer != null && claimingPlayer.isOnline()) {
			playerName = BukkitUtil.formatPlayerName(sender);

			output = getManager().getLocale().getString(UnassignPhrases.unassignedFromReport);

			output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.RED);
			output = output.replaceAll("%s", ChatColor.GOLD + playerName + ChatColor.RED);

			claimingPlayer.getPlayer().sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.RED + output);
		}
	}
}
