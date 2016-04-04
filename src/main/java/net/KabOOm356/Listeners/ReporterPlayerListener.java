package net.KabOOm356.Listeners;

import net.KabOOm356.Command.Command;
import net.KabOOm356.Command.Commands.ListCommand;
import net.KabOOm356.Command.Commands.ViewCommand;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.AlertPhrases;
import net.KabOOm356.Manager.MessageManager;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Runnable.DelayedMessage;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.Util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.UUID;

/**
 * A {@link Listener} that listens for player events.
 */
public class ReporterPlayerListener implements Listener {
	private static final Logger log = LogManager.getLogger(ReporterPlayerListener.class);
	private static final long serverTicksPerSecond = 20L;

	private final Reporter plugin;

	/**
	 * Constructor.
	 *
	 * @param instance The running instance of {@link Reporter}.
	 */
	public ReporterPlayerListener(final Reporter instance) {
		plugin = instance;
	}

	/**
	 * Run when a player joins.
	 *
	 * @param event The player join event.
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();

		plugin.getCommandManager().getLastViewed().put(player, -1);

		final MessageManager messageManager = plugin.getCommandManager().getMessageManager();

		if (messageManager.hasMessages(player.getUniqueId().toString()) || messageManager.hasMessages(player.getName())) {
			sendMessages(player);
		}

		if (plugin.getConfig().getBoolean("general.messaging.listOnLogin.listOnLogin", true)) {
			listOnLogin(player);
		}

		if (isPlayerReported(event.getPlayer())) {
			alertThatReportedPlayerLogin(event.getPlayer());
		}
	}

	/**
	 * Runs when a player quits.
	 *
	 * @param event The player quit event.
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(final PlayerQuitEvent event) {
		plugin.getCommandManager().getLastViewed().remove(event.getPlayer());
	}
	
	private void listOnLogin(Player player)
	{
		final Command listCommand = plugin.getCommandManager().getCommand(ListCommand.getCommandName());
		if(listCommand.hasPermission(player))
		{
			listCommand.setSender(player);
			listCommand.setArguments(new ArrayList<String>());
			if(plugin.getConfig().getBoolean("general.messaging.listOnLogin.useDelay", true))
			{
				final int delay = plugin.getConfig().getInt("general.messaging.listOnLogin.delay", 5);
				Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, listCommand, serverTicksPerSecond * delay);
			}
			else {
				Bukkit.getScheduler().runTaskAsynchronously(plugin, listCommand);
			}
		}
	}

	private void sendMessages(final Player player) {
		// Players can view a message if they have permission to view all reports or their submitted reports.
		boolean canView = plugin.getCommandManager().hasPermission(player, ViewCommand.getCommandPermissionNode());
		canView = canView || plugin.getConfig().getBoolean("general.canViewSubmittedReports", true);

		final MessageManager messageManager = plugin.getCommandManager().getMessageManager();

		// No point to send the message if the player can't view any reports.
		if (canView) {
			// Get the messages for the player using their UUID.
			final ArrayList<String> messages = messageManager.getMessages(player.getUniqueId().toString());
			// Get the messages for the player using their player name.
			final ArrayList<String> playerNameMessages = messageManager.getMessages(player.getName());

			// Append the message pools.
			messages.addAll(playerNameMessages);

			if (plugin.getConfig().getBoolean("general.messaging.completedMessageOnLogin.useDelay", true)) {
				int messageGroup = 1;
				int message = 0;

				long delayTime = 0;
				final int delayTimeInSeconds = plugin.getConfig().getInt("general.messaging.completedMessageOnLogin.delay", 5);

				while (!messages.isEmpty()) {
					// Calculate the delay time in bukkit ticks.
					// (20 bukkit ticks per second * user specified delay time in seconds) * message group number.
					delayTime = (serverTicksPerSecond * delayTimeInSeconds) * messageGroup;

					final String output = messages.remove(0);

					Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new DelayedMessage(player, output), delayTime);

					message++;

					if (message % 5 == 0) {
						messageGroup++;
					}
				}
			} else {
				for (final String message : messages) {
					player.sendMessage(message);
				}
			}
		}

		// Remove the messages for the player.
		messageManager.removePlayerMessages(player.getUniqueId().toString());
		messageManager.removePlayerMessages(player.getName());
	}
	
	private boolean isPlayerReported(final Player player) {
		final StringBuilder query = new StringBuilder();
		query.append("SELECT ID ")
				.append("FROM Reports ")
				.append("WHERE ReportedUUID = '").append(player.getUniqueId()).append("' AND CompletionStatus = 0");

		final SQLResultSet result;

		try {
			result = plugin.getDatabaseHandler().sqlQuery(query.toString());
			return !result.isEmpty();
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to execute sql query!", e);
		}
		return false;
	}
	
	private void alertThatReportedPlayerLogin( final Player reportedPlayer) {
		final StringBuilder query = new StringBuilder();
		query.append("SELECT ID, ClaimStatus, ClaimedByUUID ")
				.append("FROM Reports ")
				.append("WHERE ReportedUUID = '").append(reportedPlayer.getUniqueId()).append("' AND CompletionStatus = 0");

		final SQLResultSet result;

		try {
			result = plugin.getDatabaseHandler().sqlQuery(query.toString());
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to execute sql query!", e);
			return;
		}

		final ArrayList<Integer> indexes = new ArrayList<Integer>();

		String playerLoginMessage = ChatColor.BLUE + Reporter.getLogPrefix() +
				ChatColor.WHITE + plugin.getLocale().getString(AlertPhrases.alertClaimedPlayerLogin);

		for (final ResultRow row : result) {
			// If a report is claimed send a message to the claimer, if they are online.
			if (row.getBoolean("ClaimStatus")) {
				final String uuidString = row.getString("ClaimedByUUID");

				final UUID uuid = UUID.fromString(uuidString);

				final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

				if (player.isOnline()) {
					String output = playerLoginMessage.replaceAll("%r", ChatColor.RED + BukkitUtil.formatPlayerName(reportedPlayer) + ChatColor.WHITE);
					output = output.replaceAll("%i", ChatColor.GOLD + row.getString("ID") + ChatColor.WHITE);

					player.getPlayer().sendMessage(output);
				}
			} else {
				// Add the ID to the indexes to be sent to all players that can receive the alert.
				indexes.add(row.getInt("ID"));
			}
		}

		playerLoginMessage = ChatColor.BLUE + Reporter.getLogPrefix() +
				ChatColor.WHITE + plugin.getLocale().getString(AlertPhrases.alertUnclaimedPlayerLogin);

		playerLoginMessage = playerLoginMessage.replaceAll("%r", ChatColor.RED + BukkitUtil.formatPlayerName(reportedPlayer) + ChatColor.WHITE);
		playerLoginMessage = playerLoginMessage.replaceAll("%i", Util.indexesToString(indexes, ChatColor.GOLD, ChatColor.WHITE));

		for (final Player player : Bukkit.getOnlinePlayers()) {
			// Send the message to players with the permission to get it.
			if (plugin.getCommandManager().hasPermission(player, "reporter.alerts.onlogin.reportedPlayerLogin")) {
				player.sendMessage(playerLoginMessage);
			}
		}
	}
}
