package net.KabOOm356.Command;

import net.KabOOm356.Command.Commands.*;
import net.KabOOm356.Command.Help.HelpCommand;
import net.KabOOm356.Command.Help.HelpCommandDisplay;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.ClaimPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.HelpPhrases;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Manager.LastViewedReportManager;
import net.KabOOm356.Manager.MessageManager;
import net.KabOOm356.Manager.ReportLimitManager;
import net.KabOOm356.Manager.SQLStatManagers.ModeratorStatManager;
import net.KabOOm356.Manager.SQLStatManagers.PlayerStatManager;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Permission.ReporterPermissionManager;
import net.KabOOm356.Reporter.Reporter;
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
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * A Command Manager and Command Executor for all the Reporter Commands.
 */
public class ReporterCommandManager implements CommandExecutor {
	private static final Logger log = LogManager.getLogger(ReporterCommandManager.class);

	private final Reporter plugin;
	private final MessageManager messageManager;
	private final ReporterPermissionManager permissionManager;
	private final ReportLimitManager limitManager;
	private final ModeratorStatManager modStatsManager;
	private final PlayerStatManager playerStatsManager;
	private final LastViewedReportManager lastViewedReportManager;
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

		limitManager = new ReportLimitManager(plugin);

		messageManager = new MessageManager();

		permissionManager = new ReporterPermissionManager();

		modStatsManager = new ModeratorStatManager(plugin.getDatabaseHandler());
		playerStatsManager = new PlayerStatManager(plugin.getDatabaseHandler());

