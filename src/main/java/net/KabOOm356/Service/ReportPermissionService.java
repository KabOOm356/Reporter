package net.KabOOm356.Service;

import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.ClaimPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Util.BukkitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.sql.SQLException;
import java.util.UUID;

public class ReportPermissionService extends Service {
	private static final Logger log = LogManager.getLogger(ReportPermissionService.class);

	protected ReportPermissionService(final ServiceModule module) {
		super(module);
	}

	/**
	 * Checks if the player can alter the given report.
	 *
	 * @param sender The {@link CommandSender} checking if the player can alter the report.
	 * @param index  The index of the report.
	 * @param player The player to check.
	 * @return True if the player can alter the report, otherwise false.
	 */
	public boolean canAlterReport(final CommandSender sender, final int index, final CommandSender player) throws InterruptedException, SQLException, ClassNotFoundException {
		if (player == null) {
			return false;
		}

		try {
			if (!requirePriority(sender, index, player)) {
				return false;
			}

			if (!requireUnclaimedOrPriority(sender, index, player)) {
				sender.sendMessage(ChatColor.WHITE + getLocale().getString(GeneralPhrases.contactToAlter));
				return false;
			}
			return true;
		} catch (final InterruptedException | ClassNotFoundException | SQLException e) {
			log.error(String.format("Failed to check if player [%s] could alter report [%d]!", BukkitUtil.formatPlayerName(player), index));
			throw e;
		}
	}

	/**
	 * Checks if the given {@link CommandSender} can alter the report at the given index.
	 * <br /><br />
	 * Will display a message if the sender cannot alter the report.
	 * <br /><br />
	 * Console and OPs can always alter reports.
	 *
	 * @param sender The {@link CommandSender} wanting to alter a report.
	 * @param index  The index of the report.
	 * @return True if the {@link CommandSender} can alter the given report, otherwise false.
	 */
	public boolean canAlterReport(final CommandSender sender, final int index) throws InterruptedException, SQLException, ClassNotFoundException {
		return canAlterReport(sender, index, sender);
	}

	/**
	 * Checks if report is unclaimed or the player has high enough priority to supersede the player claiming it.
	 * <br /><br />If the player fails the check, the sender will be alerted.
	 *
	 * @param sender The {@link CommandSender}.
	 * @param index  The index of the report.
	 * @param player The player to check.
	 * @return True if the report is unclaimed or the player has high enough priority to supersede the player claiming it.
	 */
	public boolean requireUnclaimedOrPriority(final CommandSender sender, final int index, final CommandSender player) throws ClassNotFoundException, InterruptedException, SQLException {
		if (hasPermissionOverride(sender)) {
			return true;
		}

		final String query = "SELECT " +
				"ClaimStatus, ClaimedByUUID, ClaimedBy, ClaimPriority " +
				"FROM Reports " +
				"WHERE ID=" + index;

		final ExtendedDatabaseHandler database = getDatabase();
		Integer connectionId = null;
		try {
			connectionId = database.openPooledConnection();
			final SQLResultSet result = database.sqlQuery(connectionId, query);

			final boolean isClaimed = result.getBoolean("ClaimStatus");
			final String claimedByName = result.getString("ClaimedBy");
			final int claimPriority = result.getInt("ClaimPriority");
			final String claimedByUUIDString = result.getString("ClaimedByUUID");

			UUID claimedByUUID = null;
			if (!claimedByUUIDString.isEmpty()) {
				claimedByUUID = UUID.fromString(claimedByUUIDString);
			}
			final OfflinePlayer claimedByOfflinePlayer = BukkitUtil.getOfflinePlayer(claimedByUUID, claimedByName);
			final CommandSender claimedBy = CommandSender.class.cast(claimedByOfflinePlayer);

			// Check if the sender is the player claiming the report.
			// UUID based check.
			final boolean isClaimedBySender = BukkitUtil.playersEqual(sender, claimedBy);
			final boolean isClaimedByPlayer = BukkitUtil.playersEqual(player, claimedBy);

			if (isClaimed && !isClaimedBySender && !isClaimedByPlayer && claimPriority >= getModLevel(player).getLevel()) {
				final String formattedClaimName = BukkitUtil.formatPlayerName(claimedBy);
				final String output = getLocale().getString(ClaimPhrases.reportAlreadyClaimed)
						.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.RED)
						.replaceAll("%c", ChatColor.BLUE + formattedClaimName + ChatColor.RED);
				sender.sendMessage(ChatColor.RED + output);

				return false;
			}
		} catch (final ClassNotFoundException | SQLException | InterruptedException e) {
			log.error("Failed to check if report can be altered by player!");
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}

		return true;
	}

	/**
	 * Checks if the given {@link CommandSender} has a high enough priority to alter the report at the given index.
	 *
	 * @param sender The {@link CommandSender}.
	 * @param index  The index of the report.
	 * @return True if the {@link CommandSender} has a high enough priority to alter the report at the given index.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public boolean checkPriority(final CommandSender sender, final int index) throws ClassNotFoundException, InterruptedException, SQLException {
		if (hasPermissionOverride(sender)) {
			return true;
		}

		final ModLevel modLevel = getModLevel(sender);
		try {
			final ModLevel reportPriority = getReportPriority(index);
			return reportPriority.getLevel() <= modLevel.getLevel();
		} catch (final ClassNotFoundException | SQLException | InterruptedException e) {
			log.error(String.format("Failed to do a priority check for player [%s] on report [%d]", BukkitUtil.formatPlayerName(sender), index));
			throw e;
		}
	}

	/**
	 * Checks if the given player has a high enough priority to alter the report.
	 *
	 * @param sender The {@link CommandSender}.
	 * @param index  The index of the report.
	 * @param player The player to check.
	 * @return True if the player has a high enough priority to alter the given report, otherwise false.
	 */
	public boolean requirePriority(final CommandSender sender, final int index, final CommandSender player) throws InterruptedException, SQLException, ClassNotFoundException {
		try {
			if (!checkPriority(player, index)) {
				final ModLevel reportPriority = getReportPriority(index);

				final String output = getLocale().getString(GeneralPhrases.reportRequiresClearance)
						.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.RED)
						.replaceAll("%m", reportPriority.getColor() + reportPriority.getName() + ChatColor.RED);

				sender.sendMessage(ChatColor.RED + output);

				if (BukkitUtil.playersEqual(sender, player)) {
					displayModLevel(sender);
				} else {
					displayModLevel(sender, player);
				}

				return false;
			}
		} catch (final InterruptedException | ClassNotFoundException | SQLException e) {
			log.error(String.format("Failed to check required priority for report [%d]!", index));
			throw e;
		}

		return true;
	}

	private boolean hasPermissionOverride(final CommandSender sender) {
		return sender.isOp() || sender instanceof ConsoleCommandSender;
	}

	private ExtendedDatabaseHandler getDatabase() {
		return getStore().getDatabaseStore().get();
	}

	private ModLevel getModLevel(final CommandSender player) {
		return getModule().getPlayerService().getModLevel(player);
	}

	private void displayModLevel(final CommandSender sender) {
		getModule().getPlayerService().displayModLevel(sender);
	}

	private void displayModLevel(final CommandSender sender, final CommandSender player) {
		getModule().getPlayerService().displayModLevel(sender, player);
	}

	private Locale getLocale() {
		return getStore().getLocaleStore().get();
	}

	private ModLevel getReportPriority(final int index) throws InterruptedException, SQLException, ClassNotFoundException {
		return getModule().getReportInformationService().getReportPriority(index);
	}
}
