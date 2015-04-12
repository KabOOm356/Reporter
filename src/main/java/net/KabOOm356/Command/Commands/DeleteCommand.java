package net.KabOOm356.Command.Commands;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map.Entry;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Database.SQL.QueryType;
import net.KabOOm356.Locale.Entry.LocalePhrases.DeletePhrases;
import net.KabOOm356.Manager.SQLStatManagers.ModeratorStatManager.ModeratorStat;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.ObjectPair;
import net.KabOOm356.Util.Util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * A {@link ReporterCommand} that will handle deleting reports.
 */

public class DeleteCommand extends ReporterCommand
{
	private static final Logger log = LogManager.getLogger(DeleteCommand.class);
	
	private static final String name = "Delete";
	private static final int minimumNumberOfArguments = 1;
	private final static String permissionNode = "reporter.delete";
	
	/**
	 * Constructor.
	 * 
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public DeleteCommand(ReporterCommandManager manager)
	{
		super(manager, name, permissionNode, minimumNumberOfArguments);
		
		super.getAliases().add("Remove");
		
		updateDocumentation();
	}
	
	private enum BatchDeletionType
	{
		ALL, INCOMPLETE, COMPLETE;
	}
	
	private enum PlayerDeletionType
	{
		SENDER, REPORTED;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(CommandSender sender, ArrayList<String> args)
	{
		if(!hasRequiredPermission(sender))
			return;
		
		if(args.get(0).equalsIgnoreCase("all"))
			deleteReportBatch(sender, BatchDeletionType.ALL);
		else if(args.get(0).equalsIgnoreCase("completed") || args.get(0).equalsIgnoreCase("finished"))
			deleteReportBatch(sender, BatchDeletionType.COMPLETE);
		else if(args.get(0).equalsIgnoreCase("incomplete") || args.get(0).equalsIgnoreCase("unfinished"))
			deleteReportBatch(sender, BatchDeletionType.INCOMPLETE);
		else
		{
			if(Util.isInteger(args.get(0)) || args.get(0).equalsIgnoreCase("last"))
			{
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
				
				deleteReport(sender, index);
			}
			else // /report delete <Player Name> [reported/sender]
			{
				OfflinePlayer player = getManager().getPlayer(args.get(0));
				
				if(player != null)
				{
					if(args.size() >= 2 && args.get(1).equalsIgnoreCase("sender"))
						deletePlayer(sender, PlayerDeletionType.SENDER, player);
					else
						deletePlayer(sender, PlayerDeletionType.REPORTED, player);
				}
			}
		}
	}
	
	private void deleteReport(CommandSender sender, int index)
	{
		try
		{
			deleteReport(index);
			
			String out = getManager().getLocale().getString(DeletePhrases.deleteReport);
			
			out = out.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);
			
			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + out);
			
			reformatTables(sender, index);
			
			updateLastViewed(index);
			
			getManager().getMessageManager().removeMessage(index);
		}
		catch (final Exception e)
		{
			log.log(Level.ERROR, "Failed to delete report!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}
		finally
		{
			getManager().getDatabaseHandler().closeConnection();
		}
		
		if(BukkitUtil.isOfflinePlayer(sender))
		{
			OfflinePlayer senderPlayer = (OfflinePlayer) sender;
		
			getManager().getModStatsManager().incrementStat(senderPlayer, ModeratorStat.DELETED);
		}
	}
	
	private void deleteReport(int index) throws ClassNotFoundException, SQLException
	{
		String query = "Delete FROM Reports WHERE ID = " + index;
		
		getManager().getDatabaseHandler().updateQuery(query);
	}
	
	private void deletePlayer(CommandSender sender, PlayerDeletionType deletion, OfflinePlayer player)
	{
		String query = getQuery(sender, player, QueryType.SELECT, deletion);
		
		try
		{
			int count = getManager().getCount();
			
			ArrayList<Integer> remainingIndexes = new ArrayList<Integer>();
			
			SQLResultSet result = getManager().getDatabaseHandler().sqlQuery(query);
			
			for(ResultRow row : result)
				remainingIndexes.add(row.getInt("ID"));
			
			query = getQuery(sender, player, QueryType.DELETE, deletion);
			
			getManager().getDatabaseHandler().updateQuery(query);
			
			String message;
			
			if(deletion == PlayerDeletionType.REPORTED)
				message = getManager().getLocale().getString(DeletePhrases.deletePlayerReported);
			else
				message = getManager().getLocale().getString(DeletePhrases.deletePlayerSender);
			
			String displayName = player.getName();
			
			if(player.isOnline())
				displayName = player.getPlayer().getDisplayName();
			
			String playerName = BukkitUtil.formatPlayerName(displayName, player.getName());
			
			message = message.replaceAll("%p", ChatColor.BLUE + playerName + ChatColor.WHITE);
			
			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + 
					ChatColor.WHITE + message);
			
			int totalDeleted = count - remainingIndexes.size();
			
			// Display the total number of reports delete.
			displayTotalReportsDeleted(sender, totalDeleted);
			
			reformatTables(sender, remainingIndexes);
			
			updateLastViewed(remainingIndexes);
			
			getManager().getMessageManager().reindexMessages(remainingIndexes);
			
			// Log the statistic.
			if(BukkitUtil.isPlayer(sender))
			{
				Player senderPlayer = (Player) sender;
				
				getManager().getModStatsManager().incrementStat(
						senderPlayer,
						ModeratorStat.DELETED,
						totalDeleted);
			}
			
		}
		catch (final Exception e)
		{
			log.log(Level.ERROR, "Failed to delete reports for a player!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}
	}
	
	private String getQuery(CommandSender sender, OfflinePlayer player, QueryType queryType, PlayerDeletionType deletion)
	{
		if(queryType == QueryType.DELETE)
			return getDeleteQuery(sender, player, deletion);
		else
			return getSelectQuery(sender, player, deletion);
	}
	
	private String getSelectQuery(CommandSender sender, OfflinePlayer player, PlayerDeletionType deletion)
	{
		final StringBuilder query = new StringBuilder();
		query.append("SELECT ID FROM Reports WHERE ");
		ModLevel level = getManager().getModLevel(sender);
		
		if(sender.isOp() || sender instanceof ConsoleCommandSender)
		{
			if(player.getName().equalsIgnoreCase("* (Anonymous)"))
			{
				if(deletion == PlayerDeletionType.REPORTED)
					query.append("Reported != '").append(player.getName()).append("'");
				else if(deletion == PlayerDeletionType.SENDER)
					query.append("Sender != '").append(player.getName()).append("'");
			}
			else
			{
				if(deletion == PlayerDeletionType.REPORTED)
					query.append("ReportedUUID != '").append(player.getUniqueId()).append("'");
				else if(deletion == PlayerDeletionType.SENDER)
					query.append("SenderUUID != '").append(player.getUniqueId()).append("'");
			}
		}
		else
		{
			query.append("NOT (Priority <= ").append(level.getLevel())
					.append(" AND (ClaimStatus = 0 OR ClaimPriority < ").append(level.getLevel())
					.append(" OR ");
			
			if(BukkitUtil.isPlayer(sender))
			{
				Player senderPlayer = (Player) sender;
				
				query.append("ClaimedByUUID = '").append(senderPlayer.getUniqueId()).append("') ");
			}
			else
			{
				query.append("ClaimedBy = '").append(sender.getName()).append("') ");
			}
			
			if(player.getName().equalsIgnoreCase("* (Anonymous)"))
			{
				if(deletion == PlayerDeletionType.REPORTED)
					query.append("AND Reported = '").append(player.getName()).append("')");
				else if(deletion == PlayerDeletionType.SENDER)
					query.append("AND Sender = '").append(player.getName()).append("')");
			}
			else
			{
				if(deletion == PlayerDeletionType.REPORTED)
					query.append("AND ReportedUUID = '").append(player.getUniqueId()).append("')");
				else if(deletion == PlayerDeletionType.SENDER)
					query.append("AND SenderUUID = '").append(player.getUniqueId()).append("')");
			}
		}
		
		return query.toString();
	}
	
	private String getDeleteQuery(CommandSender sender, OfflinePlayer player, PlayerDeletionType deletion)
	{
		final StringBuilder query = new StringBuilder();
		query.append("DELETE FROM Reports WHERE ");
		ModLevel level = getManager().getModLevel(sender);
		
		if(sender.isOp() || sender instanceof ConsoleCommandSender)
		{
			if(player.getName().equals("* (Anonymous)"))
			{
				if(deletion == PlayerDeletionType.REPORTED)
					query.append("Reported = '").append(player.getName()).append("'");
				else if(deletion == PlayerDeletionType.SENDER)
					query.append("Sender = '").append(player.getName()).append("'");
			}
			else
			{
				if(deletion == PlayerDeletionType.REPORTED)
					query.append("ReportedUUID = '").append(player.getUniqueId()).append("'");
				else if(deletion == PlayerDeletionType.SENDER)
					query.append("SenderUUID = '").append(player.getUniqueId()).append("'");
			}
		}
		else
		{
			query.append("(Priority <= ").append(level.getLevel())
					.append(" AND (ClaimStatus = 0 OR ")
					.append("ClaimPriority < ").append(level.getLevel())
					.append(" OR ");
			
			if(BukkitUtil.isPlayer(sender))
			{
				Player senderPlayer = (Player) sender;
				
				query.append("ClaimedByUUID = '").append(senderPlayer.getUniqueId()).append("') ");
			}
			else
			{
				query.append("ClaimedBy = '").append(sender.getName()).append("') ");
			}
					
			if(player.getName().equals("* (Anonymous)"))
			{
				if(deletion == PlayerDeletionType.REPORTED)
					query.append("AND Reported = '").append(player.getName()).append("')");
				else if(deletion == PlayerDeletionType.SENDER)
					query.append("AND Sender = '").append(player.getName()).append("')");
			}
			else
			{
				if(deletion == PlayerDeletionType.REPORTED)
					query.append("AND ReportedUUID = '").append(player.getUniqueId()).append("')");
				else if(deletion == PlayerDeletionType.SENDER)
					query.append("AND SenderUUID = '").append(player.getUniqueId()).append("')");
			}
		}
		
		return query.toString();
	}
	
	private void deleteReportBatch(CommandSender sender, BatchDeletionType deletion)
	{
		try
		{
			int beforeDeletion = getManager().getCount();
			
			ArrayList<Integer> remainingIndexes = getRemainingIndexes(sender, deletion);
			
			int afterDeletion = remainingIndexes.size();
			
			int totalDeleted = beforeDeletion - afterDeletion;
			
			deleteBatch(sender, deletion);
			
			reformatTables(sender, remainingIndexes);
			
			updateLastViewed(remainingIndexes);
			
			getManager().getMessageManager().reindexMessages(remainingIndexes);
			
			String message = "";
			
			if (deletion == BatchDeletionType.ALL)
				message = getManager().getLocale().getString(DeletePhrases.deleteAll);
			else if (deletion == BatchDeletionType.COMPLETE)
				message = getManager().getLocale().getString(DeletePhrases.deleteComplete);
			else if (deletion == BatchDeletionType.INCOMPLETE)
				message = getManager().getLocale().getString(DeletePhrases.deleteIncomplete);
			
			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + message);
			
			displayTotalReportsDeleted(sender, totalDeleted);
			
			// Log the statistic.
			if(BukkitUtil.isPlayer(sender))
			{
				Player senderPlayer = (Player) sender;
				
				getManager().getModStatsManager().incrementStat(
						senderPlayer,
						ModeratorStat.DELETED,
						totalDeleted);
			}
		}
		catch (final Exception e)
		{
			log.log(Level.ERROR, "Failed to delete batch of reports!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}
		finally
		{
			getManager().getDatabaseHandler().closeConnection();
		}
	}
	
	private void deleteBatch(CommandSender sender, BatchDeletionType deletion) throws ClassNotFoundException, SQLException
	{
		String query = getQuery(sender, QueryType.DELETE, deletion);
		
		try
		{
			getManager().getDatabaseHandler().updateQuery(query);
		}
		finally
		{
			getManager().getDatabaseHandler().closeConnection();
		}
	}
	
	/*
	 * Will create the SQL query to perform the wanted query type and deletion type.
	 */
	private String getQuery(CommandSender sender, QueryType type, BatchDeletionType deletion)
	{
		if (type == QueryType.DELETE)
			return getDeleteQuery(sender, deletion);
		else
			return getSelectQuery(sender, deletion);
	}
	
