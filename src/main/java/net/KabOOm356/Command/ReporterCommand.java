package net.KabOOm356.Command;

import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Util.BukkitUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Abstract Reporter Command.
 */
public abstract class ReporterCommand extends Command {
	/**
	 * @see Command#Command(ReporterCommandManager, String, String, int)
	 */
	protected ReporterCommand(
			final ReporterCommandManager manager,
			final String commandName,
			final String commandPermissionNode,
			final int minimumNumberOfArguments) {
		super(manager, commandName, commandPermissionNode, minimumNumberOfArguments);
	}

	/**
	 * Gets the last viewed report index for the given sender.
	 *
	 * @param sender The {@link CommandSender} to get the last viewed report index for.
	 * @return The last viewed report index of the given {@link CommandSender}.
	 */
	public int getLastViewed(final CommandSender sender) {
		return getManager().getLastViewed().get(sender);
	}

	/**
	 * Checks if the given {@link CommandSender} has a last viewed report.
	 * <br/><br/>
	 * If the user does not have a last viewed report they will be alerted.
	 *
	 * @param sender The {@link CommandSender}.
	 * @return True if the {@link CommandSender} has a last viewed report, otherwise false.
	 * @see #hasLastViewed(CommandSender)
	 */
	public boolean hasRequiredLastViewed(final CommandSender sender) {
		if (!hasLastViewed(sender)) {
			sender.sendMessage(ChatColor.RED + BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(GeneralPhrases.noLastReport)));
			return false;
		}
		return true;
	}

	/**
	 * Checks if the given {@link CommandSender} has a last viewed report.
	 * <br/><br/>
	 * This is a silent version of {@link #hasRequiredLastViewed(CommandSender)}, meaning even if the user
	 * <br/>
	 * does not have a last viewed report they will not be alerted.
	 *
	 * @param sender The {@link CommandSender}.
	 * @return True if the {@link CommandSender} has a last viewed report, otherwise false.
	 * @see #hasRequiredLastViewed(CommandSender)
	 */
	public boolean hasLastViewed(final CommandSender sender) {
		return getManager().getLastViewed().get(sender) != -1;
	}
}
