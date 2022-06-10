package net.KabOOm356.Command.Commands;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.KabOOm356.Command.Help.Usage;
import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQL.QueryType;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.DeletePhrases;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Service.SQLStatServices.ModeratorStatService.ModeratorStat;
import net.KabOOm356.Throwable.IndexNotANumberException;
import net.KabOOm356.Throwable.IndexOutOfRangeException;
import net.KabOOm356.Throwable.NoLastViewedReportException;
import net.KabOOm356.Throwable.RequiredPermissionException;
import net.KabOOm356.Util.ArrayUtil;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.Util;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/** A {@link ReporterCommand} that will handle deleting reports. */
public class DeleteCommand extends ReporterCommand {
  private static final Logger log = LogManager.getLogger(DeleteCommand.class);

  private static final String name = "Delete";
  private static final int minimumNumberOfArguments = 1;
  private static final String permissionNode = "reporter.delete";
  private static final ModeratorStat statistic = ModeratorStat.DELETED;

  private static final List<Usage> usages =
      Collections.unmodifiableList(
          ArrayUtil.arrayToList(
              new Usage[] {
                new Usage(DeletePhrases.deleteHelp, DeletePhrases.deleteHelpDetails),
                new Usage("/report delete/remove all", DeletePhrases.deleteHelpAllDetails),
                new Usage(
                    "/report delete/remove completed|finished",
                    DeletePhrases.deleteHelpCompletedDetails),
                new Usage(
                    "/report delete/remove incomplete|unfinished",
                    DeletePhrases.deleteHelpIncompleteDetails),
                new Usage(DeletePhrases.deleteHelpPlayer, DeletePhrases.deleteHelpPlayerDetails)
              }));
  private static final List<String> aliases =
      Collections.unmodifiableList(ArrayUtil.arrayToList(new String[] {"Remove"}));

