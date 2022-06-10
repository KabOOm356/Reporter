package net.KabOOm356.Database;

/** Supported database types. */
public enum DatabaseType {
  /** SQLite Database. */
  SQLITE("SQLite"),
  /** MySQL Database. */
  MYSQL("MySQL");

  /** The predefined name of the DatabaseType. */
  private final String databaseTypeName;

  /**
   * Constructor.
   *
   * @param databaseTypeName The predefined name of the DatabaseType.
   */
  DatabaseType(final String databaseTypeName) {
    this.databaseTypeName = databaseTypeName;
  }

  @Override
  public String toString() {
    return databaseTypeName;
  }
}
