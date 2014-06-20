package net.KabOOm356.Command.Commands;

import java.util.ArrayList;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Locale.Entry.LocalePhrases.AssignPhrases;
import net.KabOOm356.Manager.SQLStatManagers.ModeratorStatManager.ModeratorStat;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.Util;

/**
 * A {@link ReporterCommand} to handle assigning players to reports.
 */
public class AssignCommand extends ReporterCommand
{
	private final static String name = "Assign";
	private final static int minimumNumberOfArguments = 2;
	private final static String permissionNode = "reporter.assign";
	
	/**
	 * Constructor.
	 * 
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public AssignCommand(ReporterCommandManager manager)
	{
		super(manager, name, permissionNode, minimumNumberOfArguments);
		
		updateDocumentation();
	}

	@Override
	public void execute(CommandSender sender, ArrayList<String> args)
	{
		if(!hasRequiredPermission(sender))
			return;
		
		int index = Util.parseInt(args.get(0));
		
		if(args.get(0).equalsIgnoreCase("last"))
		{
			if(!hasRequiredLastViewed(sender))
				return;
			
			index = getLastViewed(sender);
		}
		
		if(!getManager().isReportIndexValid(sender, index))
			return;
		
		if(!getManager().canAlterReport(sender, index))
			return;
		
		Player player = Bukkit.getPlayer(args.get(1));
		
		if(canAssignReport(sender, index, player))
			assignReport(sender, index, player);
	}
	
	private void assignReport(CommandSender sender, int index, Player player)
	{
		String query = "UPDATE Reports SET ClaimStatus=?, ClaimDate=?, ClaimedBy=?, ClaimedByUUID=?, ClaimPriority=? WHERE ID=?";
		
		ArrayList<String> params = new ArrayList<String>();
		
		params.add(0, "1");
		params.add(1, Reporter.getDateformat().format(new Date()));
		params.add(2, player.getName());
		params.add(3, player.getUniqueId().toString());
		params.add(4, Integer.toString(getManager().getModLevel(player).getLevel()));
		params.add(5, Integer.toString(index));
		
		try
		{
			getManager().getDatabaseHandler().preparedUpdateQuery(query, params);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			sender.sendMessage(getErrorMessage());
			return;
		}
		finally
		{
			try
			{
				getManager().getDatabaseHandler().closeConnection();
			}
			catch(Exception e)
			{
			}
		}
		
		String playerName;
		
		playerName = ChatColor.BLUE + BukkitUtil.formatPlayerName(player) + ChatColor.WHITE;
		
		String output = getManager().getLocale().getString(AssignPhrases.assignSuccessful);
		
		output = output.replaceAll("%p", playerName);
		output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);
		
		sender.sendMessage(ChatColor.WHITE + output);
		
		playerName = ChatColor.BLUE + BukkitUtil.formatPlayerName(sender) + ChatColor.WHITE;
		
		output = getManager().getLocale().getString(AssignPhrases.assignedToReport);
		
		output = output.replaceAll("%p", playerName);
		output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);
		
		player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + output);
		
		if(BukkitUtil.isOfflinePlayer(sender))
		{
			OfflinePlayer senderPlayer = (OfflinePlayer) sender;
			
			getManager().getModStatsManager().incrementStat(senderPlayer, ModeratorStat.ASSIGNED);
		}
	}
	
	/**
	 * Checks if the given {@link CommandSender} can assign the given player to the report at the given index.
	 * 
	 * @param sender The CommandSender.
	 * @param index The index of the report.
	 * @param player The player to assign to the report.
	 * 
	 * @return True if the {@link CommandSender} can assign the player to the report, otherwise false.
	 */
	public boolean canAssignReport(CommandSender sender, int index, Player player)
	{
		String output;
		
		if(player == null)
		{
			output = getManager().getLocale().getString(AssignPhrases.assignedPlayerMustBeOnline);
			
			sender.sendMessage(ChatColor.RED + output);
			
			return false;
		}
		
		if(!getManager().canAlterReport(sender, index, player))
			return false;
		
		if(BukkitUtil.equals(sender, player))
		{
			output = getManager().getLocale().getString(AssignPhrases.useClaimToAssignSelf);
			
			sender.sendMessage(ChatColor.RED + output);
			
			return false;
		}
		
		ModLevel senderLevel = getManager().getModLevel(sender);
		ModLevel playerLevel = getManager().getModLevel(player);
		
		boolean senderHasLowerModLevel = senderLevel.getLevel() <= playerLevel.getLevel();
		boolean senderIsConsoleOrOp = sender.isOp() || sender instanceof ConsoleCommandSender;
		
		if(!senderIsConsoleOrOp && senderHasLowerModLevel)
		{
			output = getManager().getLocale().getString(AssignPhrases.cannotAssignHigherPriority);
			
			sender.sendMessage(ChatColor.RED + output);
			
			output = getManager().getLocale().getString(AssignPhrases.playerPriority);
			
			output = output.replaceAll("%p", ChatColor.BLUE + player.getDisplayName() + ChatColor.GOLD + " (" + player.getName() + ")" + ChatColor.WHITE);
			output = output.replaceAll("%m", playerLevel.getColor() + playerLevel.getName() + ChatColor.WHITE);
			
			sender.sendMessage(ChatColor.WHITE + output);
			getManager().displayModLevel(sender);
			
			return false;
		}
		
		if(!getManager().requirePriority(sender, index, player))
			return false;
		
		return true;
	}
	
	@Override
	public void updateDocumentation()
	{
		super.updateDocumentation(
				getManager().getLocale().getString(AssignPhrases.assignHelp),
				getManager().getLocale().getString(AssignPhrases.assignHelpDetails));
	}
	
	/**
	 * Returns the name of this command.
	 * 
	 * @return The name of this command.
	 */
	public static String getCommandName()
	{
		return name;
	}
	
	/**
	 * Returns the permission node of this command.
	 * 
	 * @return The permission node of this command.
	 */
	public static String getCommandPermissionNode()
	{
		return permissionNode;
	}
}
