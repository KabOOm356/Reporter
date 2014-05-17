package net.KabOOm356.Listeners;

import java.util.ArrayList;

import net.KabOOm356.Command.Commands.ListCommand;
import net.KabOOm356.Command.Commands.ViewCommand;
import net.KabOOm356.Manager.MessageManager;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Runnable.DelayedMessage;
import net.KabOOm356.Runnable.ListOnLoginThread;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * A {@link Listener} that listens for player events.
 */
public class ReporterPlayerListener implements Listener
{
	private Reporter plugin;

	/**
	 * Constructor.
	 * 
	 * @param instance The running instance of {@link Reporter}.
	 */
	public ReporterPlayerListener(Reporter instance)
	{
		plugin = instance;
	}
	
	/**
	 * Run when a player joins.
	 * 
	 * @param event The player join event.
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		
		plugin.getCommandManager().getLastViewed().put(player, -1);
		
		MessageManager messageManager = plugin.getCommandManager().getMessageManager();
		
		if(messageManager.hasMessages(player.getUniqueId().toString()) || messageManager.hasMessages(player.getName()))
			sendMessages(player);
		
		if(plugin.getConfig().getBoolean("general.messaging.listOnLogin.listOnLogin", true))
			listOnLogin(player);
	}
	
	/**
	 * Runs when a player quits.
	 * 
	 * @param event The player quit event.
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		plugin.getCommandManager().getLastViewed().remove(event.getPlayer());
	}
	
	private void listOnLogin(Player player)
	{
		if(plugin.getCommandManager().getCommand(ListCommand.getCommandName()).hasPermission(player))
		{
			if(plugin.getConfig().getBoolean("general.messaging.listOnLogin.useDelay", true))
			{
				int delay = plugin.getConfig().getInt("general.messaging.listOnLogin.delay", 5);
				plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new ListOnLoginThread(plugin.getCommandManager(), player), 20 * delay);
			}
			else
				plugin.getCommandManager().getCommand(ListCommand.getCommandName()).execute(player, new ArrayList<String>());
		}
	}
	
	private void sendMessages(Player player)
	{
		// Players can view a message if they have permission to view all reports or their submitted reports.
		boolean canView = plugin.getCommandManager().hasPermission(player, ViewCommand.getCommandPermissionNode());
		canView = canView || plugin.getConfig().getBoolean("general.canViewSubmittedReports", true);
		
		MessageManager messageManager = plugin.getCommandManager().getMessageManager();
		
		// No point to send the message if the player can't view any reports.
		if(canView)
		{
			// Get the messages for the player using their UUID.
			ArrayList<String> messages = messageManager.getMessages(player.getUniqueId().toString());
			// Get the messages for the player using their player name.
			ArrayList<String> playerNameMessages = messageManager.getMessages(player.getName());
			
			// Append the message pools.
			messages.addAll(playerNameMessages);
			
			if(plugin.getConfig().getBoolean("general.messaging.completedMessageOnLogin.useDelay", true))
			{
				int messageGroup = 1;
				int message = 0;
				
				long delayTime = 0;
				int delayTimeInSeconds = plugin.getConfig().getInt("general.messaging.completedMessageOnLogin.delay", 5);
				
				while(!messages.isEmpty())
				{
					// Calculate the delay time in bukkit ticks.
					// (20 bukkit ticks per second * user specified delay time in seconds) * message group number.
					delayTime = (20 * delayTimeInSeconds) * messageGroup;
					
					String output = messages.remove(0);
					
					Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new DelayedMessage(player, output), delayTime);
					
					message++;
					
					if(message % 5 == 0)
						messageGroup++;
				}
			}
			else
			{
				for(String message : messages)
					player.sendMessage(message);
			}
		}
		
		// Remove the messages for the player.
		messageManager.removePlayerMessages(player.getUniqueId().toString());
		messageManager.removePlayerMessages(player.getName());
	}
}