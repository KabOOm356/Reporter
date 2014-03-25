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
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * A {@link ReporterCommand} that will handle deleting reports.
 */
public class DeleteCommand extends ReporterCommand
{
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
		
		super.addAlias("Remove");
		
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
		catch (Exception ex)
		{
			ex.printStackTrace();
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
	}
	
	private void deleteReport(int index) throws ClassNotFoundException, SQLException
	{
		String query = "Delete FROM Reports WHERE ID = " + index;
		
		getManager().getDatabaseHandler().updateQuery(query);
	}
	
	private void deletePlayer(CommandSender sender, PlayerDeletionType deletion, OfflinePlayer player)
	{
		String query = getQuery(sender, player.getName(), QueryType.SELECT, deletion);
		
		try
		{
			ArrayList<Integer> remainingIndexes = new ArrayList<Integer>();
			
			SQLResultSet result = getManager().getDatabaseHandler().sqlQuery(query);
			
			for(ResultRow row : result)
				remainingIndexes.add(row.getInt("ID"));
			
			query = getQuery(sender, player.getName(), QueryType.DELETE, deletion);
			
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
			
			reformatTables(sender, remainingIndexes);
			
			updateLastViewed(remainingIndexes);
			
			getManager().getMessageManager().reindexMessages(remainingIndexes);
		}
		catch (Exception e)
		{
			sender.sendMessage(getErrorMessage());
			e.printStackTrace();
			return;
		}
	}
	
	private String getQuery(CommandSender sender, String playerName, QueryType queryType, PlayerDeletionType deletion)
	{
		if(queryType == QueryType.DELETE)
			return getDeleteQuery(sender, playerName, deletion);
		else
			return getSelectQuery(sender, playerName, deletion);
	}
	
	private String getSelectQuery(CommandSender sender, String playerName, PlayerDeletionType deletion)
	{
		String query = "SELECT ID FROM Reports WHERE ";
		ModLevel level = getManager().getModLevel(sender);
		
		if(sender.isOp() || sender instanceof ConsoleCommandSender)
		{
			if(deletion == PlayerDeletionType.REPORTED)
				query += "ReportedRaw != '" + playerName + "'";
			else if(deletion == PlayerDeletionType.SENDER)
				query += "SenderRaw != '" + playerName + "'";
		}
		else
		{
			query += "NOT (Priority <= " + level.getLevel() + " " +
					"AND " +
					"(ClaimStatus = 0 " +
					"OR " +
					"ClaimPriority < " + level.getLevel() + " " +
					"OR " +
					"ClaimedByRaw = '" + sender.getName() + "') ";
			
			if(deletion == PlayerDeletionType.REPORTED)
				query += "AND ReportedRaw = '" + playerName + "')";
			else if(deletion == PlayerDeletionType.SENDER)
				query += "AND SenderRaw = '" + playerName + "')";
		}
		
		return query;
	}
	
	private String getDeleteQuery(CommandSender sender, String playerName, PlayerDeletionType deletion)
	{
		String query = "DELETE FROM Reports WHERE ";
		ModLevel level = getManager().getModLevel(sender);
		
		if(sender.isOp() || sender instanceof ConsoleCommandSender)
		{
			if(deletion == PlayerDeletionType.REPORTED)
				query += "ReportedRaw = '" + playerName + "'";
			else if(deletion == PlayerDeletionType.SENDER)
				query += "SenderRaw = '" + playerName + "'";
		}
		else
		{
			query += "(Priority <= " + level.getLevel() + " " +
					"AND " +
					"(ClaimStatus = 0 " +
					"OR " +
					"ClaimPriority < " + level.getLevel() + " " +
					"OR " +
					"ClaimedByRaw = '" + sender.getName() + "') ";
			
			if(deletion == PlayerDeletionType.REPORTED)
				query += "AND ReportedRaw = '" + playerName + "')";
			else if(deletion == PlayerDeletionType.SENDER)
				query += "AND SenderRaw = '" + playerName + "')";
		}
		
		return query;
	}
	