	private String getSelectQuery(CommandSender sender, BatchDeletionType deletion)
	{
		final StringBuilder query = new StringBuilder();
		query.append("SELECT ID FROM Reports WHERE ");
		
		if(sender.isOp() || sender instanceof ConsoleCommandSender)
		{
			if (deletion == BatchDeletionType.ALL)
				query.append("0");
			else if (deletion == BatchDeletionType.COMPLETE)
				query.append("CompletionStatus = 0");
			else if (deletion == BatchDeletionType.INCOMPLETE)
				query.append("CompletionStatus = 1");
		}
		else
		{
			ModLevel level = getManager().getModLevel(sender);
			
			/*
			 * Reports will be deleted (not remain) if:
			 * 
			 * 1. Their priority is less than or equal to the sender's modlevel.
			 * 
			 * 2. The report is unclaimed,
			 *    or claimed by another player with a lower modlevel,
			 *    or claimed by the sender.
			 */
			query.append("NOT (Priority <= ").append(level.getLevel()).append(" ")
					.append("AND ")
					.append("(ClaimStatus = 0 ")
					.append("OR ")
					.append("ClaimPriority < ").append(level.getLevel()).append(" ")
					.append("OR ");
			
			if(BukkitUtil.isPlayer(sender))
			{
				Player senderPlayer = (Player) sender;
				
				query.append("ClaimedByUUID = '").append(senderPlayer.getUniqueId()).append("')");
			}
			else
			{
				query.append("ClaimedBy = '").append(sender.getName()).append("')");
			}
					
			
			if (deletion == BatchDeletionType.ALL)
				query.append(")");
			else if (deletion == BatchDeletionType.COMPLETE)
			{
				query.append(" ")
						.append("AND ")
						.append("CompletionStatus = 0)");
			}
			else if (deletion == BatchDeletionType.INCOMPLETE)
			{
				query.append(" ")
						.append("AND ")
						.append("CompletionStatus = 1)");
			}
		}
		
		return query.toString();
	}
	
