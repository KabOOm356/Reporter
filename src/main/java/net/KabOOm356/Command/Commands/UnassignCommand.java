package net.KabOOm356.Command.Commands;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.UnassignPhrases;
import net.KabOOm356.Manager.SQLStatManagers.ModeratorStatManager.ModeratorStat;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.Util;

/**
 * A {@link ReporterCommand} that will handle unassigning players from reports..
 */
public class UnassignCommand extends ReporterCommand
{
	private static final Logger log = LogManager.getLogger(UnassignCommand.class);
	
	private static final String name = "Unassign";
	private static final int minimumNumberOfArguments = 1;
	private static final String permissionNode = "reporter.unassign";
	
	/**
	 * Constructor.
	 * 
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public UnassignCommand(ReporterCommandManager manager)
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
		
		unassignReport(sender, index);
	}

	private void unassignReport(CommandSender sender, int index)
	{
		// LOW Long String concatenation.
		String query = "SELECT ClaimedByUUID, ClaimedBy FROM Reports WHERE ID=" + index;
		String claimedByUUID, claimedBy;
		
		try
		{
			SQLResultSet result = getManager().getDatabaseHandler().sqlQuery(query);
			
			claimedByUUID = result.getString("ClaimedByUUID");
			claimedBy = result.getString("ClaimedBy");
			
			query = "UPDATE Reports " +
					"SET " +
					"ClaimStatus=0, ClaimedByUUID='', ClaimedBy='', ClaimPriority=0, ClaimDate='' " +
					"WHERE ID=" + index;
			
			getManager().getDatabaseHandler().updateQuery(query);
		}
		catch(final Exception e)
		{
			log.log(Level.ERROR, "Failed to unassign report!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}
		finally
		{
			getManager().getDatabaseHandler().closeConnection();
		}
		
		String playerName = claimedBy;
		OfflinePlayer claimingPlayer = null;
		
		if(!claimedByUUID.isEmpty())
		{
			UUID uuid = UUID.fromString(claimedByUUID);
			
			claimingPlayer = Bukkit.getOfflinePlayer(uuid);
			
			playerName = BukkitUtil.formatPlayerName(claimingPlayer);
		}
		
		String output = getManager().getLocale().getString(UnassignPhrases.reportUnassignSuccess);
		
		output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);
		output = output.replaceAll("%p", ChatColor.GOLD + playerName + ChatColor.WHITE);
		
		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);
		
		if(BukkitUtil.isOfflinePlayer(sender))
		{
			OfflinePlayer senderPlayer = (OfflinePlayer) sender;
		
			getManager().getModStatsManager().incrementStat(senderPlayer, ModeratorStat.UNASSIGNED);
		}
		
		if(claimingPlayer != null && claimingPlayer.isOnline())
		{
			playerName = BukkitUtil.formatPlayerName(sender);
			
			output = getManager().getLocale().getString(UnassignPhrases.unassignedFromReport);
			
			output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.RED);
			output = output.replaceAll("%s", ChatColor.GOLD + playerName + ChatColor.RED);
			
			claimingPlayer.getPlayer().sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.RED + output);
		}
	}

	
	@Override
	public void updateDocumentation()
	{
		super.updateDocumentation(
				getManager().getLocale().getString(UnassignPhrases.unassignHelp),
				getManager().getLocale().getString(UnassignPhrases.unassignHelpDetails));
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
