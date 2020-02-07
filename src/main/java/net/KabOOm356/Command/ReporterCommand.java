package net.KabOOm356.Command;

import net.KabOOm356.Command.Help.Usage;
import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Runnable.RunnableWithState;
import net.KabOOm356.Runnable.TimedRunnable;
import net.KabOOm356.Service.ServiceModule;
import net.KabOOm356.Throwable.IndexNotANumberException;
import net.KabOOm356.Throwable.IndexOutOfRangeException;
import net.KabOOm356.Throwable.NoLastViewedReportException;
import net.KabOOm356.Throwable.RequiredPermissionException;
import net.KabOOm356.Util.BukkitUtil;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Abstract Reporter Command.
 */
public abstract class ReporterCommand extends TimedRunnable implements RunnableWithState {
	private static final Logger log = LogManager.getLogger(ReporterCommand.class);

	private final ReporterCommandManager manager;
	private final String name;
	private final String permissionNode;
	private final int minimumNumberOfArguments;
	private boolean isRunning = false;
	private boolean isPendingToRun = false;
	private boolean hasRun = false;
	private CommandSender sender = null;
	private List<String> arguments = null;

	/**
	 * Constructor.
	 *
	 * @param manager                  The {@link ReporterCommandManager} that is managing this command.
	 * @param commandName              The name of the command.
	 * @param commandPermissionNode    The permission node required to run this command.
	 * @param minimumNumberOfArguments The minimum number of required arguments to run this command.
	 */
	protected ReporterCommand(
			final ReporterCommandManager manager,
			final String commandName,
			final String commandPermissionNode,
			final int minimumNumberOfArguments) {
		this.manager = manager;

		this.name = commandName;
		this.permissionNode = commandPermissionNode;

		this.minimumNumberOfArguments = minimumNumberOfArguments;
	}

	/**
	 * Executes this command.
	 *
	 * @param sender The {@link CommandSender} whom is executing this command.
	 * @param args   The given arguments from the {@link CommandSender}.
	 */
	public abstract void execute(CommandSender sender, List<String> args) throws NoLastViewedReportException, IndexOutOfRangeException, IndexNotANumberException, RequiredPermissionException;

	/**
	 * Checks if the given {@link Player} has permission to run this command, or is OP.
	 *
	 * @param player The {@link Player} to check.
	 * @return True if the {@link Player} has permission or is OP, otherwise false.
	 */
	public boolean hasPermission(final Player player) {
		return this.hasPermission(player, permissionNode);
	}

	/**
	 * Checks if the given {@link Player} has the given permission node, or is OP.
	 *
	 * @param player The {@link Player} to check.
	 * @param perm   The permission node to check.
	 * @return True if the {@link Player} has the permission node or is OP, otherwise false.
	 */
	public boolean hasPermission(final Player player, final String perm) {
		return manager.getServiceModule().getPermissionService().hasPermission(player, perm);
	}

	/**
	 * Checks if the given {@link CommandSender} has permission to run this command, or is OP.
	 *
	 * @param sender The {@link CommandSender} to check.
	 * @return True if the {@link CommandSender} has permission or is OP, otherwise false.
	 */
	public boolean hasPermission(final CommandSender sender) {
		if (BukkitUtil.isPlayer(sender)) {
			final Player player = (Player) sender;
			return hasPermission(player);
		}
		return true;
	}

	/**
	 * Checks if the given {@link CommandSender} has permission to run this command.
	 *
	 * @param sender The {@link CommandSender} to check.
	 * @throws RequiredPermissionException If the sender does not have the required permission.
	 */
	public void hasRequiredPermission(final CommandSender sender) throws RequiredPermissionException {
		if (!hasPermission(sender)) {
			throw new RequiredPermissionException(String.format("Sender [%s] does not have the required permission [%s]!", BukkitUtil.formatPlayerName(sender), permissionNode));
		}
	}

	protected ReporterCommandManager getManager() {
		return manager;
	}

	protected ServiceModule getServiceModule() {
		return getManager().getServiceModule();
	}

	public String getName() {
		return name;
	}

	public String getPermissionNode() {
		return permissionNode;
	}

	/**
	 * Gets all usages for this command.
	 *
	 * @return An {@link List} of {@link Usage}s.
	 */
	public abstract List<Usage> getUsages();

	/**
	 * Gets all aliases for this command.
	 *
	 * @return An {@link List} of Strings.
	 */
	public abstract List<String> getAliases();

	public String getUsage() {
		return getManager().getLocale().getString(getUsages().get(0).getKey());
	}

	public String getDescription() {
		return getManager().getLocale().getString(getUsages().get(0).getValue());
	}

	public String getErrorMessage() {
		return ChatColor.BLUE + Reporter.getLogPrefix() +
				ChatColor.RED + manager.getLocale().getString(GeneralPhrases.error);
	}

	public String getFailedPermissionsMessage() {
		return ChatColor.RED + BukkitUtil.colorCodeReplaceAll(
				manager.getLocale().getString(GeneralPhrases.failedPermissions));
	}

	public int getMinimumNumberOfArguments() {
		return minimumNumberOfArguments;
	}

	public void setSender(final CommandSender sender) {
		if (isRunning || isPendingToRun) {
			throw new IllegalArgumentException("The current command is in-flight and should not be modified!");
		}
		this.sender = sender;
	}

	public void setArguments(final List<String> arguments) {
		if (isRunning || isPendingToRun) {
			throw new IllegalArgumentException("The current command is in-flight and should not be modified!");
		}
		this.arguments = arguments;
	}

	public ReporterCommand getRunnableClone(final CommandSender sender, final List<String> arguments) throws Exception {
		try {
			final Class<? extends ReporterCommand> clazz = this.getClass();
			final Constructor<? extends ReporterCommand> constructor = clazz.getDeclaredConstructor(ReporterCommandManager.class);
			final ReporterCommand command = constructor.newInstance(manager);
			command.setSender(sender);
			command.setArguments(arguments);
			command.isPendingToRun = true;
			return command;
		} catch (final Exception e) {
			log.warn(String.format("Failed to clone command [%s]!", getClass().getName()));
			throw e;
		}
	}

	@Override
	public void run() {
		Validate.notNull(sender);
		Validate.notNull(arguments);
		try {
			this.start();
			isRunning = true;
			execute(sender, arguments);
		} catch (final NoLastViewedReportException e) {
			final String message = getManager().getLocale().getString(GeneralPhrases.noLastReport);
			sender.sendMessage(message);
		} catch (final IndexNotANumberException e) {
			final String message = getManager().getLocale().getString(GeneralPhrases.indexInt);
			sender.sendMessage(message);
		} catch (final IndexOutOfRangeException e) {
			final String message = getManager().getLocale().getString(GeneralPhrases.indexRange);
			sender.sendMessage(message);
		} catch (final RequiredPermissionException e) {
			final String message = getFailedPermissionsMessage();
			sender.sendMessage(message);
		} finally {
			isRunning = false;
			isPendingToRun = false;
			hasRun = true;
			this.end();
		}
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public boolean isPendingToRun() {
		return isPendingToRun;
	}

	@Override
	public boolean isStopped() {
		return !isRunning;
	}

	@Override
	public boolean hasRun() {
		return hasRun;
	}

	@Override
	public String toString() {
		return "Command Name: " + name + '\n' +
				"Permission Node: " + permissionNode + '\n' +
				"Minimum Number of Arguments: " + minimumNumberOfArguments;
	}
}
