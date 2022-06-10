package net.KabOOm356.Util;

import com.google.common.base.Charsets;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

/** A class to help with Bukkit related activities. */
public final class BukkitUtil {
  /** A UUID that Bukkit assigns to players without a valid username. */
  public static final UUID invalidUserUUID =
      UUID.nameUUIDFromBytes("InvalidUsername".getBytes(Charsets.UTF_8));

  /** A {@link Pattern} that a valid Minecraft username must match. */
  public static final Pattern validUsernamePattern = Pattern.compile("^[a-zA-Z0-9_]{2,32}$");

  public static final String BUKKIT_COLOR_CODE_PATTERN = "(&([a-f0-9]))";
  public static final String BUKKIT_COLOR_CODE_REPLACEMENT = "\u00A7$2";
  public static final OfflinePlayer anonymousPlayer = Bukkit.getOfflinePlayer("* (Anonymous)");

  private static final long serverTicksPerSecond = 20L;
  private static final String realPlayerNameFormat = "%s " + ChatColor.GOLD + "(%s)";

  private BukkitUtil() {}

  /**
   * Returns the given player's name in a custom display format. <br>
   * If the display name does not contain the real name the format is: <br>
   * <i>displayName (realName)</i>
   *
   * @param player The player whose name will be formatted.
   * @return The given player's name in a custom display format.
   */
  public static String formatPlayerName(final CommandSender player) {
    if (isOfflinePlayer(player)) {
      return formatPlayerName((OfflinePlayer) player);
    }
    return player.getName();
  }

  /**
   * Returns the given player's name in a custom display format. <br>
   * If the display name does not contain the real name the format is: <br>
   * <i>displayName (realName)</i>
   *
   * @param player The player whose name will be formatted.
   * @param displayRealName If true the player name will be formatted even if the display name
   *     contains the real name.
   * @return The given player's name in a custom display format.
   */
  public static String formatPlayerName(final CommandSender player, final boolean displayRealName) {
    if (isOfflinePlayer(player)) {
      return formatPlayerName((OfflinePlayer) player, displayRealName);
    }
    return player.getName();
  }

  /**
   * Returns the given player's name in a custom display format. <br>
   * If the display name does not contain the real name the format is: <br>
   * <i>displayName (realName)</i>
   *
   * @param player The player whose name will be formatted.
   * @return The given player's name in a custom display format.
   */
  public static String formatPlayerName(final OfflinePlayer player) {
    if (player.isOnline() && player.getPlayer() != null) {
      return formatPlayerName(player.getPlayer());
    }
    return player.getName();
  }

  /**
   * Returns the given player's name in a custom display format. <br>
   * If the display name does not contain the real name the format is: <br>
   * <i>displayName (realName)</i>
   *
   * @param player The player whose name will be formatted.
   * @param displayRealName If true the player name will be formatted even if the display name
   *     contains the real name.
   * @return The given player's name in a custom display format.
   */
  public static String formatPlayerName(final OfflinePlayer player, final boolean displayRealName) {
    if (player.isOnline() && player.getPlayer() != null) {
      return formatPlayerName(player.getPlayer(), displayRealName);
    }
    return player.getName();
  }

  /**
   * Returns the given player's name in a custom display format. <br>
   * If the display name does not contain the real name the format is: <br>
   * <i>displayName (realName)</i>
   *
   * @param player The player whose name will be formatted.
   * @param displayRealName If true the player name will be formatted even if the display name
   *     contains the real name.
   * @return The given player's name in a custom display format.
   */
  public static String formatPlayerName(final Player player, final boolean displayRealName) {
    final String displayName = player.getDisplayName();
    return formatPlayerName(displayName, player.getName(), displayRealName);
  }

  /**
   * Returns the given player's name in a custom display format. <br>
   * If the display name does not contain the real name the format is: <br>
   * <i>displayName (realName)</i>
   *
   * @param player The player whose name will be formatted.
   * @return The given player's name in a custom display format.
   */
  public static String formatPlayerName(final Player player) {
    final String displayName = player.getDisplayName();
    return formatPlayerName(displayName, player.getName());
  }

  /**
   * Returns the given player's name in a custom display format. <br>
   * If the display name does not contain the real name the format is: <br>
   * <i>displayName (realName)</i>
   *
   * @param displayName The display name of the player.
   * @param realName The real name of the player.
   * @return The display name and real name in a custom format.
   */
  public static String formatPlayerName(final String displayName, final String realName) {
    if (!displayName.contains(realName)) {
      return String.format(realPlayerNameFormat, displayName, realName);
    }
    return displayName;
  }

