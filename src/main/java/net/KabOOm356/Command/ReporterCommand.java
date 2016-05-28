package net.KabOOm356.Command;

/**
 * Abstract Reporter Command.
 */
public abstract class ReporterCommand extends Command {
	/**
	 * @see Command#Command(ReporterCommandManager, String, String, int)
	 */
	protected ReporterCommand(
			final ReporterCommandManager manager,
			final String commandName,
			final String commandPermissionNode,
			final int minimumNumberOfArguments) {
		super(manager, commandName, commandPermissionNode, minimumNumberOfArguments);
	}
}
