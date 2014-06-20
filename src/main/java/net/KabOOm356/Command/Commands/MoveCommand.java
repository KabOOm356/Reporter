package net.KabOOm356.Command.Commands;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.MovePhrases;
import net.KabOOm356.Manager.SQLStatManagers.ModeratorStatManager.ModeratorStat;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.Util;

/**
 * A {@link ReporterCommand} that will handle moving reports to different priorities.
 */
public class MoveCommand extends ReporterCommand
{
	private static final String name = "Move";
	private static final int minimumNumberOfArguments = 2;
	private final static String permissionNode = "reporter.move";
	
	/**
	 * Constructor.
	 * 
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public MoveCommand(ReporterCommandManager manager)
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
		
		if(!getManager().requireModLevelInBounds(sender, args.get(1)))
			return;
		
		ModLevel priority = ModLevel.getModLevel(args.get(1));
		
		moveReport(sender, index, priority);
	}
	
	protected void moveReport(CommandSender sender, int index, ModLevel level)
	{
		String query = "SELECT ClaimStatus, ClaimedByUUID, ClaimPriority "
				+ "FROM Reports "
				+ "WHERE ID=" + index;
		
		SQLResultSet result;
		
		try
		{
			result = getManager().getDatabaseHandler().sqlQuery(query);
			
			boolean isClaimed = result.getBoolean("ClaimStatus");
			int currentPriority = result.getInt("ClaimPriority");
			String claimedByUUIDString = result.getString("ClaimedByUUID");
			UUID claimedByUUID = null;
			
			if(!claimedByUUIDString.isEmpty())
			{
				claimedByUUID = UUID.fromString(claimedByUUIDString);
			}
			
			if(isClaimed && level.getLevel() > currentPriority)
			{
				// Clear the claim and upgrade the priority
				query = "UPDATE Reports " +
						"SET " +
						"ClaimStatus='0', " +
						"ClaimedByUUID='', " +
						"ClaimedBy='', " +
						"ClaimDate='', " +
						"ClaimPriority=0, " +
						"Priority=" + level.getLevel() + " " +
						"WHERE ID=" + index;
				
				Player claimingPlayer = null;
				
				if(claimedByUUID != null)
				{
					claimingPlayer = Bukkit.getPlayer(claimedByUUID);
				}
				
				if(claimingPlayer != null)
				{
					String playerName = BukkitUtil.formatPlayerName(sender);
					
					String output = getManager().getLocale().getString(MovePhrases.unassignedFromReport);
					
					output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.RED);
					output = output.replaceAll("%s", ChatColor.GOLD + playerName + ChatColor.RED);
					
					claimingPlayer.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.RED + output);
				}
				
			}
			else
			{
				query = "UPDATE Reports " +
						"SET " +
						"Priority = " + level.getLevel() + " " +
						"WHERE ID=" + index;
			}
			
			getManager().getDatabaseHandler().updateQuery(query);
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
		
		String output = getManager().getLocale().getString(MovePhrases.moveReportSuccess);
		
		ChatColor priorityColor = ModLevel.getModLevelColor(level);
		
		output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);
		output = output.replaceAll("%p", priorityColor + level.getName() + ChatColor.WHITE);
		
		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);
		
		if(BukkitUtil.isOfflinePlayer(sender))
		{
			OfflinePlayer senderPlayer = (OfflinePlayer) sender;
		
			getManager().getModStatsManager().incrementStat(senderPlayer, ModeratorStat.MOVED);
		}
	}

	@Override
	public void updateDocumentation()
	{
		super.updateDocumentation(
				getManager().getLocale().getString(MovePhrases.moveHelp),
				getManager().getLocale().getString(MovePhrases.moveHelpDetails));
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
