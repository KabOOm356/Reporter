package net.KabOOm356.Database.Connection;

/**
 * A manager to handle {@link PooledConnection} events.
 */
public interface ConnectionPoolManager {
	/**
	 * Alerts the manager that the pooled connection has been closed.
	 *
	 * @param connectionId The id of the connection that has been closed.
	 */
	public void connectionClosed(final Integer connectionId);
}
