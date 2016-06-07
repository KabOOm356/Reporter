package net.KabOOm356.Command.Commands;

import net.KabOOm356.Command.Help.Usage;
import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.UnclaimPhrases;
import net.KabOOm356.Service.SQLStatServices.ModeratorStatService.ModeratorStat;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Throwable.IndexNotANumberException;
import net.KabOOm356.Throwable.IndexOutOfRangeException;
import net.KabOOm356.Throwable.NoLastViewedReportException;
import net.KabOOm356.Util.ArrayUtil;
import net.KabOOm356.Util.BukkitUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A {@link ReporterCommand} that will handle unclaiming reports.
 */
public class UnclaimCommand extends ReporterCommand {
	private static final Logger log = LogManager.getLogger(UnclaimCommand.class);

	private static final String name = "Unclaim";
	private static final int minimumNumberOfArguments = 1;
	private final static String permissionNode = "reporter.claim";

	private static final List<Usage> usages = Collections.unmodifiableList(ArrayUtil.arrayToArrayList(new Usage[]{new Usage(UnclaimPhrases.unclaimHelp, UnclaimPhrases.unclaimHelpDetails)}));
	private static final List<String> aliases = Collections.emptyList();

	/**
	 * Constructor.
	 *
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public UnclaimCommand(final ReporterCommandManager manager) {
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

			final int index = getManager().getLastViewedReportService().getIndexOrLastViewedReport(sender, args.get(0));

			if (!getManager().isReportIndexValid(sender, index)) {
				return;
			}

			if (canUnclaimReport(sender, index)) {
				unclaimReport(sender, index);
			}
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to unclaim report!", e);
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

	private boolean canUnclaimReport(final CommandSender sender, final int index) throws ClassNotFoundException, SQLException, InterruptedException {
		final String query = "SELECT ClaimStatus, ClaimedByUUID, ClaimedBy FROM Reports WHERE ID=" + index;

		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			final SQLResultSet result = database.sqlQuery(connectionId, query);

			if (result.getBoolean("ClaimStatus")) {
				boolean senderIsClaimingPlayer = false;
				OfflinePlayer claimingPlayer = null;

				// Do UUID player comparison.
				if (!result.getString("ClaimedByUUID").isEmpty()) {
					final UUID uuid = UUID.fromString(result.getString("ClaimedByUUID"));

					claimingPlayer = Bukkit.getPlayer(uuid);

					if (BukkitUtil.isPlayer(sender)) {
						final Player senderPlayer = (Player) sender;

						if (senderPlayer.getUniqueId().equals(claimingPlayer.getUniqueId())) {
							senderIsClaimingPlayer = true;
						}
					}
				} else // Do name based player comparison.
				{
					if (sender.getName().equals(result.getString("ClaimedBy"))) {
						senderIsClaimingPlayer = true;
					}
				}

				if (!senderIsClaimingPlayer) {
					String output = getManager().getLocale().getString(UnclaimPhrases.reportAlreadyClaimed);

					String claimedBy = result.getString("ClaimedBy");

					if (claimingPlayer != null) {
						claimedBy = BukkitUtil.formatPlayerName(claimingPlayer);
					}

					output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.RED);
					output = output.replaceAll("%c", ChatColor.BLUE + claimedBy + ChatColor.RED);

					sender.sendMessage(ChatColor.RED + output);

					return false;
				}
			} else {
				String output = getManager().getLocale().getString(UnclaimPhrases.reportIsNotClaimed);

				output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.RED);

				sender.sendMessage(ChatColor.RED + output);

				return false;
			}
		} catch (final SQLException e) {
			log.error(String.format("Failed to determine if player can unclaim report on connection [%s]!", connectionId));
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}

		return true;
	}

	private void unclaimReport(final CommandSender sender, final int index) throws ClassNotFoundException, SQLException, InterruptedException {
		final String query = "UPDATE Reports " +
				"SET " +
				"ClaimStatus=0, ClaimedByUUID='', ClaimedBy='', ClaimPriority=0, ClaimDate='' " +
				"WHERE ID=" + index;

		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			database.updateQuery(connectionId, query);
		} catch (final SQLException e) {
			log.error(String.format("Failed to execute unclaim query on connection [%s]!", connectionId));
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}

		String output = getManager().getLocale().getString(UnclaimPhrases.reportUnclaimSuccess);

		output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);

		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);

		if (BukkitUtil.isOfflinePlayer(sender)) {
			final OfflinePlayer senderPlayer = (OfflinePlayer) sender;

			getManager().getModStatsService().incrementStat(senderPlayer, ModeratorStat.UNCLAIMED);
		}
	}
}
