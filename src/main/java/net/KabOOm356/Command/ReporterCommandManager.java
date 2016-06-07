package net.KabOOm356.Command;

import net.KabOOm356.Command.Commands.*;
import net.KabOOm356.Command.Help.HelpCommand;
import net.KabOOm356.Command.Help.HelpCommandDisplay;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.HelpPhrases;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Service.LastViewedReportService;
import net.KabOOm356.Service.ServiceModule;
import net.KabOOm356.Service.PlayerMessageService;
import net.KabOOm356.Service.ReportLimitService;
import net.KabOOm356.Service.SQLStatServices.ModeratorStatService;
import net.KabOOm356.Service.SQLStatServices.PlayerStatService;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Throwable.IndexNotANumberException;
import net.KabOOm356.Throwable.IndexOutOfRangeException;
import net.KabOOm356.Util.ArrayUtil;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.FormattingUtil;
import net.KabOOm356.Util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A Command Service and Command Executor for all the Reporter Commands.
 */
public class ReporterCommandManager implements CommandExecutor {
	private static final Logger log = LogManager.getLogger(ReporterCommandManager.class);

	private final Reporter plugin;
	private final HelpCommand reportHelp;
	private final HelpCommand respondHelp;
	private LinkedHashMap<String, ReporterCommand> reportCommands;
	private HashMap<String, String> aliasReportCommands;
	private LinkedHashMap<String, ReporterCommand> respondCommands;
	private HashMap<String, String> aliasRespondCommands;

	/**
	 * Constructor.
	 *
	 * @param plugin The current instance of {@link Reporter} running.
	 */
	public ReporterCommandManager(final Reporter plugin) {
		this.plugin = plugin;

		initCommands();

		final HelpCommandDisplay.Builder reportHelpDisplayBuilder = new HelpCommandDisplay.Builder();
		reportHelpDisplayBuilder.setHeader(HelpPhrases.reportHelpHeader)
				.setAlias(HelpPhrases.reportHelpAliases)
				.setNext(HelpPhrases.nextReportHelpPage)
				.setHint(GeneralPhrases.tryReportHelp);
		final HelpCommandDisplay reportHelpDisplay = reportHelpDisplayBuilder.build();
		reportHelp = new HelpCommand(getLocale(), getReportCommands().values(), reportHelpDisplay);
		final HelpCommandDisplay.Builder respondHelpDisplayBuilder = new HelpCommandDisplay.Builder();
		respondHelpDisplayBuilder.setHeader(HelpPhrases.respondHelpHeader)
				.setAlias(HelpPhrases.respondHelpAliases)
				.setNext(HelpPhrases.nextRespondHelpPage)
				.setHint(GeneralPhrases.tryRespondHelp);
		final HelpCommandDisplay respondHelpDisplay = respondHelpDisplayBuilder.build();
		respondHelp = new HelpCommand(getLocale(), getRespondCommands().values(), respondHelpDisplay);
	}

	private void initCommands() {
		/*
		 * Initialize the LinkedHashMap with default initialCapacity, 
		 * default loadFactor and loadOrder set to insertion-order.
		 * 
		 * As outlined here: 
		 * http://docs.oracle.com/javase/7/docs/api/java/util/LinkedHashMap.html#constructor_summary
		 */
		reportCommands = new LinkedHashMap<String, ReporterCommand>(16, 0.75F, false);
		aliasReportCommands = new HashMap<String, String>();

		respondCommands = new LinkedHashMap<String, ReporterCommand>(16, 0.75F, false);
		aliasRespondCommands = new HashMap<String, String>();

		initReportCommand(new AssignCommand(this));
		initReportCommand(new ClaimCommand(this));
		initReportCommand(new CompleteCommand(this));
		initReportCommand(new DeleteCommand(this));
		initReportCommand(new DowngradeCommand(this));
		initReportCommand(new ListCommand(this));
		initReportCommand(new MoveCommand(this));
		initReportCommand(new ReportCommand(this));
		initReportCommand(new RequestCommand(this));
		initReportCommand(new StatisticCommand(this));
		initReportCommand(new UnassignCommand(this));
		initReportCommand(new UnclaimCommand(this));
		initReportCommand(new UpgradeCommand(this));
		initReportCommand(new ViewCommand(this));

		initRespondCommand(new RespondCommand(this));
	}

