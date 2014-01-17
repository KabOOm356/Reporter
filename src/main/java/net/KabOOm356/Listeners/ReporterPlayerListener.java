package net.KabOOm356.Listeners;

import java.util.ArrayList;

import net.KabOOm356.Command.Commands.ListCommand;
import net.KabOOm356.Command.Commands.ViewCommand;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.AlertPhrases;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Runnable.DelayedMessage;
import net.KabOOm356.Runnable.ListOnLoginThread;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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
		
		if(plugin.getCommandManager().getMessageManager().hasMessages(player.getName()))
			sendMessages(player);
		
		if(plugin.getConfig().getBoolean("general.messaging.listOnLogin.listOnLogin", true))
			listOnLogin(player);
		
		if(isPlayerReported(event.getPlayer()))
			alertThatReportedPlayerLogin(event.getPlayer());
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
		boolean canView = plugin.getCommandManager().hasPermission(player, ViewCommand.getCommandPermissionNode());
		canView = canView || plugin.getConfig().getBoolean("general.canViewSubmittedReports", true);
		
		// No point to send the message if the player can't view their report.
		if(canView)
		{
			ArrayList<String> messages = plugin.getCommandManager().getMessageManager().getMessages(player.getName());
			
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
		
		plugin.getCommandManager().getMessageManager().removePlayerMessages(player.getName());
	}
	
	private boolean isPlayerReported(Player player)
	{
		String query = "SELECT ID " +
				"FROM Reports " +
				"WHERE ReportedRaw = '" + player.getName() + "' AND !CompletionStatus";
		
		SQLResultSet result = null;
		
		try
		{
			result = plugin.getDatabaseHandler().sqlQuery(query);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		if(!result.isEmpty())
			return true;
		return false;
	}
	
	private void alertThatReportedPlayerLogin(Player reportedPlayer)
	{
		String query = "SELECT ID, ClaimStatus, ClaimedByRaw " +
				"FROM Reports " +
				"WHERE ReportedRaw = '" + reportedPlayer.getName() + "' AND !CompletionStatus";
		
		SQLResultSet result = null;
		
		try
		{
			result = plugin.getDatabaseHandler().sqlQuery(query);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		String playerLoginMessage = ChatColor.BLUE + Reporter.getLogPrefix() +
				ChatColor.WHITE + plugin.getLocale().getString(AlertPhrases.alertClaimedPlayerLogin);
		
		for(ResultRow row : result)
		{
			// If a report is claimed send a message to the claimer, if they are online.
			if(row.getBoolean("ClaimStatus"))
			{
				OfflinePlayer player = Bukkit.getOfflinePlayer(row.getString("ClaimedByRaw"));
				
				if(player.isOnline())
				{
					String output = playerLoginMessage.replaceAll("%r", ChatColor.RED + BukkitUtil.formatPlayerName(reportedPlayer) + ChatColor.WHITE);
					output = output.replaceAll("%i", ChatColor.GOLD + row.getString("ID") + ChatColor.WHITE);
					
					player.getPlayer().sendMessage(output);
				}
			}
			else
			{
				// Add the ID to the indexes to be sent to all players that can receive the alert.
				indexes.add(row.getInt("ID"));
			}
		}
		
		playerLoginMessage = ChatColor.BLUE + Reporter.getLogPrefix() +
				ChatColor.WHITE + plugin.getLocale().getString(AlertPhrases.alertUnclaimedPlayerLogin);
		
		playerLoginMessage = playerLoginMessage.replaceAll("%r", ChatColor.RED + BukkitUtil.formatPlayerName(reportedPlayer) + ChatColor.WHITE);
		playerLoginMessage = playerLoginMessage.replaceAll("%i", Util.indexesToString(indexes, ChatColor.GOLD, ChatColor.WHITE));
		
		for(Player player : Bukkit.getOnlinePlayers())
		{
			// Send the message to players with the permission to get it.
			if(plugin.getCommandManager().hasPermission(player, "reporter.alerts.onlogin.reportedPlayerLogin"))
				player.sendMessage(playerLoginMessage);
		}
	}
}