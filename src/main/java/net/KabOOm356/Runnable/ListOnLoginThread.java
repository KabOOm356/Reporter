package net.KabOOm356.Runnable;

import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Command.Commands.ListCommand;

import org.bukkit.entity.Player;

/**
 * A {@link Runnable} that when run will execute the {@link net.KabOOm356.Command.Commands.ListCommand} for a {@link Player}.
 */
public class ListOnLoginThread implements Runnable
{
	/** The {@link ReporterCommandManager} that will give this thread the {@link net.KabOOm356.Command.Commands.ListCommand} to execute. */
	private final ReporterCommandManager manager;
	/** The {@link Player} to execute the {@link net.KabOOm356.Command.Commands.ListCommand} for. */
	private final Player player;
	
	/**
	 * Constructor
	 * 
	 * @param manager The {@link ReporterCommandManager} that will give this thread the {@link net.KabOOm356.Command.Commands.ListCommand} to execute.
	 * @param player The {@link Player} to execute the {@link net.KabOOm356.Command.Commands.ListCommand} for.
	 */
	public ListOnLoginThread(ReporterCommandManager manager, Player player)
	{
		this.manager = manager;
		this.player = player;
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		manager.getCommand(ListCommand.getCommandName()).execute(player, null);
	}
}
