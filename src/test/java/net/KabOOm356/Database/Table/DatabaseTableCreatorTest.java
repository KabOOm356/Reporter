package net.KabOOm356.Database.Table;

import net.KabOOm356.Database.Database;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import test.test.MockitoTest;

import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DatabaseTableCreatorTest extends MockitoTest {
	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private DatabaseTableCreator databaseTableCreator;

	@Mock
	private Database database;
	@Mock
	private Statement statement;

	@Before
	public void setupMocks() throws Exception {
		when(databaseTableCreator.getDatabase()).thenReturn(database);
		when(databaseTableCreator.getTableCreationQuery()).thenReturn("Table Creation Query");
		when(databaseTableCreator.getConnectionId()).thenReturn(1);
		when(databaseTableCreator.getTableName()).thenReturn("Test Table");
		when(database.checkTable(anyInt(), anyString())).thenReturn(false);

		when(database.createStatement(anyInt())).thenReturn(statement);
		when(database.checkTable(anyInt(), anyString())).thenReturn(false);
	}

	@Test
	public void testCreate() throws Exception {
		databaseTableCreator.create();
		verify(databaseTableCreator).addQueryToTransaction(anyString());
		verify(statement).executeBatch();
		verify(statement).close();
		verify(database).closeConnection(anyInt());
	}

	@Test
	public void testCreateNotNeeded() throws Exception {
		when(database.checkTable(anyInt(), anyString())).thenReturn(true);
		databaseTableCreator.create();
		verify(databaseTableCreator, never()).addQueryToTransaction(anyString());
		verify(statement).close();
		verify(database).closeConnection(anyInt());
	}

	@Test(expected = SQLException.class)
	public void testCreateSQLExceptionOnOpenConnection() throws Exception {
		doThrow(new SQLException("Test Exception")).when(database).openPooledConnection();
		databaseTableCreator.create();
		verify(statement, never()).executeBatch();
	}

	@Test(expected = IllegalStateException.class)
	public void testCreateCommitIllegalStateException() throws Exception {
		doThrow(new IllegalStateException("Test Exception")).when(statement).executeBatch();
		databaseTableCreator.create();
		verify(statement).close();
		verify(database).closeConnection(anyInt());
	}

	@Test(expected = SQLException.class)
	public void testCreateCommitSQLException() throws Exception {
		doThrow(new SQLException("Test Exception")).when(statement).executeBatch();
		databaseTableCreator.create();
		verify(statement).close();
		verify(database).closeConnection(anyInt());
	}
}
