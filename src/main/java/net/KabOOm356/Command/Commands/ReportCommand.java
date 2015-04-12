package net.KabOOm356.Command.Commands;

import java.util.ArrayList;
import java.util.Date;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.ReportPhrases;
import net.KabOOm356.Manager.SQLStatManagers.PlayerStatManager;
import net.KabOOm356.Manager.SQLStatManagers.PlayerStatManager.PlayerStat;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.BukkitUtil;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A {@link ReporterCommand} that will handle users submitting reports.
 */
public class ReportCommand extends ReporterCommand
{
	private static final Logger log = LogManager.getLogger(ReportCommand.class);
	
	private static final String name = "Report";
	private static final int minimumNumberOfArguments = 2;
	private final static String permissionNode = "reporter.report";
	
	/**
	 * Constructor.
	 * 
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public ReportCommand(ReporterCommandManager manager)
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
		
		OfflinePlayer reported = getManager().getPlayer(args.get(0));
		
		if (!playerExists(sender, reported))
			return;
		
		if(!canReport(sender))
			return;
		
		if(!canReport(sender, reported))
			return;
		
		reportCommand(sender, reported, getDetails(args));
	}
	
	private boolean playerExists(CommandSender sender, OfflinePlayer player)
	{
		if(player == null)
		{
			sender.sendMessage(ChatColor.RED + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(GeneralPhrases.playerDoesNotExist)));
			return false;
		}
		
		return true;
	}
	
	private void reportCommand(CommandSender sender, OfflinePlayer reported, String details)
	{
		ArrayList<String> params = new ArrayList<String>();
		int count = getManager().getCount();
		Location reportedLoc = null;
		
		if(count != -1)
		{
			params.add(0, Integer.toString(count+1));
			
			params.add(1, BukkitUtil.getUUIDString(sender));
			params.add(2, sender.getName());
			
			params.add(3, BukkitUtil.getUUIDString(reported));
			
			if(reported.isOnline())
			{
				Player reportedPlayer = reported.getPlayer();
				
				reportedLoc = reportedPlayer.getLocation();
			}
			
			params.add(4, reported.getName());
			
			params.add(5, details);
			params.add(6, Reporter.getDateformat().format(new Date()));
			
			if(BukkitUtil.isPlayer(sender))
			{
				Player player = (Player)sender;
				
				params.add(7, player.getLocation().getWorld().getName());
				params.add(8, Double.toString(player.getLocation().getX()));
				params.add(9, Double.toString(player.getLocation().getY()));
				params.add(10, Double.toString(player.getLocation().getZ()));
			}
			else
			{
				params.add(7, "");
				params.add(8, "0.0");
				params.add(9, "0.0");
				params.add(10, "0.0");
			}
			
			if(reportedLoc != null)
			{
				params.add(11, reportedLoc.getWorld().getName());
				params.add(12, Double.toString(reportedLoc.getX()));
				params.add(13, Double.toString(reportedLoc.getY()));
				params.add(14, Double.toString(reportedLoc.getZ()));
			}
			else
			{
				params.add(11, "");
				params.add(12, "0.0");
				params.add(13, "0.0");
				params.add(14, "0.0");
			}
			
			params.add(15, "0");
			params.add(16, "0");
			
			try
			{
				String query = 
						"INSERT INTO Reports " +
						"(ID, SenderUUID, Sender, ReportedUUID, Reported, Details, Date, SenderWorld, SenderX, SenderY, SenderZ, ReportedWorld, ReportedX, ReportedY, ReportedZ, CompletionStatus, ClaimStatus) " +
						"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				getManager().getDatabaseHandler().preparedUpdateQuery(query, params);
			}
			catch (final Exception e)
			{
				log.log(Level.ERROR, "Failed to report!", e);
				sender.sendMessage(getErrorMessage());
				return;
			}
			finally
			{
				getManager().getDatabaseHandler().closeConnection();
			}
			
			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
					ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ReportPhrases.playerReport)));
			
			broadcastSubmittedMessage(getManager().getCount());
			
			getManager().getReportLimitManager().hasReported(sender, reported);
			
			PlayerStatManager stats = getManager().getPlayerStatsManager();
			
			String date = Reporter.getDateformat().format(new Date());
			
			if(BukkitUtil.isPlayer(sender))
			{
				Player senderPlayer = (Player) sender;
				
				stats.incrementStat(senderPlayer, PlayerStat.REPORTCOUNT);
				stats.setStat(senderPlayer, PlayerStat.LASTREPORTDATE, date);
				
				ResultRow result = stats.getStat(senderPlayer, PlayerStat.FIRSTREPORTDATE);
				
				String firstReportDate = result.getString(PlayerStat.FIRSTREPORTDATE.getColumnName());
				
				if(firstReportDate.equals(""))
				{
					stats.setStat(senderPlayer, PlayerStat.FIRSTREPORTDATE, date);
				}
			}
			
			stats.incrementStat(reported, PlayerStat.REPORTED);
			stats.setStat(reported, PlayerStat.LASTREPORTEDDATE, date);
			
			ResultRow result = stats.getStat(reported, PlayerStat.FIRSTREPORTEDDATE);
			
			String firstReportedDate = result.getString(PlayerStat.FIRSTREPORTEDDATE.getColumnName());
			
			if(firstReportedDate.equals(""))
			{
				stats.setStat(reported, PlayerStat.FIRSTREPORTEDDATE, date);
			}
			
			// Alert the player when they reach their reporting limit
			canReport(sender);
			canReport(sender, reported);
		}
		else
			sender.sendMessage(getErrorMessage());
	}
	
	private boolean canReport(CommandSender sender, OfflinePlayer reported)
	{
		if(!getManager().getReportLimitManager().canReport(sender, reported))
		{
			String output = getManager().getLocale().getString(ReportPhrases.reachedReportingLimitAgaintPlayer);
			
			String reportedNameFormatted = BukkitUtil.formatPlayerName(reported);
			
			output = output.replaceAll("%r", ChatColor.BLUE + reportedNameFormatted + ChatColor.WHITE);
			
			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
					ChatColor.WHITE + output);
			
			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
					ChatColor.WHITE + getTimeRemaining(sender, reported));
			
			return false;
		}
		return true;
	}

	private boolean canReport(CommandSender sender)
	{
		if(!getManager().getReportLimitManager().canReport(sender))
		{
			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
					ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ReportPhrases.reachedReportingLimit)));
			
			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
					ChatColor.WHITE + getTimeRemaining(sender));
			
			return false;
		}
		return true;
	}
	
	private String getTimeRemaining(CommandSender sender, OfflinePlayer reported)
	{
		String timeRemaining = getManager().getLocale().getString(ReportPhrases.remainingTimeToReportPlayer);
		
		String reportedNameFormatted = BukkitUtil.formatPlayerName(reported);
		
		timeRemaining = timeRemaining.replaceAll("%r", ChatColor.BLUE + reportedNameFormatted + ChatColor.WHITE);
		
		int seconds = getManager().getReportLimitManager().getRemainingTime(sender, reported);
		
		return formatTimeRemaining(timeRemaining, seconds);
	}
	
	private String getTimeRemaining(CommandSender sender)
	{
		String timeRemaining = BukkitUtil.colorCodeReplaceAll(
				getManager().getLocale().getString(ReportPhrases.remainingTimeForReport));
		
		int seconds = getManager().getReportLimitManager().getRemainingTime(sender);
		
		return formatTimeRemaining(timeRemaining, seconds);
	}
	
	// TODO Move this to a utility class
	private static String formatTimeRemaining(String line, int seconds)
	{
		// Convert the seconds to hours and drop the remainder.
		int hours = (int) Math.ceil(seconds / 3600);
		seconds = seconds % 3600;
		
		// Convert the seconds to minutes and drop the remainder.
		int minutes = (int) Math.ceil(seconds / 60);
		seconds = seconds % 60;
		
		line = line.replaceAll("%h", ChatColor.GOLD + Integer.toString(hours) + ChatColor.WHITE);
		line = line.replaceAll("%m", ChatColor.GOLD + Integer.toString(minutes) + ChatColor.WHITE);
		line = line.replaceAll("%s", ChatColor.GOLD + Integer.toString(seconds) + ChatColor.WHITE);
		
		return line;
	}
	
	private String getDetails(ArrayList<String> args)
	{
		String details = "";

		for(int LCV = 1; LCV < args.size(); LCV++)
			details = details + args.get(LCV) + " ";
		
		return details;
	}

	private void broadcastSubmittedMessage(int index)
	{
		Player[] p = Bukkit.getOnlinePlayers();
		
		String reportSubmitted = BukkitUtil.colorCodeReplaceAll(
				getManager().getLocale().getString(ReportPhrases.broadcastSubmitted));
		
		reportSubmitted = reportSubmitted.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);
		
		for(int LCV = 0; LCV < p.length; LCV++)
		{
			if(hasPermission(p[LCV], "reporter.list"))
				p[LCV].sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + reportSubmitted);
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
				getManager().getLocale().getString(ReportPhrases.reportHelp),
				getManager().getLocale().getString(ReportPhrases.reportHelpDetails));
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
