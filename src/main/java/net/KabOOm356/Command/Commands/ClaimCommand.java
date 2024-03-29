package net.KabOOm356.Command.Commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import net.KabOOm356.Command.Help.Usage;
import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Locale.Entry.LocalePhrases.ClaimPhrases;
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
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/** A {@link ReporterCommand} to handle players claiming reports. */
public class ClaimCommand extends ReporterCommand {
  private static final Logger log = LogManager.getLogger(ClaimCommand.class);

  private static final String name = "Claim";
  private static final int minimumNumberOfArguments = 1;
  private static final String permissionNode = "reporter.claim";

  private static final List<Usage> usages =
      Collections.unmodifiableList(
          ArrayUtil.arrayToList(
              new Usage[] {new Usage(ClaimPhrases.claimHelp, ClaimPhrases.claimHelpDetails)}));
  private static final List<String> aliases = Collections.emptyList();

  /**
   * Constructor.
   *
   * @param manager The {@link ReporterCommandManager} managing this Command.
   */
  public ClaimCommand(final ReporterCommandManager manager) {
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
      final int index =
          getServiceModule()
              .getLastViewedReportService()
              .getIndexOrLastViewedReport(sender, args.get(0));

      getServiceModule().getReportValidatorService().requireReportIndexValid(index);

      if (!getServiceModule().getReportPermissionService().canAlterReport(sender, index)) {
        return;
      }

      claimReport(sender, index);
    } catch (final Exception e) {
      log.log(Level.ERROR, "Failed to claim report!", e);
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

  private void claimReport(final CommandSender sender, final int index)
      throws ClassNotFoundException, SQLException, InterruptedException {
    final List<String> params = new ArrayList<>();

    params.add("1");
    params.add(BukkitUtil.getUUIDString(sender));
    params.add(sender.getName());
    params.add(
        Integer.toString(getServiceModule().getPlayerService().getModLevel(sender).getLevel()));
    params.add(Reporter.getDateformat().format(new Date()));
    params.add(Integer.toString(index));

    final String query =
        "UPDATE Reports "
            + "SET ClaimStatus=?, ClaimedByUUID=?, ClaimedBy=?, ClaimPriority=?, ClaimDate=? "
            + "WHERE ID=?";

    final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
    final int connectionId = database.openPooledConnection();
    try {
      database.preparedUpdateQuery(connectionId, query, params);
    } catch (final SQLException e) {
      log.error(String.format("Failed to execute claim query on connection [%d]!", connectionId));
    } finally {
      database.closeConnection(connectionId);
    }

    String output = getManager().getLocale().getString(ClaimPhrases.reportClaimSuccess);

    output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);

    sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);

    if (BukkitUtil.isOfflinePlayer(sender)) {
      final OfflinePlayer senderPlayer = (OfflinePlayer) sender;

      getServiceModule().getModStatsService().incrementStat(senderPlayer, ModeratorStat.CLAIMED);
    }
  }
}