	private String getDeleteQuery(CommandSender sender, BatchDeletionType deletion)
	{
		final StringBuilder query = new StringBuilder();
		query.append("DELETE FROM Reports WHERE ");
		
		if(sender.isOp() || sender instanceof ConsoleCommandSender)
		{
			if (deletion == BatchDeletionType.ALL)
				query.append("1");
			else if (deletion == BatchDeletionType.COMPLETE)
				query.append("CompletionStatus = 1");
			else if (deletion == BatchDeletionType.INCOMPLETE)
				query.append("CompletionStatus = 0");
		}
		else
		{
			ModLevel level = getManager().getModLevel(sender);
			
			/*
			 * Reports will be deleted (not remain) if:
			 * 
			 * 1. Their priority is less than or equal to the sender's modlevel.
			 * 
			 * 2. The report is unclaimed,
			 *    or claimed by another player with a lower modlevel,
			 *    or claimed by the sender.
			 */
			query.append("(Priority <= ").append(level.getLevel()).append(" ")
					.append("AND ")
					.append("(ClaimStatus = 0 ")
					.append("OR ")
					.append("ClaimPriority < ").append(level.getLevel()).append(" ")
					.append("OR ");
			
			if(BukkitUtil.isPlayer(sender))
			{
				Player senderPlayer = (Player) sender;
				
				query.append("ClaimedByUUID = '").append(senderPlayer.getUniqueId()).append("')");
			}
			else
			{
				query.append("ClaimedBy = '").append(sender.getName()).append("')");
			}
			
			// Append on the rest of the query to perform the required deletion type.
			if (deletion == BatchDeletionType.ALL)
				query.append(")");
			else if (deletion == BatchDeletionType.COMPLETE)
			{
				query.append(" ")
						.append("AND ")
						.append("CompletionStatus = 1)");
			}
			else if (deletion == BatchDeletionType.INCOMPLETE)
			{
				query.append(" ")
						.append("AND ")
						.append("CompletionStatus = 0)");
			}
		}
		
		return query.toString();
	}
	
