package net.KabOOm356.Listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.KabOOm356.Command.Commands.ListCommand;
import net.KabOOm356.Command.Commands.ViewCommand;
import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.AlertPhrases;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Runnable.DelayedMessage;
import net.KabOOm356.Service.PlayerMessageService;
import net.KabOOm356.Util.ArrayUtil;
import net.KabOOm356.Util.BukkitUtil;
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

/** A {@link Listener} that listens for player events. */
public class ReporterPlayerListener implements Listener {
  private static final Logger log = LogManager.getLogger(ReporterPlayerListener.class);

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

    final PlayerMessageService playerMessageService =
        plugin.getCommandManager().getServiceModule().getPlayerMessageService();

    if (playerMessageService.hasMessages(player.getUniqueId().toString())
        || playerMessageService.hasMessages(player.getName())) {
      sendMessages(player);
    }

    if (plugin.getConfig().getBoolean("general.messaging.listOnLogin.listOnLogin", true)) {
      listOnLogin(player);
    }

    final boolean alertReportedPlayerLogin =
        plugin.getConfig().getBoolean("general.messaging.alerts.reportedPlayerLogin.enabled", true);
    if (alertReportedPlayerLogin) {
      final boolean alertConsoleReportedPlayerLogin =
          plugin
              .getConfig()
              .getBoolean("general.messaging.alerts.reportedPlayerLogin.toConsole", true);
      final boolean alertPlayersReportedPlayerLogin =
          plugin
              .getConfig()
              .getBoolean("general.messaging.alerts.reportedPlayerLogin.toPlayer", true);
      if (alertConsoleReportedPlayerLogin || alertPlayersReportedPlayerLogin) {
        if (isPlayerReported(player)) {
          alertThatReportedPlayerLogin(player);
        }
      }
    }
  }

  /**
   * Runs when a player quits.
   *
   * @param event The player quit event.
   */
  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerQuit(final PlayerQuitEvent event) {
    plugin
        .getCommandManager()
        .getServiceModule()
        .getLastViewedReportService()
        .removeLastViewedReport(event.getPlayer());
  }

  private void listOnLogin(final Player player) {
    final ReporterCommand listCommand =
        plugin.getCommandManager().getCommand(ListCommand.getCommandName());
    if (listCommand.hasPermission(player)) {
      listCommand.setSender(player);
      listCommand.setArguments(new ArrayList<>());
      if (plugin.getConfig().getBoolean("general.messaging.listOnLogin.useDelay", true)) {
        final int delay = plugin.getConfig().getInt("general.messaging.listOnLogin.delay", 5);
        Bukkit.getScheduler()
            .runTaskLaterAsynchronously(
                plugin, listCommand, BukkitUtil.convertSecondsToServerTicks(delay));
      } else {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, listCommand);
      }
    }
  }

  private void sendMessages(final Player player) {
    // Players can view a message if they have permission to view all reports or their submitted
    // reports.
    boolean canView =
        plugin
            .getCommandManager()
            .getServiceModule()
            .getPermissionService()
            .hasPermission(player, ViewCommand.getCommandPermissionNode());
    canView = canView || plugin.getConfig().getBoolean("general.canViewSubmittedReports", true);

    final PlayerMessageService playerMessageService =
        plugin.getCommandManager().getServiceModule().getPlayerMessageService();

    // No point to send the message if the player can't view any reports.
    if (canView) {
      // Get the messages for the player using their UUID.
      final List<String> messages =
          playerMessageService.getMessages(player.getUniqueId().toString());
      // Get the messages for the player using their player name.
      final List<String> playerNameMessages = playerMessageService.getMessages(player.getName());

      // Append the message pools.
      messages.addAll(playerNameMessages);

      if (plugin
          .getConfig()
          .getBoolean("general.messaging.completedMessageOnLogin.useDelay", true)) {
        int messageGroup = 1;
        int message = 0;

        long delayTime = 0;
        final int delayTimeInSeconds =
            plugin.getConfig().getInt("general.messaging.completedMessageOnLogin.delay", 5);

        while (!messages.isEmpty()) {
          // Calculate the delay time in bukkit ticks and the offset for the message group.
          delayTime = BukkitUtil.convertSecondsToServerTicks(delayTimeInSeconds) * messageGroup;

          final String output = messages.remove(0);

          Bukkit.getScheduler()
              .runTaskLaterAsynchronously(plugin, new DelayedMessage(player, output), delayTime);

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
    playerMessageService.removePlayerMessages(player.getUniqueId().toString());
    playerMessageService.removePlayerMessages(player.getName());
  }

  private boolean isPlayerReported(final Player player) {
    final StringBuilder query = new StringBuilder();
    query
        .append("SELECT ID ")
        .append("FROM Reports ")
        .append("WHERE ReportedUUID = '")
        .append(player.getUniqueId())
        .append("' AND CompletionStatus = 0");

    final SQLResultSet result;

    try {
      result = plugin.getDatabaseHandler().sqlQuery(query.toString());
      return !result.isEmpty();
    } catch (final Exception e) {
      log.error("Failed to execute sql query!", e);
    }
    return false;
  }

  private void alertThatReportedPlayerLogin(final Player reportedPlayer) {
    final StringBuilder query = new StringBuilder();
    query
        .append("SELECT ID, ClaimStatus, ClaimedByUUID ")
        .append("FROM Reports ")
        .append("WHERE ReportedUUID = '")
        .append(reportedPlayer.getUniqueId())
        .append("' AND CompletionStatus = 0");

    final SQLResultSet result;

    try {
      result = plugin.getDatabaseHandler().sqlQuery(query.toString());
    } catch (final Exception e) {
      log.error("Failed to execute sql query!", e);
      return;
    }

    final boolean displayAlertToPlayers =
        plugin
            .getConfig()
            .getBoolean("general.messaging.alerts.reportedPlayerLogin.toPlayer", true);

    final List<Integer> indexes = new ArrayList<>();

    for (final ResultRow row : result) {
      // If a report is claimed send a message to the claimer, if they are online.
      if (row.getBoolean("ClaimStatus") && displayAlertToPlayers) {
        final String uuidString = row.getString("ClaimedByUUID");
        final UUID uuid = UUID.fromString(uuidString);
        final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        alertClaimingPlayerReportedPlayerLogin(player, reportedPlayer, row.getString("ID"));
      } else {
        // Add the ID to the indexes to be sent to all players that can receive the alert.
        indexes.add(row.getInt("ID"));
      }
    }

    final String reportedPlayerName =
        ChatColor.RED + BukkitUtil.formatPlayerName(reportedPlayer) + ChatColor.WHITE;

    if (plugin
        .getConfig()
        .getBoolean("general.messaging.alerts.reportedPlayerLogin.toConsole", true)) {
      final String message =
          plugin
              .getLocale()
              .getString(AlertPhrases.alertConsoleReportedPlayerLogin)
              .replaceAll("%r", reportedPlayerName)
              .replaceAll(
                  "%i", ArrayUtil.indexesToString(indexes, ChatColor.GOLD, ChatColor.WHITE));
      log.info(message);
    }

    alertOnlinePlayersReportedPlayerLogin(reportedPlayerName, indexes);
  }

  private void alertClaimingPlayerReportedPlayerLogin(
      final OfflinePlayer player, final OfflinePlayer reportedPlayer, final String id) {
    if (player.isOnline()) {
      String message =
          ChatColor.BLUE
              + Reporter.getLogPrefix()
              + ChatColor.WHITE
              + plugin.getLocale().getString(AlertPhrases.alertClaimedPlayerLogin);

      message =
          message
              .replaceAll(
                  "%r",
                  ChatColor.RED + BukkitUtil.formatPlayerName(reportedPlayer) + ChatColor.WHITE)
              .replaceAll("%i", ChatColor.GOLD + id + ChatColor.WHITE);

      player.getPlayer().sendMessage(message);
    }
  }

  private void alertOnlinePlayersReportedPlayerLogin(
      final String reportedPlayerName, final List<Integer> indexes) {
    String message =
        ChatColor.BLUE
            + Reporter.getLogPrefix()
            + ChatColor.WHITE
            + plugin.getLocale().getString(AlertPhrases.alertUnclaimedPlayerLogin);

    message =
        message
            .replaceAll("%r", reportedPlayerName)
            .replaceAll("%i", ArrayUtil.indexesToString(indexes, ChatColor.GOLD, ChatColor.WHITE));

    for (final Player player : Bukkit.getOnlinePlayers()) {
      // Send the message to players with the permission to get it.
      if (plugin
          .getCommandManager()
          .getServiceModule()
          .getPermissionService()
          .hasPermission(player, "reporter.alerts.onlogin.reportedPlayerLogin")) {
        player.sendMessage(message);
      }
    }
  }
}
