package net.KabOOm356.Command.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.UnclaimPhrases;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.Util;

/**
 * A {@link ReporterCommand} that will handle unclaiming reports.
 */
public class UnclaimCommand extends ReporterCommand
{
	private static final String name = "Unclaim";
	private static final int minimumNumberOfArguments = 1;
	private final static String permissionNode = "reporter.claim";
	
	/**
	 * Constructor.
	 * 
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public UnclaimCommand(ReporterCommandManager manager)
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
		
		if(canUnclaimReport(sender, index))
			unclaimReport(sender, index);
	}
	
	private boolean canUnclaimReport(CommandSender sender, int index)
	{
		String query = "SELECT ClaimStatus, ClaimedBy, ClaimedByRaw FROM Reports WHERE ID=" + index;
		
		try
		{
			SQLResultSet result = getManager().getDatabaseHandler().sqlQuery(query);
			
			if(result.getBoolean("ClaimStatus"))
			{
				if(!result.getString("ClaimedByRaw").equals(sender.getName()))
				{
					String output = getManager().getLocale().getString(UnclaimPhrases.reportAlreadyClaimed);
					
					String claimedBy = result.getString("ClaimedBy") +
							ChatColor.GOLD + " (" +
							result.getString("ClaimedByRaw") +
							")";
					
					output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.RED);
					output = output.replaceAll("%c", ChatColor.BLUE + claimedBy + ChatColor.RED);
					
					sender.sendMessage(ChatColor.RED + output);
					
					return false;
				}
			}
			else
			{
				String output = getManager().getLocale().getString(UnclaimPhrases.reportIsNotClaimed);
				
				output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.RED);
				
				sender.sendMessage(ChatColor.RED + output);
				
				return false;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			sender.sendMessage(getErrorMessage());
			return false;
		}
		finally
		{
		}
		
		return true;
	}
	
	private void unclaimReport(CommandSender sender, int index)
	{
		String query = "UPDATE Reports " +
				"SET " +
				"ClaimStatus=0, ClaimedBy='', ClaimedByRaw='', ClaimPriority=0, ClaimDate='' " +
				"WHERE ID=" + index;
		
		try
		{
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
		
		String output = getManager().getLocale().getString(UnclaimPhrases.reportUnclaimSuccess);
		
		output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);
		
		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);
	}
	
	/**
	 * Updates the documentation for the command.
	 * <br/>
	 * This should be called after the locale has changed.
	 */
	public void updateDocumentation()
	{
		super.updateDocumentation(
				getManager().getLocale().getString(UnclaimPhrases.unclaimHelp),
				getManager().getLocale().getString(UnclaimPhrases.unclaimHelpDetails));
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
