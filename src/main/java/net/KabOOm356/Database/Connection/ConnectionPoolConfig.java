package net.KabOOm356.Database.Connection;

/**
 * Configuration for a connection pool.
 */
public class ConnectionPoolConfig {
	/**
	 * An instance of a ConnectionPoolConfig containing all the default values for 
	 */
	public static final ConnectionPoolConfig defaultInstance = new ConnectionPoolConfig();

	private static final int minimumMaxConnections = 1;
	private static final int minimumConnectionPoolUpdate = 10;
	private static final int minimumMaxAttemptsForConnection = 50;
	
	/** If the connection pool should be limited. */
	private boolean connectionPoolLimit = true;
	/** The max allowed connections in the connection pool. */
	private int maxConnections = 10;
	/** The amount of time, in milliseconds, between updates when waiting for a connection. */
	private long waitTimeBeforeUpdate = 10L;
	/** The number of times to attempt to get a connection before canceling the operation. */
	private int maxAttemptsForConnection = 200;
	
	private ConnectionPoolConfig() {}
	
	public ConnectionPoolConfig(final boolean connectionPoolLimit, final int maxConnections, final long connectionPoolUpdate, final int maxAttemptsForConnection) {
		if (maxConnections < minimumMaxConnections) {
			throw new IllegalArgumentException(String.format("Parameter 'maxConnections' cannot be less than [%d]!", minimumMaxConnections));
		}
		if (connectionPoolUpdate < minimumConnectionPoolUpdate) {
			throw new IllegalArgumentException(String.format("Parameter 'connectionPoolUpdate' cannot be less than [%d]!", minimumConnectionPoolUpdate));
		}
		if (maxAttemptsForConnection < minimumMaxAttemptsForConnection) {
			throw new IllegalArgumentException(String.format("Parameter 'maxAttemptsForConnection' cannot be less than [%d]!", minimumMaxAttemptsForConnection));
		}
		this.connectionPoolLimit = connectionPoolLimit;
		this.maxConnections = maxConnections;
		this.waitTimeBeforeUpdate = connectionPoolUpdate;
		this.maxAttemptsForConnection = maxAttemptsForConnection;
	}
	
	/**
	 * Returns if the connection pool is limited.
	 * 
	 * @return True if the connection pool is limited, otherwise false.
	 */
	public boolean isConnectionPoolLimited() {
		return connectionPoolLimit;
	}
	
	/**
	 * Returns the maximum number of concurrent connections to allow in the connection pool at one time.
	 * 
	 * @return The maximum number of concurrent connections to allow in the connection pool at one time.
	 */
	public int getMaxConnections() {
		return maxConnections;
	}
	
	/**
	 * Returns the amount of time, in milliseconds, to wait before updating the status of the thread waiting on a new connection.
	 * 
	 * @return The amount of time, in milliseconds, to wait before updating the status of the thread waiting on a new connection.
	 */
	public long getWaitTimeBeforeUpdate() {
		return waitTimeBeforeUpdate;
	}
	
	/**
	 * Returns the maximum number of attempts to get a new connection before canceling the operation.
	 * 
	 * @return The maximum number of attempts to get a new connection before canceling the operation.
	 */
	public int getMaxAttemptsForConnection() {
		return maxAttemptsForConnection;
	}
}
