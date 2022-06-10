package net.KabOOm356.Database.Connection;

import static org.mockito.Mockito.mock;

import java.sql.Connection;
import org.junit.Test;
import org.mockito.Mockito;
import test.test.MockitoTest;

public class AlertingPooledConnectionTest extends MockitoTest {

  @Test
  public void test() throws Exception {
    final ConnectionPoolManager connectionPoolManager = mock(ConnectionPoolManager.class);
    final Connection connection = mock(Connection.class);

    final AlertingPooledConnection apc =
        new AlertingPooledConnection(connectionPoolManager, 1, connection);

    apc.close();
    Mockito.verify(connectionPoolManager).connectionClosed(1);
  }
}
