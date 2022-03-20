package net.KabOOm356.Service;

import net.KabOOm356.Permission.PermissionHandler;
import net.KabOOm356.Util.BukkitUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

/**
 * A manager to handle permissions.
 */
public class PermissionService extends Service {
	/**
	 * Constructor.
	 */
	protected PermissionService(final ServiceModule module) {
		super(module);
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
	 * @see PermissionService#hasPermission(Player, String)
	 */
	public boolean hasPermission(final CommandSender sender, final String permission) {
		if (BukkitUtil.isPlayer(sender)) {
      final Player player = (Player) sender;
			return hasPermission(player, permission);
		}
		return true;
	}

	/**
	 * Checks if the given {@link Player} has the given permission node.
	 *
	 * @param player     The {@link Player}.
	 * @param permission The permission node to check for.
	 * @return True if the given {@link Player} has the given permission node, otherwise false.
	 */
	public boolean hasPermission(final Player player, final String permission) {
		return hasPermissionOverride(player) || checkPermission(player, permission);
	}

	private boolean checkPermission(final Player player, final String permission) {
		return getPermissions().hasPermission(player, permission);
	}

	private boolean hasPermissionOverride(final Player player) {
		return getConfiguration().getBoolean("general.permissions.opsHaveAllPermissions", true) && player.isOp();
	}

	private PermissionHandler getPermissions() {
		return getStore().getPermissionStore().get();
	}

	private Configuration getConfiguration() {
		return getStore().getConfigurationStore().get();
	}
}
