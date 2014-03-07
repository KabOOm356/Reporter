package net.KabOOm356.Command;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import net.KabOOm356.Command.Commands.AssignCommand;
import net.KabOOm356.Command.Commands.ClaimCommand;
import net.KabOOm356.Command.Commands.CompleteCommand;
import net.KabOOm356.Command.Commands.DeleteCommand;
import net.KabOOm356.Command.Commands.DowngradeCommand;
import net.KabOOm356.Command.Commands.HelpCommand;
import net.KabOOm356.Command.Commands.ListCommand;
import net.KabOOm356.Command.Commands.MoveCommand;
import net.KabOOm356.Command.Commands.ReportCommand;
import net.KabOOm356.Command.Commands.RequestCommand;
import net.KabOOm356.Command.Commands.RespondCommand;
import net.KabOOm356.Command.Commands.UnassignCommand;
import net.KabOOm356.Command.Commands.UnclaimCommand;
import net.KabOOm356.Command.Commands.UpgradeCommand;
import net.KabOOm356.Command.Commands.ViewCommand;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Locale.Entry.LocalePhrases.ClaimPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Manager.MessageManager;
import net.KabOOm356.Manager.ReportLimitManager;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Permission.ReporterPermissionManager;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.FormattingUtil;
import net.KabOOm356.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * A Command Manager and Command Executor for all the Reporter Commands.
 */
public class ReporterCommandManager implements CommandExecutor
{
	private Reporter plugin;
	
	private LinkedHashMap<String, ReporterCommand> reportCommands;
	private HashMap<String, String> aliasReportCommands;
	
	private LinkedHashMap<String, ReporterCommand> respondCommands;
	private HashMap<String, String> aliasRespondCommands;
	
	private HashMap<CommandSender, Integer> lastViewed = new HashMap<CommandSender, Integer>();
	
	private MessageManager messageManager;
	
	private ReporterPermissionManager permissionManager;
	private ReportLimitManager limitManager;
	
	/**
	 * Constructor.
	 * 
	 * @param plugin The current instance of {@link Reporter} running.
	 */
	public ReporterCommandManager(Reporter plugin)
	{
		this.plugin = plugin;
		
		limitManager = new ReportLimitManager(plugin);
		
		messageManager = new MessageManager();
		
		permissionManager = new ReporterPermissionManager();
		
		initCommands();
		
		lastViewed.put(plugin.getServer().getConsoleSender(), -1);
		
		for(Player player : Bukkit.getOnlinePlayers())
			lastViewed.put(player, -1);
	}
	
	private void initCommands()
	{
		/*
		 * Initialize the LinkedHashMap with default initialCapacity, 
		 * default loadFactor and loadOrder set to insertion-order.
		 * 
		 * As outlined here: 
		 * http://docs.oracle.com/javase/7/docs/api/java/util/LinkedHashMap.html#constructor_summary
		 */
		reportCommands = new LinkedHashMap<String, ReporterCommand>(16, 0.75F, false);
		aliasReportCommands = new HashMap<String, String>();
		
		respondCommands = new LinkedHashMap<String, ReporterCommand>(16, 0.75F, false);
		aliasRespondCommands = new HashMap<String, String>();
		
		initReportCommand(new AssignCommand(this));
		initReportCommand(new ClaimCommand(this));
		initReportCommand(new CompleteCommand(this));
		initReportCommand(new DeleteCommand(this));
		initReportCommand(new DowngradeCommand(this));
		initReportCommand(new ListCommand(this));
		initReportCommand(new MoveCommand(this));
		initReportCommand(new ReportCommand(this));
		initReportCommand(new RequestCommand(this));
		initReportCommand(new UnassignCommand(this));
		initReportCommand(new UnclaimCommand(this));
		initReportCommand(new UpgradeCommand(this));
		initReportCommand(new ViewCommand(this));
		
		initRespondCommand(new RespondCommand(this));
	}
	
	/**
	 * Adds the ReporterCommand to be executed.
	 * <br /><br />
	 * This also adds all the aliases to be executed.
	 * 
	 * @param command
	 */
	private void initReportCommand(ReporterCommand command)
	{
		reportCommands.put(command.getName(), command);
		
		for(String alias : command.getAliases())
			aliasReportCommands.put(alias, command.getName());
	}
	