  /**
   * Constructor.
   *
   * @param manager The {@link ReporterCommandManager} managing this Command.
   */
  public DeleteCommand(final ReporterCommandManager manager) {
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
  public void execute(final CommandSender sender, final List<String> args)
      throws NoLastViewedReportException, IndexOutOfRangeException, IndexNotANumberException,
          RequiredPermissionException {
    hasRequiredPermission(sender);

    try {
      int deletionCount = 0;
      if (args.get(0).equalsIgnoreCase("all")) {
        deletionCount = deleteReportBatch(sender, BatchDeletionType.ALL);
      } else if (args.get(0).equalsIgnoreCase("completed")
          || args.get(0).equalsIgnoreCase("finished")) {
        deletionCount = deleteReportBatch(sender, BatchDeletionType.COMPLETE);
      } else if (args.get(0).equalsIgnoreCase("incomplete")
          || args.get(0).equalsIgnoreCase("unfinished")) {
        deletionCount = deleteReportBatch(sender, BatchDeletionType.INCOMPLETE);
      } else {
        if (Util.isInteger(args.get(0)) || args.get(0).equalsIgnoreCase("last")) {
          final int index =
              getServiceModule()
                  .getLastViewedReportService()
                  .getIndexOrLastViewedReport(sender, args.get(0));

          getServiceModule().getReportValidatorService().requireReportIndexValid(index);

          if (!getServiceModule().getReportPermissionService().canAlterReport(sender, index)) {
            return;
          }

          deleteReport(sender, index);
          deletionCount = 1;
        } else { // /report delete <Player Name> [reported/sender]
          final OfflinePlayer player = getManager().getPlayer(args.get(0));

          if (player != null) {
            if (args.size() >= 2 && args.get(1).equalsIgnoreCase("sender")) {
              deletionCount = deletePlayer(sender, PlayerDeletionType.SENDER, player);
            } else {
              deletionCount = deletePlayer(sender, PlayerDeletionType.REPORTED, player);
            }
          }
        }
      }
      // Log the statistic
      if (deletionCount > 0) {
        incrementStatistic(sender, deletionCount);
      }
    } catch (final Exception e) {
      log.error("Failed to execute delete command!", e);
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

  private void incrementStatistic(final CommandSender sender, final int count) {
    if (BukkitUtil.isOfflinePlayer(sender)) {
      final OfflinePlayer player = (OfflinePlayer) sender;
      getServiceModule().getModStatsService().incrementStat(player, statistic, count);
    }
  }

  private void deleteReport(final CommandSender sender, final int index) throws Exception {
    try {
      deleteReport(index);

      String out = getManager().getLocale().getString(DeletePhrases.deleteReport);
      out = out.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);
      sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + out);

      reformatTables(sender, index);
      updateLastViewed(index);

      getServiceModule().getPlayerMessageService().removeMessage(index);
    } catch (final Exception e) {
      log.log(Level.ERROR, "Failed to delete single report!");
      throw e;
    }
  }

  private void deleteReport(final int index)
      throws ClassNotFoundException, SQLException, InterruptedException {
    final String query = "Delete FROM Reports WHERE ID = " + index;
    final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
    final int connectionId = database.openPooledConnection();
    try {
      database.updateQuery(connectionId, query);
    } finally {
      database.closeConnection(connectionId);
    }
  }

  private int deletePlayer(
      final CommandSender sender, final PlayerDeletionType deletion, final OfflinePlayer player)
      throws Exception {
    String query = getQuery(sender, player, QueryType.SELECT, deletion);

    final int count = getServiceModule().getReportCountService().getCount();
    final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
    final int connectionId = database.openPooledConnection();
    try {
      final List<Integer> remainingIndexes = new ArrayList<>();
      final SQLResultSet result = database.sqlQuery(connectionId, query);

      for (final ResultRow row : result) {
        remainingIndexes.add(row.getInt("ID"));
      }

      query = getQuery(sender, player, QueryType.DELETE, deletion);

      database.updateQuery(connectionId, query);

      String message;

      if (deletion == PlayerDeletionType.REPORTED) {
        message = getManager().getLocale().getString(DeletePhrases.deletePlayerReported);
      } else {
        message = getManager().getLocale().getString(DeletePhrases.deletePlayerSender);
      }

      String displayName = player.getName();

      if (player.isOnline()) {
        displayName = player.getPlayer().getDisplayName();
      }

      final String playerName = BukkitUtil.formatPlayerName(displayName, player.getName());

      message = message.replaceAll("%p", ChatColor.BLUE + playerName + ChatColor.WHITE);

      sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + message);

      final int totalDeleted = count - remainingIndexes.size();

      // Display the total number of reports delete.
      displayTotalReportsDeleted(sender, totalDeleted);

      reformatTables(sender, remainingIndexes);

      updateLastViewed(remainingIndexes);

      getServiceModule().getPlayerMessageService().reindexMessages(remainingIndexes);
      return totalDeleted;
    } catch (final Exception e) {
      log.log(Level.ERROR, "Failed to delete reports for a player!");
      throw e;
    }
  }

  private String getQuery(
      final CommandSender sender,
      final OfflinePlayer player,
      final QueryType queryType,
      final PlayerDeletionType deletion) {
    if (queryType == QueryType.DELETE) {
      return getDeleteQuery(sender, player, deletion);
    } else {
      return getSelectQuery(sender, player, deletion);
    }
  }

  private String getSelectQuery(
      final CommandSender sender, final OfflinePlayer player, final PlayerDeletionType deletion) {
    final StringBuilder query = new StringBuilder();
    query.append("SELECT ID FROM Reports WHERE ");
    final ModLevel level = getServiceModule().getPlayerService().getModLevel(sender);

    if (sender.isOp() || sender instanceof ConsoleCommandSender) {
      if (player.getName().equalsIgnoreCase("* (Anonymous)")) {
        if (deletion == PlayerDeletionType.REPORTED) {
          query.append("Reported != '").append(player.getName()).append('\'');
        } else if (deletion == PlayerDeletionType.SENDER) {
          query.append("Sender != '").append(player.getName()).append('\'');
        }
      } else {
        if (deletion == PlayerDeletionType.REPORTED) {
          query.append("ReportedUUID != '").append(player.getUniqueId()).append('\'');
        } else if (deletion == PlayerDeletionType.SENDER) {
          query.append("SenderUUID != '").append(player.getUniqueId()).append('\'');
        }
      }
    } else {
      query
          .append("NOT (Priority <= ")
          .append(level.getLevel())
          .append(" AND (ClaimStatus = 0 OR ClaimPriority < ")
          .append(level.getLevel())
          .append(" OR ");

      if (BukkitUtil.isPlayer(sender)) {
        final Player senderPlayer = (Player) sender;

        query.append("ClaimedByUUID = '").append(senderPlayer.getUniqueId()).append("') ");
      } else {
        query.append("ClaimedBy = '").append(sender.getName()).append("') ");
      }

      if (player.getName().equalsIgnoreCase("* (Anonymous)")) {
        if (deletion == PlayerDeletionType.REPORTED) {
          query.append("AND Reported = '").append(player.getName()).append("')");
        } else if (deletion == PlayerDeletionType.SENDER) {
          query.append("AND Sender = '").append(player.getName()).append("')");
        }
      } else {
        if (deletion == PlayerDeletionType.REPORTED) {
          query.append("AND ReportedUUID = '").append(player.getUniqueId()).append("')");
        } else if (deletion == PlayerDeletionType.SENDER) {
          query.append("AND SenderUUID = '").append(player.getUniqueId()).append("')");
        }
      }
    }

    return query.toString();
  }

