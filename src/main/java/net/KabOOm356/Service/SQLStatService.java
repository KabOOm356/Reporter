package net.KabOOm356.Service;

import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Service.SQLStatServices.ModeratorStatService.ModeratorStat;
import net.KabOOm356.Service.SQLStatServices.PlayerStatService.PlayerStat;
import net.KabOOm356.Util.BukkitUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * A class to manage setting statistics held in an SQL database.
 */
public class SQLStatService extends Service {
	private static final Logger log = LogManager.getLogger(SQLStatService.class);

	/**
	 * The name of the table the statistics are being stored.
	 */
	private final String tableName;
	/**
	 * The case-sensitive name of the column that should be used as the index.
	 */
	private final String indexColumn;
	/**
	 * The case-sensitive column name that should be used as a secondary index.
	 */
	private final String secondaryIndexColumn;

	/**
	 * Constructor.
	 *
	 * @param tableName   The name of the table the statistics are being stored.
	 * @param indexColumn The case-sensitive name of the column that should be used as the index.
	 * @param secondaryIndexColumn The secondary column that should be used as the index.
	 * @throws IllegalArgumentException Thrown if any of the parameters are null.
	 */
	public SQLStatService(final ServiceModule module, final String tableName, final String indexColumn, final String secondaryIndexColumn) {
		super(module);
		if (tableName == null) {
			throw new IllegalArgumentException("Parameter 'tableName' cannot be null!");
		}

		if (indexColumn == null) {
			throw new IllegalArgumentException("Parameter 'indexColumn' cannot be null!");
		}

		if (secondaryIndexColumn == null) {
			throw new IllegalArgumentException("Parameter 'secondaryIndexColumn' cannot be null!");
		}

		this.tableName = tableName;
		this.indexColumn = indexColumn;
		this.secondaryIndexColumn = secondaryIndexColumn;
	}

	/**
	 * Increments a statistic by one (1).
	 *
	 * @param player The player to increment the statistic for.
	 * @param stat   The SQLStat to increment.
	 */
	public void incrementStat(final OfflinePlayer player, final SQLStat stat) {
		incrementStat(player, stat, 1);
	}

	/**
	 * Increments a statistic by the given increment value.
	 *
	 * @param player    The player to increment the statistic for.
	 * @param stat      The SQLStat to increment.
	 * @param increment The value to increment the statistic by.
	 * @throws IllegalArgumentException If the increment value is less than one (1).
	 */
	public void incrementStat(final OfflinePlayer player, final SQLStat stat, final int increment) {
		if (increment < 1) {
			throw new IllegalArgumentException("'increment' cannot be less than one (1)!");
		}

		final String statColumn = stat.getColumnName();

		final StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(tableName).append(' ');
		query.append("SET ").append(statColumn).append(" = ").append(statColumn).append(" + ").append(increment).append(' ');
		query.append("WHERE ").append(indexColumn).append(" = ? OR ").append(secondaryIndexColumn).append(" = ?");

		final ArrayList<String> params = new ArrayList<String>();
		params.add(player.getUniqueId().toString());
		params.add(player.getName());

		try {
			addRow(player);
			try {
				final int connectionId = getDatabase().openPooledConnection();
				try {
					getDatabase().preparedUpdateQuery(connectionId, query.toString(), params);
				} catch (final Exception e) {
					log.error(String.format("Failed to increment statistic [%s] by %d for player [%s]!", stat.getName(), increment, BukkitUtil.formatPlayerName(player)), e);
				} finally {
					getDatabase().closeConnection(connectionId);
				}
			} catch (final Exception e) {
				log.error(String.format("Failed to open connection to increment statistic [%s] by %d for player [%s]!", stat.getName(), increment, BukkitUtil.formatPlayerName(player)), e);
			}
		} catch (final Exception e) {
			log.error(String.format("Failed to add row to increment statistic [%s] by %d for player [%s]!", stat.getName(), increment, BukkitUtil.formatPlayerName(player)), e);
		}
	}

	/**
	 * Decrements a statistic by one (1).
	 *
	 * @param player The player to decrement the statistic for.
	 * @param stat   The SQLStat to decrement.
	 */
	public void decrementStat(final OfflinePlayer player, final SQLStat stat) {
		decrementStat(player, stat, 1);
	}

