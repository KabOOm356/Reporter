package net.KabOOm356.Command;

import java.util.ArrayList;

/**
 * A {@link Command} that has alias names.
 */
public abstract class AliasCommand extends Command
{
	private ArrayList<String> aliases;
	
	/**
	 * @see Command#Command(ReporterCommandManager, String, String, String, String, int)
	 */
	protected AliasCommand(
			ReporterCommandManager manager,
			String commandName,
			String commandUsage,
			String commandDescription,
			String commandPermissionNode,
			int minimumNumberOfArguments)
	{
		super(manager, commandName, commandUsage, commandDescription, commandPermissionNode, minimumNumberOfArguments);
		
		aliases = new ArrayList<String>();
	}
	
	/**
	 * @see Command#Command(ReporterCommandManager, String, String, String, String, int)
	 */
	protected AliasCommand(
			ReporterCommandManager manager,
			String commandName,
			String commandPermissionNode,
			int minimumNumberOfArguments)
	{
		super(manager, commandName, commandPermissionNode, minimumNumberOfArguments);
		
		aliases = new ArrayList<String>();
	}
	
	/**
	 * Adds an alias to this Command.
	 * 
	 * @param alias The alias to add.
	 */
	protected void addAlias(String alias)
	{
		aliases.add(alias);
	}
	
	/**
	 * Removes and alias from this Command.
	 * 
	 * @param alias The alias to remove.
	 */
	protected void removeAlias(String alias)
	{
		aliases.remove(alias);
	}
	
	/**
	 * Removes all aliases from this Command.
	 */
	protected void removeAllAliases()
	{
		aliases = new ArrayList<String>();
	}
	
	/**
	 * Returns an ArrayList that contains all the aliases of this Command.
	 * 
	 * @return An ArrayList containing Strings.
	 */
	public ArrayList<String> getAliases()
	{
		return aliases;
	}
}
