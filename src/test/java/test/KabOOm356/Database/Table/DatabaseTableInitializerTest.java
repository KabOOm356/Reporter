package test.KabOOm356.Database.Table;

import net.KabOOm356.Database.Table.DatabaseTableCreator;
import net.KabOOm356.Database.Table.DatabaseTableInitializer;
import net.KabOOm356.Database.Table.DatabaseTableMigrator;
import net.KabOOm356.Database.Table.DatabaseTableUpdater;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.KabOOm356.PowerMockitoTest;

import java.sql.SQLException;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest(DatabaseTableInitializer.class)
public class DatabaseTableInitializerTest extends PowerMockitoTest {
	@Mock
	private DatabaseTableInitializer databaseTableInitializer;

	@Mock
	private DatabaseTableCreator databaseTableCreator;

	@Mock
	private DatabaseTableMigrator databaseTableMigrator;

	@Mock
	private DatabaseTableUpdater databaseTableUpdater;

	@Before
	public void setupMocks() throws Exception {
		doReturn(databaseTableCreator).when(databaseTableInitializer, "getCreator");
		doReturn(databaseTableMigrator).when(databaseTableInitializer, "getMigrator");
		doReturn(databaseTableUpdater).when(databaseTableInitializer, "getUpdater");

		doCallRealMethod().when(databaseTableInitializer).initialize();
		doCallRealMethod().when(databaseTableInitializer, "create");
		doCallRealMethod().when(databaseTableInitializer, "migrate");
		doCallRealMethod().when(databaseTableInitializer, "update");
	}

	@Test
	public void testInitialize() throws InterruptedException, SQLException, ClassNotFoundException {
		databaseTableInitializer.initialize();
		verify(databaseTableCreator).create();
		verify(databaseTableMigrator).migrate();
		verify(databaseTableUpdater).update();
	}

	@Test
	public void testInitializeNoCreate() throws Exception {
		doReturn(null).when(databaseTableInitializer, "getCreator");
		databaseTableInitializer.initialize();
		verify(databaseTableCreator, never()).create();
		verify(databaseTableMigrator).migrate();
		verify(databaseTableUpdater).update();
	}

	@Test
	public void testInitializeNoMigrate() throws Exception {
		doReturn(null).when(databaseTableInitializer, "getMigrator");
		databaseTableInitializer.initialize();
		verify(databaseTableCreator).create();
		verify(databaseTableMigrator, never()).migrate();
		verify(databaseTableUpdater).update();
	}

	@Test
	public void testInitializeNoUpdate() throws Exception {
		doReturn(null).when(databaseTableInitializer, "getUpdater");
		databaseTableInitializer.initialize();
		verify(databaseTableCreator).create();
		verify(databaseTableMigrator).migrate();
		verify(databaseTableUpdater, never()).update();
	}

	@Test(expected = InterruptedException.class)
	public void testInitializeInterruptedException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new InterruptedException("Test Exception")).when(databaseTableCreator).create();
		databaseTableInitializer.initialize();
	}

	@Test(expected = SQLException.class)
	public void testInitializeSQLException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new SQLException("Test Exception")).when(databaseTableCreator).create();
		databaseTableInitializer.initialize();
	}

	@Test(expected = ClassNotFoundException.class)
	public void testInitializeClassNotFoundException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new ClassNotFoundException("Test Exception")).when(databaseTableCreator).create();
		databaseTableInitializer.initialize();
	}
}