	/**
	 * Decrements a statistic by one (1).
	 *
	 * @param player    The player to decrement the statistic for.
	 * @param stat      The SQLStat to decrement.
	 * @param decrement The value to decrement the statistic by.
	 * @throws IllegalArgumentException If the decrement value is less than one (1).
	 */
	public void decrementStat(final OfflinePlayer player, final SQLStat stat, final int decrement) {
		if (decrement < 1) {
			throw new IllegalArgumentException("'decrement' cannot be less than one (1)!");
		}

		final String statColumn = stat.getColumnName();

		final StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(tableName).append(' ');
		query.append("SET ").append(statColumn).append(" = ").append(statColumn).append(" - ").append(decrement).append(' ');
		query.append("WHERE ").append(indexColumn).append(" = ? OR ").append(secondaryIndexColumn).append(" = ?");

		final ArrayList<String> params = new ArrayList<String>();
		params.add(player.getUniqueId().toString());
		params.add(player.getName());

		try {
			addRow(player);
			try {
				final int connectionId = getDatabase().openPooledConnection();
				try {
					getDatabase().preparedUpdateQuery(connectionId, query.toString(), params);
				} catch (final Exception e) {
					log.error(String.format("Failed to decrement statistic [%s] by %d for player [%s]!", stat.getName(), decrement, BukkitUtil.formatPlayerName(player)), e);
				} finally {
					getDatabase().closeConnection(connectionId);
				}
			} catch (final Exception e) {
				log.error(String.format("Failed to open connection to decrement statistic [%s] by %d for player [%s]!", stat.getName(), decrement, BukkitUtil.formatPlayerName(player)), e);
			}
		} catch (final Exception e) {
			log.error(String.format("Failed to add row to decrement statistic [%s] by %d for player [%s]!", stat.getName(), decrement, BukkitUtil.formatPlayerName(player)), e);
		}
	}

	/**
	 * Sets a statistic.
	 * <br /> <br />
	 * NOTE: This does not create a column for the statistic, it solely sets a value to an already existing column.
	 *
	 * @param player The player to set the statistic for.
	 * @param stat   The statistic to set.
	 * @param value  The value to set the statistic to.
	 */
	public void setStat(final OfflinePlayer player, final SQLStat stat, final String value) {
		final String statColumn = stat.getColumnName();

		final StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(tableName).append(' ');
		query.append("SET ").append(statColumn).append(" = ? ");
		query.append("WHERE ").append(indexColumn).append(" = ? OR ").append(secondaryIndexColumn).append(" = ?");

		final ArrayList<String> params = new ArrayList<String>();
		params.add(value);
		params.add(player.getUniqueId().toString());
		params.add(player.getName());

		try {
			addRow(player);
			try {
				final int connectionId = getDatabase().openPooledConnection();
				try {
					getDatabase().preparedUpdateQuery(connectionId, query.toString(), params);
				} catch (final Exception e) {
					log.error(String.format("Failed to set statistic [%s] to %s for player [%s]!", stat.getName(), value, BukkitUtil.formatPlayerName(player)), e);
				} finally {
					getDatabase().closeConnection(connectionId);
				}
			} catch (final Exception e) {
				log.error(String.format("Failed to open connection to set statistic [%s] to %s for player [%s]!", stat.getName(), value, BukkitUtil.formatPlayerName(player)), e);
			}
		} catch (final Exception e) {
			log.error(String.format("Failed to add row to set statistic [%s] to %s for player [%s]!", stat.getName(), value, BukkitUtil.formatPlayerName(player)), e);
		}
	}

