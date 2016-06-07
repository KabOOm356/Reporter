package net.KabOOm356.Permission;

import net.KabOOm356.Reporter.Reporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionHandler {
	private static final Logger log = LogManager.getLogger(PermissionHandler.class);
	private static final String permissionExName = "PermissionsEx";

	/**
	 * The type of permissions system being used.
	 */
	private PermissionType type;

	/**
	 * The permissions manager if PermissionsEX is being used.
	 */
	private PermissionManager permissionsExHandler;

	public PermissionHandler() {
		type = null;

		setupPermissionsEx();

		if (type == null) {
			type = PermissionType.SuperPerms;
		}

		log.info(Reporter.getDefaultConsolePrefix() + type + " support enabled.");
	}

	/**
	 * Called by the constructor to help initialize and set up PermissionsEX
	 */
	private void setupPermissionsEx() {
		if (Bukkit.getPluginManager().isPluginEnabled(permissionExName)) {
			permissionsExHandler = PermissionsEx.getPermissionManager();

			if (permissionsExHandler == null) {
				log.warn(Reporter.getDefaultConsolePrefix() + "Failed to obtain PermissionsEx handler.");
				log.warn(Reporter.getDefaultConsolePrefix() + "PermissionsEx support could not be enabled.");
			} else {
				type = PermissionType.PermissionsEx;
			}
		}
	}

	private boolean usingPermissionsEx() {
		return PermissionType.PermissionsEx == type;
	}

	public boolean hasPermission(final Player player, final String permission) {
		if (usingPermissionsEx()) {
			return permissionsExHandler.has(player, permission);
		}
		return player.hasPermission(permission);
	}
}
