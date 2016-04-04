package net.KabOOm356.Util;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to help with Bukkit related activities.
 */
public class BukkitUtil
{
	/**
	 * A UUID that Bukkit assigns to players without a valid username.
	 */
	public static final UUID invalidUserUUID = UUID.nameUUIDFromBytes("InvalidUsername".getBytes(Charsets.UTF_8));
	
	/**
	 * A {@link Pattern} that a valid Minecraft username must match.
	 */
	public static final Pattern validUsernamePattern = Pattern.compile("^[a-zA-Z0-9_]{2,32}$");
	
	public static final String BUKKIT_COLOR_CODE_PATTERN = "(&([a-f0-9]))";
	public static final String BUKKIT_COLOR_CODE_REPLACEMENT = "\u00A7$2";
	
	/**
	 * Returns the given player's name in a custom display format.
	 * <br />
	 * If the display name does not contain the real name the format is:
	 * <br />
	 * <i>displayName (realName)</i>
	 * 
	 * @param player The player whose name will be formatted.
	 * 
	 * @return The given player's name in a custom display format.
	 */
	public static String formatPlayerName(CommandSender player)
	{
		if(isOfflinePlayer(player))
			return formatPlayerName((OfflinePlayer) player);
		return player.getName();
	}
	
	/**
	 * Returns the given player's name in a custom display format.
	 * <br />
	 * If the display name does not contain the real name the format is:
	 * <br />
	 * <i>displayName (realName)</i>
	 * 
	 * @param player The player whose name will be formatted.
	 * @param displayRealName If true the player name will be formatted even if the display name contains the real name.
	 * 
	 * @return The given player's name in a custom display format.
	 */
	public static String formatPlayerName(CommandSender player, boolean displayRealName)
	{
		if(isOfflinePlayer(player))
			return formatPlayerName((OfflinePlayer) player, displayRealName);
		return player.getName();
	}
	
	/**
	 * Returns the given player's name in a custom display format.
	 * <br />
	 * If the display name does not contain the real name the format is:
	 * <br />
	 * <i>displayName (realName)</i>
	 * 
	 * @param player The player whose name will be formatted.
	 * 
	 * @return The given player's name in a custom display format.
	 */
	public static String formatPlayerName(OfflinePlayer player)
	{
		if(player.isOnline())
			return formatPlayerName(player.getPlayer());
		return player.getName();
	}
	
	/**
	 * Returns the given player's name in a custom display format.
	 * <br />
	 * If the display name does not contain the real name the format is:
	 * <br />
	 * <i>displayName (realName)</i>
	 * 
	 * @param player The player whose name will be formatted.
	 * @param displayRealName If true the player name will be formatted even if the display name contains the real name.
	 * 
	 * @return The given player's name in a custom display format.
	 */
	public static String formatPlayerName(OfflinePlayer player, boolean displayRealName)
	{
		if(player.isOnline())
			return formatPlayerName(player.getPlayer(), displayRealName);
		return player.getName();
	}
	
	/**
	 * Returns the given player's name in a custom display format.
	 * <br />
	 * If the display name does not contain the real name the format is:
	 * <br />
	 * <i>displayName (realName)</i>
	 * 
	 * @param player The player whose name will be formatted.
	 * @param displayRealName If true the player name will be formatted even if the display name contains the real name.
	 * 
	 * @return The given player's name in a custom display format.
	 */
	public static String formatPlayerName(Player player, boolean displayRealName)
	{
		String displayName = player.getDisplayName();
		
		return formatPlayerName(displayName, player.getName(), displayRealName);
	}
	
	/**
	 * Returns the given player's name in a custom display format.
	 * <br />
	 * If the display name does not contain the real name the format is:
	 * <br />
	 * <i>displayName (realName)</i>
	 * 
	 * @param player The player whose name will be formatted.
	 * 
	 * @return The given player's name in a custom display format.
	 */
	public static String formatPlayerName(Player player)
	{
		String displayName = player.getDisplayName();
		
		return formatPlayerName(displayName, player.getName());
	}
	
	/**
	 * Returns the given player's name in a custom display format.
	 * <br />
	 * If the display name does not contain the real name the format is:
	 * <br />
	 * <i>displayName (realName)</i>
	 * 
	 * @param displayName The display name of the player.
	 * @param realName The real name of the player.
	 * 
	 * @return The display name and real name in a custom format.
	 */
	public static String formatPlayerName(String displayName, String realName)
	{
		if(!displayName.contains(realName))
			return displayName + ChatColor.GOLD + " (" + realName + ")";
		
		return displayName;
	}
	
	/**
	 * Returns the given player's name in a custom display format.
	 * <br />
	 * If the display name does not contain the real name, or showRealName is true, the format is:
	 * <br />
	 * <i>displayName (realName)</i>
	 * 
	 * @param displayName The display name of the player.
	 * @param realName The real name of the player.
	 * @param displayRealName If true the player name will be formatted even if the display name contains the real name.
	 * 
	 * @return The display name and real name in a custom format.
	 */
	public static String formatPlayerName(String displayName, String realName, boolean displayRealName)
	{
		if(displayRealName)
			return displayName + ChatColor.GOLD + " (" + realName + ")";
		return formatPlayerName(displayName, realName);
	}
	
	/**
	 * Returns if the given player has a valid UUID.
	 * 
	 * @param player The player.
	 * 
	 * @return True if the player has a valid UUID, otherwise false.
	 * 
	 * @see #invalidUserUUID
	 */
	public static boolean isPlayerValid(OfflinePlayer player)
	{
		return !player.getUniqueId().equals(invalidUserUUID);
	}

