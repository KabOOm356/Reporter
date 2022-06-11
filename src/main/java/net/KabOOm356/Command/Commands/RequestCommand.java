package net.KabOOm356.Command.Commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.KabOOm356.Command.Help.Usage;
import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.RequestPhrases;
import net.KabOOm356.Reporter.Reporter;
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

/** A {@link ReporterCommand} that will handle users requesting reported players from reports. */
public class RequestCommand extends ReporterCommand {
  private static final Logger log = LogManager.getLogger(RequestCommand.class);

  private static final String name = "Request";
  private static final int minimumNumberOfArguments = 1;
  private static final String permissionNode = "reporter.request";

  private static final List<Usage> usages =
      Collections.unmodifiableList(
          ArrayUtil.arrayToList(
              new Usage[] {
                new Usage(RequestPhrases.requestHelp, RequestPhrases.requestHelpDetails),
                new Usage("/report request most", RequestPhrases.requestMostHelpDetails)
              }));
  private static final List<String> aliases = Collections.emptyList();

  /**
   * Constructor.
   *
   * @param manager The {@link ReporterCommandManager} managing this Command.
   */
  public RequestCommand(final ReporterCommandManager manager) {
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
      throws RequiredPermissionException {
    hasRequiredPermission(sender);

    try {
      if (args.get(0).equalsIgnoreCase("most")) {
        requestMostReported(sender);
      } else {
        requestPlayer(sender, args.get(0));
      }
    } catch (final Exception e) {
      log.log(Level.ERROR, "Failed to request!", e);
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

  private void requestMostReported(final CommandSender sender)
      throws ClassNotFoundException, SQLException, InterruptedException {
    // Return the most reported players and the number of reports against them.
    final StringBuilder query = new StringBuilder();
    query.append("SELECT COUNT(*) AS Count, ReportedUUID, Reported ");
    query.append("FROM Reports ");
    query
        .append("GROUP BY ReportedUUID HAVING COUNT(*) = ")
        .append('(')
        .append("SELECT COUNT(*) ")
        .append("FROM Reports ")
        .append("GROUP BY ReportedUUID ORDER BY COUNT(*) DESC ")
        .append("LIMIT 1")
        .append(')');

    final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
    final int connectionId = database.openPooledConnection();
    try {
      final List<String> players = new ArrayList<>();
      final SQLResultSet result;
      int numberOfReports = -1;

      result = database.sqlQuery(connectionId, query.toString());

      for (final ResultRow row : result) {
        numberOfReports = result.getInt("Count");

        final String uuidString = row.getString("ReportedUUID");

        if (!uuidString.isEmpty()) {
          final UUID uuid = UUID.fromString(uuidString);

          final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

          players.add(BukkitUtil.formatPlayerName(player));
        } else {
          players.add(result.getString("Reported"));
        }
      }

      if (!players.isEmpty()) {
        String out = getManager().getLocale().getString(RequestPhrases.numberOfReportsAgainst);

        out =
            out.replaceAll(
                "%n", ChatColor.GOLD + Integer.toString(numberOfReports) + ChatColor.WHITE);
        out =
            out.replaceAll(
                "%p",
                ArrayUtil.indexesToString(players, ChatColor.GOLD, ChatColor.WHITE)
                    + ChatColor.WHITE);

        sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + out);
      } else {
        sender.sendMessage(
            ChatColor.BLUE
                + Reporter.getLogPrefix()
                + ChatColor.WHITE
                + getManager().getLocale().getString(GeneralPhrases.noReports));
      }
    } catch (final SQLException e) {
      log.log(Level.ERROR, "Failed to request most reported player!", e);
      throw e;
    } finally {
      database.closeConnection(connectionId);
    }
  }

  private void requestPlayer(final CommandSender sender, final String playerName)
      throws ClassNotFoundException, SQLException, InterruptedException {
    final OfflinePlayer player = getManager().getPlayer(playerName);

    if (player == null) {
      sender.sendMessage(
          ChatColor.RED
              + BukkitUtil.colorCodeReplaceAll(
                  getManager().getLocale().getString(GeneralPhrases.playerDoesNotExist)));
      return;
    }

    String indexes;

    final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
    final int connectionId = database.openPooledConnection();
    try {
      final List<String> params = new ArrayList<>();
      String query = "SELECT ID FROM Reports WHERE ReportedUUID=?";

      if (!player.getName().equalsIgnoreCase("* (Anonymous)")) {
        params.add(player.getUniqueId().toString());
      } else {
        query = "SELECT ID FROM Reports WHERE Reported=?";
        params.add(player.getName());
      }

      if (getManager().getDatabaseHandler().usingSQLite()) {
        query += " COLLATE NOCASE";
      }

      final SQLResultSet result =
          getManager().getDatabaseHandler().preparedSQLQuery(connectionId, query, params);

      indexes = ArrayUtil.indexesToString(result, "ID", ChatColor.GOLD, ChatColor.WHITE);
    } catch (final SQLException e) {
      log.log(Level.ERROR, "Failed to request player!", e);
      throw e;
    } finally {
      database.closeConnection(connectionId);
    }

    String out;
    if (indexes.isEmpty()) {
      out =
          BukkitUtil.colorCodeReplaceAll(getManager().getLocale().getString(RequestPhrases.reqNF));

      out = out.replaceAll("%p", ChatColor.GOLD + player.getName() + ChatColor.RED);

      sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.RED + out);
    } else {
      out =
          BukkitUtil.colorCodeReplaceAll(getManager().getLocale().getString(RequestPhrases.reqFI));

      out = out.replaceAll("%p", ChatColor.GOLD + player.getName() + ChatColor.WHITE);

      out = out.replaceAll("%i", indexes);

      sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + out);
    }
  }
}
