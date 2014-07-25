package net.KabOOm356.Command;

import java.util.ArrayList;

import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.ObjectPair;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Abstract Command class.
 */
public abstract class Command
{
	private ReporterCommandManager manager;
	
	private String name;
	private String permissionNode;
	
	private ArrayList<String> aliases;
	
	private ArrayList<ObjectPair<String, String>> usages;
	
	private int minimumNumberOfArguments;
	
	/**
	 * Constructor.
	 * 
	 * @param manager The {@link ReporterCommandManager} that is managing this command.
	 * @param commandName The name of the command.
	 * @param commandUsage The usage of the command.
	 * @param commandDescription A description of the command.
	 * @param commandPermissionNode The permission node required to run this command.
	 * @param minimumNumberOfArguments The minimum number of required arguments to run this command.
	 */
	protected Command(
			ReporterCommandManager manager,
			String commandName,
			String commandUsage,
			String commandDescription,
			String commandPermissionNode,
			int minimumNumberOfArguments)
	{
		this.manager = manager;
		
		this.name = commandName;
		this.permissionNode = commandPermissionNode;
		
		this.minimumNumberOfArguments = minimumNumberOfArguments;
		
		this.aliases = new ArrayList<String>();
		
		this.usages = new ArrayList<ObjectPair<String, String>>();
		
		ObjectPair<String, String> entry = new ObjectPair<String, String>(commandUsage, commandDescription);
		
		updateDocumentation(entry);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param manager The {@link ReporterCommandManager} that is managing this command.
	 * @param commandName The name of the command.
	 * @param commandPermissionNode The permission node required to run this command.
	 * @param minimumNumberOfArguments The minimum number of required arguments to run this command.
	 */
	protected Command(
			ReporterCommandManager manager,
			String commandName,
			String commandPermissionNode,
			int minimumNumberOfArguments)
	{
		this.manager = manager;
		
		this.name = commandName;
		this.permissionNode = commandPermissionNode;
		
		this.minimumNumberOfArguments = minimumNumberOfArguments;
		
		this.aliases = new ArrayList<String>();
		
		this.usages = new ArrayList<ObjectPair<String, String>>();
	}
	
	/**
	 * Executes this command.
	 * 
	 * @param sender The {@link CommandSender} whom is executing this command.
	 * @param args The given arguments from the {@link CommandSender}.
	 */
	public abstract void execute(CommandSender sender, ArrayList<String> args);
	
	/**
	 * Updates the documentation for the command.
	 * <br/>
	 * This should be called after the locale has changed.
	 * 
	 * @param usage The usage of the command.
	 * @param description A description of the command.
	 */
	protected void updateDocumentation(String usage, String description)
	{
		this.usages.clear();
		
		ObjectPair<String, String> entry = new ObjectPair<String, String>(usage, description);
		
		this.usages.add(entry);
	}
	
	/**
	 * Updates the documentation for the command.
	 * <br/>
	 * This should be called after the locale has changed.
	 * 
	 * @param usage The usage and description for the command.
	 */
	protected void updateDocumentation(ObjectPair<String, String> usage)
	{
		this.usages.clear();
		
		this.usages.add(usage);
	}
	
	/**
	 * Checks if the given {@link Player} has permission to run this command, or is OP.
	 * 
	 * @param player The {@link Player} to check.
	 * 
	 * @return True if the {@link Player} has permission or is OP, otherwise false.
	 */
	public boolean hasPermission(Player player)
	{
		return this.hasPermission(player, permissionNode);
	}
	
	/**
	 * Checks if the given {@link Player} has the given permission node, or is OP.
	 * 
	 * @param player The {@link Player} to check.
	 * @param perm The permission node to check.
	 * 
	 * @return True if the {@link Player} has the permission node or is OP, otherwise false.
	 */
	public boolean hasPermission(Player player, String perm)
	{
		return manager.hasPermission(player, perm);
	}
	
	/**
	 * Checks if the given {@link CommandSender} has permission to run this command, or is OP.
	 * 
	 * @param sender The {@link CommandSender} to check.
	 * 
	 * @return True if the {@link CommandSender} has permission or is OP, otherwise false.
	 */
	public boolean hasPermission(CommandSender sender)
	{
		if(BukkitUtil.isPlayer(sender))
		{
			Player player = (Player)sender;
			if(!hasPermission(player))
				return false;
		}
		return true;
	}
	
	/**
	 * Checks if the given {@link CommandSender} has permission to run this command and alerts them if they do not.
	 * 
	 * @param sender The {@link CommandSender} to check.
	 * 
	 * @return True if the {@link CommandSender} has permission or is OP, otherwise false.
	 */
	public boolean hasRequiredPermission(CommandSender sender)
	{
		if(!hasPermission(sender))
		{
			sender.sendMessage(getFailedPermissionsMessage());
			return false;
		}
		return true;
	}
	
	protected ReporterCommandManager getManager()
	{
		return manager;
	}

	public String getName()
	{
		return name;
	}

	public String getPermissionNode()
	{
		return permissionNode;
	}
	
	public ArrayList<String> getAliases()
	{
		return aliases;
	}
	
	public ArrayList<ObjectPair<String,String>> getUsages()
	{
		return usages;
	}
	
	public String getUsage()
	{
		return usages.get(0).getKey();
	}
	
	public String getDescription()
	{
		return usages.get(0).getValue();
	}
	
	public String getErrorMessage()
	{
		return ChatColor.BLUE + Reporter.getLogPrefix() +
				ChatColor.RED + manager.getLocale().getString(GeneralPhrases.error);
	}
	
	public String getFailedPermissionsMessage()
	{
		return ChatColor.RED + BukkitUtil.colorCodeReplaceAll(
				manager.getLocale().getString(GeneralPhrases.failedPermissions));
	}
	
	public int getMinimumNumberOfArguments()
	{
		return minimumNumberOfArguments;
	}
}
