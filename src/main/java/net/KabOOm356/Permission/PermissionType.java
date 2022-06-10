package net.KabOOm356.Permission;

/** Supported permission types */
public enum PermissionType {
  Vault("Vault"),
  /** SuperPerms PermissionType. */
  SuperPerms("SuperPerms");

  /** The preferred name of the Permissions type */
  private final String typeName;

  /**
   * PermissionType constructor
   *
   * @param typeName The preferred name of the Permission type
   */
  PermissionType(final String typeName) {
    this.typeName = typeName;
  }

  @Override
  public String toString() {
    return typeName;
  }
}
