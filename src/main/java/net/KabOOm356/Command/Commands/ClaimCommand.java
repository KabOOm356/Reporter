package net.KabOOm356.Command.Commands;

import java.util.ArrayList;
import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Locale.Entry.LocalePhrases.ClaimPhrases;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.Util;

/**
 * A {@link ReporterCommand} to handle players claiming reports.
 */
public class ClaimCommand extends ReporterCommand
{
	private static final String name = "Claim";
	private static final int minimumNumberOfArguments = 1;
	private final static String permissionNode = "reporter.claim";
	
	/**
	 * Constructor.
	 * 
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public ClaimCommand(ReporterCommandManager manager)
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
		
		claimReport(sender, index);
	}
	
	private void claimReport(CommandSender sender, int index)
	{
		ArrayList<String> params = new ArrayList<String>();
		
		params.add("1");
		if(BukkitUtil.isPlayer(sender))
			params.add(((Player)sender).getDisplayName());
		else
			params.add(sender.getName());
		params.add(sender.getName());
		params.add(Integer.toString(getManager().getModLevel(sender).getLevel()));
		params.add(Reporter.getDateformat().format(new Date()));
		params.add(Integer.toString(index));
		
		String query = "UPDATE Reports " +
				"SET ClaimStatus=?, ClaimedBy=?, ClaimedByRaw=?, ClaimPriority=?, ClaimDate=? " +
				"WHERE ID=?";
		
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
		
		String output = getManager().getLocale().getString(ClaimPhrases.reportClaimSuccess);
		
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
				getManager().getLocale().getString(ClaimPhrases.claimHelp),
				getManager().getLocale().getString(ClaimPhrases.claimHelpDetails));
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
