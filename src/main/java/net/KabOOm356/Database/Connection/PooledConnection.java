package net.KabOOm356.Database.Connection;

import java.sql.Connection;

/** A pooled connection that is recognized by it's connection id. */
public class PooledConnection extends ConnectionWrapper {
  private final int connectionId;

  /**
   * Constructor.
   *
   * @param connectionId The id of this pooled connection.
   * @param connection The underlying connection that is being wrapped.
   */
  public PooledConnection(final int connectionId, final Connection connection) {
    super(connection);
    this.connectionId = connectionId;
  }

  /**
   * Gets the id of this connection.
   *
   * @return The id of this connection.
   */
  public int getConnectionId() {
    return connectionId;
  }
}