	/**
	 * Adds the ReporterCommand to be executed.
	 * <br /><br />
	 * This also adds all the aliases to be executed.
	 * 
	 * @param command
	 */
	private void initRespondCommand(ReporterCommand command)
	{
		respondCommands.put(command.getName(), command);
		
		for(String alias : command.getAliases())
			aliasRespondCommands.put(alias, command.getName());
	}

	/**
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(args == null || args.length == 0)
		{	
			if(label.equalsIgnoreCase("respond") || label.equalsIgnoreCase("resp") || label.equalsIgnoreCase("rrespond"))
				sender.sendMessage(ChatColor.RED + plugin.getLocale().getString(GeneralPhrases.tryRespondHelp));
			else
				sender.sendMessage(ChatColor.RED + plugin.getLocale().getString(GeneralPhrases.tryReportHelp));
			
			return true;
		}
		
		// Check if sender is a supported type
		if(!Reporter.isCommandSenderSupported(sender))
		{
			sender.sendMessage(ChatColor.RED + "Command Sender type is not supported!");
			return true;
		}
		
		ArrayList<String> arguments = Util.arrayToArrayList(args);
		ReporterCommand command = null;
		
		// Begin Respond Command
		if(label.equalsIgnoreCase("respond") || label.equalsIgnoreCase("resp") || label.equalsIgnoreCase("rrespond"))
		{
			command = getCommand(RespondCommand.getCommandName());
			
			// Respond help command
			if(arguments.size() >= 1 && arguments.get(0).equalsIgnoreCase("help"))
			{
				int page = 1;
				
				if(arguments.size() >= 2)
				{
					if(Util.isInteger(arguments.get(1)))
						page = Util.parseInt(arguments.get(1));
				}
				
				HelpCommand.respondHelp(this, sender, page);
				
				return true;
			}
			
			// Respond to report
			if(arguments.size() >= command.getMinimumNumberOfArguments())
			{
				command.execute(sender, arguments);
				return true;
			}
			else
				sender.sendMessage(ChatColor.RED + command.getUsage());
			
			sender.sendMessage(ChatColor.RED + plugin.getLocale().getString(GeneralPhrases.tryRespondHelp));
			
			return true;
		}
		// Begin Report Commands
		else if(label.equalsIgnoreCase("report") || label.equalsIgnoreCase("rreport") || label.equalsIgnoreCase("rep"))
		{
			String subcommand = arguments.remove(0);
			
			command = getCommand(FormattingUtil.capitalizeFirstCharacter(subcommand));
			
			// Report help command
			if(subcommand.equalsIgnoreCase("help"))
			{
				int page = 1;
				
				if(arguments.size() >= 1)
				{
					if(Util.isInteger(arguments.get(0)))
						page = Util.parseInt(arguments.get(0));
				}
				
				HelpCommand.reportHelp(this, sender, page);
				
				return true;
			}
			
			if(command != null)
			{
				if(arguments.size() >= command.getMinimumNumberOfArguments())
				{
					command.execute(sender, arguments);
					return true;
				}
				else
					sender.sendMessage(ChatColor.RED + BukkitUtil.colorCodeReplaceAll(command.getUsage()));
			}
			else // Reporting a player
			{
				command = getCommand(ReportCommand.getCommandName());
				
				arguments.add(0, subcommand);
				
				if(arguments.size() >= command.getMinimumNumberOfArguments())
				{
					command.execute(sender, arguments);
					return true;
				}
				else
					sender.sendMessage(ChatColor.RED + BukkitUtil.colorCodeReplaceAll(command.getUsage()));
			}
			
			sender.sendMessage(ChatColor.RED + plugin.getLocale().getString(GeneralPhrases.tryReportHelp));
		}

		return true;
	}
	
	/**
	 * Updates the documentation for all the commands.
	 * <br/>
	 * This should be called after the locale has changed.
	 */
	public void updateDocumentation()
	{
		for(Entry<String, ReporterCommand> e : reportCommands.entrySet())
			e.getValue().updateDocumentation();
		
		for(Entry<String, ReporterCommand> e : respondCommands.entrySet())
			e.getValue().updateDocumentation();
	}
	
