package net.KabOOm356.Service;

import net.KabOOm356.Locale.Entry.LocalePhrases.ReportPhrases;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Reporter.Configuration.Entry.ConfigurationEntries;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Runnable.Timer.ReportTimer;
import net.KabOOm356.Service.Store.type.PlayerReport;
import net.KabOOm356.Service.Store.type.PlayerReportQueue;
import net.KabOOm356.Util.BukkitUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Calendar;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * A class to manage reporting limits.
 */
public class ReportLimitService extends Service {
	private static final Logger log = LogManager.getLogger(ReportLimitService.class);

	/**
	 * An instance of the main class.
	 */
	private final Plugin plugin;

	/**
	 * Constructor
	 */
	protected ReportLimitService(final ServiceModule module) {
		super(module);

		this.plugin = BukkitUtil.getPlugin(Reporter.class.getSimpleName());
	}

	/**
	 * Checks if the given {@link CommandSender} can submit a report.
	 * <br /><br />
	 * <b>NOTE:</b> If the {@link CommandSender} cannot be converted to a player then true is returned.
	 *
	 * @param sender The {@link CommandSender}.
	 * @return True if the {@link CommandSender} can submit a report, otherwise false.
	 */
	public boolean canReport(final CommandSender sender) {
		if (getConfigurationService().get(ConfigurationEntries.limitReports)) {
			int numberOfReports = 0;
			final PlayerReportQueue playerReportQueue = getPlayerReports().get(sender);
			if (playerReportQueue != null) {
				for (final PriorityQueue<ReportTimer> queue : playerReportQueue.values()) {
					numberOfReports += queue.size();
				}
				final int limit = getConfigurationService().get(ConfigurationEntries.reportLimit);
				return canReport(sender, limit, numberOfReports);
			}
		}
		return true;
	}

	/**
	 * Checks if the given {@link CommandSender} can submit a report against the given {@link OfflinePlayer}.
	 * <br /><br />
	 * <b>NOTE:</b> If the {@link CommandSender} cannot be converted to a player then true is returned.
	 *
	 * @param sender   The {@link CommandSender}.
	 * @param reported The {@link OfflinePlayer}.
	 * @return True if the {@link CommandSender} can submit a report against
	 * the given {@link OfflinePlayer}, otherwise false.
	 */
	public boolean canReport(final CommandSender sender, final OfflinePlayer reported) {
		if (getConfigurationService().get(ConfigurationEntries.limitReportsAgainstPlayers)) {
			final PlayerReportQueue playerReportQueue = getPlayerReports().get(sender);
			if (playerReportQueue != null) {
				final PriorityQueue<ReportTimer> queue = playerReportQueue.get(reported);
				if (queue != null) {
					final int numberOfReports = playerReportQueue.get(reported).size();
					final int limit = getConfigurationService().get(ConfigurationEntries.reportLimitAgainstPlayers);
					return canReport(sender, limit, numberOfReports);
				}
			}
		}
		return true;
	}

