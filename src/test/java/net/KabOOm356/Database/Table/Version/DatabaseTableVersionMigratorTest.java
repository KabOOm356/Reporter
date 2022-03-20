package net.KabOOm356.Database.Table.Version;

import net.KabOOm356.Database.Database;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import test.test.MockitoTest;

import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.Mockito.*;

public class DatabaseTableVersionMigratorTest extends MockitoTest {
	private DatabaseTableVersionMigrator databaseTableVersionMigrator;

	@Mock
	private Database database;
	@Mock
	private Statement statement;

	@Before
	public void setupMocks() throws Exception {
		databaseTableVersionMigrator =
				mock(
						DatabaseTableVersionMigrator.class,
						withSettings()
								.defaultAnswer(CALLS_REAL_METHODS)
								.useConstructor(database, "version", "table name"));
		when(database.createStatement(anyInt())).thenReturn(statement);
	}

	@Test(expected = InterruptedException.class)
	public void testMigrateInterruptedException() throws Exception {
		when(database.openPooledConnection()).thenThrow(new InterruptedException("Test Exception"));
		databaseTableVersionMigrator.migrate();
	}

	@Test(expected = SQLException.class)
	public void testMigrateSQLException() throws Exception {
		when(database.openPooledConnection()).thenThrow(new SQLException("Test Exception"));
		databaseTableVersionMigrator.migrate();
	}

	@Test(expected = ClassNotFoundException.class)
	public void testMigrateClassNotFoundException() throws Exception {
		when(database.openPooledConnection()).thenThrow(new ClassNotFoundException("Test Exception"));
		databaseTableVersionMigrator.migrate();
	}

	@Test(expected = IllegalStateException.class)
	public void testMigrateCommitIllegalStateException() throws Exception {
		when(database.openPooledConnection()).thenThrow(new IllegalStateException("Test Exception"));
		databaseTableVersionMigrator.migrate();
	}

	@Test(expected = SQLException.class)
	public void testMigrateCommitSQLException() throws Exception {
		when(statement.executeBatch()).thenThrow(new SQLException("Test Exception"));
		databaseTableVersionMigrator.migrate();
		verify(statement).close();
	}
}