  /**
   * Returns the given player's name in a custom display format. <br>
   * If the display name does not contain the real name, or showRealName is true, the format is:
   * <br>
   * <i>displayName (realName)</i>
   *
   * @param displayName The display name of the player.
   * @param realName The real name of the player.
   * @param displayRealName If true the player name will be formatted even if the display name
   *     contains the real name.
   * @return The display name and real name in a custom format.
   */
  public static String formatPlayerName(
      final String displayName, final String realName, final boolean displayRealName) {
    if (displayRealName) {
      return String.format(realPlayerNameFormat, displayName, realName);
    }
    return formatPlayerName(displayName, realName);
  }

  /**
   * Returns if the given player has a valid UUID.
   *
   * @param player The player.
   * @return True if the player has a valid UUID, otherwise false.
   * @see #invalidUserUUID
   */
  public static boolean isPlayerValid(final OfflinePlayer player) {
    return !player.getUniqueId().equals(invalidUserUUID);
  }

  /**
   * Returns if the given UUID is valid for a user.
   *
   * @param uuid The UUID.
   * @return True if the UUID is a valid player, otherwise false.
   */
  public static boolean isPlayerIdValid(final UUID uuid) {
    return !invalidUserUUID.equals(uuid);
  }

  /**
   * Returns if the given Minecraft username is valid or not.
   *
   * @param name The username to check.
   * @return True if the username is valid, otherwise false.
   */
  public static boolean isUsernameValid(final String name) {
    final Matcher matcher = validUsernamePattern.matcher(name);
    return matcher.matches();
  }

  /**
   * Returns if the given {@link CommandSender} is an instance of a {@link
   * org.bukkit.entity.Player}.
   *
   * @param cs The {@link CommandSender} to check.
   * @return Whether the given {@link CommandSender} is an instance of a {@link
   *     org.bukkit.entity.Player}.
   */
  public static boolean isPlayer(final CommandSender cs) {
    return cs instanceof org.bukkit.entity.Player;
  }

  /**
   * Returns if the given {@link CommandSender} is an instance of an {@link
   * org.bukkit.OfflinePlayer}.
   *
   * @param cs The {@link CommandSender} to check.
   * @return Whether the given {@link CommandSender} is an instance of an {@link
   *     org.bukkit.OfflinePlayer}.
   */
  public static boolean isOfflinePlayer(final CommandSender cs) {
    return cs instanceof org.bukkit.OfflinePlayer;
  }

  /**
   * Checks if the two given {@link CommandSender}s are equal. <br>
   * <br>
   * If UUID comparison is possible, it is used. If UUID comparison is not possible, name based
   * comparison is used.
   *
   * @param commandSender A {@link CommandSender} to compare.
   * @param commandSender2 The other {@link CommandSender} to compare.
   * @return True if the {@link CommandSender}'s are equal, otherwise false.
   */
  public static boolean playersEqual(
      final CommandSender commandSender, final CommandSender commandSender2) {
    if (commandSender == null || commandSender2 == null) {
      return false;
    }
    if (isOfflinePlayer(commandSender) && isOfflinePlayer(commandSender2)) {
      final OfflinePlayer player = (OfflinePlayer) commandSender;
      final OfflinePlayer player2 = (OfflinePlayer) commandSender2;
      final UUID senderUUID = player.getUniqueId();
      final UUID senderUUID2 = player2.getUniqueId();
      if (senderUUID != null && senderUUID.equals(senderUUID2)) {
        return true;
      }
    }
    final String senderName = commandSender.getName();
    final String senderName2 = commandSender2.getName();
    return senderName != null && senderName.equals(senderName2);
  }

  /**
   * Replaces all occurrences of Minecraft color codes with bukkit ones.
   *
   * @param str The string to replace the color codes in.
   * @return A string with all the color codes in bukkit color codes.
   */
  public static String colorCodeReplaceAll(final String str) {
    return str.replaceAll(BUKKIT_COLOR_CODE_PATTERN, BUKKIT_COLOR_CODE_REPLACEMENT);
  }

  /**
   * Returns a String representation of the sender's UUID.
   *
   * @param sender The {@link CommandSender} to get the UUID from.
   * @return If the sender is a valid player a String representation of the sender's UUID, otherwise
   *     an empty String ("").
   */
  public static String getUUIDString(final CommandSender sender) {
    if (isOfflinePlayer(sender)) {
      final OfflinePlayer player = (OfflinePlayer) sender;
      return getUUIDString(player);
    }
    return "";
  }