	/**
	 * Adds the ReporterCommand to be executed.
	 * <br /><br />
	 * This also adds all the aliases to be executed.
	 *
	 * @param command The command to initialize as a Report command.
	 */
	private void initReportCommand(final ReporterCommand command) {
		reportCommands.put(command.getName(), command);

		for (final String alias : command.getAliases()) {
			aliasReportCommands.put(alias, command.getName());
		}
	}

	/**
	 * Adds the ReporterCommand to be executed.
	 * <br /><br />
	 * This also adds all the aliases to be executed.
	 *
	 * @param command The command to initialize as a Respond command.
	 */
	private void initRespondCommand(final ReporterCommand command) {
		respondCommands.put(command.getName(), command);

		for (final String alias : command.getAliases()) {
			aliasRespondCommands.put(alias, command.getName());
		}
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (args == null || args.length == 0) {
			if (label.equalsIgnoreCase("respond") || label.equalsIgnoreCase("resp") || label.equalsIgnoreCase("rrespond")) {
				sender.sendMessage(ChatColor.RED + getLocale().getString(GeneralPhrases.tryRespondHelp));
			} else {
				sender.sendMessage(ChatColor.RED + getLocale().getString(GeneralPhrases.tryReportHelp));
			}

			return true;
		}

		// Check if sender is a supported type
		if (!Reporter.isCommandSenderSupported(sender)) {
			sender.sendMessage(ChatColor.RED + "Command Sender type is not supported!");
			return true;
		}

		final ArrayList<String> arguments = ArrayUtil.arrayToArrayList(args);
		net.KabOOm356.Command.Command command;

		// Begin Respond Command
		if (label.equalsIgnoreCase("respond") || label.equalsIgnoreCase("resp") || label.equalsIgnoreCase("rrespond")) {
			command = getCommand(RespondCommand.getCommandName());

			// Respond help command
			if (arguments.size() >= 1 && arguments.get(0).equalsIgnoreCase("help")) {
				int page = 1;

				if (arguments.size() >= 2) {
					if (Util.isInteger(arguments.get(1))) {
						page = Util.parseInt(arguments.get(1));
					}
				}

				respondHelp.printHelp(sender, page);

				return true;
			}

			// Respond to report
			if (arguments.size() >= command.getMinimumNumberOfArguments()) {
				try {
					final net.KabOOm356.Command.Command commandToRun = command.getRunnableClone(sender, arguments);
					Bukkit.getScheduler().runTaskAsynchronously(plugin, commandToRun);
					return true;
				} catch (final Exception e) {
					log.error("Failed to run Respond command!", e);
				}
			} else {
				sender.sendMessage(ChatColor.RED + command.getUsage());
			}

			sender.sendMessage(ChatColor.RED + getLocale().getString(GeneralPhrases.tryRespondHelp));

			return true;
		}
		// Begin Report Commands
		else if (label.equalsIgnoreCase("report") || label.equalsIgnoreCase("rreport") || label.equalsIgnoreCase("rep")) {
			final String subcommand = arguments.remove(0);

			command = getCommand(FormattingUtil.capitalizeFirstCharacter(subcommand));

			// Report help command
			if (subcommand.equalsIgnoreCase("help")) {
				int page = 1;

				if (arguments.size() >= 1) {
					if (Util.isInteger(arguments.get(0))) {
						page = Util.parseInt(arguments.get(0));
					}
				}

				reportHelp.printHelp(sender, page);

				return true;
			}

			if (command != null) {
				if (arguments.size() >= command.getMinimumNumberOfArguments()) {
					try {
						final net.KabOOm356.Command.Command commandToRun = command.getRunnableClone(sender, arguments);
						Bukkit.getScheduler().runTaskAsynchronously(plugin, commandToRun);
						return true;
					} catch (final Exception e) {
						log.error("Failed to run Report command!", e);
					}

				} else {
					sender.sendMessage(ChatColor.RED + BukkitUtil.colorCodeReplaceAll(command.getUsage()));
				}
			} else // Reporting a player
			{
				command = getCommand(ReportCommand.getCommandName());

				arguments.add(0, subcommand);

				if (arguments.size() >= command.getMinimumNumberOfArguments()) {
					try {
						final net.KabOOm356.Command.Command commandToRun = command.getRunnableClone(sender, arguments);
						Bukkit.getScheduler().runTaskAsynchronously(plugin, commandToRun);
						return true;
					} catch (final Exception e) {
						log.error("Failed to Report!", e);
					}
				} else {
					sender.sendMessage(ChatColor.RED + BukkitUtil.colorCodeReplaceAll(command.getUsage()));
				}
			}

			sender.sendMessage(ChatColor.RED + getLocale().getString(GeneralPhrases.tryReportHelp));
		}

		return true;
	}