  private String getDeleteQuery(
      final CommandSender sender, final OfflinePlayer player, final PlayerDeletionType deletion) {
    final StringBuilder query = new StringBuilder();
    query.append("DELETE FROM Reports WHERE ");
    final ModLevel level = getServiceModule().getPlayerService().getModLevel(sender);

    if (sender.isOp() || sender instanceof ConsoleCommandSender) {
      if (player.getName().equals("* (Anonymous)")) {
        if (deletion == PlayerDeletionType.REPORTED) {
          query.append("Reported = '").append(player.getName()).append('\'');
        } else if (deletion == PlayerDeletionType.SENDER) {
          query.append("Sender = '").append(player.getName()).append('\'');
        }
      } else {
        if (deletion == PlayerDeletionType.REPORTED) {
          query.append("ReportedUUID = '").append(player.getUniqueId()).append('\'');
        } else if (deletion == PlayerDeletionType.SENDER) {
          query.append("SenderUUID = '").append(player.getUniqueId()).append('\'');
        }
      }
    } else {
      query
          .append("(Priority <= ")
          .append(level.getLevel())
          .append(" AND (ClaimStatus = 0 OR ")
          .append("ClaimPriority < ")
          .append(level.getLevel())
          .append(" OR ");

      if (BukkitUtil.isPlayer(sender)) {
        final Player senderPlayer = (Player) sender;

        query.append("ClaimedByUUID = '").append(senderPlayer.getUniqueId()).append("') ");
      } else {
        query.append("ClaimedBy = '").append(sender.getName()).append("') ");
      }

      if (player.getName().equals("* (Anonymous)")) {
        if (deletion == PlayerDeletionType.REPORTED) {
          query.append("AND Reported = '").append(player.getName()).append("')");
        } else if (deletion == PlayerDeletionType.SENDER) {
          query.append("AND Sender = '").append(player.getName()).append("')");
        }
      } else {
        if (deletion == PlayerDeletionType.REPORTED) {
          query.append("AND ReportedUUID = '").append(player.getUniqueId()).append("')");
        } else if (deletion == PlayerDeletionType.SENDER) {
          query.append("AND SenderUUID = '").append(player.getUniqueId()).append("')");
        }
      }
    }

    return query.toString();
  }

