package net.KabOOm356.Command.Commands;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.ListPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.ViewPhrases;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Util.ArrayUtil;
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

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
	public void execute(CommandSender sender, ArrayList<String> args) {
		try {
			if(hasPermission(sender)) {
				if(args.get(0).equalsIgnoreCase("all")) {
					viewAll(sender, displayRealName(args, 1));
				} else if(args.get(0).equalsIgnoreCase("completed") || args.get(0).equalsIgnoreCase("finished")) {
					viewCompleted(sender, displayRealName(args, 1));
				} else if(args.get(0).equalsIgnoreCase("incomplete") || args.get(0).equalsIgnoreCase("unfinished")) {
					viewIncomplete(sender, displayRealName(args, 1));
				} else if (args.get(0).equalsIgnoreCase("priority")) {
					if (args.size() >= 2 && ModLevel.modLevelInBounds(args.get(1))) { // /report view priority Priority [name]
						final ModLevel level = ModLevel.getModLevel(args.get(1));
						viewPriority(sender, level, displayRealName(args, 2));
					} else { // /report view priority [name]
						viewPriority(sender, displayRealName(args, 1));
					}
				} else if (args.get(0).equalsIgnoreCase("claimed")) {
					if(args.size() >= 2 && args.get(1).equalsIgnoreCase("priority")) { // report view claimed priority
						if (args.size() >= 3 && ModLevel.modLevelInBounds(args.get(2))) { // /report view claimed priority Priority [name]
							final ModLevel level = ModLevel.getModLevel(args.get(2));
							viewClaimedPriority(sender, level, displayRealName(args, 3));
						} else { // /report view claimed priority [name]
							viewClaimedPriority(sender, displayRealName(args, 2));
						}
					} else { // /report view claimed [name]
						viewClaimed(sender, displayRealName(args, 1));
					}
				} else {
					int index;
					
					if(args.get(0).equalsIgnoreCase("last")) {
						if(!hasRequiredLastViewed(sender)) {
							return;
						}
						index = getLastViewed(sender);
					} else {
						index = Util.parseInt(args.get(0));
						
						if(!getManager().isReportIndexValid(sender, index)) {
							return;
						}
					}
					viewReport(sender, index, displayRealName(args, 1));
				}
			} else if (getManager().getConfig().getBoolean("general.canViewSubmittedReports", true)) {
				ArrayList<Integer> indexes = null;
				try {
					indexes = getManager().getViewableReports((Player)sender);
				} catch (final Exception e) {
					log.log(Level.ERROR, "Failed to view submitted report!");
					throw e;
				}
				
				int index;
				
				if(args.get(0).equalsIgnoreCase("last")) {
					if(!hasRequiredLastViewed(sender)) {
						return;
					}
					index = getLastViewed(sender);
				} else {
					index = Util.parseInt(args.get(0));
					if(!getManager().isReportIndexValid(sender, index)) {
						return;
					}
				}
				
				if(indexes.contains(index)) {
					viewReport(sender, index, false);
				} else {
					displayAvailableReports(sender, indexes);
				}
			} else { 
				sender.sendMessage(getFailedPermissionsMessage());
			}
		} catch (final Exception e) {
			log.error("Failed to view report!", e);
			sender.sendMessage(getErrorMessage());
		}
	}

	private void viewPriority(CommandSender sender, boolean displayRealName) throws Exception {
		viewPriority(sender, ModLevel.NONE, displayRealName);
		viewPriority(sender, ModLevel.LOW, displayRealName);
		viewPriority(sender, ModLevel.NORMAL, displayRealName);
		viewPriority(sender, ModLevel.HIGH, displayRealName);
	}

	private void viewPriority(CommandSender sender, ModLevel level,
			boolean displayRealName) throws Exception {
		String query = "SELECT ID, SenderUUID, Sender, ReportedUUID, Reported, Details " +
				"FROM Reports " +
				"WHERE Priority = " + level.getLevel();
		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			final int numberOfReports = getManager().getNumberOfPriority(level);
			
			String[][] reports = new String[numberOfReports][4];
			
			int count = 0;
			
			final SQLResultSet result = database.sqlQuery(connectionId, query);
			
			for(ResultRow row : result) {
				reports[count] = readQuickData(row, displayRealName);
				count++;
			}
			
			printPriority(sender, level, reports);
		} catch (final Exception e) {
			log.log(Level.ERROR, String.format("Failed to view reports with priority [%s]!", level.getName()));
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}
	}

	private void viewClaimedPriority(CommandSender sender,
			boolean displayRealName) throws Exception {
		viewClaimedPriority(sender, ModLevel.NONE, displayRealName);
		viewClaimedPriority(sender, ModLevel.LOW, displayRealName);
		viewClaimedPriority(sender, ModLevel.NORMAL, displayRealName);
		viewClaimedPriority(sender, ModLevel.HIGH, displayRealName);
	}

	private void viewClaimedPriority(CommandSender sender, ModLevel level,
			boolean displayRealName) throws Exception {
		String query = "SELECT COUNT(*) AS Count " +
				"FROM Reports " +
				"WHERE ClaimStatus = 1 AND ClaimedBy = '" + sender.getName() + "' AND Priority = " + level.getLevel();
		
		Player senderPlayer = null;
		
		if(BukkitUtil.isPlayer(sender)) {
			senderPlayer = (Player) sender;
			
			UUID uuid = senderPlayer.getUniqueId();
			
			query = "SELECT COUNT(*) AS Count " +
					"FROM Reports " +
					"WHERE ClaimStatus = 1 AND ClaimedByUUID = '" + uuid.toString() + "' AND Priority = " + level.getLevel();
		}
		
		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			SQLResultSet result = database.sqlQuery(connectionId, query);
			int count = result.getInt("Count");
			if(senderPlayer != null) {
				query = "SELECT ID, SenderUUID, Sender, ReportedUUID, Reported, Details " +
						"FROM Reports " +
						"WHERE ClaimStatus = 1 AND ClaimedByUUID = '" + senderPlayer.getUniqueId() + "' AND Priority = " + level.getLevel();
			} else {
				query = "SELECT ID, SenderUUID, Sender, ReportedUUID, Reported, Details " +
						"FROM Reports " +
						"WHERE ClaimStatus = 1 AND ClaimedBy = '" + sender.getName() + "' AND Priority = " + level.getLevel();
			}
			result = database.sqlQuery(connectionId, query);
			final String[][] reports = new String[count][4];
			count = 0;
			for(final ResultRow row : result) {
				reports[count] = readQuickData(row, displayRealName);
				count++;
			}
			
			printPriority(sender, level, reports);
		}
		catch (final Exception e) {
			log.log(Level.ERROR, "Failed to view claimed reports by priority!");
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}
	}

	private void viewClaimed(CommandSender sender, boolean displayRealName) throws Exception {
		String query = "SELECT COUNT(*) AS Count " +
				"FROM Reports " +
				"WHERE ClaimStatus = 1 AND ClaimedBy = '" + sender.getName() + '\'';
		
		Player senderPlayer = null;
		
		if(BukkitUtil.isPlayer(sender)) {
			senderPlayer = (Player) sender;
			final UUID uuid = senderPlayer.getUniqueId();
			query = "SELECT COUNT(*) AS Count " +
					"FROM Reports " +
					"WHERE ClaimStatus = 1 AND ClaimedByUUID = '" + uuid + '\'';
		}
		
		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			SQLResultSet result = database.sqlQuery(connectionId, query);
			final int claimedCount = result.getInt("Count");
			final String[][] claimed = new String[claimedCount][4];
			
			if(senderPlayer != null) {
				query = "SELECT ID, SenderUUID, Sender, ReportedUUID, Reported, Details " +
						"FROM Reports " +
						"WHERE ClaimStatus = 1 AND ClaimedByUUID = '" + senderPlayer.getUniqueId() + "'";
			}
			else {
				query = "SELECT ID, SenderUUID, Sender, ReportedUUID, Reported, Details " +
						"FROM Reports " +
						"WHERE ClaimStatus = 1 AND ClaimedBy = '" + sender.getName() + "'";
			}
			
			int count = 0;
			result = database.sqlQuery(connectionId, query);
			
			for(final ResultRow row : result) {
				claimed[count] = readQuickData(row, displayRealName);
				count++;
			}
			
			final String header = getManager().getLocale().getString(ViewPhrases.viewYourClaimedReportsHeader);
			sender.sendMessage(ChatColor.GREEN + "-----" + header + "-----");
			
			printQuickView(sender, claimed);
		}
		catch (final Exception e) {
			log.log(Level.ERROR, "Failed to view claimed reports!");
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}
	}
	
	private void printPriority(final CommandSender sender, final ModLevel level, final String[][] reports) {
		String header = getManager().getLocale().getString(ViewPhrases.viewPriorityHeader);
		header = header.replaceAll("%p", level.getColor() + level.getName() + ChatColor.GREEN);
		sender.sendMessage(ChatColor.GREEN + "-----" + ChatColor.GREEN + header + ChatColor.GREEN + "------");
		
		printQuickView(sender, reports);
	}
	
	private void printQuickView(final CommandSender sender, final String[] report) {
		final Locale locale = getManager().getLocale();
		final String reportHeader = BukkitUtil.colorCodeReplaceAll(
				locale.getString(ViewPhrases.viewAllReportHeader));
		
		final String reportDetails = BukkitUtil.colorCodeReplaceAll(
				locale.getString(ViewPhrases.viewAllReportDetails));
		
		String out = reportHeader.replaceAll("%i", ChatColor.GOLD + report[0] + ChatColor.WHITE);
		out = out.replaceAll("%s", ChatColor.BLUE + report[1] + ChatColor.WHITE);
		out = out.replaceAll("%r", ChatColor.RED + report[2] + ChatColor.WHITE);
		
		sender.sendMessage(ChatColor.WHITE + out);
		
		// Fix for replace all error with $
		report[3] = report[3].replaceAll("\\$", "\\\\\\$");
		
		sender.sendMessage(ChatColor.WHITE + reportDetails.replaceAll("%d", ChatColor.GOLD + report[3] + ChatColor.WHITE));
	}
	
	private void printQuickView(CommandSender sender, String[][] reports) {
		for(final String[] entry : reports) {
			printQuickView(sender, entry);
		}
	}
	
	private static String[] readQuickData(ResultRow row, boolean displayRealName) throws SQLException {
		final String[] array = new String[4];
		array[0] = row.getString("ID");
		String senderName = row.getString("Sender");
		
		if(!row.getString("SenderUUID").isEmpty()) {
			final UUID uuid = UUID.fromString(row.getString("SenderUUID"));
			final OfflinePlayer sender = Bukkit.getOfflinePlayer(uuid);
			senderName = BukkitUtil.formatPlayerName(sender, displayRealName);
		}
		
		array[1] = senderName;
		String reportedName = row.getString("Reported");
		if(!row.getString("ReportedUUID").isEmpty()) {
			final UUID uuid = UUID.fromString(row.getString("ReportedUUID"));
			final OfflinePlayer reported = Bukkit.getOfflinePlayer(uuid);
			reportedName = BukkitUtil.formatPlayerName(reported, displayRealName);
		}
		
		array[2] = reportedName;
		array[3] = row.getString("Details");
		
		return array;
	}

	private boolean displayRealName(final ArrayList<String> args, final int index) {
		boolean displayRealName = getManager().getConfig().getBoolean("general.viewing.displayRealName", false);

		if(args.size() >= (index+1)) {
			String argument = args.get(index);
			if(argument != null && argument.equalsIgnoreCase("name")) {
				displayRealName = true;
			}
		}
		
		return displayRealName;
	}
	
	private void displayAvailableReports(final CommandSender sender, final ArrayList<Integer> indexes) {
		final String indexesString = ArrayUtil.indexesToString(indexes, ChatColor.GOLD, ChatColor.WHITE);
		final Locale locale = getManager().getLocale();
		
		if(!indexesString.equals("")) {
			String out = locale.getString(ListPhrases.listReportsAvailable);
			out = out.replaceAll("%i", ChatColor.GOLD + indexesString + ChatColor.WHITE);
			sender.sendMessage(ChatColor.WHITE + out);
		} else {
			sender.sendMessage(ChatColor.RED + 
					locale.getString(ListPhrases.listNoReportsAvailable));
		}
	}

	private void viewAll(final CommandSender sender, final boolean displayRealName) throws Exception {
		final String query = "SELECT ID, SenderUUID, Sender, ReportedUUID, Reported, Details, CompletionStatus FROM Reports";
		
		String notCompleted[][] = null;
		String completed[][] = null;
		try {
			notCompleted = new String[getManager().getIncompleteReports()][4];
			completed = new String[getManager().getCompletedReports()][4];
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to initialize report arrays!");
			throw e;
		}
		
		int cIndex = 0;
		int ncIndex = 0;
		
		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			final SQLResultSet result = database.sqlQuery(connectionId, query);
			
			for(final ResultRow row : result) {
				if(row.getBoolean("CompletionStatus")) {
					completed[cIndex] = readQuickData(row, displayRealName);
					cIndex++;
				} else {
					notCompleted[ncIndex] = readQuickData(row, displayRealName);
					ncIndex++;
				}
			}
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to view all reports!");
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}
		
		if(cIndex != 0 || ncIndex != 0) {
			quickViewAll(sender, completed, notCompleted);
		} else {
			sender.sendMessage(ChatColor.RED + 
					getManager().getLocale().getString(ViewPhrases.noReportsToView));
		}
	}
	
	private void viewCompleted(final CommandSender sender, final boolean displayRealName) throws Exception {
		final String query = "SELECT ID, SenderUUID, Sender, ReportedUUID, Reported, Details, CompletionStatus " +
				"FROM Reports " +
				"WHERE CompletionStatus = 1";
		
		String reports[][] = null;
		try {
			reports = new String[getManager().getCompletedReports()][4];
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to initialize completed report array!");
			throw e;
		}
		
		int index = 0;
		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			final SQLResultSet result = database.sqlQuery(connectionId, query);
			
			for(final ResultRow row : result) {
				reports[index] = readQuickData(row, displayRealName);
				index++;
			}
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to view all completed reports!");
			throw e;
		} finally {
			database.closeConnection(connectionId);;
		}
		
		if(index != 0) {
			quickViewCompleted(sender, reports);
		} else {
			sender.sendMessage(ChatColor.RED + 
					getManager().getLocale().getString(ListPhrases.listReportNoCompleteIndexes));
		}
	}
	
	private void viewIncomplete(CommandSender sender, boolean displayRealName) throws Exception {
		final String query = "SELECT ID, SenderUUID, Sender, ReportedUUID, Reported, Details, CompletionStatus " +
				"FROM Reports " +
				"WHERE CompletionStatus = 0";
		
		String reports[][] = null;
		try {
			reports = new String[getManager().getIncompleteReports()][4];
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to initialize unfinished report array!");
			throw e;
		}
		
		int index = 0;
		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			final SQLResultSet result = database.sqlQuery(connectionId, query);
				
			for(final ResultRow row : result) {
				reports[index] = readQuickData(row, displayRealName);
				index++;
			}
		} catch (Exception e) {
			log.log(Level.ERROR, "Failed to view all incomplete reports!");
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}
		
		if(index != 0) {
			quickViewIncomplete(sender, reports);
		} else {
			sender.sendMessage(ChatColor.RED + 
					getManager().getLocale().getString(ListPhrases.listReportNoIncompleteIndexes));
		}
	}
	
	private void viewReport(CommandSender sender, int index, boolean displayRealName) throws Exception {
		final String query = "SELECT * FROM Reports WHERE ID = " + index;
		
		String reporter = null, reportedPlayer = null, reportDetails = null, dateReport = null, priority = null;
		
		String senderWorld = null, reportedWorld = null;
		int senderX = 0, senderY = 0, senderZ = 0, reportedX = 0, reportedY = 0, reportedZ = 0;
		
		boolean claimStatus = false;
		String claimedBy = null, claimDate = null;
		
		boolean completionStatus = false;
		String completedBy = null, completionDate = null, summaryDetails = null;
		
		OfflinePlayer player = null;
		
		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			final SQLResultSet result = database.sqlQuery(connectionId, query);
			
			reporter = result.getString("Sender");
			
			if(!result.getString("SenderUUID").isEmpty()) {
				final UUID uuid = UUID.fromString(result.getString("SenderUUID"));
				player = Bukkit.getOfflinePlayer(uuid);
				reporter = BukkitUtil.formatPlayerName(player, displayRealName);
			}
			
			senderWorld = result.getString("SenderWorld");
			senderX = (int) Math.round(result.getDouble("SenderX"));
			senderY = (int) Math.round(result.getDouble("SenderY"));
			senderZ = (int) Math.round(result.getDouble("SenderZ"));
			
			reportedPlayer = result.getString("Reported");
			
			if(!result.getString("ReportedUUID").isEmpty()) {
				final UUID uuid = UUID.fromString(result.getString("ReportedUUID"));
				player = Bukkit.getOfflinePlayer(uuid);
				reportedPlayer = BukkitUtil.formatPlayerName(player, displayRealName);
			}
			
			reportedWorld = result.getString("ReportedWorld");
			reportedX = (int) Math.round(result.getDouble("ReportedX"));
			reportedY = (int) Math.round(result.getDouble("ReportedY"));
			reportedZ = (int) Math.round(result.getDouble("ReportedZ"));
			
			reportDetails = result.getString("Details");
			dateReport = result.getString("Date");
			
			final int priorityLevel = result.getInt("Priority");
			final ModLevel priorityModLevel = ModLevel.getByLevel(priorityLevel);
			priority = priorityModLevel.getColor() + priorityModLevel.getName();
			
			claimStatus = result.getBoolean("ClaimStatus");
			claimedBy = result.getString("ClaimedBy");
			if(!result.getString("ClaimedByUUID").isEmpty()) {
				final UUID uuid = UUID.fromString(result.getString("ClaimedByUUID"));
				player = Bukkit.getOfflinePlayer(uuid);
				claimedBy = BukkitUtil.formatPlayerName(player, displayRealName);
			}
			
			claimDate = result.getString("ClaimDate");
			completionStatus = result.getBoolean("CompletionStatus");
			completedBy = result.getString("CompletedBy");
			
			if(!result.getString("CompletedByUUID").isEmpty()) {
				final UUID uuid = UUID.fromString(result.getString("CompletedByUUID"));
				player = Bukkit.getOfflinePlayer(uuid);
				completedBy = BukkitUtil.formatPlayerName(player, displayRealName);
			}
			
			completionStatus = result.getBoolean("CompletionStatus");
			
			completionDate = result.getString("CompletionDate");
			summaryDetails = result.getString("CompletionSummary");
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to display report view!");
			throw e;
		} finally {
			database.closeConnection(connectionId);
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

	private void quickViewCompleted(final CommandSender sender, final String[][] reports) {
		final Locale locale = getManager().getLocale();
		final String header = locale.getString(ViewPhrases.viewAllCompleteHeader);
		
		sender.sendMessage(ChatColor.GREEN + "-----" + ChatColor.GREEN + header + ChatColor.GREEN + "------");
		
		final String reportHeader = locale.getString(ViewPhrases.viewAllReportHeader);
		final String reportDetails = locale.getString(ViewPhrases.viewAllReportDetails);
		
		for(int LCV = 0; LCV < reports.length; LCV++) {
			String out = reportHeader.replaceAll("%i", ChatColor.GOLD + reports[LCV][0] + ChatColor.WHITE);
			out = out.replaceAll("%s", ChatColor.BLUE + reports[LCV][1] + ChatColor.WHITE);
			out = out.replaceAll("%r", ChatColor.RED + reports[LCV][2] + ChatColor.WHITE);
			
			sender.sendMessage(ChatColor.WHITE + out);
			
			// Fix for replace all error with $
			reports[LCV][3] = reports[LCV][3].replaceAll("\\$", "\\\\\\$");
			
			sender.sendMessage(ChatColor.WHITE + reportDetails.replaceAll("%d", ChatColor.GOLD + reports[LCV][3] + ChatColor.WHITE));
		}
	}
	
	private void quickViewIncomplete(final CommandSender sender, final String[][] reports) {
		final Locale locale = getManager().getLocale();
		final String header = locale.getString(ViewPhrases.viewAllUnfinishedHeader);
		
		sender.sendMessage(ChatColor.GREEN + "-----" + ChatColor.GREEN + header + ChatColor.GREEN + "------");
		
		final String reportHeader = BukkitUtil.colorCodeReplaceAll(
				locale.getString(ViewPhrases.viewAllReportHeader));
		final String reportDetails = BukkitUtil.colorCodeReplaceAll(
				locale.getString(ViewPhrases.viewAllReportDetails));
		
		for(int LCV = 0; LCV < reports.length; LCV++) {
			String out = reportHeader.replaceAll("%i", ChatColor.GOLD + reports[LCV][0] + ChatColor.WHITE);
			out = out.replaceAll("%s", ChatColor.BLUE + reports[LCV][1] + ChatColor.WHITE);
			out = out.replaceAll("%r", ChatColor.RED + reports[LCV][2] + ChatColor.WHITE);
			
			sender.sendMessage(ChatColor.WHITE + out);
			
			// Fix for replace all error with $
			reports[LCV][3] = reports[LCV][3].replaceAll("\\$", "\\\\\\$");
			
			sender.sendMessage(ChatColor.WHITE + reportDetails.replaceAll("%d", ChatColor.GOLD + reports[LCV][3] + ChatColor.WHITE));
		}
	}
	
	private void quickViewAll(final CommandSender sender, final String[][] complete, final String[][] notComplete) {
		final String viewAllBegin = BukkitUtil.colorCodeReplaceAll(
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
			String completedBy, String completionDate, String summaryDetails) {
		final Locale locale = getManager().getLocale();
		final boolean displayLocation = getManager().getConfig().getBoolean("general.viewing.displayLocation", true);
		StringBuilder output;
		
		String begin = BukkitUtil.colorCodeReplaceAll(locale.getString(ViewPhrases.viewBegin));
		begin = begin.replaceAll("%i", ChatColor.GOLD + Integer.toString(id));
		sender.sendMessage(ChatColor.WHITE + "-----" + ChatColor.BLUE + begin + ChatColor.WHITE + "------");
		
		if(!displayLocation || senderWorld.equals("") && senderX == 0 && senderY == 0 && senderZ == 0) {
			sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					locale.getString(ViewPhrases.viewSender)) + ' ' + ChatColor.BLUE + reporter);
		} else {
			output = new StringBuilder();
			output.append(locale.getString(ViewPhrases.viewSender)).append(' ');
			output.append(ChatColor.BLUE).append(reporter).append(ChatColor.GOLD).append(' ');
			output.append('(').append(senderWorld).append(": ");
			output.append(senderX).append(", ").append(senderY).append(", ").append(senderZ).append(')');
			sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(output.toString()));
		}
		
		if(!displayLocation || reportedWorld.equals("") && reportedX == 0 && reportedY == 0 && reportedZ == 0) {
			sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ViewPhrases.viewReported)) + ' ' + ChatColor.RED + reportedPlayer);
		} else {
			output = new StringBuilder();
			output.append(locale.getString(ViewPhrases.viewReported)).append(' ');
			output.append(ChatColor.BLUE).append(reportedPlayer).append(ChatColor.GOLD).append(' ');
			output.append('(').append(reportedWorld).append(": ");
			output.append(reportedX).append(", ").append(reportedY).append(", ").append(reportedZ).append(')');
			sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(output.toString()));
		}
		
		sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
				locale.getString(ViewPhrases.viewDetails)) + ' ' + ChatColor.GOLD + reportDetails);
		sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
				locale.getString(ViewPhrases.viewPriority)) + ' ' + priority);
		sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
				locale.getString(ViewPhrases.viewDate)) + ' ' + ChatColor.GREEN + dateReport);
		
		sender.sendMessage(ChatColor.WHITE + "------" + ChatColor.BLUE + BukkitUtil.colorCodeReplaceAll(
				locale.getString(ViewPhrases.viewSummaryTitle)) + ChatColor.WHITE + "------");
		
		if(!completionStatus) {
			if(claimStatus) {
				sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
						locale.getString(ViewPhrases.viewClaimHeader) + ' ' +
						ChatColor.GREEN + locale.getString(ViewPhrases.viewStatusClaimed)));
				
				sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
						locale.getString(ViewPhrases.viewClaimedBy) + ' ' + ChatColor.BLUE + claimedBy));
				
				sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
						locale.getString(ViewPhrases.viewClaimedOn) + ' ' + ChatColor.GREEN + claimDate));
			} else {
				sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
						locale.getString(ViewPhrases.viewClaimHeader) + ' ' +
						ChatColor.RED + locale.getString(ViewPhrases.viewStatusUnclaimed)));
			}
		}
		
		if(!completionStatus) {
			sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					locale.getString(ViewPhrases.viewCompletionStatus)) + ' ' +
					ChatColor.RED + BukkitUtil.colorCodeReplaceAll(
							locale.getString(ViewPhrases.viewUnfinished)));
		} else {
			sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					locale.getString(ViewPhrases.viewCompletionStatus)) + ' ' +
					ChatColor.GREEN + BukkitUtil.colorCodeReplaceAll(
							locale.getString(ViewPhrases.viewFinished)));
			sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					locale.getString(ViewPhrases.viewCompletedBy)) + ' ' + ChatColor.BLUE + completedBy);
			sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					locale.getString(ViewPhrases.viewCompletedOn)) + ' ' + ChatColor.GREEN + completionDate);
			if(!summaryDetails.equals("")) {
				sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
						locale.getString(ViewPhrases.viewCompletedSummary)) + ' ' + ChatColor.GOLD + summaryDetails);
			} else {
				sender.sendMessage(ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
						locale.getString(ViewPhrases.viewCompletedSummary)) + ' ' +
						ChatColor.GOLD + BukkitUtil.colorCodeReplaceAll(
								locale.getString(ViewPhrases.viewNoSummary)));
			}
		}
	}
	
	/**
	 * @see net.KabOOm356.Command.ReporterCommand#updateDocumentation()
	 */
	@Override
	public void updateDocumentation()
	{
		final Locale locale = getManager().getLocale();
		final ArrayList<ObjectPair<String, String>> usages = super.getUsages();
		usages.clear();
		
		String usage = locale.getString(ViewPhrases.viewHelp);
		String description = locale.getString(ViewPhrases.viewHelpDetails);
		
		ObjectPair<String, String> entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report view all [name]";
		description = locale.getString(ViewPhrases.viewHelpAllDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report view completed|finished [name]";
		description = locale.getString(ViewPhrases.viewHelpCompletedDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report view incomplete|unfinished [name]";
		description = locale.getString(ViewPhrases.viewHelpIncompleteDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report view priority [name]";
		description = locale.getString(ViewPhrases.viewHelpPriorityDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = locale.getString(ViewPhrases.viewHelpGivenPriority);
		description = locale.getString(ViewPhrases.viewHelpGivenPriorityDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report view claimed [name]";
		description = locale.getString(ViewPhrases.viewHelpClaimedDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report view claimed priority [name]";
		description = locale.getString(ViewPhrases.viewHelpClaimedPriorityDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = locale.getString(ViewPhrases.viewHelpClaimedGivenPriority);
		description = locale.getString(ViewPhrases.viewHelpClaimedPriorityDetails);
		
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
