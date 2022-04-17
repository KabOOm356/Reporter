package net.KabOOm356.Command.Commands;

import net.KabOOm356.Command.Help.Usage;
import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.RespondPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.ViewPhrases;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Service.SQLStatServices.ModeratorStatService.ModeratorStat;
import net.KabOOm356.Throwable.IndexNotANumberException;
import net.KabOOm356.Throwable.IndexOutOfRangeException;
import net.KabOOm356.Throwable.NoLastViewedReportException;
import net.KabOOm356.Throwable.RequiredPermissionException;
import net.KabOOm356.Util.ArrayUtil;
import net.KabOOm356.Util.BukkitUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A {@link ReporterCommand} that will handle users responding to reports.
 */
public class RespondCommand extends ReporterCommand {
	private static final Logger log = LogManager.getLogger(RespondCommand.class);

	private static final String name = "Respond";
	private static final int minimumNumberOfArguments = 1;
	private final static String permissionNode = "reporter.respond";

	private static final List<Usage> usages = Collections.unmodifiableList(ArrayUtil.arrayToList(new Usage[]{new Usage(RespondPhrases.respondHelp, RespondPhrases.respondHelpDetails)}));
	private static final List<String> aliases = Collections.emptyList();

	/**
	 * Constructor.
	 *
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public RespondCommand(final ReporterCommandManager manager) {
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
	public void execute(final CommandSender sender, final List<String> args) throws NoLastViewedReportException, IndexOutOfRangeException, IndexNotANumberException, RequiredPermissionException {
		hasRequiredPermission(sender);

		try {
			// Cast the sender to type Player or tell the sender they must be a player
			Player player;
			if (BukkitUtil.isPlayer(sender)) {
				player = (Player) sender;
			} else {
				sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.RED + "You must be a player to use this command!");
				return;
			}

			final int index = getServiceModule().getLastViewedReportService().getIndexOrLastViewedReport(sender, args.get(0));

			getServiceModule().getReportValidatorService().requireReportIndexValid(index);

			if (args.size() == 1) {
				teleportToReport(player, index, "reported");
			} else if (args.size() >= 2) {
				teleportToReport(player, index, args.get(1));
			}
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to respond to report!", e);
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

	private void teleportToReport(final Player player, final int index, final String playerLoc) throws ClassNotFoundException, SQLException, InterruptedException {
		if (!playerLoc.equalsIgnoreCase("sender") && !playerLoc.equalsIgnoreCase("reported")) {
			player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.RED + BukkitUtil.colorCodeReplaceAll(getUsage()));
		} else {
			final boolean requestedToReported = playerLoc.equalsIgnoreCase("reported");
			boolean sendToReported = requestedToReported;

			int id;
			double X = 0.0, Y = 0.0, Z = 0.0;
			String World = null, reported, sender, details;

			final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
			final int connectionId = database.openPooledConnection();
			try {
				final String query = "SELECT ID, ReportedUUID, Reported, SenderUUID, Sender, Details, SenderX, SenderY, SenderZ, SenderWorld, ReportedX, ReportedY, ReportedZ, ReportedWorld " +
						"FROM Reports " +
						"WHERE ID=" + index;

				final SQLResultSet result = database.sqlQuery(connectionId, query);

				for (int LCV = 0; LCV < 2; LCV++) {
					if (sendToReported) {
						X = result.getDouble("ReportedX");
						Y = result.getDouble("ReportedY");
						Z = result.getDouble("ReportedZ");
						World = result.getString("ReportedWorld");
					} else {
						X = result.getDouble("SenderX");
						Y = result.getDouble("SenderY");
						Z = result.getDouble("SenderZ");
						World = result.getString("SenderWorld");
					}

					if (X == 0.0 && Y == 0.0 && Z == 0.0 || World == null || World.isEmpty()) {
						sendToReported = !sendToReported;
					} else {
						break;
					}
				}

				if (X == 0.0 && Y == 0.0 && Z == 0.0 || World == null || World.isEmpty()) {
					player.sendMessage(ChatColor.RED + getManager().getLocale().getString(RespondPhrases.bothPlayerLocNF));

					player.sendMessage(ChatColor.RED + getManager().getLocale().getString(RespondPhrases.teleAbort));
					return;
				}

				id = result.getInt("ID");

				if (!result.getString("ReportedUUID").isEmpty()) {
					final UUID uuid = UUID.fromString(result.getString("ReportedUUID"));

					final OfflinePlayer reportedPlayer = Bukkit.getOfflinePlayer(uuid);

					reported = BukkitUtil.formatPlayerName(reportedPlayer);
				} else {
					reported = result.getString("Reported");
				}

				if (!result.getString("SenderUUID").isEmpty()) {
					final UUID uuid = UUID.fromString(result.getString("SenderUUID"));

					final OfflinePlayer senderPlayer = Bukkit.getOfflinePlayer(uuid);

					sender = BukkitUtil.formatPlayerName(senderPlayer);
				} else {
					sender = result.getString("Sender");
				}

				details = result.getString("Details");
			} catch (final SQLException e) {
				log.log(Level.ERROR, String.format("Failed to respond to report on connection [%s]!", connectionId));
				throw e;
			} finally {
				database.closeConnection(connectionId);
			}

			if (requestedToReported) {
				if (sendToReported) {
					player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
							ChatColor.WHITE + getManager().getLocale().getString(RespondPhrases.telReported));
				} else {
					player.sendMessage(ChatColor.RED + getManager().getLocale().getString(RespondPhrases.reportedPlayerLocNF));

					player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
							ChatColor.WHITE + getManager().getLocale().getString(RespondPhrases.telSender));
				}
			} else {
				if (sendToReported) {
					player.sendMessage(ChatColor.RED + getManager().getLocale().getString(RespondPhrases.senderLocNF));

					player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
							ChatColor.WHITE + getManager().getLocale().getString(RespondPhrases.telReported));
				} else {
					player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
							ChatColor.WHITE + getManager().getLocale().getString(RespondPhrases.telSender));
				}
			}

			String out = getManager().getLocale().getString(RespondPhrases.respondTeleportLocation);

			out = out.replaceAll("%world", ChatColor.GOLD + World + ChatColor.WHITE);
			out = out.replaceAll("%x", ChatColor.GOLD + Double.toString(Math.round(X)) + ChatColor.WHITE);
			out = out.replaceAll("%y", ChatColor.GOLD + Double.toString(Math.round(Y)) + ChatColor.WHITE);
			out = out.replaceAll("%z", ChatColor.GOLD + Double.toString(Math.round(Z)) + ChatColor.WHITE);

			player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + out);

			String reportInfo = getManager().getLocale().getString(ViewPhrases.viewAllReportHeader);

			String reportInfoDetails = getManager().getLocale().getString(ViewPhrases.viewAllReportDetails);

			reportInfo = reportInfo.replaceAll("%i", ChatColor.GOLD + Integer.toString(id) + ChatColor.WHITE);
			reportInfo = reportInfo.replaceAll("%r", ChatColor.GOLD + reported + ChatColor.WHITE);
			reportInfo = reportInfo.replaceAll("%s", ChatColor.GOLD + sender + ChatColor.WHITE);

			reportInfoDetails = reportInfoDetails.replaceAll("%d", ChatColor.GOLD + details);

			player.sendMessage(ChatColor.WHITE + reportInfo);

			player.sendMessage(ChatColor.WHITE + reportInfoDetails);

			final Location loc = new Location(Bukkit.getWorld(World), X, Y, Z);

			player.teleport(loc);

			getServiceModule().getModStatsService().incrementStat(player, ModeratorStat.RESPONDED);
		}
	}
}
