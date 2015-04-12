package net.KabOOm356.Command.Commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.ListPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.ViewPhrases;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.ObjectPair;
import net.KabOOm356.Util.Util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A {@link ReporterCommand} that will handle viewing reports.
 */
public class ViewCommand extends ReporterCommand
{
	private static final Logger log = LogManager.getLogger(ViewCommand.class);
	
	private static final String name = "View";
	private static final int minimumNumberOfArguments = 1;
	private static final String permissionNode = "reporter.view";
	
	/**
	 * Constructor.
	 * 
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public ViewCommand(ReporterCommandManager manager)
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
		if(hasPermission(sender))
		{
			if(args.get(0).equalsIgnoreCase("all"))
				viewAll(sender, displayRealName(args, 1));
			else if(args.get(0).equalsIgnoreCase("completed") || args.get(0).equalsIgnoreCase("finished"))
				viewCompleted(sender, displayRealName(args, 1));
			else if(args.get(0).equalsIgnoreCase("incomplete") || args.get(0).equalsIgnoreCase("unfinished"))
				viewIncomplete(sender, displayRealName(args, 1));
			else if (args.get(0).equalsIgnoreCase("priority"))
			{
				if (args.size() >= 2 && ModLevel.modLevelInBounds(args.get(1))) // /report view priority Priority [name]
				{
					ModLevel level = ModLevel.getModLevel(args.get(1));
					
					viewPriority(sender, level, displayRealName(args, 2));
				}
				else // /report view priority [name]
					viewPriority(sender, displayRealName(args, 1));
			}
			else if (args.get(0).equalsIgnoreCase("claimed"))
			{
				if(args.size() >= 2 && args.get(1).equalsIgnoreCase("priority")) // report view claimed priority 
				{
					if (args.size() >= 3 && ModLevel.modLevelInBounds(args.get(2))) // /report view claimed priority Priority [name]
					{
						ModLevel level = ModLevel.getModLevel(args.get(2));
						
						viewClaimedPriority(sender, level, displayRealName(args, 3));
					}
					else // /report view claimed priority [name]
						viewClaimedPriority(sender, displayRealName(args, 2));
				}
				else // /report view claimed [name]
					viewClaimed(sender, displayRealName(args, 1));
			}
			else
			{
				int index;
				
				if(args.get(0).equalsIgnoreCase("last"))
				{
					if(!hasRequiredLastViewed(sender))
						return;
				
					index = getLastViewed(sender);
				}
				else
				{
					index = Util.parseInt(args.get(0));
					
					if(!getManager().isReportIndexValid(sender, index))
						return;
				}
				
				viewReport(sender, index, displayRealName(args, 1));
			}
		}
		else if (getManager().getConfig().getBoolean("general.canViewSubmittedReports", true))
		{
			ArrayList<Integer> indexes = null;
			try {
				indexes = getManager().getViewableReports((Player)sender);
			} catch (final Exception e) {
				sender.sendMessage(getErrorMessage());
				log.log(Level.ERROR, "Failed to view submitted report!", e);
				return;
			}
			
			int index;
			
			if(args.get(0).equalsIgnoreCase("last"))
			{
				if(!hasRequiredLastViewed(sender))
					return;
			
				index = getLastViewed(sender);
			}
			else
			{
				index = Util.parseInt(args.get(0));
				
				if(!getManager().isReportIndexValid(sender, index))
					return;
			}
			
			if(indexes.contains(index))
				viewReport(sender, index, false);
			else
				displayAvailableReports(sender, indexes);
		}
		else
			sender.sendMessage(getFailedPermissionsMessage());
	}

	private void viewPriority(CommandSender sender, boolean displayRealName)
	{
		viewPriority(sender, ModLevel.NONE, displayRealName);
		viewPriority(sender, ModLevel.LOW, displayRealName);
		viewPriority(sender, ModLevel.NORMAL, displayRealName);
		viewPriority(sender, ModLevel.HIGH, displayRealName);
	}

	private void viewPriority(CommandSender sender, ModLevel level,
			boolean displayRealName)
	{
		String query = "SELECT ID, SenderUUID, Sender, ReportedUUID, Reported, Details " +
				"FROM Reports " +
				"WHERE Priority = " + level.getLevel();
		
		try
		{
			int count = getManager().getNumberOfPriority(level);
			
			String[][] reports = new String[count][4];
			
			count = 0;
			
			SQLResultSet result = getManager().getDatabaseHandler().sqlQuery(query);
			
			for(ResultRow row : result)
			{
				reports[count] = readQuickData(row, displayRealName);
				count++;
			}
			
			printPriority(sender, level, reports);
		}
		catch (final Exception e)
		{
			log.log(Level.ERROR, "Failed to view priority!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}
		finally
		{
			getManager().getDatabaseHandler().closeConnection();
		}
	}

	private void viewClaimedPriority(CommandSender sender,
			boolean displayRealName)
	{
		viewClaimedPriority(sender, ModLevel.NONE, displayRealName);
		viewClaimedPriority(sender, ModLevel.LOW, displayRealName);
		viewClaimedPriority(sender, ModLevel.NORMAL, displayRealName);
		viewClaimedPriority(sender, ModLevel.HIGH, displayRealName);
	}

	private void viewClaimedPriority(CommandSender sender, ModLevel level,
			boolean displayRealName)
	{
		String query = "SELECT COUNT(*) AS Count " +
				"FROM Reports " +
				"WHERE ClaimStatus = 1 AND ClaimedBy = '" + sender.getName() + "' AND Priority = " + level.getLevel();
		
		Player senderPlayer = null;
		
		if(BukkitUtil.isPlayer(sender))
		{
			senderPlayer = (Player) sender;
			
			UUID uuid = senderPlayer.getUniqueId();
			
			query = "SELECT COUNT(*) AS Count " +
					"FROM Reports " +
					"WHERE ClaimStatus = 1 AND ClaimedByUUID = '" + uuid.toString() + "' AND Priority = " + level.getLevel();
		}
		
		try
		{
			SQLResultSet result = getManager().getDatabaseHandler().sqlQuery(query);
			
			int count = result.getInt("Count");
			
			getManager().getDatabaseHandler().closeConnection();
			
			if(senderPlayer != null)
			{
				query = "SELECT ID, SenderUUID, Sender, ReportedUUID, Reported, Details " +
						"FROM Reports " +
						"WHERE ClaimStatus = 1 AND ClaimedByUUID = '" + senderPlayer.getUniqueId() + "' AND Priority = " + level.getLevel();
			}
			else
			{
				query = "SELECT ID, SenderUUID, Sender, ReportedUUID, Reported, Details " +
						"FROM Reports " +
						"WHERE ClaimStatus = 1 AND ClaimedBy = '" + sender.getName() + "' AND Priority = " + level.getLevel();
			}
			
			result = getManager().getDatabaseHandler().sqlQuery(query);
			
			String[][] reports = new String[count][4];
			
			count = 0;
			
			for(ResultRow row : result)
			{
				reports[count] = readQuickData(row, displayRealName);
				count++;
			}
			
			printPriority(sender, level, reports);
		}
		catch (final Exception e)
		{
			log.log(Level.ERROR, "Failed to view claimed reports by priority!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}
		finally
		{
			getManager().getDatabaseHandler().closeConnection();
		}
	}

	private void viewClaimed(CommandSender sender, boolean displayRealName)
	{
		String query = "SELECT COUNT(*) AS Count " +
				"FROM Reports " +
				"WHERE ClaimStatus = 1 AND ClaimedBy = '" + sender.getName() + "'";
		
		Player senderPlayer = null;
		
		if(BukkitUtil.isPlayer(sender))
		{
			senderPlayer = (Player) sender;
			
			UUID uuid = senderPlayer.getUniqueId();
			
			query = "SELECT COUNT(*) AS Count " +
					"FROM Reports " +
					"WHERE ClaimStatus = 1 AND ClaimedByUUID = '" + uuid + "'";
		}
		
		try
		{
			SQLResultSet result = getManager().getDatabaseHandler().sqlQuery(query);
			
			int claimedCount = result.getInt("Count");
			
			String[][] claimed = new String[claimedCount][4];
			
			getManager().getDatabaseHandler().closeConnection();
			
			if(senderPlayer != null)
			{
				query = "SELECT ID, SenderUUID, Sender, ReportedUUID, Reported, Details " +
						"FROM Reports " +
						"WHERE ClaimStatus = 1 AND ClaimedByUUID = '" + senderPlayer.getUniqueId() + "'";
			}
			else
			{
				query = "SELECT ID, SenderUUID, Sender, ReportedUUID, Reported, Details " +
						"FROM Reports " +
						"WHERE ClaimStatus = 1 AND ClaimedBy = '" + sender.getName() + "'";
			}
			
			int count = 0;
			
			result = getManager().getDatabaseHandler().sqlQuery(query);
			
			for(ResultRow row : result)
			{
				claimed[count] = readQuickData(row, displayRealName);
				count++;
			}
			
			String header = getManager().getLocale().getString(ViewPhrases.viewYourClaimedReportsHeader);
			
			sender.sendMessage(ChatColor.GREEN + "-----" + header + "-----");
			
			printQuickView(sender, claimed);
		}
		catch (final Exception e)
		{
			log.log(Level.ERROR, "Failed to view claimed reports!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}
		finally
		{
			getManager().getDatabaseHandler().closeConnection();
		}
	}
	
	private void printPriority(CommandSender sender, ModLevel level, String[][] reports)
	{
		String header = getManager().getLocale().getString(ViewPhrases.viewPriorityHeader);
		
		header = header.replaceAll("%p", level.getColor() + level.getName() + ChatColor.GREEN);
		
		sender.sendMessage(ChatColor.GREEN + "-----" + ChatColor.GREEN + header + ChatColor.GREEN + "------");
		
		printQuickView(sender, reports);
	}
	
	private void printQuickView(CommandSender sender, String[] report)
	{
		String reportHeader = BukkitUtil.colorCodeReplaceAll(
				getManager().getLocale().getString(ViewPhrases.viewAllReportHeader));
		
		String reportDetails = BukkitUtil.colorCodeReplaceAll(
				getManager().getLocale().getString(ViewPhrases.viewAllReportDetails));
		
		String out = reportHeader.replaceAll("%i", ChatColor.GOLD + report[0] + ChatColor.WHITE);
		out = out.replaceAll("%s", ChatColor.BLUE + report[1] + ChatColor.WHITE);
		out = out.replaceAll("%r", ChatColor.RED + report[2] + ChatColor.WHITE);
		
		sender.sendMessage(ChatColor.WHITE + out);
		
		// Fix for replace all error with $
		report[3] = report[3].replaceAll("\\$", "\\\\\\$");
		
		sender.sendMessage(ChatColor.WHITE + reportDetails.replaceAll("%d", ChatColor.GOLD + report[3] + ChatColor.WHITE));
	}
	
	private void printQuickView(CommandSender sender, String[][] reports)
	{
		for(String[] entry : reports)
			printQuickView(sender, entry);
	}
	
	private static String[] readQuickData(ResultRow row, boolean displayRealName) throws SQLException
	{
		String[] array = new String[4];
		
		array[0] = row.getString("ID");
		
		String senderName = row.getString("Sender");
		
		if(!row.getString("SenderUUID").isEmpty())
		{
			UUID uuid = UUID.fromString(row.getString("SenderUUID"));
			
			OfflinePlayer sender = Bukkit.getOfflinePlayer(uuid);
			
			senderName = BukkitUtil.formatPlayerName(sender, displayRealName);
		}
		
		array[1] = senderName;
		
		String reportedName = row.getString("Reported");
		
		if(!row.getString("ReportedUUID").isEmpty())
		{
			UUID uuid = UUID.fromString(row.getString("ReportedUUID"));
			
			OfflinePlayer reported = Bukkit.getOfflinePlayer(uuid);
			
			reportedName = BukkitUtil.formatPlayerName(reported, displayRealName);
		}
		
		array[2] = reportedName;
		
		array[3] = row.getString("Details");
		
		return array;
	}

	private boolean displayRealName(ArrayList<String> args, int index)
	{
		boolean displayRealName = getManager().getConfig().getBoolean("general.viewing.displayRealName", false);
		
		if(args.size() >= (index+1))
			if(args.get(index).equalsIgnoreCase("name"))
				displayRealName = true;
		
		return displayRealName;
	}
	
	private void displayAvailableReports(CommandSender sender, ArrayList<Integer> indexes)
	{
		String indexesString = Util.indexesToString(indexes, ChatColor.GOLD, ChatColor.WHITE);
		
		if(!indexesString.equals(""))
		{
			String out = getManager().getLocale().getString(ListPhrases.listReportsAvailable);
			
			out = out.replaceAll("%i", ChatColor.GOLD + indexesString + ChatColor.WHITE);
			
			sender.sendMessage(ChatColor.WHITE + out);
		}
		else
		{
			sender.sendMessage(ChatColor.RED + 
					getManager().getLocale().getString(ListPhrases.listNoReportsAvailable));
		}
	}

	private void viewAll(CommandSender sender, boolean displayRealName)
	{
		String query = "SELECT ID, SenderUUID, Sender, ReportedUUID, Reported, Details, CompletionStatus FROM Reports";
		
		String notCompleted[][] = null;
		String completed[][] = null;
		try {
			notCompleted = new String[getManager().getIncompleteReports()][4];
			completed = new String[getManager().getCompletedReports()][4];
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to initialize report arrays!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}
		
		int cIndex = 0;
		int ncIndex = 0;
		
		try
		{
			SQLResultSet result = getManager().getDatabaseHandler().sqlQuery(query);
			
			for(ResultRow row : result)
			{
				if(row.getBoolean("CompletionStatus"))
				{
					completed[cIndex] = readQuickData(row, displayRealName);
					cIndex++;
				}
				else
				{
					notCompleted[ncIndex] = readQuickData(row, displayRealName);
					ncIndex++;
				}
			}
		}
		catch (final Exception e)
		{
			log.log(Level.ERROR, "Failed to view all reports!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}
		finally
		{
			getManager().getDatabaseHandler().closeConnection();
		}
		
		if(cIndex != 0 || ncIndex != 0)
			quickViewAll(sender, completed, notCompleted);
		else
		{
			sender.sendMessage(ChatColor.RED + 
					getManager().getLocale().getString(ViewPhrases.noReportsToView));
		}
	}
	
	private void viewCompleted(CommandSender sender, boolean displayRealName)
	{
		String query = "SELECT ID, SenderUUID, Sender, ReportedUUID, Reported, Details, CompletionStatus " +
				"FROM Reports " +
				"WHERE CompletionStatus = 1";
		
		String reports[][] = null;
		try {
			reports = new String[getManager().getCompletedReports()][4];
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to initialize completed report array!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}
		
		int index = 0;
		
		try
		{
			SQLResultSet result = getManager().getDatabaseHandler().sqlQuery(query);
			
			for(ResultRow row : result)
			{
				reports[index] = readQuickData(row, displayRealName);
				index++;
			}
		}
		catch (final Exception e)
		{
			log.log(Level.ERROR, "Failed to view all completed reports!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}
		finally
		{
			getManager().getDatabaseHandler().closeConnection();
		}
		
		if(index != 0)
			quickViewCompleted(sender, reports);
		else
		{
			sender.sendMessage(ChatColor.RED + 
					getManager().getLocale().getString(ListPhrases.listReportNoCompleteIndexes));
		}
	}
	
	private void viewIncomplete(CommandSender sender, boolean displayRealName)
	{
		String query = "SELECT ID, SenderUUID, Sender, ReportedUUID, Reported, Details, CompletionStatus " +
				"FROM Reports " +
				"WHERE CompletionStatus = 0";
		
		String reports[][] = null;
		try {
			reports = new String[getManager().getIncompleteReports()][4];
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to initialize unfinished report array!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}
		
		int index = 0;
		
		try
		{
			SQLResultSet result = getManager().getDatabaseHandler().sqlQuery(query);
				
			for(ResultRow row : result)
			{
				reports[index] = readQuickData(row, displayRealName);
				index++;
			}
		}
		catch (final Exception e)
		{
			log.log(Level.ERROR, "Failed to view all incomplete reports!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}
		finally
		{
			getManager().getDatabaseHandler().closeConnection();
		}
		
		if(index != 0)
			quickViewIncomplete(sender, reports);
		else
		{
			sender.sendMessage(ChatColor.RED + 
					getManager().getLocale().getString(ListPhrases.listReportNoIncompleteIndexes));
		}
	}
	
	private void viewReport(CommandSender sender, int index, boolean displayRealName)
	{
		String query = "SELECT * FROM Reports WHERE ID = " + index;
		
		String reporter = null, reportedPlayer = null, reportDetails = null, dateReport = null, priority = null;
		
		String senderWorld = null, reportedWorld = null;
		int senderX = 0, senderY = 0, senderZ = 0, reportedX = 0, reportedY = 0, reportedZ = 0;
		
		boolean claimStatus = false;
		String claimedBy = null, claimDate = null;
		
		boolean completionStatus = false;
		String completedBy = null, completionDate = null, summaryDetails = null;
		
		OfflinePlayer player = null;
		
		try
		{
			SQLResultSet result = getManager().getDatabaseHandler().sqlQuery(query);
			
			reporter = result.getString("Sender");
			
			if(!result.getString("SenderUUID").isEmpty())
			{
				UUID uuid = UUID.fromString(result.getString("SenderUUID"));
				
				player = Bukkit.getOfflinePlayer(uuid);
				
				reporter = BukkitUtil.formatPlayerName(player, displayRealName);
			}
			
			senderWorld = result.getString("SenderWorld");
			senderX = (int) Math.round(result.getDouble("SenderX"));
			senderY = (int) Math.round(result.getDouble("SenderY"));
			senderZ = (int) Math.round(result.getDouble("SenderZ"));
			
			reportedPlayer = result.getString("Reported");
			
			if(!result.getString("ReportedUUID").isEmpty())
			{
				UUID uuid = UUID.fromString(result.getString("ReportedUUID"));
				
				player = Bukkit.getOfflinePlayer(uuid);
				
				reportedPlayer = BukkitUtil.formatPlayerName(player, displayRealName);
			}
			
			reportedWorld = result.getString("ReportedWorld");
			reportedX = (int) Math.round(result.getDouble("ReportedX"));
			reportedY = (int) Math.round(result.getDouble("ReportedY"));
			reportedZ = (int) Math.round(result.getDouble("ReportedZ"));
			
			reportDetails = result.getString("Details");
			dateReport = result.getString("Date");
			
			int priorityLevel = result.getInt("Priority");
			
			ModLevel priorityModLevel = ModLevel.getByLevel(priorityLevel);
			
			priority = ModLevel.getModLevelColor(priorityModLevel) + priorityModLevel.getName();
			
			claimStatus = result.getBoolean("ClaimStatus");
			
			claimedBy = result.getString("ClaimedBy");
			
			if(!result.getString("ClaimedByUUID").isEmpty())
			{
				UUID uuid = UUID.fromString(result.getString("ClaimedByUUID"));
				
				player = Bukkit.getOfflinePlayer(uuid);
				
				claimedBy = BukkitUtil.formatPlayerName(player, displayRealName);
			}
			
			claimDate = result.getString("ClaimDate");
			
			completionStatus = result.getBoolean("CompletionStatus");
			
			completedBy = result.getString("CompletedBy");
			
			if(!result.getString("CompletedByUUID").isEmpty())
			{
				UUID uuid = UUID.fromString(result.getString("CompletedByUUID"));
				
				player = Bukkit.getOfflinePlayer(uuid);
				
				completedBy = BukkitUtil.formatPlayerName(player, displayRealName);
			}
			
			completionStatus = result.getBoolean("CompletionStatus");
			
			completionDate = result.getString("CompletionDate");
			summaryDetails = result.getString("CompletionSummary");
		}
		catch (final Exception e)
		{
			log.log(Level.ERROR, "Failed to view report!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}
		finally
		{
			getManager().getDatabaseHandler().closeConnection();
		}
		
		printReport(sender, 
				index, priority,
				reporter, senderWorld, senderX, senderY, senderZ,
				reportedPlayer, reportedWorld, reportedX, reportedY, reportedZ,
				reportDetails, dateReport,
				claimStatus,
				claimedBy, claimDate,
				completionStatus, 
				completedBy, completionDate, summaryDetails);
		
		getManager().getLastViewed().put(sender, index);
	}

	private void quickViewCompleted(CommandSender sender, String[][] reports)
	{
		String header = getManager().getLocale().getString(ViewPhrases.viewAllCompleteHeader);
		
		String out;
		
		sender.sendMessage(ChatColor.GREEN + "-----" + ChatColor.GREEN + header + ChatColor.GREEN + "------");
		
		String reportHeader = getManager().getLocale().getString(ViewPhrases.viewAllReportHeader);
		String reportDetails = getManager().getLocale().getString(ViewPhrases.viewAllReportDetails);
		
		for(int LCV = 0; LCV < reports.length; LCV++)
		{
			out = reportHeader.replaceAll("%i", ChatColor.GOLD + reports[LCV][0] + ChatColor.WHITE);
			out = out.replaceAll("%s", ChatColor.BLUE + reports[LCV][1] + ChatColor.WHITE);
			out = out.replaceAll("%r", ChatColor.RED + reports[LCV][2] + ChatColor.WHITE);
			
			sender.sendMessage(ChatColor.WHITE + out);
			
			// Fix for replace all error with $
			reports[LCV][3] = reports[LCV][3].replaceAll("\\$", "\\\\\\$");
			
			sender.sendMessage(ChatColor.WHITE + reportDetails.replaceAll("%d", ChatColor.GOLD + reports[LCV][3] + ChatColor.WHITE));
		}
	}
	
	private void quickViewIncomplete(CommandSender sender, String[][] reports)
	{
		String header = getManager().getLocale().getString(ViewPhrases.viewAllUnfinishedHeader);
		
		String out;
		
		sender.sendMessage(ChatColor.GREEN + "-----" + ChatColor.GREEN + header + ChatColor.GREEN + "------");
		
		String reportHeader = BukkitUtil.colorCodeReplaceAll(
				getManager().getLocale().getString(ViewPhrases.viewAllReportHeader));
		String reportDetails = BukkitUtil.colorCodeReplaceAll(
				getManager().getLocale().getString(ViewPhrases.viewAllReportDetails));
		
		for(int LCV = 0; LCV < reports.length; LCV++)
		{
			out = reportHeader.replaceAll("%i", ChatColor.GOLD + reports[LCV][0] + ChatColor.WHITE);
			out = out.replaceAll("%s", ChatColor.BLUE + reports[LCV][1] + ChatColor.WHITE);
			out = out.replaceAll("%r", ChatColor.RED + reports[LCV][2] + ChatColor.WHITE);
			
			sender.sendMessage(ChatColor.WHITE + out);
			
			// Fix for replace all error with $
			reports[LCV][3] = reports[LCV][3].replaceAll("\\$", "\\\\\\$");
			
			sender.sendMessage(ChatColor.WHITE + reportDetails.replaceAll("%d", ChatColor.GOLD + reports[LCV][3] + ChatColor.WHITE));
		}
	}
	
	private void quickViewAll(CommandSender sender, String[][] complete, String[][] notComplete)
	{
		String viewAllBegin = BukkitUtil.colorCodeReplaceAll(
				getManager().getLocale().getString(ViewPhrases.viewAllBeginHeader));
		
		sender.sendMessage(ChatColor.GOLD + "-----" + ChatColor.GOLD + viewAllBegin + ChatColor.GOLD + "------");
		
		quickViewCompleted(sender, complete);
		
		quickViewIncomplete(sender, notComplete);
	}
	
	private void printReport(CommandSender sender,
			int id, String priority,
			String reporter, String senderWorld, int senderX, int senderY, int senderZ,
			String reportedPlayer, String reportedWorld, int reportedX, int reportedY, int reportedZ,
			String reportDetails, String dateReport,
			boolean claimStatus,
			String claimedBy, String claimDate, 
			boolean completionStatus,
			String completedBy, String completionDate, String summaryDetails)
	{
		boolean displayLocation = getManager().getConfig().getBoolean("general.viewing.displayLocation", true);
		
		String begin = BukkitUtil.colorCodeReplaceAll(getManager().getLocale().getString(ViewPhrases.viewBegin));
		
		begin = begin.replaceAll("%i", ChatColor.GOLD + Integer.toString(id));
		
		sender.sendMessage(ChatColor.WHITE + "-----" + ChatColor.BLUE + begin + ChatColor.WHITE + "------");
		
		if(!displayLocation || senderWorld.equals("") && senderX == 0 && senderY == 0 && senderZ == 0)
			sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ViewPhrases.viewSender)) + " " + ChatColor.BLUE + reporter);
		else
			sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ViewPhrases.viewSender)) + " " +
					ChatColor.BLUE + reporter + ChatColor.GOLD + " (" + senderWorld + ": " + senderX + ", " + senderY + ", " + senderZ + ")");
		
		if(!displayLocation || reportedWorld.equals("") && reportedX == 0 && reportedY == 0 && reportedZ == 0)
			sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ViewPhrases.viewReported)) + " " + ChatColor.RED + reportedPlayer);
		else
			sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ViewPhrases.viewReported)) + " " +
					ChatColor.RED + reportedPlayer + ChatColor.GOLD + " (" + reportedWorld + ": " + reportedX + ", " + reportedY + ", " + reportedZ + ")");
		
		sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
				getManager().getLocale().getString(ViewPhrases.viewDetails)) + " " + ChatColor.GOLD + reportDetails);
		sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
				getManager().getLocale().getString(ViewPhrases.viewPriority)) + " " + priority);
		sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
				getManager().getLocale().getString(ViewPhrases.viewDate)) + " " + ChatColor.GREEN + dateReport);
		
		sender.sendMessage(ChatColor.WHITE + "------" + ChatColor.BLUE + BukkitUtil.colorCodeReplaceAll(
				getManager().getLocale().getString(ViewPhrases.viewSummaryTitle)) + ChatColor.WHITE + "------");
		
		if(!completionStatus)
		{
			if(claimStatus)
			{
				sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
						getManager().getLocale().getString(ViewPhrases.viewClaimHeader) + " " +
						ChatColor.GREEN + getManager().getLocale().getString(ViewPhrases.viewStatusClaimed)));
				
				sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
						getManager().getLocale().getString(ViewPhrases.viewClaimedBy) + " " + ChatColor.BLUE + claimedBy));
				
				sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
						getManager().getLocale().getString(ViewPhrases.viewClaimedOn) + " " + ChatColor.GREEN + claimDate));
			}
			else
			{
				sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
						getManager().getLocale().getString(ViewPhrases.viewClaimHeader) + " " +
						ChatColor.RED + getManager().getLocale().getString(ViewPhrases.viewStatusUnclaimed)));
			}
		}
		
		if(!completionStatus)
			sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ViewPhrases.viewCompletionStatus)) + " " +
					ChatColor.RED + BukkitUtil.colorCodeReplaceAll(
							getManager().getLocale().getString(ViewPhrases.viewUnfinished)));
		else
		{
			sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ViewPhrases.viewCompletionStatus)) + " " +
					ChatColor.GREEN + BukkitUtil.colorCodeReplaceAll(
							getManager().getLocale().getString(ViewPhrases.viewFinished)));
			sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ViewPhrases.viewCompletedBy)) + " " + ChatColor.BLUE + completedBy);
			sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ViewPhrases.viewCompletedOn)) + " " + ChatColor.GREEN + completionDate);
			if(!summaryDetails.equals(""))
				sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
						getManager().getLocale().getString(ViewPhrases.viewCompletedSummary)) + " " + ChatColor.GOLD + summaryDetails);
			else
				sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
						getManager().getLocale().getString(ViewPhrases.viewCompletedSummary)) + " " +
						ChatColor.GOLD + BukkitUtil.colorCodeReplaceAll(
								getManager().getLocale().getString(ViewPhrases.viewNoSummary)));
		}
	}
	
	/**
	 * @see net.KabOOm356.Command.ReporterCommand#updateDocumentation()
	 */
	@Override
	public void updateDocumentation()
	{
		ArrayList<ObjectPair<String, String>> usages = super.getUsages();
		usages.clear();
		
		String usage = getManager().getLocale().getString(ViewPhrases.viewHelp);
		String description = getManager().getLocale().getString(ViewPhrases.viewHelpDetails);
		
		ObjectPair<String, String> entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report view all [name]";
		description = getManager().getLocale().getString(ViewPhrases.viewHelpAllDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report view completed|finished [name]";
		description = getManager().getLocale().getString(ViewPhrases.viewHelpCompletedDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report view incomplete|unfinished [name]";
		description = getManager().getLocale().getString(ViewPhrases.viewHelpIncompleteDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report view priority [name]";
		description = getManager().getLocale().getString(ViewPhrases.viewHelpPriorityDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = getManager().getLocale().getString(ViewPhrases.viewHelpGivenPriority);
		description = getManager().getLocale().getString(ViewPhrases.viewHelpGivenPriorityDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report view claimed [name]";
		description = getManager().getLocale().getString(ViewPhrases.viewHelpClaimedDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report view claimed priority [name]";
		description = getManager().getLocale().getString(ViewPhrases.viewHelpClaimedPriorityDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = getManager().getLocale().getString(ViewPhrases.viewHelpClaimedGivenPriority);
		description = getManager().getLocale().getString(ViewPhrases.viewHelpClaimedPriorityDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
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