	/**
	 * Called when the given {@link CommandSender} has submitted a report.
	 *
	 * @param sender         The {@link CommandSender} that has reported.
	 * @param reportedPlayer The {@link OfflinePlayer} the report was against.
	 */
	public void hasReported(final CommandSender sender, final OfflinePlayer reportedPlayer) {
		final boolean isPlayer = BukkitUtil.isPlayer(sender);
		final boolean canReport = canReport(sender);
		final boolean canReportPlayer = canReport(sender, reportedPlayer);

		if (isPlayer && canReport && canReportPlayer) {
			final Player player = Player.class.cast(sender);
			final boolean noLimit = hasPermission(player, "reporter.report.nolimit");

			if (!noLimit) {
				final ReportTimer timer = new ReportTimer();

				final Calendar executionTime = Calendar.getInstance();
				executionTime.add(Calendar.SECOND, getConfigurationService().get(ConfigurationEntries.limitTime));

				timer.init(this, player, reportedPlayer, executionTime.getTimeInMillis());

				// Convert from seconds to bukkit ticks
				final long bukkitTicks = BukkitUtil.convertSecondsToServerTicks(getConfigurationService().get(ConfigurationEntries.limitTime));

				Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, timer, bukkitTicks);

				getPlayerReports().put(sender, reportedPlayer, timer);

				// Alert the console if the player has reached their limit in total number of reports.
				if (getConfigurationService().get(ConfigurationEntries.alertConsoleWhenLimitReached) && !canReport(sender)) {
					final String output = "%p has reached their reporting limit!"
							.replaceAll("%p", player.getName());
					log.log(Level.INFO, Reporter.getLogPrefix() + output);
				}

				// Alert the console if the player has reached their limit for reporting another player.
				if (getConfigurationService().get(ConfigurationEntries.alertConsoleWhenLimitAgainstPlayerReached) && !canReport(sender, reportedPlayer)) {
					final String output = "%p has reached their reporting limit for reporting %r!"
							.replaceAll("%p", player.getName())
							.replaceAll("%r", BukkitUtil.formatPlayerName(reportedPlayer));
					log.log(Level.INFO, Reporter.getLogPrefix() + output);
				}
			}
		}
	}

	/**
	 * Returns the remaining time before the sender can report the given player again (in Seconds).
	 *
	 * @param sender   The sender to get the remaining time for.
	 * @param reported The reported player to get the remaining time for.
	 * @return The seconds remaining before the sender can report the given player again, if the sender has reported the player.  Otherwise, null.
	 */
	public int getRemainingTime(final CommandSender sender, final OfflinePlayer reported) {
		final PlayerReportQueue playerReports = getPlayerReports().get(sender);
		if (playerReports != null) {
			final Queue<ReportTimer> timers = playerReports.get(reported);
			if (timers != null && timers.peek() != null) {
				return timers.peek().getTimeRemaining();
			}
		}
		return 0;
	}

	/**
	 * Returns the remaining time before the sender can report again (in Seconds).
	 *
	 * @param sender The sender to get the remaining time for.
	 * @return The seconds remaining before the sender can report again, if the sender has reported.  Otherwise, zero (0).
	 */
	public int getRemainingTime(final CommandSender sender) {
		final PlayerReportQueue entry = getPlayerReports().get(sender);
		Integer time = null;
		if (entry != null) {
			// Find the timer that will expire next.
			for (final PriorityQueue<ReportTimer> timers : entry.values()) {
				final ReportTimer timer = timers.peek();
				if (timer != null) {
					if (time == null || timer.getTimeRemaining() < time) {
						time = timer.getTimeRemaining();
					}
				}
			}
		}

		return (time != null) ? time : 0;
	}

	/**
	 * Called when a {@link ReportTimer} is run, meaning that the time limit for that report has expired.
	 *
	 * @param expired The expiring {@link ReportTimer}.
	 */
	public void limitExpired(final ReportTimer expired) {
		final Player player = expired.getPlayer();
		final OfflinePlayer reported = expired.getReported();

		if (!canReport(player)) {
			// Alert player they can report again
			if (getConfigurationService().get(ConfigurationEntries.alertPlayerWhenAllowedToReportAgain)) {
				player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE +
						getLocale().getString(ReportPhrases.allowedToReportAgain));
			}

			// Alert console if configured to
			if (getConfigurationService().get(ConfigurationEntries.alertConsoleWhenAllowedToReportAgain)) {
				log.log(Level.INFO, Reporter.getLogPrefix() +
						player.getName() + " is now allowed to report again!");
			}
		}

		if (getConfigurationService().get(ConfigurationEntries.limitReportsAgainstPlayers) && !canReport(player, reported)) {
			if (getConfigurationService().get(ConfigurationEntries.alertPlayerWhenAllowedToReportPlayerAgain)) {
				final String output = getLocale().getString(ReportPhrases.allowedToReportPlayerAgain)
						.replaceAll("%r", ChatColor.BLUE + BukkitUtil.formatPlayerName(reported) + ChatColor.WHITE);
				player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);
			}

			if (getConfigurationService().get(ConfigurationEntries.alertConsoleWhenAllowedToReportPlayerAgain)) {
				log.log(Level.INFO, Reporter.getLogPrefix() +
						String.format("%s is now allowed to report %s again!",
								BukkitUtil.formatPlayerName(player),
								BukkitUtil.formatPlayerName(reported)));
			}
		}

		// Remove the expired report from the player reports queue.
		getPlayerReports().remove(player, reported, expired);
	}

	private boolean hasReported(final CommandSender sender) {
		return !getPlayerReports().get(sender).isEmpty();
	}

	private boolean isPlayerAndHasReported(final CommandSender sender) {
		return BukkitUtil.isPlayer(sender) && hasReported(sender);
	}

	private boolean canReport(final CommandSender sender, final int limit, final int numberOfReports) {
		if (isPlayerAndHasReported(sender)) {
			final Player player = Player.class.cast(sender);
			if (!hasLimitOverride(player)) {
				return numberOfReports < limit;
			}
		}
		return true;
	}

	private boolean hasLimitOverride(final Player player) {
		return hasPermission(player, "reporter.report.nolimit");
	}

	private ConfigurationService getConfigurationService() {
		return getModule().getConfigurationService();
	}

	private Locale getLocale() {
		return getStore().getLocaleStore().get();
	}

	private boolean hasPermission(final Player player, final String permission) {
		return getModule().getPermissionService().hasPermission(player, permission);
	}

	private PlayerReport getPlayerReports() {
		return getStore().getPlayerReportStore().get();
	}
}
