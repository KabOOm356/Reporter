package net.KabOOm356.Command.Commands;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.MovePhrases;
import net.KabOOm356.Manager.SQLStatManagers.ModeratorStatManager.ModeratorStat;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.Util;
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
import java.util.UUID;

/**
 * A {@link ReporterCommand} that will handle moving reports to different priorities.
 */
public class MoveCommand extends ReporterCommand {
	private static final Logger log = LogManager.getLogger(MoveCommand.class);

	private static final String name = "Move";
	private static final int minimumNumberOfArguments = 2;
	private final static String permissionNode = "reporter.move";

	/**
	 * Constructor.
	 *
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public MoveCommand(ReporterCommandManager manager) {
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

			if (!getManager().requireModLevelInBounds(sender, args.get(1)))
				return;

			final ModLevel priority = ModLevel.getModLevel(args.get(1));
			moveReport(sender, index, priority);
		} catch (final Exception e) {
			log.error("Failed to execute move command!", e);
			sender.sendMessage(getErrorMessage());
		}
	}

	protected void moveReport(CommandSender sender, int index, ModLevel level) throws ClassNotFoundException, SQLException, InterruptedException {
		StringBuilder query = new StringBuilder();
		query.append("SELECT ClaimStatus, ClaimedByUUID, ClaimPriority ");
		query.append("FROM Reports ");
		query.append("WHERE ID=").append(index);

		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			final SQLResultSet result = database.sqlQuery(connectionId, query.toString());

			boolean isClaimed = result.getBoolean("ClaimStatus");
			int currentPriority = result.getInt("ClaimPriority");
			String claimedByUUIDString = result.getString("ClaimedByUUID");
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
					String playerName = BukkitUtil.formatPlayerName(sender);

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
			OfflinePlayer senderPlayer = (OfflinePlayer) sender;

			getManager().getModStatsManager().incrementStat(senderPlayer, ModeratorStat.MOVED);
		}
	}

	@Override
	public void updateDocumentation() {
		super.updateDocumentation(
				getManager().getLocale().getString(MovePhrases.moveHelp),
				getManager().getLocale().getString(MovePhrases.moveHelpDetails));
	}
}