  private int deleteReportBatch(final CommandSender sender, final BatchDeletionType deletion)
      throws Exception {
    try {
      final int beforeDeletion = getServiceModule().getReportCountService().getCount();
      final List<Integer> remainingIndexes = getRemainingIndexes(sender, deletion);
      final int afterDeletion = remainingIndexes.size();
      final int totalDeleted = beforeDeletion - afterDeletion;

      deleteBatch(sender, deletion);
      reformatTables(sender, remainingIndexes);
      updateLastViewed(remainingIndexes);
      getServiceModule().getPlayerMessageService().reindexMessages(remainingIndexes);

      final Locale locale = getManager().getLocale();
      String message = "";

      if (deletion == BatchDeletionType.ALL) {
        message = locale.getString(DeletePhrases.deleteAll);
      } else if (deletion == BatchDeletionType.COMPLETE) {
        message = locale.getString(DeletePhrases.deleteComplete);
      } else if (deletion == BatchDeletionType.INCOMPLETE) {
        message = locale.getString(DeletePhrases.deleteIncomplete);
      }

      sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + message);
      displayTotalReportsDeleted(sender, totalDeleted);
      return totalDeleted;
    } catch (final Exception e) {
      log.log(Level.ERROR, "Failed to delete batch of reports!");
      throw e;
    }
  }

  private void deleteBatch(final CommandSender sender, final BatchDeletionType deletion)
      throws ClassNotFoundException, SQLException, InterruptedException {
    final String query = getQuery(sender, QueryType.DELETE, deletion);

    final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
    final int connectionId = database.openPooledConnection();
    try {
      database.updateQuery(connectionId, query);
    } finally {
      database.closeConnection(connectionId);
    }
  }

  /*
   * Will create the SQL query to perform the wanted query type and deletion type.
   */
  private String getQuery(
      final CommandSender sender, final QueryType type, final BatchDeletionType deletion) {
    if (type == QueryType.DELETE) {
      return getDeleteQuery(sender, deletion);
    } else {
      return getSelectQuery(sender, deletion);
    }
  }

  private String getSelectQuery(final CommandSender sender, final BatchDeletionType deletion) {
    final StringBuilder query = new StringBuilder();
    query.append("SELECT ID FROM Reports WHERE ");

    if (sender.isOp() || sender instanceof ConsoleCommandSender) {
      if (deletion == BatchDeletionType.ALL) {
        query.append('0');
      } else if (deletion == BatchDeletionType.COMPLETE) {
        query.append("CompletionStatus = 0");
      } else if (deletion == BatchDeletionType.INCOMPLETE) {
        query.append("CompletionStatus = 1");
      }
    } else {
      final ModLevel level = getServiceModule().getPlayerService().getModLevel(sender);

      /*
       * Reports will be deleted (not remain) if:
       *
       * 1. Their priority is less than or equal to the sender's modlevel.
       *
       * 2. The report is unclaimed,
       *    or claimed by another player with a lower modlevel,
       *    or claimed by the sender.
       */
      query
          .append("NOT (Priority <= ")
          .append(level.getLevel())
          .append(' ')
          .append("AND ")
          .append("(ClaimStatus = 0 ")
          .append("OR ")
          .append("ClaimPriority < ")
          .append(level.getLevel())
          .append(' ')
          .append("OR ");

      if (BukkitUtil.isPlayer(sender)) {
        final Player senderPlayer = (Player) sender;

        query.append("ClaimedByUUID = '").append(senderPlayer.getUniqueId()).append("')");
      } else {
        query.append("ClaimedBy = '").append(sender.getName()).append("')");
      }

      if (deletion == BatchDeletionType.ALL) {
        query.append(')');
      } else if (deletion == BatchDeletionType.COMPLETE) {
        query.append(' ').append("AND ").append("CompletionStatus = 0)");
      } else if (deletion == BatchDeletionType.INCOMPLETE) {
        query.append(' ').append("AND ").append("CompletionStatus = 1)");
      }
    }

    return query.toString();
  }

  private String getDeleteQuery(final CommandSender sender, final BatchDeletionType deletion) {
    final StringBuilder query = new StringBuilder();
    query.append("DELETE FROM Reports WHERE ");

    if (sender.isOp() || sender instanceof ConsoleCommandSender) {
      if (deletion == BatchDeletionType.ALL) {
        query.append('1');
      } else if (deletion == BatchDeletionType.COMPLETE) {
        query.append("CompletionStatus = 1");
      } else if (deletion == BatchDeletionType.INCOMPLETE) {
        query.append("CompletionStatus = 0");
      }
    } else {
      final ModLevel level = getServiceModule().getPlayerService().getModLevel(sender);

      /*
       * Reports will be deleted (not remain) if:
       *
       * 1. Their priority is less than or equal to the sender's modlevel.
       *
       * 2. The report is unclaimed,
       *    or claimed by another player with a lower modlevel,
       *    or claimed by the sender.
       */
      query
          .append("(Priority <= ")
          .append(level.getLevel())
          .append(' ')
          .append("AND ")
          .append("(ClaimStatus = 0 ")
          .append("OR ")
          .append("ClaimPriority < ")
          .append(level.getLevel())
          .append(' ')
          .append("OR ");

      if (BukkitUtil.isPlayer(sender)) {
        final Player senderPlayer = (Player) sender;

        query.append("ClaimedByUUID = '").append(senderPlayer.getUniqueId()).append("')");
      } else {
        query.append("ClaimedBy = '").append(sender.getName()).append("')");
      }

      // Append on the rest of the query to perform the required deletion type.
      if (deletion == BatchDeletionType.ALL) {
        query.append(')');
      } else if (deletion == BatchDeletionType.COMPLETE) {
        query.append(' ').append("AND ").append("CompletionStatus = 1)");
      } else if (deletion == BatchDeletionType.INCOMPLETE) {
        query.append(' ').append("AND ").append("CompletionStatus = 0)");
      }
    }

    return query.toString();
  }

  private List<Integer> getRemainingIndexes(
      final CommandSender sender, final BatchDeletionType deletion)
      throws ClassNotFoundException, SQLException, InterruptedException {
    final List<Integer> remainingIDs = new ArrayList<>();
    final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
    final int connectionId = database.openPooledConnection();
    try {
      final String query = getQuery(sender, QueryType.SELECT, deletion);
      final SQLResultSet result = database.sqlQuery(connectionId, query);

      for (final ResultRow row : result) {
        remainingIDs.add(row.getInt("ID"));
      }
    } finally {
      database.closeConnection(connectionId);
    }

    return remainingIDs;
  }

  private void updateLastViewed(final int removedIndex) {
    getServiceModule().getLastViewedReportService().deleteIndex(removedIndex);
  }

  private void updateLastViewed(final List<Integer> remainingIndexes) {
    getServiceModule().getLastViewedReportService().deleteBatch(remainingIndexes);
  }

  private void reformatTables(final CommandSender sender, final List<Integer> remainingIndexes)
      throws Exception {
    StringBuilder query;
    Statement stmt = null;

    final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
    final int connectionId = database.openPooledConnection();
    try {
      stmt = database.createStatement(connectionId);

      for (int LCV = 0; LCV < remainingIndexes.size(); LCV++) {
        query = new StringBuilder();
        query
            .append("UPDATE Reports SET ID=")
            .append((LCV + 1))
            .append(" WHERE ID=")
            .append(remainingIndexes.get(LCV));

        stmt.addBatch(query.toString());
      }

      stmt.executeBatch();
    } catch (final Exception e) {
      log.log(Level.ERROR, "Failed reformatting tables after batch delete!");
      throw e;
    } finally {
      closeStatement(stmt);
      database.closeConnection(connectionId);
    }

    sender.sendMessage(
        ChatColor.BLUE
            + Reporter.getLogPrefix()
            + ChatColor.WHITE
            + BukkitUtil.colorCodeReplaceAll(
                getManager().getLocale().getString(DeletePhrases.SQLTablesReformat)));
  }

  private void reformatTables(final CommandSender sender, final int removedIndex) throws Exception {
    final int count = getServiceModule().getReportCountService().getCount();
    final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
    final int connectionId = database.openPooledConnection();
    Statement statement = null;
    try {
      statement = database.createStatement(connectionId);
      StringBuilder formatQuery;

      for (int LCV = removedIndex; LCV <= count; LCV++) {
        formatQuery = new StringBuilder();
        formatQuery
            .append("UPDATE Reports SET ID=")
            .append(LCV)
            .append(" WHERE ID=")
            .append((LCV + 1));

        statement.addBatch(formatQuery.toString());
      }
      statement.executeBatch();
      sender.sendMessage(
          ChatColor.BLUE
              + Reporter.getLogPrefix()
              + ChatColor.WHITE
              + BukkitUtil.colorCodeReplaceAll(
                  getManager().getLocale().getString(DeletePhrases.SQLTablesReformat)));
    } catch (final Exception e) {
      log.log(Level.ERROR, "Failed to reformat table after single delete!");
      throw e;
    } finally {
      closeStatement(statement);
      database.closeConnection(connectionId);
    }
  }

  private static void closeStatement(final Statement statement) {
    try {
      if (statement != null) {
        statement.close();
      }
    } catch (final Exception e) {
      if (log.isDebugEnabled()) {
        log.warn("Failed to close statement!", e);
      }
    }
  }

  private void displayTotalReportsDeleted(final CommandSender sender, final int totalDeleted) {
    String message = getManager().getLocale().getString(DeletePhrases.deletedReportsTotal);
    message =
        message.replaceAll("%r", ChatColor.RED + Integer.toString(totalDeleted) + ChatColor.WHITE);
    sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + message);
  }

  private enum BatchDeletionType {
    ALL,
    INCOMPLETE,
    COMPLETE
  }

  private enum PlayerDeletionType {
    SENDER,
    REPORTED
  }
}
