package net.KabOOm356.Command;

import java.util.ArrayList;

/**
 * A {@link Command} that has multiple usages and descriptions.
 */
public abstract class MultiUsageCommand extends AliasCommand
{
	private ArrayList<String> alternateUsages;
	private ArrayList<String> alternateDescriptions;
	
	/**
	 * @see AliasCommand#AliasCommand(ReporterCommandManager, String, String, String, String, int)
	 */
	protected MultiUsageCommand(
			ReporterCommandManager manager,
			String commandName,
			String commandUsage,
			String commandDescription,
			String commandPermissionNode,
			int minimumNumberOfArguments)
	{
		super(manager, commandName, commandUsage, commandDescription, commandPermissionNode, minimumNumberOfArguments);
		
		alternateUsages = new ArrayList<String>();
		alternateDescriptions = new ArrayList<String>();
	}
	
	/**
	 * @see AliasCommand#AliasCommand(ReporterCommandManager, String, String, int)
	 */
	protected MultiUsageCommand(
			ReporterCommandManager manager,
			String commandName,
			String commandPermissionNode,
			int minimumNumberOfArguments)
	{
		super(manager, commandName, commandPermissionNode, minimumNumberOfArguments);
		
		alternateUsages = new ArrayList<String>();
		alternateDescriptions = new ArrayList<String>();
	}
	
	/**
	 * Returns a usage of this command.
	 * <br /><br />
	 * NOTE: The main usage is returned for index zero.
	 * 
	 * @param index The usage to get.
	 * 
	 * @return The usage located at the given index if the given index is in bounds, otherwise null.
	 */
	public String getUsage(int index)
	{
		try
		{
			if(index == 0)
				return getUsage();
			return alternateUsages.get(index-1);
		}
		catch(IndexOutOfBoundsException e)
		{
			return null;
		}
	}
	
	/**
	 * Returns a description of this command.
	 * <br /><br />
	 * NOTE: The main description is returned for index zero.
	 * 
	 * @param index The description to get.
	 * 
	 * @return The description located at the given index if the given index is in bounds, otherwise null.
	 */
	public String getDescription(int index)
	{
		try
		{
			if(index == 0)
				return getDescription();
			return alternateDescriptions.get(index-1);
		}
		catch(IndexOutOfBoundsException e)
		{
			return null;
		}
	}
	
	/**
	 * Adds a alternate usage and alternate description.
	 * 
	 * @param usage The usage to add.
	 * @param description The alternative description to add.
	 */
	protected void addUsageAndDescritpion(String usage, String description)
	{
		addUsage(usage);
		addDescription(description);
	}
	
	/**
	 * Adds an alternate usage to this Command.
	 * 
	 * @param usage The usage to add.
	 */
	protected void addUsage(String usage)
	{
		alternateUsages.add(usage);
	}
	
	/**
	 * Returns the number of usages this command has.
	 * 
	 * @return The number of usages this command has.
	 */
	public int getNumberOfUsages()
	{
		return 1 + alternateUsages.size();
	}
	
	/**
	 * Returns the number of descriptions this command has.
	 * 
	 * @return The number of descriptions this command has.
	 */
	public int getNumberOfDescriptions()
	{
		return 1 + alternateDescriptions.size();
	}
	
	/**
	 * Adds an alternative description to this command.
	 * 
	 * @param description The alternative description to add.
	 */
	protected void addDescription(String description)
	{
		alternateDescriptions.add(description);
	}
	
	/**
	 * Removes all alternative descriptions from this command.
	 */
	protected void removeAllAlternateDescriptions()
	{
		alternateDescriptions = new ArrayList<String>();
	}
	
	/**
	 * Removes all alternate usages from this command.
	 */
	protected void removeAllAlternateUsages()
	{
		alternateUsages = new ArrayList<String>();
	}
	
	/**
	 * Removes all alternate usages and descriptions from this command.
	 */
	protected void removeAllAlternateUsagesAndDescriptions()
	{
		removeAllAlternateUsages();
		removeAllAlternateDescriptions();
	}
}
