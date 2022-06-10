package net.KabOOm356.Service;

import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Util.BukkitUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class PlayerService extends Service {
  private static final String highModLevelPermission = "reporter.modlevel.high";
  private static final String normalModLevelPermission = "reporter.modlevel.normal";
  private static final String lowModLevelPermission = "reporter.modlevel.low";

  protected PlayerService(final ServiceModule module) {
    super(module);
  }

  /**
   * Returns the {@link ModLevel} of the given {@link CommandSender}.
   *
   * @param sender The {@link CommandSender} to get the {@link ModLevel} for.
   * @return The {@link ModLevel} of the given {@link CommandSender}.
   */
  public ModLevel getModLevel(final CommandSender sender) {
    if (sender.isOp()) {
      return ModLevel.HIGH;
    } else if (sender instanceof ConsoleCommandSender) {
      return ModLevel.HIGH;
    } else {
      if (BukkitUtil.isPlayer(sender)) {
        final Player player = (Player) sender;
        if (hasPermission(player, highModLevelPermission)) {
          return ModLevel.HIGH;
        } else if (hasPermission(player, normalModLevelPermission)) {
          return ModLevel.NORMAL;
        } else if (hasPermission(player, lowModLevelPermission)) {
          return ModLevel.LOW;
        }
      }
    }
    return ModLevel.NONE;
  }

  /**
   * Checks if the given String is a {@link ModLevel}, if it is not the given {@link CommandSender}
   * is alerted.
   *
   * @param sender The {@link CommandSender} to alert if the String is not a {@link ModLevel} or not
   *     in bounds.
   * @param modLevel The String representation of the {@link ModLevel} to check if is in bounds.
   * @return True if the String is a {@link ModLevel} and in bounds, otherwise false.
   */
  public boolean requireModLevelInBounds(final CommandSender sender, final String modLevel) {
    if (ModLevel.modLevelInBounds(modLevel)) {
      return true;
    }
    sender.sendMessage(
        ChatColor.RED + getLocale().getString(GeneralPhrases.priorityLevelNotInBounds));
    return false;
  }

  /**
   * Displays the current {@link ModLevel} to the given {@link CommandSender}.
   *
   * @param sender The {@link CommandSender} to display their {@link ModLevel} to.
   */
  public void displayModLevel(final CommandSender sender) {
    final ModLevel level = getModLevel(sender);
    final String output =
        getLocale()
            .getString(GeneralPhrases.displayModLevel)
            .replaceAll("%m", level.getColor() + level.getName() + ChatColor.WHITE);
    sender.sendMessage(ChatColor.WHITE + output);
  }

  public void displayModLevel(final CommandSender sender, final CommandSender player) {
    final String playerName = BukkitUtil.formatPlayerName(player);
    final ModLevel level = getModLevel(player);
    final String output =
        getLocale()
            .getString(GeneralPhrases.displayOtherModLevel)
            .replaceAll("%p", ChatColor.BLUE + playerName + ChatColor.WHITE)
            .replaceAll("%m", level.getColor() + level.getName() + ChatColor.WHITE);
    sender.sendMessage(ChatColor.WHITE + output);
  }

  private boolean hasPermission(final Player sender, final String permission) {
    return getModule().getPermissionService().hasPermission(sender, permission);
  }

  private Locale getLocale() {
    return getStore().getLocaleStore().get();
  }

  public static String getHighModLevelPermission() {
    return highModLevelPermission;
  }

  public static String getNormalModLevelPermission() {
    return normalModLevelPermission;
  }

  public static String getLowModLevelPermission() {
    return lowModLevelPermission;
  }
}
