package net.KabOOm356.Permission;

import net.KabOOm356.Reporter.Reporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;

public class PermissionHandler {
	private static final Logger log = LogManager.getLogger(PermissionHandler.class);

	/**
	 * The type of permissions system being used.
	 */
	private PermissionType type;

	public PermissionHandler() {
		type = PermissionType.SuperPerms;

		log.info(Reporter.getDefaultConsolePrefix() + type + " support enabled.");
	}

	public boolean hasPermission(final Player player, final String permission) {
		return player.hasPermission(permission);
	}
}
