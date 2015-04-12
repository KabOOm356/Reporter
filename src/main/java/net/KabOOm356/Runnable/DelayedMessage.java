package net.KabOOm356.Runnable;

import org.bukkit.entity.Player;

/**
 * A {@link Runnable} that will send a {@link Player} a message when it is run by it's thread manager.
 */
public class DelayedMessage implements Runnable
{
	/** The {@link Player} to send the message to. */
	private final Player player;
	/** The message to send the {@link Player}. */
	private final String message;
	
	/**
	 * Constructor
	 * 
	 * @param player The {@link Player} to send the message to.
	 * @param message The message to send the {@link Player}.
	 */
	public DelayedMessage(final Player player, final String message)
	{
		this.player = player;
		this.message = message;
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		if(player.isOnline())
			player.sendMessage(message);
	}
	
	/**
	 * Returns the {@link Player} that will receive the message.
	 * 
	 * @return The {@link Player} that will receive the message.
	 */
	public Player getPlayer()
	{
		return player;
	}
	
	/**
	 * Returns the message that will be sent to the {@link Player}.
	 * 
	 * @return The message that will be sent to the {@link Player}.
	 */
	public String getMessage()
	{
		return message;
	}
}