	private ArrayList<Integer> getRemainingIndexes(CommandSender sender, BatchDeletionType deletion) throws ClassNotFoundException, SQLException
	{
		ArrayList<Integer> remainingIDs = new ArrayList<Integer>();
		
		try
		{
			String query = getQuery(sender, QueryType.SELECT, deletion);
			
			SQLResultSet result = getManager().getDatabaseHandler().sqlQuery(query);
			
			for(ResultRow row : result)
				remainingIDs.add(row.getInt("ID"));
		}
		finally
		{
			getManager().getDatabaseHandler().closeConnection();
		}
		
		return remainingIDs;
	}
	
	private void updateLastViewed(int removedIndex)
	{
		for(Entry<CommandSender, Integer> e : getManager().getLastViewed().entrySet())
		{
			if(e.getValue() == removedIndex)
				e.setValue(-1);
			else if(e.getValue() > removedIndex)
				e.setValue(e.getValue()-1);
		}
	}
	
	private void updateLastViewed(ArrayList<Integer> remainingIndexes)
	{
		for(Entry<CommandSender, Integer> e : getManager().getLastViewed().entrySet())
		{
			if(remainingIndexes.contains(e.getValue()))
				e.setValue(remainingIndexes.indexOf(e.getValue())+1);
			else
				e.setValue(-1);
		}
	}
	