	/**
	 * Returns if the given UUID is valid for a user.
	 *
	 * @param uuid The UUID.
	 *
	 * @return True if the UUID is a valid player, otherwise false.
	 */
	public static boolean isPlayerValid(final UUID uuid) {
		return !invalidUserUUID.equals(uuid);
	}
	
	/**
	 * Returns if the given Minecraft username is valid or not.
	 * 
	 * @param name The username to check.
	 * 
	 * @return True if the username is valid, otherwise false.
	 */
	public static boolean isUsernameValid(String name)
	{
		Matcher matcher = validUsernamePattern.matcher(name);
		
		return matcher.matches();
	}
	
	/**
	 * Returns if the given {@link CommandSender} is an instance of a {@link org.bukkit.entity.Player}.
	 * 
	 * @param cs The {@link CommandSender} to check.
	 * 
	 * @return Whether the given {@link CommandSender} is an instance of a {@link org.bukkit.entity.Player}.
	 */
	public static boolean isPlayer(CommandSender cs)
	{
		return cs instanceof org.bukkit.entity.Player;
	}
	
	/**
	 * Returns if the given {@link CommandSender} is an instance of an {@link org.bukkit.OfflinePlayer}.
	 * 
	 * @param cs The {@link CommandSender} to check.
	 * 
	 * @return Whether the given {@link CommandSender} is an instance of an {@link org.bukkit.OfflinePlayer}.
	 */
	public static boolean isOfflinePlayer(CommandSender cs)
	{
		return cs instanceof org.bukkit.OfflinePlayer;
	}
	
	/**
	 * Checks if the two given {@link CommandSender}s are equal.
	 * <br /><br />
	 * If UUID comparison is possible, it is used.
	 * If UUID comparison is not possible, name based comparison is used.
	 * 
	 * @param commandSender A {@link CommandSender} to compare.
	 * @param commandSender2 The other {@link CommandSender} to compare.
	 * 
	 * @return True if the {@link CommandSender}'s are equal, otherwise false.
	 */
	public static boolean playersEqual(CommandSender commandSender, CommandSender commandSender2) {
		if (commandSender == null || commandSender2 == null) {
			return false;
		}
		if (isOfflinePlayer(commandSender) && isOfflinePlayer(commandSender2)) {
			final OfflinePlayer player = (OfflinePlayer) commandSender;
			final OfflinePlayer player2 = (OfflinePlayer) commandSender2;
			final UUID senderUUID = player.getUniqueId();
			final UUID senderUUID2 = player2.getUniqueId();
			if (senderUUID != null && senderUUID2 != null && senderUUID.equals(senderUUID2)) {
				return true;
			}
		}
		final String senderName = commandSender.getName();
		final String senderName2 = commandSender2.getName();
		if (senderName != null && commandSender2 != null && senderName.equals(senderName2)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the {@link OfflinePlayer} has the given {@link UUID}.
	 * 
	 * @param player The {@link OfflinePlayer}.
	 * @param uuid The {@link UUID}.
	 * 
	 * @return True if the {@link OfflinePlayer} has the given {@link UUID}.
	 */
	public static boolean playersEqual(OfflinePlayer player, UUID uuid)
	{
		return player.getUniqueId().equals(uuid);
	}
	
	/**
	 * Replaces all occurrences of Minecraft color codes with bukkit ones.
	 * 
	 * @param str The string to replace the color codes in.
	 * 
	 * @return A string with all the color codes in bukkit color codes.
	 */
	public static String colorCodeReplaceAll(String str)
	{
		return str.replaceAll(BUKKIT_COLOR_CODE_PATTERN, BUKKIT_COLOR_CODE_REPLACEMENT);
	}
	
	/**
	 * Returns a String representation of the sender's UUID.
	 * 
	 * @param sender The {@link CommandSender} to get the UUID from.
	 * 
	 * @return If the sender is a valid player a String representation of the
	 * sender's UUID, otherwise an empty String ("").
	 */
	public static String getUUIDString(CommandSender sender)
	{
		if(isOfflinePlayer(sender))
		{
			OfflinePlayer player = (OfflinePlayer) sender;
			
			return getUUIDString(player);
		}
		
		return "";
	}
	
	/**
	 * Return a String representation of the player's UUID.
	 * 
	 * @param player The {@link OfflinePlayer} to get the UUID from.
	 * 
	 * @return If the player is valid a String representation of the
	 * player's UUID, otherwise an empty String ("").
	 */
	public static String getUUIDString(OfflinePlayer player)
	{
		if(isUsernameValid(player.getName()) && isPlayerValid(player))
		{
			return player.getUniqueId().toString();
		}
		
		return "";
	}
	
	/**
	 * Returns the UUID of the given {@link CommandSender}.
	 * 
	 * @param sender The {@link CommandSender} to get the UUID of.
	 * 
	 * @return The UUID of the sender if the sender has a UUID, otherwise null.
	 */
	public static UUID getUUID(final CommandSender sender) {
		if(isOfflinePlayer(sender)) {
			OfflinePlayer player = (OfflinePlayer) sender;
			return player.getUniqueId();
		}
		return null;
	}

    /**
     * Gets an offline player by UUID or name if the UUID is not valid or null.
     *
     * @param uuid The UUID.
     * @param name The name of the player.
     *
     * @return An {@link OfflinePlayer} by UUID or by name if the UUID is not valid or null.  If the UUID is not valid and the name is null or empty, null is returned.
     */
	public static OfflinePlayer getOfflinePlayer(final UUID uuid, final String name) {
		if (uuid != null && isPlayerValid(uuid)) {
			return Bukkit.getOfflinePlayer(uuid);
		}
        // Getting the player by UUID failed, try getting by name
        if (name == null || name.isEmpty()) {
            return null;
        }
		return Bukkit.getOfflinePlayer(name);
	}
}
