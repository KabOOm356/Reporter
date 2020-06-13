package net.KabOOm356.Command;

import net.KabOOm356.Command.Commands.*;
import net.KabOOm356.Command.Help.HelpCommand;
import net.KabOOm356.Command.Help.HelpCommandDisplay;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.HelpPhrases;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Service.ServiceModule;
import net.KabOOm356.Throwable.RequiredPermissionException;
import net.KabOOm356.Util.ArrayUtil;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.FormattingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A Command Service and Command Executor for all the Reporter Commands.
 */
public class ReporterCommandManager implements CommandExecutor {
	private static final Logger log = LogManager.getLogger(ReporterCommandManager.class);

	private final Reporter plugin;
	private final HelpCommand respondHelp;
	private Map<String, ReporterCommand> reportCommands;
	private Map<String, String> aliasReportCommands;
	private Map<String, ReporterCommand> respondCommands;
	private Map<String, String> aliasRespondCommands;

	/**
	 * Constructor.
	 *
	 * @param plugin The current instance of {@link Reporter} running.
	 */
	public ReporterCommandManager(final Reporter plugin) {
		this.plugin = plugin;

		initCommands();

		final HelpCommandDisplay.Builder respondHelpDisplayBuilder = new HelpCommandDisplay.Builder();
		respondHelpDisplayBuilder.setHeader(HelpPhrases.respondHelpHeader)
				.setAlias(HelpPhrases.respondHelpAliases)
				.setNext(HelpPhrases.nextRespondHelpPage)
				.setHint(GeneralPhrases.tryRespondHelp);
		final HelpCommandDisplay respondHelpDisplay = respondHelpDisplayBuilder.build();
		respondHelp = new HelpCommand(this, getLocale(), getRespondCommands().values(), respondHelpDisplay);
	}

	private void initCommands() {
		/*
		 * Initialize the LinkedHashMap with default initialCapacity, 
		 * default loadFactor and loadOrder set to insertion-order.
		 * 
		 * As outlined here: 
		 * http://docs.oracle.com/javase/7/docs/api/java/util/LinkedHashMap.html#constructor_summary
		 */
		reportCommands = new LinkedHashMap<>(16, 0.75F, false);
		aliasReportCommands = new HashMap<>();

		respondCommands = new LinkedHashMap<>(16, 0.75F, false);
		aliasRespondCommands = new HashMap<>();

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
		final HelpCommandDisplay.Builder reportHelpDisplayBuilder = new HelpCommandDisplay.Builder();
		reportHelpDisplayBuilder.setHeader(HelpPhrases.reportHelpHeader)
				.setAlias(HelpPhrases.reportHelpAliases)
				.setNext(HelpPhrases.nextReportHelpPage)
				.setHint(GeneralPhrases.tryReportHelp);
		final HelpCommandDisplay reportHelpDisplay = reportHelpDisplayBuilder.build();
		initReportCommand(new HelpCommand(this, getLocale(), getReportCommands().values(), reportHelpDisplay));

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
		final boolean isRespondCommand = label.equalsIgnoreCase("respond") || label.equalsIgnoreCase("resp") || label.equalsIgnoreCase("rrespond");
		if (args == null || args.length == 0) {
			if (isRespondCommand) {
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

		final List<String> arguments = ArrayUtil.arrayToList(args);
		ReporterCommand command;

		// Begin Respond Command
		if (isRespondCommand) {
			command = getCommand(RespondCommand.getCommandName());

			// Respond help command
			if (arguments.size() >= 1 && arguments.get(0).equalsIgnoreCase("help")) {
				arguments.remove(0);
				try {
					respondHelp.execute(sender, arguments);
				} catch (final RequiredPermissionException e) {
					sender.sendMessage(command.getFailedPermissionsMessage());
				} catch (final Throwable e) {
					log.error("Failed to run Respond help command!", e);
				}

				return true;
			}

			// Respond to report
			if (arguments.size() >= command.getMinimumNumberOfArguments()) {
				try {
					command.setSender(sender);
					command.setArguments(arguments);
					command.run();
					return true;
				} catch (final Throwable e) {
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

			if (command != null) {
				// Report help command
				if (command.getName().toLowerCase().equals("help")) {
					try {
						command.execute(sender, arguments);
					} catch (final RequiredPermissionException e) {
						sender.sendMessage(command.getFailedPermissionsMessage());
					} catch (final Throwable e) {
						log.error("Failed to run Report help command!", e);
					}

					return true;
				}

				if (arguments.size() >= command.getMinimumNumberOfArguments()) {
					try {
						final ReporterCommand commandToRun = command.getRunnableClone(sender, arguments);
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
						final ReporterCommand commandToRun = command.getRunnableClone(sender, arguments);
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

	public Map<String, ReporterCommand> getReportCommands() {
		return reportCommands;
	}

	public Map<String, String> getAliasReportCommands() {
		return aliasReportCommands;
	}

	public Map<String, ReporterCommand> getRespondCommands() {
		return respondCommands;
	}

	public Map<String, String> getAliasRespondCommands() {
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

	public ServiceModule getServiceModule() {
		return getPlugin().getServiceModule();
	}
}