	/**
	 * @see BukkitUtil#getPlayer(String, boolean)
	 */
	public final OfflinePlayer getPlayer(final String playerName) {
		return BukkitUtil.getPlayer(playerName, getConfig().getBoolean("general.matchPartialOfflineUsernames", true));
	}


	protected Reporter getPlugin() {
		return plugin;
	}

	public ExtendedDatabaseHandler getDatabaseHandler() {
		return plugin.getDatabaseHandler();
	}

	public Locale getLocale() {
		return plugin.getLocale();
	}

	public HashMap<String, ReporterCommand> getReportCommands() {
		return reportCommands;
	}

	public HashMap<String, String> getAliasReportCommands() {
		return aliasReportCommands;
	}

	public HashMap<String, ReporterCommand> getRespondCommands() {
		return respondCommands;
	}

	public HashMap<String, String> getAliasRespondCommands() {
		return aliasRespondCommands;
	}

	public ReporterCommand getCommand(final String commandName) {
		ReporterCommand command = null;

		if (reportCommands.containsKey(commandName)) {
			command = reportCommands.get(commandName);
		} else if (respondCommands.containsKey(commandName)) {
			command = respondCommands.get(commandName);
		} else if (aliasReportCommands.containsKey(commandName)) {
			command = reportCommands.get(aliasReportCommands.get(commandName));
		} else if (aliasRespondCommands.containsKey(commandName)) {
			command = respondCommands.get(aliasRespondCommands.get(commandName));
		}

		return command;
	}

	public FileConfiguration getConfig() {
		return plugin.getConfig();
	}

	public ReportLimitService getReportLimitService() {
		return getModule().getLimitService();
	}

	public LastViewedReportService getLastViewedReportService() {
		return getModule().getLastViewedReportService();
	}

	public PlayerMessageService getMessageService() {
		return getModule().getPlayerMessageService();
	}

	public ModeratorStatService getModStatsService() {
		return getModule().getModStatsService();
	}

	public PlayerStatService getPlayerStatsService() {
		return getModule().getPlayerStatsService();
	}

	private ServiceModule getModule() {
		return getPlugin().getServiceModule();
	}

	// This is only here for backwards compatibility as things get porter to the service module. It wil be removed!
	@Deprecated
	public boolean isReportIndexValid(final CommandSender sender, final int index) throws ClassNotFoundException, SQLException, IndexNotANumberException, InterruptedException, IndexOutOfRangeException {
		getModule().getReportValidatorService().requireReportIndexValid(index);
		// If we didn't throw by this point the index is in range.
		return true;
	}

	// This is only here for backwards compatibility as things get ported to the service module.  It will be removed!
	@Deprecated
	public int getCount() throws InterruptedException, SQLException, ClassNotFoundException {
		return getModule().getReportCountService().getCount();
	}

