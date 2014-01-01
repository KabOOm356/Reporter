package net.KabOOm356.Permission;

/**
 * Supported permission types
 */
public enum PermissionType
{
	PermissionsEx("PermissionsEx"),
	/** SuperPerms PermissionType. */
	SuperPerms("SuperPerms");
	
	/** The preferred name of the Permissions type */
	private String typeName;
	
	/**
	 * PermissionType constructor
	 * 
	 * @param typeName The preferred name of the Permission type
	 */
	private PermissionType(String typeName)
	{
		this.typeName = typeName;
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString()
	{
		return typeName;
	}
}