	/**
	 * Checks if the given {@link Player} has the given permission node, or is OP.
	 * 
	 * @param player The {@link Player} to check if they have the permission node.
	 * @param permission The permission node to check.
	 * 
	 * @return True if the given {@link Player} has the permission node or is OP, otherwise false.
	 * 
	 * @see ReporterPermissionManager#hasPermission(Player, String)
	 */
	public boolean hasPermission(Player player, String permission)
	{
		if(getConfig().getBoolean("general.permissions.opsHaveAllPermissions", true) && player.isOp())
			return true;
		
		return permissionManager.hasPermission(player, permission);
	}
	
	/**
	 * Checks if the given {@link CommandSender} has the given permission node.
	 * <br /><br />
	 * <b>NOTE:</b> The given {@link CommandSender} will be converted to a {@link Player} first.
	 * <br />But, if the given {@link CommandSender} is not a {@link Player}, {@link Boolean#TRUE} will be returned.
	 * 
	 * @param sender The {@link CommandSender} to check if they have the permission node.
	 * @param permission The permission node to check.
	 * 
	 * @return True if the given {@link CommandSender} is not a player or has the permission node, otherwise false.
	 * 
	 * @see ReporterPermissionManager#hasPermission(Player, String)
	 */
	public boolean hasPermission(CommandSender sender, String permission)
	{
		if(BukkitUtil.isPlayer(sender))
		{
			Player player = (Player)sender;
			if(!hasPermission(player, permission))
				return false;
		}
		return true;
	}
	
	/**
	 * Returns a {@link OfflinePlayer} object that has a name that most closely resembles the given player name.
	 * <br/><br/>
	 * <b>NOTE:</b> If the given player name is '!' or '*', a {@link OfflinePlayer} is still returned and not null.
	 * 
	 * @param playerName The name of the {@link OfflinePlayer}.
	 * 
	 * @return An {@link OfflinePlayer} that most with a name that most closely resembles the given player name, if no player matches null.
	 * 
	 * @see org.bukkit.Server#getPlayer(String)
	 * @see org.bukkit.Server#getOfflinePlayer(String)
	 */
	public OfflinePlayer getPlayer(String playerName)
	{
		playerName = playerName.replaceAll("\\ ", "");
		
		if(playerName.equals(""))
			return null;
		
		OfflinePlayer player = Bukkit.getServer().getPlayer(playerName);
		
		if(playerName.equalsIgnoreCase("!") || playerName.equalsIgnoreCase("*"))
			player = Bukkit.getServer().getOfflinePlayer("* (Anonymous)");

		if(player == null)
			player = matchOfflinePlayer(playerName);
		
		return player;
	}
	
	/**
	 * Returns a {@link OfflinePlayer} whose name most closely matches the given player name.
	 * 
	 * @param playerName The player name to get
	 * 
	 * @return The {@link OfflinePlayer} whose name most closely matches the given player name if one can be matched, otherwise null.
	 */
	public OfflinePlayer matchOfflinePlayer(String playerName)
	{
		OfflinePlayer player = null;
		
		String lowerName = playerName.toLowerCase();
		int delta = Integer.MAX_VALUE;
		
		for (OfflinePlayer op : Bukkit.getServer().getOfflinePlayers())
		{
			if (op.getName().toLowerCase().startsWith(lowerName))
			{
				int curDelta = op.getName().length() - lowerName.length();
				
				if (curDelta < delta)
				{
					player = op;
					delta = curDelta;
				}
				
				if(curDelta == 0)
					break;
			}
		}
		
		return player;
	}
	