	/**
	 * Gets a statistic.
	 *
	 * @param player The player to get the statistic for.
	 * @param stat   The statistic to get.
	 * @return An {@link ResultRow} containing the statistic requested.
	 * An empty {@link ResultRow} is returned if there is not an entry for the given player.
	 * If an exception is thrown while querying the database, null is returned.
	 */
	public ResultRow getStat(final OfflinePlayer player, final SQLStat stat) {
		final String statColumn = stat.getColumnName();

		final StringBuilder query = new StringBuilder();
		query.append("SELECT ").append(statColumn);
		query.append(" FROM ").append(tableName);
		query.append(" WHERE ").append(indexColumn).append(" = ? OR ").append(secondaryIndexColumn).append(" = ?");

		final ArrayList<String> params = new ArrayList<String>();
		params.add(player.getUniqueId().toString());
		params.add(player.getName());

		ResultRow resultRow = new ResultRow();

		try {
			final int connectionId = getDatabase().openPooledConnection();
			try {
				final SQLResultSet result = getDatabase().preparedSQLQuery(connectionId, query.toString(), params);

				if (result != null && !result.isEmpty()) {
					resultRow = result.get(SQLResultSet.FIRSTROW);
				}
			} catch (final Exception e) {
				log.error(String.format("Failed to get SQL stat [%s] for player [%s]!", stat.getName(), BukkitUtil.formatPlayerName(player)), e);
				return null;
			} finally {
				getDatabase().closeConnection(connectionId);
			}
		} catch (final Exception e) {
			log.error(String.format("Failed to open connection to get SQL stat [%s] for player [%s]!", stat.getName(), BukkitUtil.formatPlayerName(player)), e);
		}

		return resultRow;
	}

	/**
	 * Gets the values for the statistics passed.
	 *
	 * @param player The player to get the statistics for.
	 * @param stats  The SQLStats to get.
	 * @return A {@link ResultRow} containing the values for the statistics passed.
	 * If an exception occurs while getting a statistic it is omitted from the returned {@link ResultRow}.
	 */
	public ResultRow getStat(final OfflinePlayer player, final Iterable<SQLStat> stats) {
		final ResultRow returnedRow = new ResultRow();

		for (final SQLStat stat : stats) {
			final ResultRow result = getStat(player, stat);

			if (result != null) {
				returnedRow.putAll(result);
			}
		}

		return returnedRow;
	}

	/**
	 * Creates a new row for the player, if one does not exist.
	 *
	 * @param player The player to create a new row for, if one does not already exist.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	protected void addRow(final OfflinePlayer player) throws ClassNotFoundException, SQLException, InterruptedException {
		final SQLResultSet rs = getIndex(player);
		final int connectionId;
		try {
			connectionId = getDatabase().openPooledConnection();
		} catch (final ClassNotFoundException e) {
			log.warn(String.format("Failed to open connection to insert new SQL stat row for player [%s]!", BukkitUtil.formatPlayerName(player)));
			throw e;
		} catch (final SQLException e) {
			log.warn(String.format("Failed to open connection to insert new SQL stat row for player [%s]!", BukkitUtil.formatPlayerName(player)));
			throw e;
		} catch (final InterruptedException e) {
			log.warn(String.format("Failed to open connection to insert new SQL stat row for player [%s]!", BukkitUtil.formatPlayerName(player)));
			throw e;
		}
		try {
			if (rs.isEmpty()) {
				final String query = "INSERT INTO " + tableName + ' ' +
						'(' + indexColumn + ',' + secondaryIndexColumn + ')' +
						"VALUES (?,?)";

				final ArrayList<String> params = new ArrayList<String>();

				if (BukkitUtil.isPlayerValid(player)) {
					params.add(player.getUniqueId().toString());
				} else {
					params.add("");
				}

				params.add(player.getName());

				getDatabase().preparedUpdateQuery(connectionId, query, params);
			}
		} catch (final SQLException e) {
			log.warn(String.format("Failed to insert new SQL stat row for player [%s]!", BukkitUtil.formatPlayerName(player)));
			throw e;
		} finally {
			getDatabase().closeConnection(connectionId);
		}
	}

	/**
	 * Gets the SQL index for the given player.
	 *
	 * @param player The {@link OfflinePlayer} to get the index for.
	 * @return An {@link SQLResultSet} that either contains the ID for the given player, or is empty.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	protected SQLResultSet getIndex(final OfflinePlayer player) throws ClassNotFoundException, SQLException, InterruptedException {
		final int connectionId;
		try {
			connectionId = getDatabase().openPooledConnection();
		} catch (final ClassNotFoundException e) {
			log.warn(String.format("Failed to open connection to get SQL stat index for player [%s]!", BukkitUtil.formatPlayerName(player)));
			throw e;
		} catch (final SQLException e) {
			log.warn(String.format("Failed to open connection to get SQL stat index for player [%s]!", BukkitUtil.formatPlayerName(player)));
			throw e;
		} catch (final InterruptedException e) {
			log.warn(String.format("Failed to open connection to get SQL stat index for player [%s]!", BukkitUtil.formatPlayerName(player)));
			throw e;
		}
		try {
			final StringBuilder query = new StringBuilder();
			query.append("SELECT ID ");
			query.append("FROM ").append(tableName).append(' ');
			query.append("WHERE ").append(indexColumn).append(" = ? OR ").append(secondaryIndexColumn).append(" = ?");

			final ArrayList<String> params = new ArrayList<String>();

			params.add(player.getUniqueId().toString());
			params.add(player.getName());

			try {
				return getDatabase().preparedSQLQuery(connectionId, query.toString(), params);
			} catch (final SQLException e) {
				log.warn(String.format("Failed to get SQL stat index for player [%s]!", BukkitUtil.formatPlayerName(player)));
				throw e;
			}
		} finally {
			getDatabase().closeConnection(connectionId);
		}
	}

	/**
	 * Returns the database the statistics are being stored.
	 *
	 * @return The database the statistics are being stored.
	 */
	protected ExtendedDatabaseHandler getDatabase() {
		return getStore().getDatabaseStore().get();
	}

