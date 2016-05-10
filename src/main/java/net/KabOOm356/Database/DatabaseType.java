package net.KabOOm356.Database;

/**
 * Supported database types.
 */
public enum DatabaseType {
	/**
	 * SQLite Database.
	 */
	SQLITE("SQLite"),
	/**
	 * MySQL Database.
	 */
	MYSQL("MySQL");

	/**
	 * The predefined name of the DatabaseType.
	 */
	private String databaseTypeName;

	/**
	 * Constructor.
	 *
	 * @param databaseTypeName The predefined name of the DatabaseType.
	 */
	private DatabaseType(String databaseTypeName) {
		this.databaseTypeName = databaseTypeName;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return databaseTypeName;
	}
}