	/**
	 * Returns the report indexes of all reports the given {@link CommandSender} can view.
	 * 
	 * @param sender The {@link CommandSender}
	 * 
	 * @return An {@link ArrayList} of integers that contains all the report indexes the {@link CommandSender} can view.
	 */
	public ArrayList<Integer> getViewableReports(CommandSender sender)
	{
		String query = "SELECT ID FROM Reports WHERE SenderRaw=?";
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		try
		{
			ArrayList<String> params = new ArrayList<String>();
			
			params.add(sender.getName());
			
			SQLResultSet result = plugin.getDatabaseHandler().preparedSQLQuery(query, params);
			
			for(ResultRow row : result)
				indexes.add(row.getInt("ID"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{	
			try
			{
				plugin.getDatabaseHandler().closeConnection();
			}
			catch(Exception e)
			{
			}	
		}
		
		return indexes;
	}
	
	/**
	 * Checks if the given report index is valid.
	 * <br/>If the report index is not valid, the the given {@link CommandSender} will be alerted.
	 * 
	 * @param sender The {@link CommandSender} to alert if the report index is not valid.
	 * @param repIndex The report index to check if it is valid.
	 * 
	 * @return True if the report index is valid, otherwise false.
	 */
	public boolean isReportIndexValid(CommandSender sender, int repIndex)
	{
		int count = getCount();
		
		if(repIndex == -1)
		{
			sender.sendMessage(ChatColor.RED + plugin.getLocale().getString(GeneralPhrases.indexInt));
			return false;
		}
		else if(count == -1)
		{
			sender.sendMessage(ChatColor.RED + Reporter.getLogPrefix() + plugin.getLocale().getString(GeneralPhrases.error));
			return false;
		}
		else if(repIndex < 1 || repIndex > count)
		{
			sender.sendMessage(ChatColor.RED + plugin.getLocale().getString(GeneralPhrases.indexRange));
			return false;
		}
		return true;
	}
	
	/**
	 * Returns the current number of incomplete reports in the database.
	 * 
	 * @return The current number of incomplete reports in the database.
	 */
	public int getIncompleteReports()
	{
		return getIncompleteReportIndexes().size();
	}
	
	/**
	 * Returns the current number of complete reports in the database.
	 * 
	 * @return The current number of complete reports in the database.
	 */
	public int getCompletedReports()
	{
		return getCompletedReportIndexes().size();
	}
	
	/**
	 * Returns the indexes of the completed reports in the database.
	 * 
	 * @return An {@link ArrayList} of integers containing the indexes to all the completed reports in the database.
	 */
	public ArrayList<Integer> getCompletedReportIndexes()
	{
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		try
		{
			String query = "SELECT ID FROM Reports WHERE CompletionStatus=1";
			
			SQLResultSet result = plugin.getDatabaseHandler().sqlQuery(query);
			
			for(ResultRow row : result)
				indexes.add(row.getInt("ID"));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				plugin.getDatabaseHandler().closeConnection();
			}
			catch(Exception ex)
			{
			}
		}
		
		return indexes;
	}
	
	/**
	 * Returns the indexes of the incomplete reports in the database.
	 * 
	 * @return An {@link ArrayList} of integers containing the indexes to all the incomplete reports in the database.
	 */
	public ArrayList<Integer> getIncompleteReportIndexes()
	{
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		try
		{
			String query = "SELECT ID FROM Reports WHERE CompletionStatus=0";

			SQLResultSet result = plugin.getDatabaseHandler().sqlQuery(query);
			
			for(ResultRow row : result)
				indexes.add(row.getInt("ID"));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				plugin.getDatabaseHandler().closeConnection();
			}
			catch(Exception ex)
			{
			}
		}
		
		return indexes;
	}
	
	/**
	 * Returns the current number of reports in the database.
	 * 
	 * @return The current number of reports in the database.  If an exception occurs -1 is returned.
	 */
	public int getCount()
	{
		int count = -1;
		
		try
		{
			String query = "SELECT COUNT(*) AS Count FROM Reports";
			
			SQLResultSet result = plugin.getDatabaseHandler().sqlQuery(query);
			
			count = result.getInt("Count");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				plugin.getDatabaseHandler().closeConnection();
			}
			catch(Exception ex)
			{
			}
		}
		
		return count;
	}
	
