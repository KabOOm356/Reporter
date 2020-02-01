package net.KabOOm356.Command.Commands;

import net.KabOOm356.Command.Help.Usage;
import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Locale.Entry.LocalePhrases.AssignPhrases;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Service.SQLStatServices.ModeratorStatService.ModeratorStat;
import net.KabOOm356.Throwable.IndexNotANumberException;
import net.KabOOm356.Throwable.IndexOutOfRangeException;
import net.KabOOm356.Throwable.NoLastViewedReportException;
import net.KabOOm356.Util.ArrayUtil;
import net.KabOOm356.Util.BukkitUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A {@link ReporterCommand} to handle assigning players to reports.
 */
public class AssignCommand extends ReporterCommand {
	private final static Logger log = LogManager.getLogger(AssignCommand.class);

	private final static String name = "Assign";
	private final static int minimumNumberOfArguments = 2;
	private final static String permissionNode = "reporter.assign";

	private static final List<Usage> usages = Collections.unmodifiableList(ArrayUtil.arrayToList(new Usage[]{new Usage(AssignPhrases.assignHelp, AssignPhrases.assignHelpDetails)}));
	private static final List<String> aliases = Collections.emptyList();

	/**
	 * Constructor.
	 *
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public AssignCommand(final ReporterCommandManager manager) {
		super(manager, name, permissionNode, minimumNumberOfArguments);
	}

	/**
	 * Returns the name of this command.
	 *
	 * @return The name of this command.
	 */
	public static String getCommandName() {
		return name;
	}

	/**
	 * Returns the permission node of this command.
	 *
	 * @return The permission node of this command.
	 */
	public static String getCommandPermissionNode() {
		return permissionNode;
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) throws NoLastViewedReportException, IndexOutOfRangeException, IndexNotANumberException {
		try {
			if (!hasRequiredPermission(sender)) {
				return;
			}

			final int index = getServiceModule().getLastViewedReportService().getIndexOrLastViewedReport(sender, args.get(0));

			if (!getServiceModule().getReportValidatorService().isReportIndexValid(index)) {
				return;
			}

			if (!getServiceModule().getReportPermissionService().canAlterReport(sender, index)) {
				return;
			}

			final Player player = BukkitUtil.getOfflinePlayer(args.get(1)).getPlayer();

			if (canAssignReport(sender, index, player)) {
				assignReport(sender, index, player);
			}
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to assign player!", e);
			sender.sendMessage(getErrorMessage());
		}
	}

	private void assignReport(final CommandSender sender, final int index, final Player player) throws ClassNotFoundException, SQLException, InterruptedException {
		final String query = "UPDATE Reports SET ClaimStatus=?, ClaimDate=?, ClaimedBy=?, ClaimedByUUID=?, ClaimPriority=? WHERE ID=?";
		final List<String> params = new ArrayList<>();

		params.add(0, "1");
		params.add(1, Reporter.getDateformat().format(new Date()));
		params.add(2, player.getName());
		params.add(3, player.getUniqueId().toString());
		params.add(4, Integer.toString(getServiceModule().getPlayerService().getModLevel(player).getLevel()));
		params.add(5, Integer.toString(index));

		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			database.preparedUpdateQuery(connectionId, query, params);
		} catch (final SQLException e) {
			log.error(String.format("Failed to execute assign query on connection [%d]!", connectionId));
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}

		String playerName;

		playerName = ChatColor.BLUE + BukkitUtil.formatPlayerName(player) + ChatColor.WHITE;

		String output = getManager().getLocale().getString(AssignPhrases.assignSuccessful);

		output = output.replaceAll("%p", playerName);
		output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);

		sender.sendMessage(ChatColor.WHITE + output);

		playerName = ChatColor.BLUE + BukkitUtil.formatPlayerName(sender) + ChatColor.WHITE;

		output = getManager().getLocale().getString(AssignPhrases.assignedToReport);

		output = output.replaceAll("%p", playerName);
		output = output.replaceAll("%i", ChatColor.GOLD + Integer.toString(index) + ChatColor.WHITE);

		player.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + output);

		if (BukkitUtil.isOfflinePlayer(sender)) {
			final OfflinePlayer senderPlayer = (OfflinePlayer) sender;

			getServiceModule().getModStatsService().incrementStat(senderPlayer, ModeratorStat.ASSIGNED);
		}
	}

	/**
	 * Checks if the given {@link CommandSender} can assign the given player to the report at the given index.
	 *
	 * @param sender The CommandSender.
	 * @param index  The index of the report.
	 * @param player The player to assign to the report.
	 * @return True if the {@link CommandSender} can assign the player to the report, otherwise false.
	 */
	public boolean canAssignReport(final CommandSender sender, final int index, final Player player) throws InterruptedException, SQLException, ClassNotFoundException {
		String output;

		if (player == null) {
			output = getManager().getLocale().getString(AssignPhrases.assignedPlayerMustBeOnline);
			sender.sendMessage(ChatColor.RED + output);

			return false;
		}

		if (!getServiceModule().getReportPermissionService().canAlterReport(sender, index, player)) {
			return false;
		}

		if (BukkitUtil.playersEqual(sender, player)) {
			output = getManager().getLocale().getString(AssignPhrases.useClaimToAssignSelf);
			sender.sendMessage(ChatColor.RED + output);

			return false;
		}

		final ModLevel senderLevel = getServiceModule().getPlayerService().getModLevel(sender);
		final ModLevel playerLevel = getServiceModule().getPlayerService().getModLevel(player);

		final boolean senderHasLowerModLevel = senderLevel.getLevel() <= playerLevel.getLevel();
		final boolean senderIsConsoleOrOp = sender.isOp() || sender instanceof ConsoleCommandSender;

		if (!senderIsConsoleOrOp && senderHasLowerModLevel) {
			output = getManager().getLocale().getString(AssignPhrases.cannotAssignHigherPriority);
			sender.sendMessage(ChatColor.RED + output);

			output = getManager().getLocale().getString(AssignPhrases.playerPriority);
			output = output.replaceAll("%p", ChatColor.BLUE + BukkitUtil.formatPlayerName(player) + ChatColor.WHITE);
			output = output.replaceAll("%m", playerLevel.getColor() + playerLevel.getName() + ChatColor.WHITE);
			sender.sendMessage(ChatColor.WHITE + output);
			getServiceModule().getPlayerService().displayModLevel(sender);

			return false;
		}

		return getServiceModule().getReportPermissionService().requirePriority(sender, index, player);
	}

	@Override
	public List<Usage> getUsages() {
		return usages;
	}

	@Override
	public List<String> getAliases() {
		return aliases;
	}
}
