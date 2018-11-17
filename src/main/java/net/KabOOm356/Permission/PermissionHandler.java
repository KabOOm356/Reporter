package net.KabOOm356.Permission;

import net.KabOOm356.Reporter.Reporter;
import net.milkbowl.vault.permission.Permission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PermissionHandler {
	private static final Logger log = LogManager.getLogger(PermissionHandler.class);

	/**
	 * The type of permissions system being used.
	 */
	private final PermissionType type;

	private Permission permission;

	public PermissionHandler() {
		if (setupVault()) {
			type = PermissionType.Vault;
		} else {
			type = PermissionType.SuperPerms;
		}

		log.info(Reporter.getDefaultConsolePrefix() + type + " support enabled.");
	}

	public PermissionType getPermissionType() {
		return type;
	}

	public boolean hasPermission(final Player player, final String permission) {
		if (type == PermissionType.Vault) {
			return this.permission.has(player, permission);
		}
		return player.hasPermission(permission);
	}

	private boolean setupVault() {
		RegisteredServiceProvider<Permission> rsp = null;
		try {
			rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
		} catch (final NoClassDefFoundError e) {
			if (log.isDebugEnabled()) {
				log.info("Could not enable Vault support", e);
			}
		}

		if (rsp != null) {
			this.permission = rsp.getProvider();
			return true;
		}
		return false;
	}
}
