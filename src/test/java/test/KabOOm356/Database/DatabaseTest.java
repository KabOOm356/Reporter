package test.KabOOm356.Database;

import net.KabOOm356.Database.Connection.ConnectionPoolConfig;
import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.KabOOm356.PowerMockitoTest;

import java.sql.SQLException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;

@PrepareForTest({Database.class, Class.class})
public class DatabaseTest extends PowerMockitoTest {
	public static Database buildDatabase(final DatabaseType databaseType, final String driver, final String connectionURL, final ConnectionPoolConfig config) {
		final DatabaseType databaseTypeToUse = (databaseType != null) ? databaseType : DatabaseType.MYSQL;
		final String driverToUse = (driver != null) ? driver : "testDriver";
		final String connectionURLToUse = (connectionURL != null) ? connectionURL : "testURL";
		final ConnectionPoolConfig configToUse = (config != null) ? config : ConnectionPoolConfig.defaultInstance;
		return new Database(databaseTypeToUse, driverToUse, connectionURLToUse, configToUse);
	}

	@Test
	public void constructorTest() {
		assertNotNull(buildDatabase(null, null, null, null));
	}

	@Test
	public void testOpenConnection() throws InterruptedException, SQLException, ClassNotFoundException {
		final Database database = spy(buildDatabase(null, null, null, null));
		doReturn(1).when(database).openPooledConnection();
		database.openConnection();
		verify(database).openPooledConnection();
		try {
			database.openConnection();
			fail("Expected exception was not thrown!");
		} catch (final IllegalStateException e) {
			// Expected exception
		}
	}
}
