package net.KabOOm356.Command.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.StatisticPhrases;
import net.KabOOm356.Manager.SQLStatManager.SQLStat;
import net.KabOOm356.Manager.SQLStatManagers.ModeratorStatManager.ModeratorStat;
import net.KabOOm356.Manager.SQLStatManagers.PlayerStatManager.PlayerStat;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.ObjectPair;

public class StatisticCommand extends ReporterCommand
{
	private final static String name = "Statistic";
	private final static int minimumNumberOfArguments = 1;
	private final static String permissionNode = "reporter.statistic.*";
	
	public StatisticCommand(ReporterCommandManager manager)
	{
		super(manager, name, permissionNode, minimumNumberOfArguments);
		
		super.getAliases().add("Stat");
		
		updateDocumentation();
	}
	
	@Override
	public void execute(CommandSender sender, ArrayList<String> args)
	{
		if(args.get(0).equalsIgnoreCase("list"))
		{
			if(getManager().hasPermission(sender, "reporter.statistic.list"))
			{
				listStatistics(sender);
			}
			else
			{
				sender.sendMessage(ChatColor.RED +
						getManager().getLocale().getString(GeneralPhrases.failedPermissions));
			}
		}
		else if(args.size() >= 2)
		{
			SQLStat statistic = SQLStat.getByName(args.get(1));
			
			if(statistic == null)
			{
				String message = getManager().getLocale().getString(StatisticPhrases.notValidStatistic);
				sender.sendMessage(ChatColor.RED + message);
				
				message = getManager().getLocale().getString(StatisticPhrases.tryStatisticList);
				sender.sendMessage(ChatColor.GOLD + message);
				
				return;
			}
			
			OfflinePlayer player = getManager().getPlayer(args.get(0));
			
			if(player == null)
			{
				String message = getManager().getLocale().getString(GeneralPhrases.playerDoesNotExist);
				sender.sendMessage(ChatColor.RED + message);
				return;
			}
			
			if(statistic == SQLStat.ALL)
			{
				if(args.size() >= 3)
				{
					if(args.get(2).equalsIgnoreCase("mod"))
					{
						if(getManager().hasPermission(sender, "reporter.statistic.read.mod"))
						{
							displayAllModStatistics(sender, player);
						}
						else
						{
							sender.sendMessage(ChatColor.RED +
									getManager().getLocale().getString(GeneralPhrases.failedPermissions));
						}
					}
					else if(args.get(2).equalsIgnoreCase("player"))
					{
						if(getManager().hasPermission(sender, "reporter.statistic.read.player"))
						{
							displayAllPlayerStatistics(sender, player);
						}
						else
						{
							sender.sendMessage(ChatColor.RED +
									getManager().getLocale().getString(GeneralPhrases.failedPermissions));
						}
					}
					else
					{
						displayAllStatistics(sender, player);
					}
				}
				else
				{
					displayAllStatistics(sender, player);
				}
			}
			else // View an individual statistic.
			{
				// Check the permission for the individual statistic.
				String permission = getStatisticPermission(statistic);
				
				if(!getManager().hasPermission(sender, permission))
				{
					sender.sendMessage(ChatColor.RED + getManager().getLocale().getString(GeneralPhrases.failedPermissions));
					return;
				}
				
				displayStatistic(sender, player, statistic);
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + getUsage());
		}
	}
	
	private void displayStatistic(CommandSender sender, OfflinePlayer player,
			SQLStat statistic)
	{
		ResultRow result = this.getStatistic(player, statistic);
		
		if(result.isEmpty() || result.getString(statistic.getColumnName()).equals(""))
		{
			String message = getManager().getLocale().getString(StatisticPhrases.noStatisticEntry);
			
			message = message.replaceAll("%s", ChatColor.GREEN + statistic.getName() + ChatColor.WHITE);
			message = message.replaceAll("%p", ChatColor.BLUE + BukkitUtil.formatPlayerName(player) + ChatColor.WHITE);
			
			sender.sendMessage(ChatColor.WHITE + message);
		}
		else
		{
			String message = getManager().getLocale().getString(StatisticPhrases.displayStatistic);
			
			message = message.replaceAll("%s", ChatColor.GREEN + statistic.getName() + ChatColor.WHITE);
			message = message.replaceAll("%p", ChatColor.BLUE + BukkitUtil.formatPlayerName(player) + ChatColor.WHITE);
			message = message.replaceAll("%v", ChatColor.GOLD + result.getString(statistic.getColumnName()) + ChatColor.WHITE);
			
			sender.sendMessage(ChatColor.WHITE + message);
		}
	}