		lastViewedReportManager = new LastViewedReportManager();

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
				sender.sendMessage(ChatColor.RED + plugin.getLocale().getString(GeneralPhrases.tryRespondHelp));
			} else {
				sender.sendMessage(ChatColor.RED + plugin.getLocale().getString(GeneralPhrases.tryReportHelp));
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

			sender.sendMessage(ChatColor.RED + plugin.getLocale().getString(GeneralPhrases.tryRespondHelp));

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

			sender.sendMessage(ChatColor.RED + plugin.getLocale().getString(GeneralPhrases.tryReportHelp));
		}

		return true;
	}

	/**
	 * Checks if the given {@link Player} has the given permission node, or is OP.
	 *
	 * @param player     The {@link Player} to check if they have the permission node.
	 * @param permission The permission node to check.
	 * @return True if the given {@link Player} has the permission node or is OP, otherwise false.
	 * @see ReporterPermissionManager#hasPermission(Player, String)
	 */
	public boolean hasPermission(final Player player, final String permission) {
		return getConfig().getBoolean("general.permissions.opsHaveAllPermissions", true) && player.isOp() || permissionManager.hasPermission(player, permission);

	}

	/**
	 * Checks if the given {@link CommandSender} has the given permission node.
	 * <br /><br />
	 * <b>NOTE:</b> The given {@link CommandSender} will be converted to a {@link Player} first.
	 * <br />But, if the given {@link CommandSender} is not a {@link Player}, {@link Boolean#TRUE} will be returned.
	 *
	 * @param sender     The {@link CommandSender} to check if they have the permission node.
	 * @param permission The permission node to check.
	 * @return True if the given {@link CommandSender} is not a player or has the permission node, otherwise false.
	 * @see ReporterPermissionManager#hasPermission(Player, String)
	 */
	public boolean hasPermission(final CommandSender sender, final String permission) {
		if (BukkitUtil.isPlayer(sender)) {
			final Player player = (Player) sender;
			if (!hasPermission(player, permission)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @see BukkitUtil#getPlayer(String, boolean)
	 */
	public final OfflinePlayer getPlayer(final String playerName) {
		return BukkitUtil.getPlayer(playerName, getConfig().getBoolean("general.matchPartialOfflineUsernames", true));
	}

	/**
	 * Returns the report indexes of all reports the given {@link CommandSender} can view.
	 *
	 * @param sender The {@link CommandSender}
	 * @return An {@link ArrayList} of integers that contains all the report indexes the {@link CommandSender} can view.
	 * @throws Exception
	 */
	public ArrayList<Integer> getViewableReports(final CommandSender sender) throws Exception {
		String query = null;
		final ArrayList<String> params = new ArrayList<String>();

		if (BukkitUtil.isPlayer(sender)) {
			final OfflinePlayer player = (OfflinePlayer) sender;

			query = "SELECT ID FROM Reports WHERE SenderUUID=?";

			params.add(player.getUniqueId().toString());
		} else {
			query = "SELECT ID FROM Reports WHERE Sender=?";
			params.add(sender.getName());
		}

		final ArrayList<Integer> indexes = new ArrayList<Integer>();
		final ExtendedDatabaseHandler database = getDatabaseHandler();
		final int connectionId = database.openPooledConnection();

		try {
			final SQLResultSet result = database.preparedSQLQuery(connectionId, query, params);

			for (final ResultRow row : result) {
				indexes.add(row.getInt("ID"));
			}
		} catch (final Exception e) {
			log.warn("Failed to get viewable reports!");
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}

		return indexes;
	}

	/**
	 * Checks if the given report index is valid.
	 * <br/>If the report index is not valid, the the given {@link CommandSender} will be alerted.
	 *
	 * @param sender   The {@link CommandSender} to alert if the report index is not valid.
	 * @param repIndex The report index to check if it is valid.
	 * @return True if the report index is valid, otherwise false.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public boolean isReportIndexValid(final CommandSender sender, final Integer repIndex) throws ClassNotFoundException, SQLException, InterruptedException {
		final Locale locale = getLocale();
		try {
			final int count = getCount();
			if (repIndex == null) {
				sender.sendMessage(ChatColor.RED + locale.getString(GeneralPhrases.indexInt));
				return false;
			} else if (repIndex < 1 || repIndex > count) {
				sender.sendMessage(ChatColor.RED + locale.getString(GeneralPhrases.indexRange));
				return false;
			}
			return true;
		} catch (final ClassNotFoundException e) {
			log.error("Failed to check if report index is valid!");
			throw e;
		} catch (final SQLException e) {
			log.error("Failed to check if report index is valid!");
			throw e;
		} catch (final InterruptedException e) {
			log.error("Failed to check if report index is valid!");
			throw e;
		}
	}

	/**
	 * Returns the current number of incomplete reports in the database.
	 *
	 * @return The current number of incomplete reports in the database.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public int getIncompleteReports() throws ClassNotFoundException, SQLException, InterruptedException {
		try {
			return getIncompleteReportIndexes().size();
		} catch (final ClassNotFoundException e) {
			log.warn("Failed to get the number of incomplete reports!");
			throw e;
		} catch (final SQLException e) {
			log.warn("Failed to get the number of incomplete reports!");
			throw e;
		} catch (final InterruptedException e) {
			log.warn("Failed to get the number of incomplete reports!");
			throw e;
		}
	}

	/**
	 * Returns the current number of complete reports in the database.
	 *
	 * @return The current number of complete reports in the database.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public int getCompletedReports() throws ClassNotFoundException, SQLException, InterruptedException {
		try {
			return getCompletedReportIndexes().size();
		} catch (final ClassNotFoundException e) {
			log.warn("Failed to get number of completed reports!");
			throw e;
		} catch (final SQLException e) {
			log.warn("Failed to get number of completed reports!");
			throw e;
		} catch (final InterruptedException e) {
			log.warn("Failed to get number of completed reports!");
			throw e;
		}
	}

	/**
	 * Returns the indexes of the completed reports in the database.
	 *
	 * @return An {@link ArrayList} of integers containing the indexes to all the completed reports in the database.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public ArrayList<Integer> getCompletedReportIndexes() throws SQLException, ClassNotFoundException, InterruptedException {
		final ArrayList<Integer> indexes = new ArrayList<Integer>();
		final ExtendedDatabaseHandler database = getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			final String query = "SELECT ID FROM Reports WHERE CompletionStatus=1";
			final SQLResultSet result = database.sqlQuery(connectionId, query);

			for (final ResultRow row : result) {
				indexes.add(row.getInt("ID"));
			}
		} catch (final SQLException e) {
			log.warn("Failed to get completed report indexes!");
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}

		return indexes;
	}

	/**
	 * Returns the indexes of the incomplete reports in the database.
	 *
	 * @return An {@link ArrayList} of integers containing the indexes to all the incomplete reports in the database.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public ArrayList<Integer> getIncompleteReportIndexes() throws ClassNotFoundException, SQLException, InterruptedException {
		final ArrayList<Integer> indexes = new ArrayList<Integer>();
		final ExtendedDatabaseHandler database = getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			final String query = "SELECT ID FROM Reports WHERE CompletionStatus=0";

			final SQLResultSet result = database.sqlQuery(connectionId, query);

			for (final ResultRow row : result) {
				indexes.add(row.getInt("ID"));
			}
		} catch (final SQLException e) {
			log.warn("Failed to get incomplete report indexes!");
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}

		return indexes;
	}

	/**
	 * Returns the current number of reports in the database.
	 *
	 * @return The current number of reports in the database.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public int getCount() throws ClassNotFoundException, SQLException, InterruptedException {
		final ExtendedDatabaseHandler database = getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			final String query = "SELECT COUNT(*) AS Count FROM Reports";
			final SQLResultSet result = database.sqlQuery(connectionId, query);

			return result.getInt("Count");
		} catch (final SQLException e) {
			log.warn("Failed to get total report count!");
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}
	}

	/**
	 * Returns the {@link ModLevel} of the given {@link CommandSender}.
	 *
	 * @param sender The {@link CommandSender} to get the {@link ModLevel} for.
	 * @return The {@link ModLevel} of the given {@link CommandSender}.
	 */
	public ModLevel getModLevel(final CommandSender sender) {
		if (sender.isOp()) {
			return ModLevel.HIGH;
		} else if (sender instanceof ConsoleCommandSender) {
			return ModLevel.HIGH;
		} else {
			if (BukkitUtil.isPlayer(sender)) {
				if (hasPermission((Player) sender, "reporter.modlevel.high")) {
					return ModLevel.HIGH;
				} else if (hasPermission((Player) sender, "reporter.modlevel.normal")) {
					return ModLevel.NORMAL;
				} else if (hasPermission((Player) sender, "reporter.modlevel.low")) {
					return ModLevel.LOW;
				}
			}
		}
		return ModLevel.NONE;
	}

	/**
	 * Checks if the given String is a {@link ModLevel}, if it is not the given {@link CommandSender} is alerted.
	 *
	 * @param sender   The {@link CommandSender} to alert if the String is not a {@link ModLevel} or not in bounds.
	 * @param modLevel The String representation of the {@link ModLevel} to check if is in bounds.
	 * @return True if the String is a {@link ModLevel} and in bounds, otherwise false.
	 */
	public boolean requireModLevelInBounds(final CommandSender sender, final String modLevel) {
		if (ModLevel.modLevelInBounds(modLevel)) {
			return true;
		}
		sender.sendMessage(ChatColor.RED +
				getLocale().getString(GeneralPhrases.priorityLevelNotInBounds));
		return false;
	}

	/**
	 * Checks if the player can alter the given report.
	 *
	 * @param sender The {@link CommandSender} checking if the player can alter the report.
	 * @param index  The index of the report.
	 * @param player The player to check.
	 * @return True if the player can alter the report, otherwise false.
	 */
	public boolean canAlterReport(final CommandSender sender, final int index, final CommandSender player) throws InterruptedException, SQLException, ClassNotFoundException {
		if (player == null) {
			return false;
		}

		try {
			if (!requirePriority(sender, index, player)) {
				return false;
			}

			if (!requireUnclaimedOrPriority(sender, index, player)) {
				sender.sendMessage(ChatColor.WHITE + getLocale().getString(GeneralPhrases.contactToAlter));
				return false;
			}
			return true;
		} catch (final InterruptedException e) {
			log.error(String.format("Failed to check if player [%s] could alter report [%d]!", BukkitUtil.formatPlayerName(player), index));
			throw e;
		} catch (final SQLException e) {
			log.error(String.format("Failed to check if player [%s] could alter report [%d]!", BukkitUtil.formatPlayerName(player), index));
			throw e;
		} catch (final ClassNotFoundException e) {
			log.error(String.format("Failed to check if player [%s] could alter report [%d]!", BukkitUtil.formatPlayerName(player), index));
			throw e;
		}
	}

	/**
	 * Checks if the given {@link CommandSender} can alter the report at the given index.
	 * <br /><br />
	 * Will display a message if the sender cannot alter the report.
	 * <br /><br />
	 * Console and OPs can always alter reports.
	 *
	 * @param sender The {@link CommandSender} wanting to alter a report.
	 * @param index  The index of the report.
	 * @return True if the {@link CommandSender} can alter the given report, otherwise false.
	 */
	public boolean canAlterReport(final CommandSender sender, final int index) throws InterruptedException, SQLException, ClassNotFoundException {
		return canAlterReport(sender, index, sender);
	}

	/**
	 * Checks if report is unclaimed or the sender has high enough priority to supersede the player claiming it.
	 * <br /><br />If the player fails the check, the sender will be alerted.
	 *
	 * @param sender The {@link CommandSender}.
	 * @param index  The index of the report.
	 * @return True if the report is unclaimed or the sender has high enough priority to supersede the player claiming it.
	 */
	public boolean requireUnclaimedOrPriority(final CommandSender sender, final int index) throws ClassNotFoundException, InterruptedException, SQLException {
		return requireUnclaimedOrPriority(sender, index, sender);
	}

	/**
	 * Checks if report is unclaimed or the player has high enough priority to supersede the player claiming it.
	 * <br /><br />If the player fails the check, the sender will be alerted.
	 *
	 * @param sender The {@link CommandSender}.
	 * @param index  The index of the report.
	 * @param player The player to check.
	 * @return True if the report is unclaimed or the player has high enough priority to supersede the player claiming it.
	 */
	public boolean requireUnclaimedOrPriority(final CommandSender sender, final int index, final CommandSender player) throws ClassNotFoundException, InterruptedException, SQLException {
		if (sender.isOp() || sender instanceof ConsoleCommandSender) {
			return true;
		}

		final String query = "SELECT " +
				"ClaimStatus, ClaimedByUUID, ClaimedBy, ClaimPriority " +
				"FROM Reports " +
				"WHERE ID=" + index;

		final ExtendedDatabaseHandler database = getDatabaseHandler();
		Integer connectionId = null;
		try {
			connectionId = database.openPooledConnection();
			final SQLResultSet result = database.sqlQuery(connectionId, query);

			final String claimedByName = result.getString("ClaimedBy");
			final boolean isClaimed = result.getBoolean("ClaimStatus");
			final int claimPriority = result.getInt("ClaimPriority");

			UUID claimedByUUID = null;
			if (!result.getString("ClaimedByUUID").isEmpty()) {
				claimedByUUID = UUID.fromString(result.getString("ClaimedByUUID"));
			}
			final CommandSender claimedBy = CommandSender.class.cast(BukkitUtil.getOfflinePlayer(claimedByUUID, claimedByName));

			// Check if the sender is the player claiming the report.
			// UUID based check.
			final boolean isClaimedBySender = BukkitUtil.playersEqual(sender, claimedBy);
			final boolean isClaimedByPlayer = BukkitUtil.playersEqual(player, claimedBy);

			if (isClaimed && !isClaimedBySender && !isClaimedByPlayer && claimPriority >= getModLevel(player).getLevel()) {
				String output = getLocale().getString(ClaimPhrases.reportAlreadyClaimed);
				final String formattedClaimName = BukkitUtil.formatPlayerName(claimedBy);
				output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.RED);
				output = output.replaceAll("%c", ChatColor.BLUE + formattedClaimName + ChatColor.RED);
				sender.sendMessage(ChatColor.RED + output);

				return false;
			}
		} catch (final ClassNotFoundException e) {
			log.error("Failed to check if report can be altered by player!");
			throw e;
		} catch (final InterruptedException e) {
			log.error("Failed to check if report can be altered by player!");
			throw e;
		} catch (final SQLException e) {
			log.error("Failed to check if report can be altered by player!");
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}

		return true;
	}

	/**
	 * Checks if the given {@link CommandSender} has a high enough priority to alter the report at the given index.
	 *
	 * @param player The {@link CommandSender}.
	 * @param index  The index of the report.
	 * @return True if the {@link CommandSender} has a high enough priority to alter the report at the given index.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public boolean checkPriority(final CommandSender player, final int index) throws ClassNotFoundException, InterruptedException, SQLException {
		if (player instanceof ConsoleCommandSender) {
			return true;
		}

		if (BukkitUtil.isPlayer(player) && Player.class.cast(player).isOp()) {
			return true;
		}

		final ModLevel modLevel = getModLevel(player);
		try {
			final ModLevel reportPriority = getReportPriority(index);
			return reportPriority.getLevel() <= modLevel.getLevel();
		} catch (final ClassNotFoundException e) {
			log.error(String.format("Failed to do a priority check for player [%s] on report [%d]", BukkitUtil.formatPlayerName(player), index));
			throw e;
		} catch (final InterruptedException e) {
			log.error(String.format("Failed to do a priority check for player [%s] on report [%d]", BukkitUtil.formatPlayerName(player), index));
			throw e;
		} catch (final SQLException e) {
			log.error(String.format("Failed to do a priority check for player [%s] on report [%d]", BukkitUtil.formatPlayerName(player), index));
			throw e;
		}
	}

	/**
	 * Checks if the {@link CommandSender} has a high enough priority to alter the report.
	 *
	 * @param sender The {@link CommandSender}.
	 * @param index  The index of the report.
	 * @return True if the {@link CommandSender} has a high enough priority to alter the given report, otherwise false.
	 */
	public boolean requirePriority(final CommandSender sender, final int index) throws InterruptedException, SQLException, ClassNotFoundException {
		return requirePriority(sender, index, sender);
	}

	/**
	 * Checks if the given player has a high enough priority to alter the report.
	 *
	 * @param sender The {@link CommandSender}.
	 * @param index  The index of the report.
	 * @param player The player to check.
	 * @return True if the player has a high enough priority to alter the given report, otherwise false.
	 */
	public boolean requirePriority(final CommandSender sender, final int index, final CommandSender player) throws InterruptedException, SQLException, ClassNotFoundException {
		try {
			if (!checkPriority(player, index)) {
				final ModLevel reportPriority = getReportPriority(index);

				String output = getLocale().getString(GeneralPhrases.reportRequiresClearance);
				output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.RED);
				output = output.replaceAll("%m", reportPriority.getColor() + reportPriority.getName() + ChatColor.RED);

				sender.sendMessage(ChatColor.RED + output);

				if (sender.equals(player)) {
					displayModLevel(sender);
				} else {
					displayModLevel(sender, player);
				}

				return false;
			}
		} catch (final InterruptedException e) {
			log.error(String.format("Failed to check required priority for report [%d]!", index));
			throw e;
		} catch (final SQLException e) {
			log.error(String.format("Failed to check required priority for report [%d]!", index));
			throw e;
		} catch (final ClassNotFoundException e) {
			log.error(String.format("Failed to check required priority for report [%d]!", index));
			throw e;
		}

		return true;
	}

	private ModLevel getReportPriority(final int index) throws ClassNotFoundException, InterruptedException, SQLException {
		final String query = "SELECT Priority FROM Reports WHERE ID=" + index;
		final ExtendedDatabaseHandler database = getDatabaseHandler();
		Integer connectionId = null;
		try {
			connectionId = database.openPooledConnection();
			final SQLResultSet result = database.sqlQuery(connectionId, query);
			final int level = result.getInt("Priority");
			return ModLevel.getByLevel(level);
		} catch (final InterruptedException e) {
			log.error(String.format("Failed to get report priority for index [%d]!", index));
			throw e;
		} catch (final SQLException e) {
			log.error(String.format("Failed to get report priority for index [%d]!", index));
			throw e;
		} catch (final ClassNotFoundException e) {
			log.error(String.format("Failed to get report priority for index [%d]!", index));
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}
	}

	/**
	 * Gets the number of reports with a given {@link ModLevel} priority.
	 *
	 * @param level The {@link ModLevel} to get the number of reports for.
	 * @return The number of reports with the given {@link ModLevel} priority.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public int getNumberOfPriority(final ModLevel level) throws SQLException, ClassNotFoundException, InterruptedException {
		final String query = "SELECT COUNT(*) AS Count FROM Reports WHERE Priority = " + level.getLevel();
		int count = 0;

		final ExtendedDatabaseHandler database = getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			final SQLResultSet result = database.sqlQuery(connectionId, query);

			count = result.getInt("Count");
		} finally {
			database.closeConnection(connectionId);
		}

		return count;
	}

	/**
	 * Gets the indexes of the reports with a given {@link ModLevel} priority.
	 *
	 * @param level The {@link ModLevel} priority of the reports to get the indexes for.
	 * @return The indexes of the reports with the given {@link ModLevel} priority.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public ArrayList<Integer> getIndexesOfPriority(final ModLevel level) throws ClassNotFoundException, SQLException, InterruptedException {
		final ArrayList<Integer> indexes = new ArrayList<Integer>();
		final String query = "SELECT ID FROM Reports WHERE Priority = " + level.getLevel();

		final ExtendedDatabaseHandler database = getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			final SQLResultSet result = database.sqlQuery(connectionId, query);

			for (final ResultRow row : result) {
				indexes.add(row.getInt("ID"));
			}
		} finally {
			database.closeConnection(connectionId);
		}

		return indexes;
	}

	/**
	 * Displays the current {@link ModLevel} to the given {@link CommandSender}.
	 *
	 * @param sender The {@link CommandSender} to display their {@link ModLevel} to.
	 */
	public void displayModLevel(final CommandSender sender) {
		String output = getLocale().getString(GeneralPhrases.displayModLevel);

		final ModLevel level = getModLevel(sender);

		output = output.replaceAll("%m", level.getColor() + level.getName() + ChatColor.WHITE);

		sender.sendMessage(ChatColor.WHITE + output);
	}

	public void displayModLevel(final CommandSender sender, final CommandSender player) {
		final String playerName = BukkitUtil.formatPlayerName(player);

		String output = getLocale().getString(GeneralPhrases.displayOtherModLevel);

		final ModLevel level = getModLevel(player);

		output = output.replaceAll("%p", ChatColor.BLUE + playerName + ChatColor.WHITE);
		output = output.replaceAll("%m", level.getColor() + level.getName() + ChatColor.WHITE);

		sender.sendMessage(ChatColor.WHITE + output);
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

	public ReportLimitManager getReportLimitManager() {
		return limitManager;
	}

	public LastViewedReportManager getLastViewedReportManager() {
		return lastViewedReportManager;
	}

	public MessageManager getMessageManager() {
		return messageManager;
	}

	public ModeratorStatManager getModStatsManager() {
		return modStatsManager;
	}

	public PlayerStatManager getPlayerStatsManager() {
		return playerStatsManager;
	}
}
