package net.KabOOm356.Database.Connection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A {@link PooledConnection} that will alert a manager when it is closed.
 */
public class AlertingPooledConnection extends PooledConnection {
	private final ConnectionPoolManager manager;

	/**
	 * Constructor.
	 *
	 * @param manager      The connection pool manager.
	 * @param connectionId The id of this connection.
	 * @param connection   The connection.
	 */
	public AlertingPooledConnection(final ConnectionPoolManager manager, final int connectionId, final Connection connection) {
		super(connectionId, connection);
		this.manager = manager;
	}

	/**
	 * Closes this connection and alerts the manager.
	 *
	 * @see net.KabOOm356.Database.Connection.ConnectionWrapper#close()
	 */
	@Override
	public void close() throws SQLException {
		super.close();
		final int connectionId = getConnectionId();
		manager.connectionClosed(connectionId);
	}
}