	/**
	 * Returns the {@link ModLevel} of the given {@link CommandSender}.
	 * 
	 * @param sender The {@link CommandSender} to get the {@link ModLevel} for.
	 * 
	 * @return The {@link ModLevel} of the given {@link CommandSender}.
	 */
	public ModLevel getModLevel(CommandSender sender)
	{
		if(sender.isOp())
			return ModLevel.HIGH;
		else if(sender instanceof ConsoleCommandSender)
			return ModLevel.HIGH;
		else
		{
			if(BukkitUtil.isPlayer(sender))
			{
				if(hasPermission((Player)sender, "reporter.modlevel.high"))
					return ModLevel.HIGH;
				else if(hasPermission((Player)sender, "reporter.modlevel.normal"))
					return ModLevel.NORMAL;
				else if(hasPermission((Player)sender, "reporter.modlevel.low"))
					return ModLevel.LOW;
			}
		}
		return ModLevel.NONE;
	}
	
	/**
	 * Checks if the given String is a {@link ModLevel}, if it is not the given {@link CommandSender} is alerted.
	 * 
	 * @param sender The {@link CommandSender} to alert if the String is not a {@link ModLevel} or not in bounds.
	 * @param modLevel The String representation of the {@link ModLevel} to check if is in bounds.
	 * 
	 * @return True if the String is a {@link ModLevel} and in bounds, otherwise false.
	 */
	public boolean requireModLevelInBounds(CommandSender sender, String modLevel)
	{
		if(ModLevel.modLevelInBounds(modLevel))
			return true;
		sender.sendMessage(ChatColor.RED + 
				getLocale().getString(GeneralPhrases.priorityLevelNotInBounds));
		return false;
	}
	
