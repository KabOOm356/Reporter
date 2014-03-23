package net.KabOOm356.Command.Commands;

import java.util.ArrayList;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.RespondPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.ViewPhrases;
import net.KabOOm356.Manager.SQLStatManagers.ModeratorStatManager.ModeratorStat;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A {@link ReporterCommand} that will handle users responding to reports.
 */
public class RespondCommand extends ReporterCommand
{
	private static final String name = "Respond";
	private static final int minimumNumberOfArguments = 1;
	private final static String permissionNode = "reporter.respond";
	
	/**
	 * Constructor.
	 * 
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public RespondCommand(ReporterCommandManager manager)
	{
		super(manager, name, permissionNode, minimumNumberOfArguments);
		
		updateDocumentation();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(CommandSender sender, ArrayList<String> args)
	{
		if(!hasRequiredPermission(sender))
			return;
		
		// Cast the sender to type Player or tell the sender they must be a player
		Player player = null;
		if(BukkitUtil.isPlayer(sender))
			player = (Player)sender;
		else
		{
			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.RED + "You must be a player to use this command!");
			return;
		}
		
		int index;
		
		// Get the report index
		if(args.get(0).equalsIgnoreCase("last"))
		{
			if(!hasRequiredLastViewed(sender))
				return;
			
			index = getLastViewed(sender);
		}
		else
			index = Util.parseInt(args.get(0));
		
		if(!getManager().isReportIndexValid(sender, index))
			return;
		
		if(args.size() == 1)
			teleportToReport(player, index, "reported");
		else if(args.size() >= 2)
			teleportToReport(player, index, args.get(1));
	}
	
	private void teleportToReport(Player player, int index, String playerLoc)
	{
		if(!playerLoc.equalsIgnoreCase("sender") && !playerLoc.equalsIgnoreCase("reported"))
			player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.RED + BukkitUtil.colorCodeReplaceAll(getUsage()));
		else
		{
			boolean requestedToReported = (playerLoc.equalsIgnoreCase("reported")) ? true : false;
			boolean sendToReported = requestedToReported;
			
			int id = -1;
			double X = 0.0, Y = 0.0, Z = 0.0;
			String World = null, reported = null, sender = null, details = null;
			
			try
			{
				String query = "SELECT ID, Reported, Sender, Details, SenderX, SenderY, SenderZ, SenderWorld, ReportedX, ReportedY, ReportedZ, ReportedWorld " +
						"FROM Reports " +
						"WHERE ID=" + index;

				SQLResultSet result = getManager().getDatabaseHandler().sqlQuery(query);
				
				for(int LCV = 0; LCV < 2; LCV++)
				{
					if(sendToReported)
					{
						X = result.getDouble("ReportedX");
						Y = result.getDouble("ReportedY");
						Z = result.getDouble("ReportedZ");
						World = result.getString("ReportedWorld");
					}
					else
					{
						X = result.getDouble("SenderX");
						Y = result.getDouble("SenderY");
						Z = result.getDouble("SenderZ");
						World = result.getString("SenderWorld");
					}
					
					if(X == 0.0 && Y == 0.0 && Z == 0.0 || World == null || World.equals(""))
						sendToReported = !sendToReported;
					else
						break;
				}
				
				if(X == 0.0 && Y == 0.0 && Z == 0.0 || World == null || World.equals(""))
				{
					player.sendMessage(ChatColor.RED + getManager().getLocale().getString(RespondPhrases.bothPlayerLocNF));
					
					player.sendMessage(ChatColor.RED + getManager().getLocale().getString(RespondPhrases.teleAbort));
					return;
				}
				
				id = result.getInt("ID");
				
				reported = result.getString("Reported");
				sender = result.getString("Sender");
				details = result.getString("Details");
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				player.sendMessage(getErrorMessage());
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
			
			if(requestedToReported)
			{
				if(sendToReported)
				{
					player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
							ChatColor.WHITE + getManager().getLocale().getString(RespondPhrases.telReported));
				}
				else
				{
					player.sendMessage(ChatColor.RED + getManager().getLocale().getString(RespondPhrases.reportedPlayerLocNF));
					
					player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
							ChatColor.WHITE + getManager().getLocale().getString(RespondPhrases.telSender));
				}
			}
			else
			{
				if(sendToReported)
				{
					player.sendMessage(ChatColor.RED + getManager().getLocale().getString(RespondPhrases.senderLocNF));
					
					player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
							ChatColor.WHITE + getManager().getLocale().getString(RespondPhrases.telReported));
				}
				else
				{
					player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
							ChatColor.WHITE + getManager().getLocale().getString(RespondPhrases.telSender));
				}
			}
			
			String out = getManager().getLocale().getString(RespondPhrases.respondTeleportLocation);
			
			out = out.replaceAll("%world", ChatColor.GOLD + World + ChatColor.WHITE);
			out = out.replaceAll("%x", ChatColor.GOLD + Double.toString(Math.round(X)) + ChatColor.WHITE);
			out = out.replaceAll("%y", ChatColor.GOLD + Double.toString(Math.round(Y)) + ChatColor.WHITE);
			out = out.replaceAll("%z", ChatColor.GOLD + Double.toString(Math.round(Z)) + ChatColor.WHITE);
			
			player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + out);
			
			String reportInfo = getManager().getLocale().getString(ViewPhrases.viewAllReportHeader);
					
			String reportInfoDetails = getManager().getLocale().getString(ViewPhrases.viewAllReportDetails);
			
			reportInfo = reportInfo.replaceAll("%i", ChatColor.GOLD + Integer.toString(id) + ChatColor.WHITE);
			reportInfo = reportInfo.replaceAll("%r", ChatColor.GOLD + reported + ChatColor.WHITE);
			reportInfo = reportInfo.replaceAll("%s", ChatColor.GOLD + sender + ChatColor.WHITE);
			
			reportInfoDetails = reportInfoDetails.replaceAll("%d", ChatColor.GOLD + details);
			
			player.sendMessage(ChatColor.WHITE + reportInfo);
			
			player.sendMessage(ChatColor.WHITE + reportInfoDetails);
			
			Location loc = new Location(Bukkit.getWorld(World), X, Y, Z);

			player.teleport(loc);
			
			getManager().getModStatsManager().incrementStat(player, ModeratorStat.RESPONDED);
		}
	}
	
	/**
	 * Updates the documentation for the command.
	 * <br/>
	 * This should be called after the locale has changed.
	 */
	public void updateDocumentation()
	{
		super.updateDocumentation(
				getManager().getLocale().getString(RespondPhrases.respondHelp),
				getManager().getLocale().getString(RespondPhrases.respondHelpDetails));
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
