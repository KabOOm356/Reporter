package net.KabOOm356.Util;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A class to help with Bukkit related activities.
 */
public class BukkitUtil
{
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
		if(isPlayer(player))
			return formatPlayerName((Player) player);
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
	 * Replaces all occurrences of Minecraft color codes with bukkit ones.
	 * 
	 * @param str The string to replace the color codes in.
	 * 
	 * @return A string with all the color codes in bukkit color codes.
	 */
	public static String colorCodeReplaceAll(String str)
	{
		return str.replaceAll("(&([a-f0-9]))", "\u00A7$2");
	}
}
