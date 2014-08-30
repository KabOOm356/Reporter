package net.KabOOm356.Command.Commands;

import java.util.ArrayList;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.ListPhrases;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.ObjectPair;
import net.KabOOm356.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A {@link ReporterCommand} that will handle listing reports.
 */
public class ListCommand extends ReporterCommand
{
	private static final String name = "List";
	private static final int minimumNumberOfArguments = 0;
	private final static String permissionNode = "reporter.list";
	
	/**
	 * Constructor.
	 * 
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public ListCommand(ReporterCommandManager manager)
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
			if (args == null || args.isEmpty())
				listCommand(sender);
			else if (args.size() >= 1 && args.get(0).equalsIgnoreCase("indexes"))
				listIndexes(sender);
			else if (args.size() >= 1 && args.get(0).equalsIgnoreCase("priority"))
			{
				if (args.size() >= 2 && args.get(1).equalsIgnoreCase("indexes"))
					listPriorityIndexes(sender);
				else
					listPriority(sender);
			}
			else if (args.size() >= 1 && args.get(0).equalsIgnoreCase("claimed"))
			{
				if (args.size() >= 3 && args.get(1).equalsIgnoreCase("priority") && args.get(2).equalsIgnoreCase("indexes"))
					listClaimedPriorityIndexes(sender);
				else if (args.size() >= 2 && args.get(1).equalsIgnoreCase("indexes"))
					listClaimedIndexes(sender);
				else if (args.size() >= 2 && args.get(1).equalsIgnoreCase("priority"))
					listClaimedPriority(sender);
				else
					listClaimed(sender);
			}
			else
				sender.sendMessage(ChatColor.RED + BukkitUtil.colorCodeReplaceAll(getUsage()));
		}
		else if (getManager().getConfig().getBoolean("general.canViewSubmittedReports", true))
		{
			ArrayList<Integer> indexes = getManager().getViewableReports((Player)sender);
			
			String indexesString = Util.indexesToString(indexes, ChatColor.GOLD, ChatColor.WHITE);
			
			if(!indexesString.equals(""))
			{
				String out = ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
						getManager().getLocale().getString(ListPhrases.listReportsAvailable));
				out = out.replaceAll("%i", ChatColor.GOLD + indexesString + ChatColor.WHITE);
				sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + out);
			}
			else
				sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
						ChatColor.RED + BukkitUtil.colorCodeReplaceAll(
						getManager().getLocale().getString(ListPhrases.listNoReportsAvailable)));
		}
		else
			sender.sendMessage(getFailedPermissionsMessage());
	}
	
	private void listClaimed(CommandSender sender)
	{
		String query = "SELECT COUNT(*) AS Count " +
				"FROM Reports " +
				"WHERE ClaimStatus = 1 AND ";
		
		if(BukkitUtil.isPlayer(sender))
		{
			Player p = (Player) sender;
			query += "ClaimedByUUID = '" + p.getUniqueId() + "'";
		}
		else
		{
			query += "ClaimedBy = '" + sender.getName() + "'";
		}
		
		try
		{
			SQLResultSet result = getManager().getDatabaseHandler().sqlQuery(query);
			
			int count = result.getInt("Count");
			
			String message = getManager().getLocale().getString(ListPhrases.listClaimed);
			
			message = message.replaceAll("%n", ChatColor.GOLD + Integer.toString(count) + ChatColor.WHITE);
			
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

	private void listClaimedPriority(CommandSender sender)
	{
		String queryFormat = null;
		
		if(BukkitUtil.isPlayer(sender))
		{
			Player p = (Player) sender;
			
			queryFormat = "SELECT COUNT(*) AS Count " +
					"FROM Reports " +
					"WHERE ClaimStatus = 1 " +
					"AND ClaimedByUUID = '" + p.getUniqueId() + "' " +
					"AND Priority = ";
		}
		else
		{
			queryFormat = "SELECT COUNT(*) AS Count " +
					"FROM Reports " +
					"WHERE ClaimStatus = 1 " +
					"AND ClaimedBy = '" + sender.getName() + "' " +
					"AND Priority = ";
		}
		
		int noPriorityCount = 0;
		int lowPriorityCount = 0;
		int normalPriorityCount = 0;
		int highPriorityCount = 0;
		
		SQLResultSet result;
		
		try
		{
			result = getManager().getDatabaseHandler().sqlQuery(queryFormat + ModLevel.NONE.getLevel());
			
			noPriorityCount = result.getInt("Count");
			
			result = getManager().getDatabaseHandler().sqlQuery(queryFormat + ModLevel.LOW.getLevel());
			
			lowPriorityCount = result.getInt("Count");
			
			result = getManager().getDatabaseHandler().sqlQuery(queryFormat + ModLevel.NORMAL.getLevel());
			
			normalPriorityCount = result.getInt("Count");
			
			result = getManager().getDatabaseHandler().sqlQuery(queryFormat + ModLevel.HIGH.getLevel());
			
			highPriorityCount = result.getInt("Count");
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
		
		printClaimedPriorityCount(sender, ModLevel.NONE, noPriorityCount);
		printClaimedPriorityCount(sender, ModLevel.LOW, lowPriorityCount);
		printClaimedPriorityCount(sender, ModLevel.NORMAL, normalPriorityCount);
		printClaimedPriorityCount(sender, ModLevel.HIGH, highPriorityCount);
	}

	private void printClaimedPriorityCount(CommandSender sender, ModLevel level, int count)
	{
		String format = getManager().getLocale().getString(ListPhrases.listClaimedPriorityCount);
		
		ChatColor priorityColor = ModLevel.getModLevelColor(level);
		
		String output = format.replaceAll("%n", priorityColor + Integer.toString(count) + ChatColor.WHITE);
		output = output.replaceAll("%p", priorityColor + level.getName() + ChatColor.WHITE);
		
		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);
	}

	private void listClaimedIndexes(CommandSender sender)
	{
		String query = "SELECT ID " +
				"FROM Reports " +
				"WHERE ClaimStatus = 1 AND ";
		
		if(BukkitUtil.isPlayer(sender))
		{
			Player p = (Player) sender;
			query += "ClaimedByUUID = '" + p.getUniqueId() + "'";
		}
		else
		{
			query += "ClaimedBy = '" + sender.getName() + "'";
		}
		
		try
		{
			SQLResultSet result = getManager().getDatabaseHandler().sqlQuery(query);
			
			String indexes = Util.indexesToString(
					result,
					"ID", ChatColor.GOLD, ChatColor.WHITE);
			
			String message = getManager().getLocale().getString(ListPhrases.listClaimedIndexes);
			
			message = message.replaceAll("%i", indexes);
			
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

	private void listClaimedPriorityIndexes(CommandSender sender)
	{
		String queryFormat = null;
		
		if(BukkitUtil.isPlayer(sender))
		{
			Player p = (Player) sender;
			queryFormat += "SELECT ID " +
					"FROM Reports " +
					"WHERE " +
					"ClaimStatus = 1 " +
					"AND ClaimedByUUID = '" + p.getUniqueId() + "' " +
					"AND Priority = ";
		}
		else
		{
			queryFormat += "SELECT ID " +
					"FROM Reports " +
					"WHERE " +
					"ClaimStatus = 1 " +
					"AND ClaimedBy = '" + sender.getName() + "' " +
					"AND Priority = ";
		}
		
		String noPriorityIndexes;
		String lowPriorityIndexes;
		String normalPriorityIndexes;
		String highPriorityIndexes;
		
		SQLResultSet result;
		
		try
		{
			result = getManager().getDatabaseHandler().sqlQuery(queryFormat + ModLevel.NONE.getLevel());
			noPriorityIndexes = Util.indexesToString(result, "ID", ModLevel.NONE.getColor(), ChatColor.WHITE);
			
			result = getManager().getDatabaseHandler().sqlQuery(queryFormat + ModLevel.LOW.getLevel());
			lowPriorityIndexes = Util.indexesToString(result, "ID", ModLevel.LOW.getColor(), ChatColor.WHITE);
			
			result = getManager().getDatabaseHandler().sqlQuery(queryFormat + ModLevel.NORMAL.getLevel());
			normalPriorityIndexes = Util.indexesToString(result, "ID", ModLevel.NORMAL.getColor(), ChatColor.WHITE);
			
			result = getManager().getDatabaseHandler().sqlQuery(queryFormat + ModLevel.HIGH.getLevel());
			highPriorityIndexes = Util.indexesToString(result, "ID", ModLevel.HIGH.getColor(), ChatColor.WHITE);
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
		
		printClaimedPriorityIndexes(sender, ModLevel.NONE, noPriorityIndexes);
		printClaimedPriorityIndexes(sender, ModLevel.LOW, lowPriorityIndexes);
		printClaimedPriorityIndexes(sender, ModLevel.NORMAL, normalPriorityIndexes);
		printClaimedPriorityIndexes(sender, ModLevel.HIGH, highPriorityIndexes);
	}
	
	private void printClaimedPriorityIndexes(CommandSender sender, ModLevel level, String indexes)
	{
		String output;
		
		if (!indexes.equals(""))
		{
			output = getManager().getLocale().getString(ListPhrases.listClaimedPriorityIndexes);
			
			output = output.replaceAll("%p", level.getColor() + level.getName() + ChatColor.WHITE);
			output = output.replaceAll("%i", indexes);
		}
		else
		{
			output = getManager().getLocale().getString(ListPhrases.listNoClaimedPriorityIndexes);
			
			output = output.replaceAll("%p", level.getColor() + level.getName() + ChatColor.WHITE);
		}
		
		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);
	}

	private void listPriority(CommandSender sender)
	{
		int noPriorityCount = 0;
		int lowPriorityCount = 0;
		int normalPriorityCount = 0;
		int highPriorityCount = 0;
		
		try
		{
			noPriorityCount = getManager().getNumberOfPriority(ModLevel.NONE);
			lowPriorityCount = getManager().getNumberOfPriority(ModLevel.LOW);
			normalPriorityCount = getManager().getNumberOfPriority(ModLevel.NORMAL);
			highPriorityCount = getManager().getNumberOfPriority(ModLevel.HIGH);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			sender.sendMessage(getErrorMessage());
			return;
		}
		
		printPriorityCount(sender, ModLevel.NONE, noPriorityCount);
		printPriorityCount(sender, ModLevel.LOW, lowPriorityCount);
		printPriorityCount(sender, ModLevel.NORMAL, normalPriorityCount);
		printPriorityCount(sender, ModLevel.HIGH, highPriorityCount);
	}
	
	private void printPriorityCount(CommandSender sender, ModLevel level, int count)
	{
		String format = getManager().getLocale().getString(ListPhrases.listPriorityCount);
		
		ChatColor priorityColor = ModLevel.getModLevelColor(level);
		
		String output = format.replaceAll("%n", priorityColor + Integer.toString(count) + ChatColor.WHITE);
		output = output.replaceAll("%p", priorityColor + level.getName() + ChatColor.WHITE);
		
		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);
	}
	
	private void listPriorityIndexes(CommandSender sender)
	{
		ArrayList<Integer> noPriorityIndexes;
		ArrayList<Integer> lowPriorityIndexes;
		ArrayList<Integer> normalPriorityIndexes;
		ArrayList<Integer> highPriorityIndexes;
		
		try
		{
			noPriorityIndexes = getManager().getIndexesOfPriority(ModLevel.NONE);
			lowPriorityIndexes = getManager().getIndexesOfPriority(ModLevel.LOW);
			normalPriorityIndexes = getManager().getIndexesOfPriority(ModLevel.NORMAL);
			highPriorityIndexes = getManager().getIndexesOfPriority(ModLevel.HIGH);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			sender.sendMessage(getErrorMessage());
			return;
		}
		
		printPriorityIndexes(sender, ModLevel.NONE, noPriorityIndexes);
		printPriorityIndexes(sender, ModLevel.LOW, lowPriorityIndexes);
		printPriorityIndexes(sender, ModLevel.NORMAL, normalPriorityIndexes);
		printPriorityIndexes(sender, ModLevel.HIGH, highPriorityIndexes);
	}
	
	private void printPriorityIndexes(CommandSender sender, ModLevel level, ArrayList<Integer> indexes)
	{
		ChatColor priorityColor = ModLevel.getModLevelColor(level);
		String format, output;
		
		if(!indexes.isEmpty())
		{
			format = getManager().getLocale().getString(ListPhrases.listPriorityIndexes);
			
			output = format.replaceAll("%p", priorityColor + level.getName() + ChatColor.WHITE);
			output = output.replaceAll("%i", Util.indexesToString(indexes, priorityColor, ChatColor.WHITE));
		}
		else
		{
			format = getManager().getLocale().getString(ListPhrases.listNoReportsWithPriority);
			
			output = format.replaceAll("%p", priorityColor + level.getName() + ChatColor.WHITE);
		}
		
		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);
	}
	
	private void listCommand(CommandSender sender)
	{
		String listString = BukkitUtil.colorCodeReplaceAll(
				getManager().getLocale().getString(ListPhrases.reportList));
		
		int incompleteReports = getManager().getIncompleteReports();
		int completeReports = getManager().getCompletedReports();
		
		if(completeReports != -1 && incompleteReports != -1)
		{
			listString = listString.replaceAll("%r", ChatColor.RED + Integer.toString(incompleteReports) + ChatColor.WHITE);
			listString = listString.replaceAll("%c", ChatColor.GREEN + Integer.toString(completeReports) + ChatColor.WHITE);
			
			String[] parts = listString.split("%n");
			
			for(int LCV = 0; LCV < parts.length; LCV++)
			{
				listString = parts[LCV];
				sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + listString);
			}
		}
		else
			sender.sendMessage(getErrorMessage());
	}
	
	private void listIndexes(CommandSender sender)
	{
		ArrayList<Integer> completeIndexes = getManager().getCompletedReportIndexes();
		ArrayList<Integer> incompleteIndexes = getManager().getIncompleteReportIndexes();
		
		String complete = Util.indexesToString(completeIndexes, ChatColor.GREEN, ChatColor.WHITE);
		String incomplete = Util.indexesToString(incompleteIndexes, ChatColor.RED, ChatColor.WHITE);
		
		String out;
		
		if(!completeIndexes.isEmpty())
			out = BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ListPhrases.listReportCompleteIndexes));
		else
			out = BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ListPhrases.listReportNoCompleteIndexes));
		
		out = out.replaceAll("%i", complete);
		
		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + out);
		
		if(!incompleteIndexes.isEmpty())
			out = BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ListPhrases.listReportIncompleteIndexes));
		else
			out = BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ListPhrases.listReportNoIncompleteIndexes));
		
		out = out.replaceAll("%i", incomplete);
		
		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + out);
	}
	
	/**
	 * Updates the documentation for the command.
	 * <br/>
	 * This should be called after the locale has changed.
	 */
	public void updateDocumentation()
	{
		ArrayList<ObjectPair<String, String>> usages = super.getUsages();
		
		usages.clear();
		
		String usage = "/report list [indexes]";
		String description = getManager().getLocale().getString(ListPhrases.listHelpDetails);
				
		ObjectPair<String, String> entry = new ObjectPair<String, String>(usage, description);
		
		usages.add(entry);
		
		usage = "/report list priority [indexes]";
		description = getManager().getLocale().getString(ListPhrases.listHelpPriorityDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report list claimed [indexes]";
		description = getManager().getLocale().getString(ListPhrases.listHelpClaimedDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
		
		usage = "/report list claimed priority [indexes]";
		description = getManager().getLocale().getString(ListPhrases.listHelpClaimedPriorityDetails);
		
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