  /**
   * Return a String representation of the player's UUID.
   *
   * @param player The {@link OfflinePlayer} to get the UUID from.
   * @return If the player is valid a String representation of the player's UUID, otherwise an empty
   *     String ("").
   */
  public static String getUUIDString(final OfflinePlayer player) {
    if (isUsernameValid(player.getName()) && isPlayerValid(player)) {
      return player.getUniqueId().toString();
    }
    return "";
  }

  /**
   * Gets an offline player by UUID or name if the UUID is not valid or null.
   *
   * @param uuid The UUID.
   * @param name The name of the player.
   * @return An {@link OfflinePlayer} by UUID or by name if the UUID is not valid or null. If the
   *     UUID is not valid and the name is null or empty, null is returned.
   */
  public static OfflinePlayer getOfflinePlayer(final UUID uuid, final String name) {
    if (uuid != null && isPlayerIdValid(uuid)) {
      return Bukkit.getOfflinePlayer(uuid);
    }
    // Getting the player by UUID failed, try getting by name
    if (name == null || name.isEmpty()) {
      return null;
    }
    return Bukkit.getOfflinePlayer(name);
  }

  /**
   * Gets an offline player by name.
   *
   * @param playerName The name of the player
   * @return An {@link OfflinePlayer} with the given name. If the name is null or empty, null is
   *     returned.
   * @see BukkitUtil#getOfflinePlayer(UUID, String)
   */
  public static OfflinePlayer getOfflinePlayer(final String playerName) {
    return getOfflinePlayer(null, playerName);
  }

  /**
   * Returns a {@link OfflinePlayer} object that has a name that most closely resembles the given
   * player name. <br>
   * <br>
   * <b>NOTE:</b> If the given player name is '!' or '*', a {@link OfflinePlayer} is still returned
   * and not null.
   *
   * @param playerName The name of the {@link OfflinePlayer}.
   * @return An {@link OfflinePlayer} that most with a name that most closely resembles the given
   *     player name, if no player matches null.
   * @see org.bukkit.Server#getPlayer(String)
   * @see org.bukkit.Server#getOfflinePlayer(String)
   * @deprecated Deprecated due to dependency deprecation.
   */
  @Deprecated
  public static OfflinePlayer getPlayer(
      final String playerName, final boolean matchPartialOfflineUsernames) {
    if (BukkitUtil.isUsernameValid(playerName)) {
      // Attempt to get an online player.
      OfflinePlayer player = Bukkit.getPlayer(playerName);

      if (player == null) {
        // Attempt to get an OfflinePlayer with an exact name.
        player = Bukkit.getOfflinePlayer(playerName);

        // If the OfflinePlayer has not played before.
        if (!player.hasPlayedBefore()) {
          // If the configuration allows for partial username matching, match the player.
          if (matchPartialOfflineUsernames) {
            player = matchOfflinePlayer(playerName);
          } else {
            player = null;
          }
        }
      }
      return player;
    } else if (playerName.equalsIgnoreCase("!") || playerName.equalsIgnoreCase("*")) {
      return anonymousPlayer;
    }

    return null;
  }

  /**
   * Returns a {@link OfflinePlayer} whose name most closely matches the given player name.
   *
   * @param playerName The player name to get
   * @return The {@link OfflinePlayer} whose name most closely matches the given player name if one
   *     can be matched, otherwise null.
   */
  public static OfflinePlayer matchOfflinePlayer(final String playerName) {
    OfflinePlayer player = null;

    final String lowerName = playerName.toLowerCase();
    int delta = Integer.MAX_VALUE;

    for (final OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
      if (offlinePlayer != null && offlinePlayer.getName() != null) {
        if (offlinePlayer.getName().toLowerCase().startsWith(lowerName)) {
          final int currentDelta = offlinePlayer.getName().length() - lowerName.length();

          if (currentDelta < delta) {
            player = offlinePlayer;
            delta = currentDelta;
          }

          if (currentDelta == 0) {
            return player;
          }
        }
      }
    }
    return player;
  }

  /**
   * Gets a Bukkit plugin.
   *
   * @param plugin The name of the plugin to get.
   * @return A Bukkit plugin.
   */
  public static Plugin getPlugin(final String plugin) {
    if (!Bukkit.getPluginManager().isPluginEnabled(plugin)) {
      throw new IllegalPluginAccessException(
          String.format("The plugin [%s] is not enabled!", plugin));
    }
    return Bukkit.getPluginManager().getPlugin(plugin);
  }

  /**
   * Converts seconds to server ticks.
   *
   * @param seconds The number of seconds.
   * @return The number of server ticks.
   */
  public static Long convertSecondsToServerTicks(final int seconds) {
    Validate.isTrue(seconds >= 0, "Number of seconds must be positive!");
    return seconds * serverTicksPerSecond;
  }
}
