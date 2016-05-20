package test.KabOOm356.Database.Connection;

import net.KabOOm356.Database.Connection.AlertingPooledConnection;
import net.KabOOm356.Database.Connection.ConnectionPoolManager;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.PowerMockitoTest;

import java.sql.Connection;

import static org.powermock.api.mockito.PowerMockito.mock;

@PrepareForTest(AlertingPooledConnection.class)
public class AlertingPooledConnectionTest extends PowerMockitoTest {

	@Test
	public void test() throws Exception {
		final ConnectionPoolManager connectionPoolManager = mock(ConnectionPoolManager.class);
		final Connection connection = mock(Connection.class);

		final AlertingPooledConnection apc = new AlertingPooledConnection(connectionPoolManager, 1, connection);

		apc.close();
		Mockito.verify(connectionPoolManager).connectionClosed(1);
	}

}
