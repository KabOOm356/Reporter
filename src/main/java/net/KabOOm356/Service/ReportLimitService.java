package net.KabOOm356.Service;

import net.KabOOm356.Locale.Entry.LocalePhrases.ReportPhrases;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Runnable.Timer.ReportTimer;
import net.KabOOm356.Util.BukkitUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Calendar;
import java.util.HashMap;
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
	 * A {@link HashMap} that holds all the players who have reported.
	 * The key has to be a string because if the player logs off then logs
	 * in again the Player object has a different hash.
	 */
	private final HashMap<String, HashMap<String, PriorityQueue<ReportTimer>>> playerReports;
	/**
	 * If the configuration is set to limit the number of reports.
	 */
	private boolean limitReports = true;
	/**
	 * The number of reports allowed within a certain amount of time.
	 */
	private int reportLimit = 5;
	/**
	 * The time that it takes before the player can report again.
	 */
	private int limitTime = 600;
	/**
	 * If the configuration is set to limit the number
	 * of reports against another player.
	 */
	private boolean limitReportsAgainstPlayers = false;

	/**
	 * The number of reports allowed against another player.
	 */
	private int reportLimitAgainstPlayers = 2;
	/**
	 * If the configuration is set to alert the
	 * console when a player reaches their reporting limit.
	 */
	private boolean alertConsoleWhenLimitReached = true;

	/**
	 * If the configuration is set to alert the
	 * console when a player is allowed to report again
	 * after reaching their limit.
	 */
	private boolean alertConsoleWhenAllowedToReportAgain = true;

	/**
	 * If the configuration is set to alert the
	 * console when a player reaches their reporting limit
	 * against another player.
	 */
	private boolean alertConsoleWhenLimitAgainstPlayerReached = true;

	/**
	 * If the configuration is set to alert the
	 * console when a player is allowed to report another
	 * player again after reaching their limit.
	 */
	private boolean alertConsoleWhenAllowedToReportPlayerAgain = true;

	/**
	 * If the configuration is set to alert the
	 * player when they are allowed to report again.
	 */
	private boolean alertPlayerWhenAllowedToReportAgain = true;

	/**
	 * If the configuration is set to alert the
	 * player when they are allowed to report
	 * another player again.
	 */
	private boolean alertPlayerWhenAllowedToReportPlayerAgain = true;

	/**
	 * Constructor
	 */
	protected ReportLimitService(final ServiceModule module) {
		super(module);

		this.plugin = BukkitUtil.getPlugin(Reporter.class.getSimpleName());

		this.limitReports = getConfig().getBoolean(
				"general.reporting.limitNumberOfReports",
				limitReports);

		this.limitReportsAgainstPlayers = getConfig().getBoolean(
				"general.reporting.limitReportsAgainstPlayers",
				limitReportsAgainstPlayers);

		this.reportLimit = getConfig().getInt(
				"general.reporting.limitNumber",
				reportLimit);

		this.reportLimitAgainstPlayers = getConfig().getInt(
				"general.reporting.limitNumberAgainstPlayers",
				reportLimitAgainstPlayers);

		this.limitTime = getConfig().getInt(
				"general.reporting.limitTime",
				limitTime);

		this.alertConsoleWhenLimitReached = getConfig().getBoolean(
				"general.reporting.alerts.toConsole.limitReached",
				alertConsoleWhenLimitReached);

		this.alertConsoleWhenLimitAgainstPlayerReached = getConfig().getBoolean(
				"general.reporting.alerts.toConsole.limitAgainstPlayerReached",
				alertConsoleWhenLimitAgainstPlayerReached);

		this.alertConsoleWhenAllowedToReportAgain = getConfig().getBoolean(
				"general.reporting.alerts.toConsole.allowedToReportAgain",
				alertConsoleWhenAllowedToReportAgain);

		this.alertConsoleWhenAllowedToReportPlayerAgain = getConfig().getBoolean(
				"general.reporting.alerts.toConsole.allowedToReportPlayerAgain",
				alertConsoleWhenAllowedToReportPlayerAgain);

		this.alertPlayerWhenAllowedToReportAgain = getConfig().getBoolean(
				"general.reporting.alerts.toPlayer.allowedToReportAgain",
				alertPlayerWhenAllowedToReportAgain);

		this.alertPlayerWhenAllowedToReportPlayerAgain = getConfig().getBoolean(
				"general.reporting.alerts.toPlayer.allowedToReportPlayerAgain",
				alertPlayerWhenAllowedToReportPlayerAgain);

		this.playerReports = new HashMap<String, HashMap<String, PriorityQueue<ReportTimer>>>();
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
		final boolean isPlayer = BukkitUtil.isPlayer(sender);

		if (isPlayer) {
			final Player player = (Player) sender;

			final boolean hasReported = !this.getReportedPlayers(sender).isEmpty();

			if (limitReports && hasReported) {
				final boolean override = hasPermission(player, "reporter.report.nolimit");

				if (override) {
					return true;
				}

				final int numberOfReports = this.getAllReportTimers(sender).size();

				return numberOfReports < reportLimit;
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
		final boolean isPlayer = BukkitUtil.isPlayer(sender);

		if (isPlayer) {
			final Player player = (Player) sender;

			final boolean hasReported = !this.getReportedPlayers(sender).isEmpty();

			if (limitReportsAgainstPlayers && hasReported) {
				final boolean override = hasPermission(player, "reporter.report.nolimit");

				if (override) {
					return true;
				}

				final Queue<ReportTimer> timers = this.getAllReportTimers(sender, reported);

				return timers.size() < reportLimitAgainstPlayers;
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
		final boolean canReport = limitReports && canReport(sender);
		final boolean canReportPlayer = limitReportsAgainstPlayers && canReport(sender, reportedPlayer);

		if (isPlayer && (canReport || canReportPlayer)) {
			final Player player = (Player) sender;
			final boolean noLimit = hasPermission(player, "reporter.report.nolimit");

			if (!noLimit) {
				final ReportTimer timer = new ReportTimer();

				final Calendar executionTime = Calendar.getInstance();
				executionTime.add(Calendar.SECOND, limitTime);

				timer.init(this, player, reportedPlayer, executionTime.getTimeInMillis());

				// Convert from seconds to bukkit ticks
				final long bukkitTicks = limitTime * 20;

				Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, timer, bukkitTicks);

				addReportToPlayer(sender, timer);

				boolean alert = limitReports && !canReport(sender);

				// Alert the console if the player has reached their limit in total number of reports.
				if (alertConsoleWhenLimitReached && alert) {
					String output = "%p has reached their reporting limit!";

					output = output.replaceAll("%p", player.getName());

					log.log(Level.INFO, Reporter.getLogPrefix() + output);
				}

				alert = limitReportsAgainstPlayers && !canReport(sender, reportedPlayer);

				// Alert the console if the player has reached their limit for reporting another player.
				if (alertConsoleWhenLimitAgainstPlayerReached && alert) {
					String output = "%p has reached their reporting limit for reporting %r!";

					output = output.replaceAll("%p", player.getName());
					output = output.replaceAll("%r", BukkitUtil.formatPlayerName(reportedPlayer));

					log.log(Level.INFO, Reporter.getLogPrefix() + output);
				}
			}
		}
	}

	/**
	 * Stores the given {@link ReportTimer} for the {@link CommandSender}.
	 *
	 * @param sender The {@link CommandSender}.
	 * @param timer  The {@link ReportTimer}.
	 */
	private void addReportToPlayer(final CommandSender sender, final ReportTimer timer) {
		HashMap<String, PriorityQueue<ReportTimer>> entry;

		if (BukkitUtil.isPlayer(sender)) {
			final Player player = (Player) sender;

			if (!playerReports.containsKey(player.getUniqueId().toString())) {
				entry = new HashMap<String, PriorityQueue<ReportTimer>>();

				playerReports.put(player.getUniqueId().toString(), entry);
			}

			entry = playerReports.get(player.getUniqueId().toString());
		} else {
			if (!playerReports.containsKey(sender.getName())) {
				entry = new HashMap<String, PriorityQueue<ReportTimer>>();

				playerReports.put(sender.getName(), entry);
			}

			entry = playerReports.get(sender.getName());
		}

		boolean containsReported = entry.containsKey(timer.getReported().getName());

		if (BukkitUtil.isPlayerValid(timer.getReported())) {
			containsReported = containsReported || entry.containsKey(timer.getReported().getUniqueId().toString());
		}

		if (!containsReported) {
			final PriorityQueue<ReportTimer> queue = new PriorityQueue<ReportTimer>(
					reportLimitAgainstPlayers,
					ReportTimer.compareByTimeRemaining);

			if (BukkitUtil.isPlayerValid(timer.getReported())) {
				entry.put(timer.getReported().getUniqueId().toString(), queue);
			} else {
				entry.put(timer.getReported().getName(), queue);
			}
		}

		if (BukkitUtil.isPlayerValid(timer.getReported())) {
			entry.get(timer.getReported().getUniqueId().toString()).add(timer);
		} else {
			entry.get(timer.getReported().getName()).add(timer);
		}
	}

	/**
	 * Returns the remaining time before the sender can report the given player again (in Seconds).
	 *
	 * @param sender   The sender to get the remaining time for.
	 * @param reported The reported player to get the remaining time for.
	 * @return The seconds remaining before the sender can report the given player again.
	 */
	public int getRemainingTime(final CommandSender sender, final OfflinePlayer reported) {
		final Queue<ReportTimer> timers = this.getAllReportTimers(sender, reported);

		if (timers == null || timers.peek() == null) {
			return Integer.MAX_VALUE;
		}

		return timers.peek().getTimeRemaining();
	}

	/**
	 * Returns the remaining time before the sender can report the given player again (in Seconds).
	 *
	 * @param sender The sender to get the remaining time for.
	 * @param key    The key to the reported player to get the remaining time for.
	 * @return The seconds remaining before the sender can report the given player again.
	 */
	private int getRemainingTime(final CommandSender sender, final String key) {
		final HashMap<String, PriorityQueue<ReportTimer>> reportedPlayers = getReportedPlayers(sender);

		final Queue<ReportTimer> timers = reportedPlayers.get(key);

		if (timers == null || timers.peek() == null) {
			return Integer.MAX_VALUE;
		}

		return timers.peek().getTimeRemaining();
	}

	/**
	 * Returns the remaining time before the sender can report again (in Seconds).
	 *
	 * @param sender The sender to get the remaining time for.
	 * @return The seconds remaining before the sender can report again.
	 */
	public int getRemainingTime(final CommandSender sender) {
		final HashMap<String, PriorityQueue<ReportTimer>> entry = getReportedPlayers(sender);

		int time = Integer.MAX_VALUE;

		// Find the timer that will expire next.
		for (final String key : entry.keySet()) {
			final int current = getRemainingTime(sender, key);

			if (current < time) {
				time = current;
			}
		}

		return time;
	}

	/**
	 * Called when a {@link ReportTimer} is run, meaning that the time limit for that report has expired.
	 *
	 * @param expired The expiring {@link ReportTimer}.
	 */
	public void limitExpired(final ReportTimer expired) {
		if (!canReport(expired.getPlayer())) {
			// Alert player they can report again
			if (alertPlayerWhenAllowedToReportAgain) {
				expired.getPlayer().sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE +
						getLocale().getString(ReportPhrases.allowedToReportAgain));
			}

			// Alert console if configured to
			if (alertConsoleWhenAllowedToReportAgain) {
				log.log(Level.INFO, Reporter.getLogPrefix() +
						expired.getPlayer().getName() + " is now allowed to report again!");
			}
		}

		if (limitReportsAgainstPlayers && !canReport(expired.getPlayer(), expired.getReported())) {
			String output;

			if (this.alertPlayerWhenAllowedToReportPlayerAgain) {
				output = getLocale().getString(ReportPhrases.allowedToReportPlayerAgain);
				final String reportedNameFormatted = BukkitUtil.formatPlayerName(expired.getReported());

				output = output.replaceAll("%r", ChatColor.BLUE + reportedNameFormatted + ChatColor.WHITE);
				expired.getPlayer().sendMessage(
						ChatColor.BLUE + Reporter.getLogPrefix() +
								ChatColor.WHITE + output);
			}

			if (alertConsoleWhenAllowedToReportPlayerAgain) {
				output = "%p is now allowed to report %r again!";

				output = output.replaceAll("%p", BukkitUtil.formatPlayerName(expired.getPlayer()));
				output = output.replaceAll("%r", BukkitUtil.formatPlayerName(expired.getReported()));

				log.log(Level.INFO, Reporter.getLogPrefix() + output);
			}
		}

		final Player player = expired.getPlayer();
		final OfflinePlayer reported = expired.getReported();

		// Remove the expired report from the player reports queue.
		final HashMap<String, PriorityQueue<ReportTimer>> reportedPlayers = getReportedPlayers(player);

		if (BukkitUtil.isPlayerValid(reported)) {
			reportedPlayers.get(reported.getUniqueId().toString()).remove(expired);
		} else {
			reportedPlayers.get(reported.getName()).remove(expired);
		}
	}

	/**
	 * Returns all the players the given {@link CommandSender} has reported.
	 *
	 * @param sender The {@link CommandSender}.
	 * @return A HashMap containing all the players the {@link CommandSender} has reported.
	 */
	private HashMap<String, PriorityQueue<ReportTimer>> getReportedPlayers(final CommandSender sender) {
		final HashMap<String, PriorityQueue<ReportTimer>> reportedPlayers =
				new HashMap<String, PriorityQueue<ReportTimer>>();

		if (playerReports.get(sender.getName()) != null) {
			reportedPlayers.putAll(playerReports.get(sender.getName()));
		}

		if (BukkitUtil.isPlayer(sender)) {
			final Player player = (Player) sender;

			if (playerReports.get(player.getUniqueId().toString()) != null) {
				reportedPlayers.putAll(playerReports.get(player.getUniqueId().toString()));
			}
		}

		return reportedPlayers;
	}

	/**
	 * Gets all {@link ReportTimer}s where the given player is reported.
	 *
	 * @param reported        The player.
	 * @param reportedPlayers A HashMap containing all the players reported by a certain sender.
	 * @return All {@link ReportTimer}s where the given player is reported.
	 */
	private PriorityQueue<ReportTimer> getTimers(final OfflinePlayer reported, final HashMap<String, PriorityQueue<ReportTimer>> reportedPlayers) {
		final PriorityQueue<ReportTimer> timers = new PriorityQueue<ReportTimer>();

		if (reportedPlayers.get(reported.getName()) != null) {
			timers.addAll(reportedPlayers.get(reported.getName()));
		}

		if (reportedPlayers.get(reported.getUniqueId().toString()) != null) {
			timers.addAll(reportedPlayers.get(reported.getUniqueId().toString()));
		}

		return timers;
	}

	/**
	 * Gets all the {@link ReportTimer}s where the given {@link CommandSender} has reported the given {@link OfflinePlayer}.
	 *
	 * @param sender   The {@link CommandSender}.
	 * @param reported The {@link OfflinePlayer}.
	 * @return A {@link PriorityQueue} containing {@link ReportTimer}s where the given
	 * {@link CommandSender} has reported the given {@link OfflinePlayer}.
	 */
	private PriorityQueue<ReportTimer> getAllReportTimers(final CommandSender sender, final OfflinePlayer reported) {
		final HashMap<String, PriorityQueue<ReportTimer>> reportedPlayers = this.getReportedPlayers(sender);

		return this.getTimers(reported, reportedPlayers);
	}

	/**
	 * Gets all the report timers for all players reported by the given {@link CommandSender}.
	 *
	 * @param sender The {@link CommandSender}.
	 * @return A {@link PriorityQueue} containing {@link ReportTimer}s the given {@link CommandSender} has created.
	 */
	private PriorityQueue<ReportTimer> getAllReportTimers(final CommandSender sender) {
		final HashMap<String, PriorityQueue<ReportTimer>> reportedPlayers = this.getReportedPlayers(sender);

		final PriorityQueue<ReportTimer> timers = new PriorityQueue<ReportTimer>();

		for (final PriorityQueue<ReportTimer> e : reportedPlayers.values()) {
			timers.addAll(e);
		}

		return timers;
	}

	private Configuration getConfig() {
		return getStore().getConfigurationStore().get();
	}

	private Locale getLocale() {
		return getStore().getLocaleStore().get();
	}

	private boolean hasPermission(final Player player, final String permission) {
		return getModule().getPermissionService().hasPermission(player, permission);
	}
}
