package net.KabOOm356.Command.Commands;

import net.KabOOm356.Command.Help.Usage;
import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.MovePhrases;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Service.SQLStatServices.ModeratorStatService.ModeratorStat;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A {@link ReporterCommand} that will handle moving reports to different priorities.
 */
public class MoveCommand extends ReporterCommand {
	private static final Logger log = LogManager.getLogger(MoveCommand.class);

	private static final String name = "Move";
	private static final int minimumNumberOfArguments = 2;
	private final static String permissionNode = "reporter.move";

	private static final List<Usage> usages = Collections.unmodifiableList(ArrayUtil.arrayToList(new Usage[]{new Usage(MovePhrases.moveHelp, MovePhrases.moveHelpDetails)}));
	private static final List<String> aliases = Collections.emptyList();

	/**
	 * Constructor.
	 *
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public MoveCommand(final ReporterCommandManager manager) {
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

			if (!getServiceModule().getPlayerService().requireModLevelInBounds(sender, args.get(1))) {
				return;
			}

			final ModLevel priority = ModLevel.getModLevel(args.get(1));
			moveReport(sender, index, priority);
		} catch (final Exception e) {
			log.error("Failed to execute move command!", e);
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

	protected void moveReport(final CommandSender sender, final int index, final ModLevel level) throws ClassNotFoundException, SQLException, InterruptedException {
		StringBuilder query = new StringBuilder();
		query.append("SELECT ClaimStatus, ClaimedByUUID, ClaimPriority ");
		query.append("FROM Reports ");
		query.append("WHERE ID=").append(index);

		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			final SQLResultSet result = database.sqlQuery(connectionId, query.toString());

			final boolean isClaimed = result.getBoolean("ClaimStatus");
			final int currentPriority = result.getInt("ClaimPriority");
			final String claimedByUUIDString = result.getString("ClaimedByUUID");
			UUID claimedByUUID = null;

			if (!claimedByUUIDString.isEmpty()) {
				claimedByUUID = UUID.fromString(claimedByUUIDString);
			}

			if (isClaimed && level.getLevel() > currentPriority) {
				// Clear the claim and upgrade the priority
				query = new StringBuilder();
				query.append("UPDATE Reports ");
				query.append("SET ")
						.append("ClaimStatus='0', ")
						.append("ClaimedByUUID='', ")
						.append("ClaimedBy='', ")
						.append("ClaimDate='', ")
						.append("ClaimPriority=0, ")
						.append("Priority=").append(level.getLevel()).append(' ');
				query.append("WHERE ID=").append(index);

				Player claimingPlayer = null;

				if (claimedByUUID != null) {
					claimingPlayer = Bukkit.getPlayer(claimedByUUID);
				}

				if (claimingPlayer != null) {
					final String playerName = BukkitUtil.formatPlayerName(sender);

					String output = getManager().getLocale().getString(MovePhrases.unassignedFromReport);

					output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.RED);
					output = output.replaceAll("%s", ChatColor.GOLD + playerName + ChatColor.RED);

					claimingPlayer.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.RED + output);
				}

			} else {
				query = new StringBuilder();
				query.append("UPDATE Reports ");
				query.append("SET ");
				query.append("Priority = ").append(level.getLevel()).append(' ');
				query.append("WHERE ID=").append(index);
			}

			database.updateQuery(connectionId, query.toString());
		} catch (final SQLException e) {
			log.log(Level.ERROR, String.format("Failed to move report priority on connection [%d]!", connectionId));
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}

		String output = getManager().getLocale().getString(MovePhrases.moveReportSuccess);

		output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);
		output = output.replaceAll("%p", level.getColor() + level.getName() + ChatColor.WHITE);

		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);

		if (BukkitUtil.isOfflinePlayer(sender)) {
			final OfflinePlayer senderPlayer = (OfflinePlayer) sender;

			getServiceModule().getModStatsService().incrementStat(senderPlayer, ModeratorStat.MOVED);
		}
	}
}
