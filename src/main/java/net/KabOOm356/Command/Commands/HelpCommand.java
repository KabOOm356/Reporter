package net.KabOOm356.Command.Commands;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.HelpPhrases;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.ObjectPair;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * A command that will display how to use the other commands.
 */
public class HelpCommand
{
	private static final String format = ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.RED + "%usage" + ChatColor.WHITE + " - %description";
	
	/**
	 * Displays help messages for the report commands.
	 * 
	 * @param manager The CommandManager to get the commands from.
	 * @param sender The {@link CommandSender} to display the help messages to.
	 * @param page The help page number requested.
	 */
	public static void reportHelp(ReporterCommandManager manager, CommandSender sender, int page)
	{
		HashMap<String, ReporterCommand> commands = manager.getReportCommands();
		
		int numberOfHelpMessages = getNumberOfHelpMessages(commands);
		
		int pageCount = getNumberOfPages(numberOfHelpMessages);
		
		if(requireHelpPageValid(manager, sender, pageCount, page))
		{
			int startNumber = getStartNumber(page);
			int endNumber = getEndNumber(numberOfHelpMessages, startNumber);
			
			String line = manager.getLocale().getString(HelpPhrases.reportHelpHeader);
			line = line.replaceAll("%p", ChatColor.GOLD + Integer.toString(page) + ChatColor.GREEN);
			line = line.replaceAll("%c", ChatColor.GOLD + Integer.toString(pageCount) + ChatColor.GREEN);
			
			sender.sendMessage(ChatColor.GREEN + line);
			
			printHelp(sender, commands, startNumber, endNumber);
			
			if(page != pageCount)
			{
				line = manager.getLocale().getString(HelpPhrases.nextReportHelpPage);
				
				line = line.replaceAll("%p", Integer.toString(page+1));
				
				sender.sendMessage(ChatColor.GOLD + line);
			}
			else
			{
				sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.GOLD +
						manager.getLocale().getString(HelpPhrases.reportHelpAliases));
			}
		}
	}
	
	/**
	 * Displays help messages for the respond commands.
	 * 
	 * @param manager The CommandManager to get the commands from.
	 * @param sender The {@link CommandSender} to display the help messages to.
	 * @param page The help page number requested.
	 */
	public static void respondHelp(ReporterCommandManager manager, CommandSender sender, int page)
	{
		HashMap<String, ReporterCommand> commands = manager.getRespondCommands();
		
		int numberOfHelpMessages = getNumberOfHelpMessages(commands);
		
		int pageCount = getNumberOfPages(numberOfHelpMessages);
		
		if(requireHelpPageValid(manager, sender, pageCount, page))
		{
			int startNumber = getStartNumber(page);
			int endNumber = getEndNumber(numberOfHelpMessages, startNumber);
			
			String line = manager.getLocale().getString(HelpPhrases.respondHelpHeader);
			line = line.replaceAll("%p", ChatColor.GOLD + Integer.toString(page) + ChatColor.GREEN);
			line = line.replaceAll("%c", ChatColor.GOLD + Integer.toString(pageCount) + ChatColor.GREEN);
			
			sender.sendMessage(ChatColor.GREEN + line);
			
			printHelp(sender, commands, startNumber, endNumber);
			
			if(page != pageCount)
			{
				line = manager.getLocale().getString(HelpPhrases.nextRespondHelpPage);
				
				line = line.replaceAll("%p", Integer.toString(page+1));
				
				sender.sendMessage(ChatColor.GOLD + line);
			}
			else
			{
				sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.GOLD + 
						manager.getLocale().getString(HelpPhrases.respondHelpAliases));
			}
		}
	}
	
	/**
	 * Displays all the help messages between the given start number and ending number.
	 * 
	 * @param sender The {@link CommandSender} to display the help messages to.
	 * @param commands The commands to get the help messages from.
	 * @param start The number to start displaying help messages from.
	 * @param end The number to stop displaying help messages at.
	 */
	public static void printHelp(CommandSender sender, HashMap<String, ReporterCommand> commands, int start, int end)
	{
		String[][] usagesAndDescriptions = getUsagesAndDescriptions(commands, start, end);
		
		for (int LCV = 0; LCV < usagesAndDescriptions.length; LCV++)
		{
			String[] usageDescription = usagesAndDescriptions[LCV];
			
			printHelpLine(sender, usageDescription[0], usageDescription[1]);
		}
	}
	
	/**
	 * Returns the usages and descriptions of the commands that fall between the start number and end number.
	 * 
	 * @param commands The commands to get the usages and descriptions from.
	 * @param startNumber The number to begin getting usages and descriptions.
	 * @param endNumber The number to stop getting usages and descriptions.
	 * 
	 * @return The usages, in index zero, and descriptions, in index one, of the commands that fall between the start number and end number.
	 */
	private static String[][] getUsagesAndDescriptions(HashMap<String, ReporterCommand> commands, int startNumber, int endNumber)
	{
		int difference = endNumber - startNumber;
		
		String[][] usagesAndDescriptions = new String[difference][2];
		
		int arrayCount = 0;
		int current = 0;
		
		for(Entry<String, ReporterCommand> e : commands.entrySet())
		{
			ReporterCommand cmd = e.getValue();
			
			ArrayList<ObjectPair<String, String>> usages = cmd.getUsages();
			int usagesCount = usages.size();
			
			if ((current+usagesCount) > startNumber)
			{
				for(int LCV = 0; LCV < usagesCount; LCV++)
				{
					if (current >= startNumber && current <= endNumber)
					{
						ObjectPair<String, String> usage = usages.get(LCV);
						
						usagesAndDescriptions[arrayCount][0] = usage.getKey();
						usagesAndDescriptions[arrayCount][1] = usage.getValue();
						
						arrayCount++;
					}
					
					current++;
					
					if(current > endNumber || arrayCount >= difference)
						break;
				}
			}
			else
			{
				current += usagesCount;
			}
			
			if(current > endNumber || arrayCount >= difference)
				break;
		}
		
		return usagesAndDescriptions;
	}
	
	/**
	 * Returns the total number of help messages.
	 * 
	 * @param commands The pool of commands to get the number of help messages from.
	 * 
	 * @return The total number of help messages.
	 */
	private static int getNumberOfHelpMessages(HashMap<String, ReporterCommand> commands)
	{
		int numberOfHelpMessages = 0;
		
		for(Entry<String, ReporterCommand> e : commands.entrySet())
			numberOfHelpMessages += e.getValue().getUsages().size();
		
		return numberOfHelpMessages;
	}
	
	/**
	 * Returns the number of pages that are available.
	 * 
	 * @param numberOfHelpMessages The total number of help messages.
	 * 
	 * @return The number of pages that are available.
	 */
	private static int getNumberOfPages(int numberOfHelpMessages)
	{
		return (int) Math.ceil(numberOfHelpMessages / 5f);
	}
	
	/**
	 * Checks if the given page is a valid page number.
	 * <br /><br />
	 * If it is not then the given {@link CommandSender} is alerted.
	 * 
	 * @param manager The {@link ReporterCommandManager}.
	 * @param sender The {@link CommandSender}.
	 * @param pageCount The number of pages available.
	 * @param page The page number being requested.
	 * 
	 * @return True if the page number requested is valid, otherwise false.
	 */
	private static boolean requireHelpPageValid(ReporterCommandManager manager, CommandSender sender, int pageCount, int page)
	{
		String line;
		
		if(page <= 0)
		{
			line = manager.getLocale().getString(HelpPhrases.pageNumberOutOfRange);
			sender.sendMessage(ChatColor.RED + line);
			
			line = manager.getLocale().getString(GeneralPhrases.tryReportHelp);
			sender.sendMessage(ChatColor.RED + line);
			
			return false;
		}
		else if (page > pageCount)
		{
			line = manager.getLocale().getString(HelpPhrases.numberOfHelpPages);
			line = line.replaceAll("%p", Integer.toString(pageCount));
			sender.sendMessage(ChatColor.RED + line);
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Gets the starting number for the given page.
	 * 
	 * @param page The page number to get the starting number for.
	 * 
	 * @return The starting number for the given page.
	 */
	private static int getStartNumber(int page)
	{
		return (page-1) * 5;
	}
	
	/**
	 * Gets the ending number based off the starting number and total number of help messages.
	 * 
	 * @param numberOfHelpMessages The total number of help messages.
	 * @param startNumber The starting number.
	 * 
	 * @return The ending number based off the starting number and total number of help messages.
	 */
	private static int getEndNumber(int numberOfHelpMessages, int startNumber)
	{
		return ((startNumber + 5) > numberOfHelpMessages) ? numberOfHelpMessages : startNumber + 5;
	}
	
	/**
	 * Formats and prints the given usage and description.
	 * 
	 * @param sender The {@link CommandSender} to send the help messages to.
	 * @param usage The usage of the command.
	 * @param description The description of the usage of the command.
	 */
	private static void printHelpLine(CommandSender sender, String usage, String description)
	{
		String line = format.replaceAll("%usage", usage);
		line = line.replaceAll("%description", description);
		sender.sendMessage(BukkitUtil.colorCodeReplaceAll(line));
	}
}