	private void displayAllStatistics(CommandSender sender, OfflinePlayer player)
	{
		displayAllModStatistics(sender, player);
		displayAllPlayerStatistics(sender, player);
	}
	
	private void displayAllModStatistics(CommandSender sender, OfflinePlayer player)
	{
		if(!getManager().hasPermission(sender, "reporter.statistic.read.mod"))
		{
			return;
		}
		
		ArrayList<SQLStat> stats = SQLStat.getAll(ModeratorStat.class);
		
		for(SQLStat stat : stats)
		{
			displayStatistic(sender, player, stat);
		}
	}
	
	private void displayAllPlayerStatistics(CommandSender sender, OfflinePlayer player)
	{
		if(!getManager().hasPermission(sender, "reporter.statistic.read.player"))
		{
			return;
		}
		
		ArrayList<SQLStat> stats = SQLStat.getAll(PlayerStat.class);
		
		for(SQLStat stat : stats)
		{
			displayStatistic(sender, player, stat);
		}
	}

	private void listStatistics(CommandSender sender)
	{
		String playerStatsList = ChatColor.WHITE + getManager().getLocale().getString(StatisticPhrases.availablePlayerStatistics);
		String modStatsList = ChatColor.WHITE + getManager().getLocale().getString(StatisticPhrases.availableModeratorStatistics);
		
		ArrayList<SQLStat> stats = SQLStat.getAll(PlayerStat.class);
		
		playerStatsList = playerStatsList.replaceAll("%s", getStatisticNameString(stats));
		
		stats = SQLStat.getAll(ModeratorStat.class);
		
		modStatsList = modStatsList.replaceAll("%s", getStatisticNameString(stats));
		
		sender.sendMessage(playerStatsList);
		sender.sendMessage(modStatsList);
	}
	
	private static String getStatisticNameString(ArrayList<SQLStat> stats)
	{
		StringBuilder stb = new StringBuilder();
		
		for(int LCV = 0; LCV < stats.size(); LCV++)
		{
			SQLStat stat = stats.get(LCV);
			
			stb.append(ChatColor.GOLD + stat.getName());
			
			if(LCV != stats.size()-1)
			{
				stb.append(ChatColor.WHITE + ", ");
			}
		}
		
		return stb.toString();
	}
	
	private ResultRow getStatistic(OfflinePlayer player, SQLStat statistic)
	{
		if(statistic instanceof ModeratorStat)
		{
			return getManager().getModStatsManager().getStat(player, statistic);
		}
		else if(statistic instanceof PlayerStat)
		{
			return getManager().getPlayerStatsManager().getStat(player, statistic);
		}
		
		return null;
	}
	
	private static String getStatisticPermission(SQLStat statistic)
	{
		String permission = "reporter.statistic.read.*";
		
		if(statistic instanceof ModeratorStat)
		{
			permission = "reporter.statistic.read.mod";
		}
		else if(statistic instanceof PlayerStat)
		{
			permission = "reporter.statistic.read.player";
		}
		
		return permission;
	}
	
	@Override
	public void updateDocumentation()
	{
		Locale locale = getManager().getLocale();
		ArrayList<ObjectPair<String, String>> usages = super.getUsages();
		
		usages.clear();
		
		String usage = locale.getString(StatisticPhrases.statisticHelp);
		String description = locale.getString(StatisticPhrases.statisticHelpDetails);
		
		ObjectPair<String, String> entry = new ObjectPair<String, String>(usage, description);
		
		usages.add(entry);
		
		usage = "/report statistic/stat list";
		description = locale.getString(StatisticPhrases.statisticListHelpDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		
		usages.add(entry);
		
		usage = "/report statistic/stat <Player Name> all";
		description = locale.getString(StatisticPhrases.statisticAllHelpDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		
		usages.add(entry);
		
		usage = "/report statistic/stat <Player Name> all mod|player";
		description = locale.getString(StatisticPhrases.statisticAllModPlayerHelpDetails);
		
		entry = new ObjectPair<String, String>(usage, description);
		
		usages.add(entry);
	}
}
