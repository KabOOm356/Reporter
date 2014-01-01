package net.KabOOm356.Permission;

import net.KabOOm356.Reporter.Reporter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * A manager to handle permissions.
 */
public class ReporterPermissionManager
{
	/** The type of permissions system being used. */
	private PermissionType type;
	
	/** The permissions manager if PermissionsEX is being used. */
	private PermissionManager permissionsExHandler;
	
	/**
	 * Constructor.
	 */
	public ReporterPermissionManager()
	{
		type = null;
		
		setupPermissionsEx();
		
		if(type == null)
			type = PermissionType.SuperPerms;
		
		Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + type + " support enabled.");
	}
	
	/**
	 * Called by the constructor to help initialize and set up PermissionsEX
	 */
	private void setupPermissionsEx()
	{
		if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx"))
		{
			permissionsExHandler = PermissionsEx.getPermissionManager();
			
			if(permissionsExHandler == null)
			{
				Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Failed to obtain PermissionsEx handler.");
				Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "PermissionsEx support could not be enabled.");
			}
			else
				type = PermissionType.PermissionsEx;
		}
	}
	
	/**
	 * Returns the {@link PermissionType} that is being used.
	 * 
	 * @return The {@link PermissionType} that is being used.
	 */
	public PermissionType getPermissionType()
	{
		return type;
	}
	
	/**
	 * Checks if the given {@link Player} has the given permission node.
	 * 
	 * @param player The {@link Player}.
	 * @param perm The permission node to check for.
	 * 
	 * @return True if the given {@link Player} has the given permission node, otherwise false.
	 */
	public boolean hasPermission(Player player, String perm)
	{
		if(type == PermissionType.PermissionsEx)
			return permissionsExHandler.has(player, perm);
		return player.hasPermission(perm);
	}
}
