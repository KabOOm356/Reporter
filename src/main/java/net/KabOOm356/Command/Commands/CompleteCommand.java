package net.KabOOm356.Command.Commands;

import java.sql.SQLException;
import java.util.*;
import net.KabOOm356.Command.Help.Usage;
import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.CompletePhrases;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Service.Messager.Group;
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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** A {@link ReporterCommand} that will handle completing reports. */
public class CompleteCommand extends ReporterCommand {
  public static final Group messageGroup = new Group("Completion");
  private static final Logger log = LogManager.getLogger(CompleteCommand.class);
  private static final String name = "Complete";
  private static final int minimumNumberOfArguments = 1;
  private static final String permissionNode = "reporter.complete";
  private static final List<Usage> usages =
      Collections.unmodifiableList(
          ArrayUtil.arrayToList(
              new Usage[] {
                new Usage(CompletePhrases.completeHelp, CompletePhrases.completeHelpDetails)
              }));
  private static final List<String> aliases =
      Collections.unmodifiableList(ArrayUtil.arrayToList(new String[] {"Finish"}));
  private final boolean sendMessage;

  /**
   * Constructor.
   *
   * @param manager The {@link ReporterCommandManager} managing this Command.
   */
  public CompleteCommand(final ReporterCommandManager manager) {
    super(manager, name, permissionNode, minimumNumberOfArguments);

    sendMessage =
        getManager()
            .getConfig()
            .getBoolean("general.messaging.completedMessageOnLogin.completedMessageOnLogin", true);
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
  public void execute(final CommandSender sender, final List<String> args)
      throws NoLastViewedReportException, IndexOutOfRangeException, IndexNotANumberException,
          RequiredPermissionException {
    hasRequiredPermission(sender);

    try {
      final int index =
          getServiceModule()
              .getLastViewedReportService()
              .getIndexOrLastViewedReport(sender, args.get(0));

      getServiceModule().getReportValidatorService().requireReportIndexValid(index);

      if (!getServiceModule().getReportPermissionService().canAlterReport(sender, index)) {
        return;
      }

      final StringBuilder summaryBuilder = new StringBuilder();
      for (int LCV = 1; LCV < args.size(); LCV++) {
        summaryBuilder.append(' ').append(args.get(LCV));
      }

      final String summary = summaryBuilder.toString().trim();

      if (!isSummaryValid(sender, summary)) {
        return;
      }

      completeReport(sender, index, summary);
      broadcastCompletedMessage(index);
    } catch (final Exception e) {
      log.log(Level.ERROR, "Failed to complete report!", e);
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

  private void completeReport(final CommandSender sender, final int index, final String summary)
      throws ClassNotFoundException, SQLException, InterruptedException {
    final List<String> params = new ArrayList<>(5);
    params.add(0, "1");
    params.add(BukkitUtil.getUUIDString(sender));
    params.add(2, sender.getName());
    params.add(3, Reporter.getDateformat().format(new Date()));
    params.add(4, summary);
    params.add(5, Integer.toString(index));

    final String query =
        "UPDATE Reports "
            + "SET CompletionStatus=?, "
            + "CompletedByUUID=?, "
            + "CompletedBy=?, "
            + "CompletionDate=?, "
            + "CompletionSummary=? "
            + "WHERE id=?";

    final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
    final int connectionId = database.openPooledConnection();
    try {
      database.preparedUpdateQuery(connectionId, query, params);
    } catch (final SQLException e) {
      log.error(
          String.format("Failed to execute complete query on connection [%d]!", connectionId));
      throw e;
    } finally {
      database.closeConnection(connectionId);
    }

    sender.sendMessage(
        ChatColor.BLUE
            + Reporter.getLogPrefix()
            + ChatColor.WHITE
            + BukkitUtil.colorCodeReplaceAll(
                getManager().getLocale().getString(CompletePhrases.playerComplete)));

    if (BukkitUtil.isOfflinePlayer(sender)) {
      final OfflinePlayer senderPlayer = (OfflinePlayer) sender;

      getServiceModule().getModStatsService().incrementStat(senderPlayer, ModeratorStat.COMPLETED);
    }
  }

  private boolean isSummaryValid(final CommandSender sender, final String summary) {
    if (!summary.isEmpty()
        || getManager().getConfig().getBoolean("general.canCompleteWithoutSummary", false)) {
      return true;
    }

    sender.sendMessage(
        ChatColor.RED + getManager().getLocale().getString(CompletePhrases.completeNoSummary));

    return false;
  }

  private void broadcastCompletedMessage(final int index) {
    final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

    String reportCompleted =
        BukkitUtil.colorCodeReplaceAll(
            getManager().getLocale().getString(CompletePhrases.broadcastCompleted));

    reportCompleted =
        reportCompleted.replaceAll(
            "%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);

    OfflinePlayer sender = null;

    String playerName = null;
    String yourReportCompleted = null;

    if (getManager().getConfig().getBoolean("general.canViewSubmittedReports", true)) {
      final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
      Integer connectionId = null;
      try {
        connectionId = database.openPooledConnection();

        final String query = "SELECT SenderUUID, Sender FROM Reports WHERE ID=" + index;

        final SQLResultSet result = database.sqlQuery(connectionId, query);

        final String uuidString = result.getString("SenderUUID");

        if (!uuidString.isEmpty()) {
          final UUID uuid = UUID.fromString(uuidString);
          sender = Bukkit.getOfflinePlayer(uuid);
          playerName = sender.getName();
        } else {
          playerName = result.getString("Sender");
        }
      } catch (final Exception e) {
        log.warn(
            String.format(
                "Failed to broadcast report completion message on connection [%d]!", connectionId),
            e);
      } finally {
        if (connectionId != null) {
          database.closeConnection(connectionId);
        }
      }

      yourReportCompleted =
          BukkitUtil.colorCodeReplaceAll(
              getManager().getLocale().getString(CompletePhrases.broadcastYourReportCompleted));

      yourReportCompleted =
          yourReportCompleted.replaceAll(
              "%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);
    }

    boolean isReporterOnline = false;

    for (final Player player : onlinePlayers) {
      if (hasPermission(player, "reporter.list")) {
        player.sendMessage(
            ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + reportCompleted);
      } else if (playerName != null
          && !playerName.isEmpty()
          && playerName.equals(player.getName())) {
        isReporterOnline = true;
        player.sendMessage(
            ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + yourReportCompleted);
      }
    }

    if (sendMessage && !isReporterOnline) {
      final String message =
          ChatColor.BLUE
              + Reporter.getLogPrefix()
              + ChatColor.WHITE
              + getManager().getLocale().getString(CompletePhrases.yourReportsCompleted);

      if (sender != null) {
        getServiceModule()
            .getPlayerMessageService()
            .addMessage(sender.getUniqueId().toString(), messageGroup, message, index);
      } else if (playerName != null && !playerName.isEmpty()) {
        getServiceModule()
            .getPlayerMessageService()
            .addMessage(playerName, messageGroup, message, index);
      }
    }
  }
}
