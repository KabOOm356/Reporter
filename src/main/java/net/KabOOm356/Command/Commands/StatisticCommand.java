package net.KabOOm356.Command.Commands;

import java.util.Collections;
import java.util.List;
import net.KabOOm356.Command.Help.Usage;
import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.StatisticPhrases;
import net.KabOOm356.Service.SQLStatService.SQLStat;
import net.KabOOm356.Service.SQLStatServices.ModeratorStatService.ModeratorStat;
import net.KabOOm356.Service.SQLStatServices.PlayerStatService.PlayerStat;
import net.KabOOm356.Util.ArrayUtil;
import net.KabOOm356.Util.BukkitUtil;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class StatisticCommand extends ReporterCommand {
  private static final String name = "Statistic";
  private static final int minimumNumberOfArguments = 1;
  private static final String permissionNode = "reporter.statistic.*";

  private static final List<Usage> usages =
      Collections.unmodifiableList(
          ArrayUtil.arrayToList(
              new Usage[] {
                new Usage(StatisticPhrases.statisticHelp, StatisticPhrases.statisticHelpDetails),
                new Usage("/report statistic/stat list", StatisticPhrases.statisticListHelpDetails),
                new Usage(
                    "/report statistic/stat <Player Name> all",
                    StatisticPhrases.statisticAllHelpDetails),
                new Usage(
                    "/report statistic/stat <Player Name> all mod|player",
                    StatisticPhrases.statisticAllModPlayerHelpDetails)
              }));
  private static final List<String> aliases =
      Collections.unmodifiableList(ArrayUtil.arrayToList(new String[] {"Stat"}));

  public StatisticCommand(final ReporterCommandManager manager) {
    super(manager, name, permissionNode, minimumNumberOfArguments);
  }

  private static String getStatisticNameString(final List<SQLStat> stats) {
    final StringBuilder stb = new StringBuilder();

    for (int LCV = 0; LCV < stats.size(); LCV++) {
      final SQLStat stat = stats.get(LCV);

      stb.append(ChatColor.GOLD).append(stat.getName());

      if (LCV != stats.size() - 1) {
        stb.append(ChatColor.WHITE).append(", ");
      }
    }

    return stb.toString();
  }

  private static String getStatisticPermission(final SQLStat statistic) {
    String permission = "reporter.statistic.read.*";

    if (statistic instanceof ModeratorStat) {
      permission = "reporter.statistic.read.mod";
    } else if (statistic instanceof PlayerStat) {
      permission = "reporter.statistic.read.player";
    }

    return permission;
  }

  @Override
  public void execute(final CommandSender sender, final List<String> args) {
    if (args.get(0).equalsIgnoreCase("list")) {
      if (getServiceModule()
          .getPermissionService()
          .hasPermission(sender, "reporter.statistic.list")) {
        listStatistics(sender);
      } else {
        sender.sendMessage(
            ChatColor.RED + getManager().getLocale().getString(GeneralPhrases.failedPermissions));
      }
    } else if (args.size() >= 2) {
      final SQLStat statistic = SQLStat.getByName(args.get(1));

      if (statistic == null) {
        String message = getManager().getLocale().getString(StatisticPhrases.notValidStatistic);
        sender.sendMessage(ChatColor.RED + message);

        message = getManager().getLocale().getString(StatisticPhrases.tryStatisticList);
        sender.sendMessage(ChatColor.GOLD + message);

        return;
      }

      final OfflinePlayer player = getManager().getPlayer(args.get(0));

      if (player == null) {
        final String message =
            getManager().getLocale().getString(GeneralPhrases.playerDoesNotExist);
        sender.sendMessage(ChatColor.RED + message);
        return;
      }

      if (statistic == SQLStat.ALL) {
        if (args.size() >= 3) {
          if (args.get(2).equalsIgnoreCase("mod")) {
            if (getServiceModule()
                .getPermissionService()
                .hasPermission(sender, "reporter.statistic.read.mod")) {
              displayAllModStatistics(sender, player);
            } else {
              sender.sendMessage(
                  ChatColor.RED
                      + getManager().getLocale().getString(GeneralPhrases.failedPermissions));
            }
          } else if (args.get(2).equalsIgnoreCase("player")) {
            if (getServiceModule()
                .getPermissionService()
                .hasPermission(sender, "reporter.statistic.read.player")) {
              displayAllPlayerStatistics(sender, player);
            } else {
              sender.sendMessage(
                  ChatColor.RED
                      + getManager().getLocale().getString(GeneralPhrases.failedPermissions));
            }
          } else {
            displayAllStatistics(sender, player);
          }
        } else {
          displayAllStatistics(sender, player);
        }
      } else // View an individual statistic.
      {
        // Check the permission for the individual statistic.
        final String permission = getStatisticPermission(statistic);

        if (!getServiceModule().getPermissionService().hasPermission(sender, permission)) {
          sender.sendMessage(
              ChatColor.RED + getManager().getLocale().getString(GeneralPhrases.failedPermissions));
          return;
        }

        displayStatistic(sender, player, statistic);
      }
    } else {
      sender.sendMessage(ChatColor.RED + getUsage());
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

  private void displayStatistic(
      final CommandSender sender, final OfflinePlayer player, final SQLStat statistic) {
    final ResultRow result = this.getStatistic(player, statistic);

    if (result.isEmpty() || result.getString(statistic.getColumnName()).isEmpty()) {
      String message = getManager().getLocale().getString(StatisticPhrases.noStatisticEntry);

      message = message.replaceAll("%s", ChatColor.GREEN + statistic.getName() + ChatColor.WHITE);
      message =
          message.replaceAll(
              "%p", ChatColor.BLUE + BukkitUtil.formatPlayerName(player) + ChatColor.WHITE);

      sender.sendMessage(ChatColor.WHITE + message);
    } else {
      String message = getManager().getLocale().getString(StatisticPhrases.displayStatistic);

      message = message.replaceAll("%s", ChatColor.GREEN + statistic.getName() + ChatColor.WHITE);
      message =
          message.replaceAll(
              "%p", ChatColor.BLUE + BukkitUtil.formatPlayerName(player) + ChatColor.WHITE);
      message =
          message.replaceAll(
              "%v", ChatColor.GOLD + result.getString(statistic.getColumnName()) + ChatColor.WHITE);

      sender.sendMessage(ChatColor.WHITE + message);
    }
  }

  private void displayAllStatistics(final CommandSender sender, final OfflinePlayer player) {
    displayAllModStatistics(sender, player);
    displayAllPlayerStatistics(sender, player);
  }

  private void displayAllModStatistics(final CommandSender sender, final OfflinePlayer player) {
    if (!getServiceModule()
        .getPermissionService()
        .hasPermission(sender, "reporter.statistic.read.mod")) {
      return;
    }

    final List<SQLStat> stats = SQLStat.getAll(ModeratorStat.class);

    for (final SQLStat stat : stats) {
      displayStatistic(sender, player, stat);
    }
  }

  private void displayAllPlayerStatistics(final CommandSender sender, final OfflinePlayer player) {
    if (!getServiceModule()
        .getPermissionService()
        .hasPermission(sender, "reporter.statistic.read.player")) {
      return;
    }

    final List<SQLStat> stats = SQLStat.getAll(PlayerStat.class);

    for (final SQLStat stat : stats) {
      displayStatistic(sender, player, stat);
    }
  }

  private void listStatistics(final CommandSender sender) {
    String playerStatsList =
        ChatColor.WHITE
            + getManager().getLocale().getString(StatisticPhrases.availablePlayerStatistics);
    String modStatsList =
        ChatColor.WHITE
            + getManager().getLocale().getString(StatisticPhrases.availableModeratorStatistics);

    List<SQLStat> stats = SQLStat.getAll(PlayerStat.class);

    playerStatsList = playerStatsList.replaceAll("%s", getStatisticNameString(stats));

    stats = SQLStat.getAll(ModeratorStat.class);

    modStatsList = modStatsList.replaceAll("%s", getStatisticNameString(stats));

    sender.sendMessage(playerStatsList);
    sender.sendMessage(modStatsList);
  }

  private ResultRow getStatistic(final OfflinePlayer player, final SQLStat statistic) {
    if (statistic instanceof ModeratorStat) {
      return getServiceModule().getModStatsService().getStat(player, statistic);
    } else if (statistic instanceof PlayerStat) {
      return getServiceModule().getPlayerStatsService().getStat(player, statistic);
    }

    return null;
  }
}
