package test.KabOOm356.Database.Table;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.DatabaseTableMigrator;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionMigrator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.KabOOm356.PowerMockitoTest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest(DatabaseTableMigrator.class)
public class DatabaseTableMigratorTest extends PowerMockitoTest {
	@Mock
	private DatabaseTableMigrator databaseTableMigrator;

	@Mock
	private Database database;

	@Mock
	private DatabaseTableVersionMigrator databaseTableVersionMigrator;

	@Mock
	private DatabaseTableVersionMigrator databaseTableVersionMigrator2;

	@Before
	public void setupMocks() throws Exception {
		doCallRealMethod().when(databaseTableMigrator).migrate();
		doCallRealMethod().when(databaseTableMigrator, "migrateTable");
		final List<DatabaseTableVersionMigrator> databaseTableVersionMigratorList = new ArrayList<DatabaseTableVersionMigrator>();
		databaseTableVersionMigratorList.add(databaseTableVersionMigrator);
		databaseTableVersionMigratorList.add(databaseTableVersionMigrator2);
		when(databaseTableMigrator.getDatabaseTableVersionMigrators()).thenReturn(databaseTableVersionMigratorList);
	}

	@Test
	public void testMigrate() throws Exception {
		databaseTableMigrator.migrate();
		verify(databaseTableVersionMigrator).migrate();
		verify(databaseTableVersionMigrator2).migrate();
	}

	@Test(expected = InterruptedException.class)
	public void testMigrageInterruptedException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new InterruptedException("Test Exception")).when(databaseTableVersionMigrator).migrate();
		databaseTableMigrator.migrate();
	}

	@Test(expected = SQLException.class)
	public void testMigrateSQLException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new SQLException("Test Exception")).when(databaseTableVersionMigrator).migrate();
		databaseTableMigrator.migrate();
	}

	@Test(expected = ClassNotFoundException.class)
	public void testMigrateClassNotFoundException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new ClassNotFoundException("Test Exception")).when(databaseTableVersionMigrator).migrate();
		databaseTableMigrator.migrate();
	}
}