	/**
	 * Checks if the player can alter the given report.
	 * 
	 * @param sender The {@link CommandSender} checking if the player can alter the report.
	 * @param index The index of the report.
	 * @param player The player to check.
	 * 
	 * @return True if the player can alter the report, otherwise false.
	 */
	public boolean canAlterReport(CommandSender sender, int index, CommandSender player)
	{
		if(player == null)
			return false;
		
		if(!requirePriority(sender, index, player))
			return false;
		
		if(!requireUnclaimedOrPriority(sender, index, player))
		{
			sender.sendMessage(ChatColor.WHITE + getLocale().getString(GeneralPhrases.contactToAlter));
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if the given {@link CommandSender} can alter the report at the given index.
	 * <br /><br />
	 * Will display a message if the sender cannot alter the report.
	 * <br /><br />
	 * Console and OPs can always alter reports.
	 * 
	 * @param sender The {@link CommandSender} wanting to alter a report.
	 * @param index The index of the report.
	 * 
	 * @return True if the {@link CommandSender} can alter the given report, otherwise false.
	 */
	public boolean canAlterReport(CommandSender sender, int index)
	{
		return canAlterReport(sender, index, sender);
	}
	
	/**
	 * Checks if report is unclaimed or the sender has high enough priority to supersede the player claiming it.
	 * <br /><br />If the player fails the check, the sender will be alerted.
	 * 
	 * @param sender The {@link CommandSender}.
	 * @param index The index of the report.
	 * 
	 * @return True if the report is unclaimed or the sender has high enough priority to supersede the player claiming it.
	 */
	public boolean requireUnclaimedOrPriority(CommandSender sender, int index)
	{
		return requireUnclaimedOrPriority(sender, index, sender);
	}
	
	/**
	 * Checks if report is unclaimed or the player has high enough priority to supersede the player claiming it.
	 * <br /><br />If the player fails the check, the sender will be alerted.
	 * 
	 * @param sender The {@link CommandSender}.
	 * @param index The index of the report.
	 * @param player The player to check.
	 * 
	 * @return True if the report is unclaimed or the player has high enough priority to supersede the player claiming it.
	 */
	public boolean requireUnclaimedOrPriority(CommandSender sender, int index, CommandSender player)
	{
		String query = "SELECT " +
				"ClaimStatus, ClaimedBy, ClaimedByRaw, ClaimPriority " +
				"FROM Reports " +
				"WHERE ID=" + index;
		
		boolean isClaimed;
		String claimedBy, claimedByRaw;
		int claimPriority;
		
		try
		{
			SQLResultSet result = getDatabaseHandler().sqlQuery(query);
			
			claimedBy = result.getString("ClaimedBy");
			isClaimed = result.getBoolean("ClaimStatus");
			claimedByRaw = result.getString("ClaimedByRaw");
			claimPriority = result.getInt("ClaimPriority");
			
			if(isClaimed && !player.getName().equals(claimedByRaw) && !sender.getName().equals(claimedByRaw) && claimPriority >= getModLevel(player).getLevel())
			{
				String output = getLocale().getString(ClaimPhrases.reportAlreadyClaimed);
				
				if(!claimedBy.equals(claimedByRaw))
					claimedBy += ChatColor.GOLD + " (" + claimedByRaw + ")";
				
				output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.RED);
				
				output = output.replaceAll("%c", ChatColor.BLUE + claimedBy + ChatColor.RED);
				
				sender.sendMessage(ChatColor.RED + output);
				
				return false;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			sender.sendMessage(ChatColor.RED + Reporter.getLogPrefix() + getLocale().getString(GeneralPhrases.error));
			return false;
		}
		finally
		{
			try
			{
				getDatabaseHandler().closeConnection();
			}
			catch(Exception e)
			{
			}
		}
		
		return true;
	}
	
	/**
	 * Checks if the given {@link CommandSender} has a high enough priority to alter the report at the given index.
	 * 
	 * @param player The {@link CommandSender}.
	 * @param index The index of the report.
	 * 
	 * @return True if the {@link CommandSender} has a high enough priority to alter the report at the given index.
	 * 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public boolean checkPriority(CommandSender player, int index) throws ClassNotFoundException, SQLException
	{
		if(player instanceof ConsoleCommandSender)
			return true;
		
		if(BukkitUtil.isPlayer(player) && ((Player)player).isOp())
			return true;
		
		ModLevel modLevel = getModLevel(player);
		
		ModLevel reportPriority = getReportPriority(index);
		
		if(reportPriority.getLevel() <= modLevel.getLevel())
			return true;
		
		return false;
	}
	
	/**
	 * Checks if the {@link CommandSender} has a high enough priority to alter the report.
	 * 
	 * @param sender The {@link CommandSender}.
	 * @param index The index of the report.
	 * 
	 * @return True if the {@link CommandSender} has a high enough priority to alter the given report, otherwise false.
	 */
	public boolean requirePriority(CommandSender sender, int index)
	{
		return requirePriority(sender, index, sender);
	}
	
	/**
	 * Checks if the given player has a high enough priority to alter the report.
	 * 
	 * @param sender The {@link CommandSender}.
	 * @param index The index of the report.
	 * @param player The player to check.
	 * 
	 * @return True if the player has a high enough priority to alter the given report, otherwise false.
	 */
	public boolean requirePriority(CommandSender sender, int index, CommandSender player)
	{
		try
		{
			if(!checkPriority(player, index))
			{
				ModLevel reportPriority = getReportPriority(index);
				
				String output = getLocale().getString(GeneralPhrases.reportRequiresClearance);
				
				output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.RED);
				output = output.replaceAll("%m", reportPriority.getColor() + reportPriority.getName() + ChatColor.RED);
				
				sender.sendMessage(ChatColor.RED + output);
				
				if(sender.equals(player))
					displayModLevel(sender);
				else
					displayModLevel(sender, player);
				
				return false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			sender.sendMessage(ChatColor.RED + Reporter.getLogPrefix() + getLocale().getString(GeneralPhrases.error));
			return false;
		}
		
		return true;
	}
	
	private ModLevel getReportPriority(int index) throws SQLException, ClassNotFoundException
	{
		String query = "SELECT Priority FROM Reports WHERE ID=" + index;
		
		try
		{
			SQLResultSet result = getDatabaseHandler().sqlQuery(query);
			
			int level = result.getInt("Priority");
			
			return ModLevel.getByLevel(level);
		}
		finally
		{
			try
			{
				getDatabaseHandler().closeConnection();
			}
			catch(Exception e)
			{
			}
		}
	}
	
	/**
	 * Gets the number of reports with a given {@link ModLevel} priority.
	 * 
	 * @param level The {@link ModLevel} to get the number of reports for.
	 * 
	 * @return The number of reports with the given {@link ModLevel} priority.
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public int getNumberOfPriority(ModLevel level) throws SQLException, ClassNotFoundException
	{
		String query = "SELECT COUNT(*) AS Count FROM Reports WHERE Priority = " + level.getLevel();
		int count = 0;
		
		try
		{
			SQLResultSet result = getDatabaseHandler().sqlQuery(query);
			
			count = result.getInt("Count");
		}
		finally
		{
			try
			{
				getDatabaseHandler().closeConnection();
			}
			catch (SQLException e)
			{
			}
		}
		
		return count;
	}
	
	/**
	 * Gets the indexes of the reports with a given {@link ModLevel} priority.
	 * 
	 * @param level The {@link ModLevel} priority of the reports to get the indexes for.
	 * 
	 * @return The indexes of the reports with the given {@link ModLevel} priority.
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public ArrayList<Integer> getIndexesOfPriority(ModLevel level) throws ClassNotFoundException, SQLException
	{
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		String query = "SELECT ID FROM Reports WHERE Priority = " + level.getLevel();
		
		try
		{
			SQLResultSet result = getDatabaseHandler().sqlQuery(query);
			
			for(ResultRow row : result)
				indexes.add(row.getInt("ID"));
		}
		finally
		{
			getDatabaseHandler().closeConnection();
		}
		
		return indexes;
	}
	
	/**
	 * Displays the current {@link ModLevel} to the given {@link CommandSender}.
	 * 
	 * @param sender The {@link CommandSender} to display their {@link ModLevel} to.
	 */
	public void displayModLevel(CommandSender sender)
	{
		String output = getLocale().getString(GeneralPhrases.displayModLevel);
		
		ModLevel level = getModLevel(sender);
		
		output = output.replaceAll("%m", level.getColor() + level.getName() + ChatColor.WHITE);
		
		sender.sendMessage(ChatColor.WHITE + output);
	}
	
	public void displayModLevel(CommandSender sender, CommandSender player)
	{
		String playerName = BukkitUtil.formatPlayerName(player);
		
		String output = getLocale().getString(GeneralPhrases.displayOtherModLevel);
		
		ModLevel level = getModLevel(player);
		
		output = output.replaceAll("%p", ChatColor.BLUE + playerName + ChatColor.WHITE);
		output = output.replaceAll("%m", level.getColor() + level.getName() + ChatColor.WHITE);
		
		sender.sendMessage(ChatColor.WHITE + output);
	}
	
	protected Reporter getPlugin()
	{
		return plugin;
	}

	public ExtendedDatabaseHandler getDatabaseHandler()
	{
		return plugin.getDatabaseHandler();
	}

	public Locale getLocale()
	{
		return plugin.getLocale();
	}
	
	public HashMap<String, ReporterCommand> getReportCommands()
	{
		return reportCommands;
	}
	
	public HashMap<String, String> getAliasReportCommands()
	{
		return aliasReportCommands;
	}
	
	public HashMap<String, ReporterCommand> getRespondCommands()
	{
		return respondCommands;
	}
	
	public HashMap<String, String> getAliasRespondCommands()
	{
		return aliasRespondCommands;
	}
	
	public ReporterCommand getCommand(String commandName)
	{
		ReporterCommand command = null;
		
		if (reportCommands.containsKey(commandName))
			command = reportCommands.get(commandName);
		else if (respondCommands.containsKey(commandName))
			command = respondCommands.get(commandName);
		else if (aliasReportCommands.containsKey(commandName))
			command = reportCommands.get(aliasReportCommands.get(commandName));
		else if (aliasRespondCommands.containsKey(commandName))
			command = respondCommands.get(aliasRespondCommands.get(commandName));
		
		return command;
	}

	public FileConfiguration getConfig()
	{
		return plugin.getConfig();
	}
	
	public ReportLimitManager getReportLimitManager()
	{
		return limitManager;
	}

	public HashMap<CommandSender, Integer> getLastViewed()
	{
		return lastViewed;
	}
	
	public MessageManager getMessageManager()
	{
		return messageManager;
	}
}