package test.KabOOm356.Database.Table;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Database.Table.DatabaseTableCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.KabOOm356.PowerMockitoTest;

import java.sql.SQLException;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest(DatabaseTableCreator.class)
public class DatabaseTableCreatorTest extends PowerMockitoTest {
	@Mock
	private DatabaseTableCreator databaseTableCreator;

	@Mock
	private Database database;

	@Before
	public void setupMocks() throws Exception {
		doCallRealMethod().when(databaseTableCreator).create();
		doCallRealMethod().when(databaseTableCreator, "createTable");
		doCallRealMethod().when(databaseTableCreator, "needsToCreateTable");
		when(databaseTableCreator, "getDatabase").thenReturn(database);
		when(database.checkTable(anyInt(), anyString())).thenReturn(false);
		when(database.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
	}

	@After
	public void verifyMocks() throws Exception {
		verifyPrivate(databaseTableCreator).invoke("startTransaction");
		verifyPrivate(databaseTableCreator).invoke("commitTransaction");
	}

	@Test
	public void testCreate() throws Exception {
		databaseTableCreator.create();
		verifyPrivate(databaseTableCreator).invoke("addQueryToTransaction", anyString());
	}

	@Test
	public void testCreateNotNeeded() throws Exception {
		when(database.checkTable(anyInt(), anyString())).thenReturn(true);
		databaseTableCreator.create();
		verifyPrivate(databaseTableCreator, never()).invoke("addQueryToTransaction", anyString());
	}

	@Test(expected = InterruptedException.class)
	public void testCreateInterruptedException() throws Exception {
		doThrow(new InterruptedException("Test Exception")).when(databaseTableCreator, "startTransaction");
		databaseTableCreator.create();
	}

	@Test(expected = SQLException.class)
	public void testCreateSQLException() throws Exception {
		doThrow(new SQLException("Test Exception")).when(databaseTableCreator, "needsToCreateTable");
		databaseTableCreator.create();
	}

	@Test(expected = ClassNotFoundException.class)
	public void testCreateClassNotFoundException() throws Exception {
		doThrow(new ClassNotFoundException("Test Exception")).when(databaseTableCreator, "startTransaction");
		databaseTableCreator.create();
	}

	@Test(expected = IllegalStateException.class)
	public void testCreateCommitIllegalStateException() throws Exception {
		doThrow(new IllegalStateException("Test Exception")).when(databaseTableCreator, "commitTransaction");
		databaseTableCreator.create();
	}

	@Test(expected = SQLException.class)
	public void testCreateCommitSQLException() throws Exception {
		doThrow(new SQLException("Test Exception")).when(databaseTableCreator, "commitTransaction");
		databaseTableCreator.create();
	}
}
