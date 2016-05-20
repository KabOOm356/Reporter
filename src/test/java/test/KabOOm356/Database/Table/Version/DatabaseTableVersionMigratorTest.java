package test.KabOOm356.Database.Table.Version;

import net.KabOOm356.Database.Table.DatabaseTableCreator;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionMigrator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.PowerMockitoTest;

import java.lang.reflect.Method;
import java.sql.SQLException;

import static org.mockito.Mockito.atLeastOnce;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest(DatabaseTableVersionMigrator.class)
public class DatabaseTableVersionMigratorTest extends PowerMockitoTest {
	private static final Method needsMigration = method(DatabaseTableVersionMigrator.class, "needsMigration");
	private static final Method commitTransaction = method(DatabaseTableVersionMigrator.class, "commitTransaction");
	@Mock
	private DatabaseTableVersionMigrator databaseTableVersionMigrator;
	@Mock
	private DatabaseTableCreator databaseTableCreator;

	@Before
	public void setupMocks() throws Exception {
		doCallRealMethod().when(databaseTableVersionMigrator).migrate();
		when(databaseTableVersionMigrator, "getCreator").thenReturn(databaseTableCreator);
		when(databaseTableVersionMigrator, needsMigration).withNoArguments().thenReturn(true);

		when(databaseTableVersionMigrator, "dropTemporaryTable").thenCallRealMethod();
		when(databaseTableVersionMigrator, "migrateTable").thenCallRealMethod();
		when(databaseTableVersionMigrator, "dropTable").thenCallRealMethod();
		when(databaseTableVersionMigrator, "createTemporaryTable").thenCallRealMethod();
		when(databaseTableVersionMigrator, "populateTemporaryTable").thenCallRealMethod();
	}

	@After
	public void verifyMocks() throws Exception {
		verifyPrivate(databaseTableVersionMigrator, atLeastOnce()).invoke("startTransaction");
		verifyPrivate(databaseTableVersionMigrator).invoke(commitTransaction).withNoArguments();
	}

	@Test
	public void testMigrate() throws InterruptedException, SQLException, ClassNotFoundException {
		databaseTableVersionMigrator.migrate();
	}

	@Test(expected = InterruptedException.class)
	public void testMigrateInterruptedException() throws Exception {
		doThrow(new InterruptedException("Test Exception")).when(databaseTableVersionMigrator, needsMigration).withNoArguments();
		databaseTableVersionMigrator.migrate();
	}

	@Test(expected = SQLException.class)
	public void testMigrateSQLException() throws Exception {
		doThrow(new SQLException("Test Exception")).when(databaseTableVersionMigrator, needsMigration).withNoArguments();
		databaseTableVersionMigrator.migrate();
	}

	@Test(expected = ClassNotFoundException.class)
	public void testMigrateClassNotFoundException() throws Exception {
		doThrow(new ClassNotFoundException("Test Exception")).when(databaseTableVersionMigrator, needsMigration).withNoArguments();
		databaseTableVersionMigrator.migrate();
	}

	@Test(expected = IllegalStateException.class)
	public void testMigrateCommitIllegalStateException() throws Exception {
		doThrow(new IllegalStateException("Test Exception")).when(databaseTableVersionMigrator, commitTransaction).withNoArguments();
		databaseTableVersionMigrator.migrate();
	}

	@Test(expected = SQLException.class)
	public void testMigrateCommitSQLException() throws Exception {
		doThrow(new SQLException("Test Exception")).when(databaseTableVersionMigrator, commitTransaction).withNoArguments();
		databaseTableVersionMigrator.migrate();
	}
}