	private void deleteReportBatch(CommandSender sender, BatchDeletionType deletion)
	{
		try
		{
			ArrayList<Integer> remainingIndexes = getRemainingIndexes(sender, deletion);
			
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
		}
		catch (Exception e)
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
			catch (Exception e)
			{}
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
			try
			{
				getManager().getDatabaseHandler().closeConnection();
			}
			catch (Exception e)
			{}
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
		String query = "SELECT ID FROM Reports WHERE ";
		
		if(sender.isOp() || sender instanceof ConsoleCommandSender)
		{
			if (deletion == BatchDeletionType.ALL)
				query += "0";
			else if (deletion == BatchDeletionType.COMPLETE)
				query += "CompletionStatus = 0";
			else if (deletion == BatchDeletionType.INCOMPLETE)
				query += "CompletionStatus = 1";
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
			query += "NOT (Priority <= " + level.getLevel() + " " +
					"AND " +
					"(ClaimStatus = 0 " +
					"OR " +
					"ClaimPriority < " + level.getLevel() + " " +
					"OR " +
					"ClaimedByRaw = '" + sender.getName() + "')";
			
			if (deletion == BatchDeletionType.ALL)
				query += ")";
			else if (deletion == BatchDeletionType.COMPLETE)
			{
				query += " " +
						"AND " +
						"CompletionStatus = 0)";
			}
			else if (deletion == BatchDeletionType.INCOMPLETE)
			{
				query += " " +
						"AND " +
						"CompletionStatus = 1)";
			}
		}
		
		return query;
	}
	
	private String getDeleteQuery(CommandSender sender, BatchDeletionType deletion)
	{
		String query = "DELETE FROM Reports WHERE ";
		
		if(sender.isOp() || sender instanceof ConsoleCommandSender)
		{
			if (deletion == BatchDeletionType.ALL)
				query += "1";
			else if (deletion == BatchDeletionType.COMPLETE)
				query += "CompletionStatus = 1";
			else if (deletion == BatchDeletionType.INCOMPLETE)
				query += "CompletionStatus = 0";
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
			query += "(Priority <= " + level.getLevel() + " " +
					"AND " +
					"(ClaimStatus = 0 " +
					"OR " +
					"ClaimPriority < " + level.getLevel() + " " +
					"OR " +
					"ClaimedByRaw = '" + sender.getName() + "')";
			
			// Append on the rest of the query to perform the required deletion type.
			if (deletion == BatchDeletionType.ALL)
				query += ")";
			else if (deletion == BatchDeletionType.COMPLETE)
			{
				query += " " +
						"AND " +
						"CompletionStatus = 1)";
			}
			else if (deletion == BatchDeletionType.INCOMPLETE)
			{
				query += " " +
						"AND " +
						"CompletionStatus = 0)";
			}
		}
		
		return query;
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
			try
			{
				getManager().getDatabaseHandler().closeConnection();
			}
			catch(Exception e)
			{
			}
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
		String query;
		Statement stmt = null;
		
		try
		{
			stmt = getManager().getDatabaseHandler().createStatement();
			
			for(int LCV = 0; LCV < remainingIndexes.size(); LCV++)
			{
				query = "UPDATE Reports SET ID=" + (LCV+1) + " WHERE ID=" + remainingIndexes.get(LCV);
				
				stmt.addBatch(query);
			}
			
			stmt.executeBatch();
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
				if(stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
			}
			
			try
			{
				getManager().getDatabaseHandler().closeConnection();
			}
			catch (Exception e)
			{
			}
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
			String formatQuery;
			
			for (int LCV = removedIndex; LCV <= count; LCV++)
			{
				formatQuery = "UPDATE Reports SET ID=" + LCV + " WHERE ID=" + (LCV+1);
				
				getManager().getDatabaseHandler().updateQuery(formatQuery);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
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
		
		if(count != -1)
			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
					ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(DeletePhrases.SQLTablesReformat)));
	}
	
	/**
	 * @see net.KabOOm356.Command.ReporterCommand#updateDocumentation()
	 */
	@Override
	public void updateDocumentation()
	{
		super.updateDocumentation(
				getManager().getLocale().getString(DeletePhrases.deleteHelp),
				getManager().getLocale().getString(DeletePhrases.deleteHelpDetails));
		
		removeAllAlternateUsagesAndDescriptions();
		
		addUsage("/report delete/remove all");
		addDescription(getManager().getLocale().getString(DeletePhrases.deleteHelpAllDetails));
		
		addUsage("/report delete/remove completed|finished");
		addDescription(getManager().getLocale().getString(DeletePhrases.deleteHelpCompletedDetails));
		
		addUsage("/report delete/remove incomplete|unfinished");
		addDescription(getManager().getLocale().getString(DeletePhrases.deleteHelpIncompleteDetails));
		
		addUsage(getManager().getLocale().getString(DeletePhrases.deleteHelpPlayer));
		addDescription(getManager().getLocale().getString(DeletePhrases.deleteHelpPlayerDetails));
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