	private void reformatTables(CommandSender sender, ArrayList<Integer> remainingIndexes)
	{
		StringBuilder query;
		Statement stmt = null;
		
		try
		{
			stmt = getManager().getDatabaseHandler().createStatement();
			
			for(int LCV = 0; LCV < remainingIndexes.size(); LCV++)
			{
				query = new StringBuilder();
				query.append("UPDATE Reports SET ID=").append((LCV+1)).append(" WHERE ID=").append(remainingIndexes.get(LCV));
				
				stmt.addBatch(query.toString());
			}
			
			stmt.executeBatch();
		}
		catch(final Exception e)
		{
			log.log(Level.ERROR, "Failed reformatting tables after batch delete!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}
		finally
		{
			try
			{
				if(stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
			}
			
			getManager().getDatabaseHandler().closeConnection();
		}
		
		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
				ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
				getManager().getLocale().getString(DeletePhrases.SQLTablesReformat)));
	}
	
	private void reformatTables(CommandSender sender, int removedIndex)
	{
		int count = getManager().getCount();
		
		try
		{
			StringBuilder formatQuery;
			
			for (int LCV = removedIndex; LCV <= count; LCV++)
			{
				formatQuery = new StringBuilder();
				formatQuery.append("UPDATE Reports SET ID=").append(LCV).append(" WHERE ID=").append((LCV+1));
				
				getManager().getDatabaseHandler().updateQuery(formatQuery.toString());
			}
		}
		catch(final Exception e)
		{
			log.log(Level.ERROR, "Failed to reformat table after delete!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}
		finally
		{
			getManager().getDatabaseHandler().closeConnection();
		}
		
		if(count != -1)
			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
					ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(DeletePhrases.SQLTablesReformat)));
	}
	
	private void displayTotalReportsDeleted(CommandSender sender, int totalDeleted)
	{
		String message = getManager().getLocale().getString(DeletePhrases.deletedReportsTotal);
		
		message = message.replaceAll("%r", ChatColor.RED + Integer.toString(totalDeleted) + ChatColor.WHITE);
		
		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + message);
	}
	
	/**
	 * @see net.KabOOm356.Command.ReporterCommand#updateDocumentation()
	 */
	@Override
	public void updateDocumentation()
	{
		ArrayList<ObjectPair<String, String>> usages = super.getUsages();
		
		usages.clear();
		
		String usage = getManager().getLocale().getString(DeletePhrases.deleteHelp);
		String description = getManager().getLocale().getString(DeletePhrases.deleteHelpDetails);
		
		ObjectPair<String, String> entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report delete/remove all";
		description = getManager().getLocale().getString(DeletePhrases.deleteHelpAllDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report delete/remove completed|finished";
		description = getManager().getLocale().getString(DeletePhrases.deleteHelpCompletedDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report delete/remove incomplete|unfinished";
		description = getManager().getLocale().getString(DeletePhrases.deleteHelpIncompleteDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = getManager().getLocale().getString(DeletePhrases.deleteHelpPlayer);
		description = getManager().getLocale().getString(DeletePhrases.deleteHelpPlayerDetails);
		
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
