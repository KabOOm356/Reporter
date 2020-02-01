package net.KabOOm356.Command.Commands;

import net.KabOOm356.Command.Help.Usage;
import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.ReportPhrases;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Service.SQLStatServices.PlayerStatService;
import net.KabOOm356.Service.SQLStatServices.PlayerStatService.PlayerStat;
import net.KabOOm356.Util.ArrayUtil;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.FormattingUtil;
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
import java.util.*;

/**
 * A {@link ReporterCommand} that will handle users submitting reports.
 */
public class ReportCommand extends ReporterCommand {
	private static final Logger log = LogManager.getLogger(ReportCommand.class);

	private static final String name = "Report";
	private static final int minimumNumberOfArguments = 2;
	private final static String permissionNode = "reporter.report";

	private static final List<Usage> usages = Collections.unmodifiableList(ArrayUtil.arrayToArrayList(new Usage[]{new Usage(ReportPhrases.reportHelp, ReportPhrases.reportHelpDetails)}));
	private static final List<String> aliases = Collections.emptyList();

	/**
	 * Constructor.
	 *
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public ReportCommand(final ReporterCommandManager manager) {
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
	public void execute(final CommandSender sender, final ArrayList<String> args) {
		if (!hasRequiredPermission(sender)) {
			return;
		}

		final OfflinePlayer reported = getManager().getPlayer(args.get(0));

		if (!playerExists(sender, reported)) {
			return;
		}

		if (!canReport(sender)) {
			return;
		}

		if (!canReport(sender, reported)) {
			return;
		}

		try {
			reportCommand(sender, reported, getDetails(args));
		} catch (final Exception e) {
			log.error("Failed to report!", e);
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

	private boolean playerExists(final CommandSender sender, final OfflinePlayer player) {
		if (player == null) {
			sender.sendMessage(ChatColor.RED + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(GeneralPhrases.playerDoesNotExist)));
			return false;
		}

		return true;
	}

	private void reportCommand(final CommandSender sender, final OfflinePlayer reported, final String details) throws ClassNotFoundException, SQLException, InterruptedException {
		final ArrayList<String> params = new ArrayList<>();
		final int count = getServiceModule().getReportCountService().getCount();
		Location reportedLoc = null;

		if (count != -1) {
			params.add(0, Integer.toString(count + 1));

			params.add(1, BukkitUtil.getUUIDString(sender));
			params.add(2, sender.getName());

			params.add(3, BukkitUtil.getUUIDString(reported));

			if (reported.isOnline()) {
				final Player reportedPlayer = reported.getPlayer();

				reportedLoc = reportedPlayer.getLocation();
			}

			params.add(4, reported.getName());

			params.add(5, details);
			params.add(6, Reporter.getDateformat().format(new Date()));

			if (BukkitUtil.isPlayer(sender)) {
				final Player player = (Player) sender;

				params.add(7, player.getLocation().getWorld().getName());
				params.add(8, Double.toString(player.getLocation().getX()));
				params.add(9, Double.toString(player.getLocation().getY()));
				params.add(10, Double.toString(player.getLocation().getZ()));
			} else {
				params.add(7, "");
				params.add(8, "0.0");
				params.add(9, "0.0");
				params.add(10, "0.0");
			}

			if (reportedLoc != null) {
				params.add(11, reportedLoc.getWorld().getName());
				params.add(12, Double.toString(reportedLoc.getX()));
				params.add(13, Double.toString(reportedLoc.getY()));
				params.add(14, Double.toString(reportedLoc.getZ()));
			} else {
				params.add(11, "");
				params.add(12, "0.0");
				params.add(13, "0.0");
				params.add(14, "0.0");
			}

			params.add(15, "0");
			params.add(16, "0");

			final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
			final int connectionId = database.openPooledConnection();
			try {
				final String query =
						"INSERT INTO Reports " +
								"(ID, SenderUUID, Sender, ReportedUUID, Reported, Details, Date, SenderWorld, SenderX, SenderY, SenderZ, ReportedWorld, ReportedX, ReportedY, ReportedZ, CompletionStatus, ClaimStatus) " +
								"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				database.preparedUpdateQuery(connectionId, query, params);
			} catch (final SQLException e) {
				log.log(Level.ERROR, String.format("Failed to execute report query on connection [%d]!", connectionId));
				throw e;
			} finally {
				database.closeConnection(connectionId);
			}

			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
					ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ReportPhrases.playerReport)));

			broadcastSubmittedMessage(getServiceModule().getReportCountService().getCount());

			getServiceModule().getReportLimitService().hasReported(sender, reported);

			final PlayerStatService stats = getServiceModule().getPlayerStatsService();

			final String date = Reporter.getDateformat().format(new Date());

			if (BukkitUtil.isPlayer(sender)) {
				final Player senderPlayer = (Player) sender;

				stats.incrementStat(senderPlayer, PlayerStat.REPORTCOUNT);
				stats.setStat(senderPlayer, PlayerStat.LASTREPORTDATE, date);

				final ResultRow result = stats.getStat(senderPlayer, PlayerStat.FIRSTREPORTDATE);

				final String firstReportDate = result.getString(PlayerStat.FIRSTREPORTDATE.getColumnName());

				if (firstReportDate.isEmpty()) {
					stats.setStat(senderPlayer, PlayerStat.FIRSTREPORTDATE, date);
				}
			}

			stats.incrementStat(reported, PlayerStat.REPORTED);
			stats.setStat(reported, PlayerStat.LASTREPORTEDDATE, date);

			final ResultRow result = stats.getStat(reported, PlayerStat.FIRSTREPORTEDDATE);

			final String firstReportedDate = result.getString(PlayerStat.FIRSTREPORTEDDATE.getColumnName());

			if (firstReportedDate.isEmpty()) {
				stats.setStat(reported, PlayerStat.FIRSTREPORTEDDATE, date);
			}

			// Alert the player when they reach their reporting limit
			canReport(sender);
			canReport(sender, reported);
		} else {
			sender.sendMessage(getErrorMessage());
		}
	}

	private boolean canReport(final CommandSender sender, final OfflinePlayer reported) {
		if (!getServiceModule().getReportLimitService().canReport(sender, reported)) {
			String output = getManager().getLocale().getString(ReportPhrases.reachedReportingLimitAgaintPlayer);

			final String reportedNameFormatted = BukkitUtil.formatPlayerName(reported);

			output = output.replaceAll("%r", ChatColor.BLUE + reportedNameFormatted + ChatColor.WHITE);

			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
					ChatColor.WHITE + output);

			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
					ChatColor.WHITE + getTimeRemaining(sender, reported));

			return false;
		}
		return true;
	}

	private boolean canReport(final CommandSender sender) {
		if (!getServiceModule().getReportLimitService().canReport(sender)) {
			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
					ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ReportPhrases.reachedReportingLimit)));

			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
					ChatColor.WHITE + getTimeRemaining(sender));

			return false;
		}
		return true;
	}

	private String getTimeRemaining(final CommandSender sender, final OfflinePlayer reported) {
		String timeRemaining = getManager().getLocale().getString(ReportPhrases.remainingTimeToReportPlayer);

		final String reportedNameFormatted = BukkitUtil.formatPlayerName(reported);

		timeRemaining = timeRemaining.replaceAll("%r", ChatColor.BLUE + reportedNameFormatted + ChatColor.WHITE);

		final int seconds = getServiceModule().getReportLimitService().getRemainingTime(sender, reported);

		return FormattingUtil.formatTimeRemaining(timeRemaining, seconds);
	}

	private String getTimeRemaining(final CommandSender sender) {
		final String timeRemaining = BukkitUtil.colorCodeReplaceAll(
				getManager().getLocale().getString(ReportPhrases.remainingTimeForReport));

		final int seconds = getServiceModule().getReportLimitService().getRemainingTime(sender);

		return FormattingUtil.formatTimeRemaining(timeRemaining, seconds);
	}

	private String getDetails(final ArrayList<String> args) {
		final StringBuilder details = new StringBuilder();
		for (int LCV = 1; LCV < args.size(); LCV++) {
			details.append(args.get(LCV)).append(' ');
		}
		return details.toString().trim();
	}

	private void broadcastSubmittedMessage(final int index) {
		final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

		String reportSubmitted = BukkitUtil.colorCodeReplaceAll(
				getManager().getLocale().getString(ReportPhrases.broadcastSubmitted));

		reportSubmitted = reportSubmitted.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);

		for (final Player player : onlinePlayers) {
			if (hasPermission(player, "reporter.list")) {
				player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + reportSubmitted);
			}
		}
	}
}