	/**
	 * A class that represents a statistic column in an SQL database.
	 */
	public static class SQLStat {
		/**
		 * Represents all statistic columns in the database.
		 */
		public static final SQLStat ALL = new SQLStat("All", "*");

		/**
		 * The case-sensitive column name of the statistic this SQLStat represents.
		 */
		private final String columnName;

		/**
		 * The name of this statistic.
		 */
		private final String name;

		/**
		 * Constructor.
		 *
		 * @param name       The name of the statistic.
		 * @param columnName The case-sensitive column name of the statistic.
		 */
		protected SQLStat(final String name, final String columnName) {
			this.name = name;
			this.columnName = columnName;
		}

		/**
		 * Gets an {@link SQLStat}, {@link PlayerStat}, or a {@link ModeratorStat} by the given name.
		 *
		 * @param name The name of the {@link SQLStat} to return.
		 * @return An {@link SQLStat} if one matches the given name, otherwise null.
		 */
		public static SQLStat getByName(final String name) {
			if (name.equalsIgnoreCase("all")) {
				return SQLStat.ALL;
			} else {
				final ArrayList<SQLStat> stats = SQLStat.getAll(ModeratorStat.class);
				stats.addAll(SQLStat.getAll(PlayerStat.class));

				for (final SQLStat stat : stats) {
					if (name.equalsIgnoreCase(stat.getName())) {
						return stat;
					}
				}
			}

			return null;
		}

		/**
		 * Returns all {@link SQLStat}s that are static fields of the given Class.
		 *
		 * @param clazz The Class to get the static {@link SQLStat} fields from.
		 * @return An {@link ArrayList} containing all the {@link SQLStat}s from the given class.
		 */
		public static <T extends SQLStat> ArrayList<SQLStat> getAll(final Class<T> clazz) {
			final ArrayList<SQLStat> stats = new ArrayList<SQLStat>();

			for (final Field f : clazz.getDeclaredFields()) {
				try {
					// Only care if the field is static and an instance of a SQLStat.
					if (Modifier.isStatic(f.getModifiers()) && f.get(null) instanceof SQLStat) {
						final SQLStat stat = (SQLStat) f.get(null);

						stats.add(stat);
					}
				} catch (final IllegalAccessException e) {
					if (log.isDebugEnabled()) {
						log.log(Level.WARN, "Could not access field: " + f.getName(), e);
					}
				}
			}

			return stats;
		}

		/**
		 * Returns the column name of this SQLStat.
		 *
		 * @return The column name of this SQLStat.
		 */
		public String getColumnName() {
			return columnName;
		}

		/**
		 * Returns the name of this SQLStat.
		 *
		 * @return The name of this SQLStat.
		 */
		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