	// This is only here for backwards compatibility as things get ported to the service module.  It will be removed!
	@Deprecated
	public List<Integer> getViewableReports(final CommandSender sender) throws InterruptedException, SQLException, ClassNotFoundException {
		return getModule().getReportInformationService().getViewableReports(sender);
	}

	// This is only here for backwards compatibility as things get ported to the service module.  It will be removed!
	@Deprecated
	public int getIncompleteReports() throws InterruptedException, SQLException, ClassNotFoundException {
		return getModule().getReportCountService().getIncompleteReports();
	}

	// This is only here for backwards compatibility as things get ported to the service module.  It will be removed!
	@Deprecated
	public int getCompletedReports() throws InterruptedException, SQLException, ClassNotFoundException {
		return getModule().getReportCountService().getCompletedReports();
	}

	// This is only here for backwards compatibility as things get ported to the service module.  It will be removed!
	@Deprecated
	public List<Integer> getCompletedReportIndexes() throws InterruptedException, SQLException, ClassNotFoundException {
		return getModule().getReportInformationService().getCompletedReportIndexes();
	}

	// This is only here for backwards compatibility as things get ported to the service module.  It will be removed!
	@Deprecated
	public List<Integer> getIncompleteReportIndexes() throws InterruptedException, SQLException, ClassNotFoundException {
		return getModule().getReportInformationService().getIncompleteReportIndexes();
	}

	// This is only here for backwards compatibility as things get ported to the service module.  It will be removed!
	@Deprecated
	public int getNumberOfPriority(final ModLevel level) throws InterruptedException, SQLException, ClassNotFoundException {
		return getModule().getReportCountService().getNumberOfPriority(level);
	}

	// This is only here for backwards compatibility as things get ported to the service module.  It will be removed!
	@Deprecated
	public List<Integer> getIndexesOfPriority(final ModLevel level) throws InterruptedException, SQLException, ClassNotFoundException {
		return getModule().getReportInformationService().getIndexesOfPriority(level);
	}

	// This is only here for backwards compatibility as things get ported to the service module.  It will be removed!
	@Deprecated
	public boolean hasPermission(final CommandSender sender, final String permission) {
		return getModule().getPermissionService().hasPermission(sender, permission);
	}

	// This is only here for backwards compatibility as things get ported to the service module.  It will be removed!
	@Deprecated
	public boolean hasPermission(final Player player, final String permission) {
		return getModule().getPermissionService().hasPermission(player, permission);
	}

	// This is only here for backwards compatibility as things get ported to the service module.  It will be removed!
	@Deprecated
	public boolean canAlterReport(final CommandSender sender, final int index) throws InterruptedException, SQLException, ClassNotFoundException {
		return getModule().getReportPermissionService().canAlterReport(sender, index);
	}

	// This is only here for backwards compatibility as things get ported to the service module.  It will be removed!
	@Deprecated
	public boolean requireModLevelInBounds(final CommandSender sender, final String modLevel) {
		return getModule().getPlayerService().requireModLevelInBounds(sender, modLevel);
	}

	// This is only here for backwards compatibility as things get ported to the service module.  It will be removed!
	@Deprecated
	public ModLevel getModLevel(final CommandSender sender) {
		return getModule().getPlayerService().getModLevel(sender);
	}

	// This is only here for backwards compatibility as things get ported to the service module.  It will be removed!
	@Deprecated
	public boolean canAlterReport(final CommandSender sender, final int index, final Player player) throws InterruptedException, SQLException, ClassNotFoundException {
		return getModule().getReportPermissionService().canAlterReport(sender, index, player);
	}

	// This is only here for backwards compatibility as things get ported to the service module.  It will be removed!
	@Deprecated
	public void displayModLevel(final CommandSender sender) {
		getModule().getPlayerService().displayModLevel(sender);
	}

	// This is only here for backwards compatibility as things get ported to the service module.  It will be removed!
	@Deprecated
	public boolean requirePriority(final CommandSender sender, final int index, final Player player) throws InterruptedException, SQLException, ClassNotFoundException {
		return getModule().getReportPermissionService().requirePriority(sender, index, player);
	}
}
