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

public class DatabaseTableUpdateHandlerTest extends MockitoTest {
	private static final int connectionId = 123456;
	private static final String query = "Test Query";

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private DatabaseTableUpdateHandler databaseTableUpdateHandler;

	@Mock
	private Database database;
	@Mock
	private Statement statement;

	@Before
	public void setupMocks() throws Exception {
		when(databaseTableUpdateHandler.getDatabase()).thenReturn(database);
		when(database.openPooledConnection()).thenReturn(connectionId);
		when(database.createStatement(anyInt())).thenReturn(statement);
	}

	@Test
	public void testStartTransaction()
			throws InterruptedException, SQLException, ClassNotFoundException {
		databaseTableUpdateHandler.startTransaction();
		verify(database).openPooledConnection();
		verify(database).createStatement(connectionId);
	}

	@Test(expected = InterruptedException.class)
	public void testStartTransactionInterruptedException() throws Exception {
		when(database.openPooledConnection()).thenThrow(new InterruptedException("Test Exception"));
		try {
			databaseTableUpdateHandler.startTransaction();
		} finally {
			verify(databaseTableUpdateHandler).terminateTransaction();
			verify(databaseTableUpdateHandler).endTransaction();
		}
	}

	@Test(expected = SQLException.class)
	public void testStartTransactionSQLException() throws Exception {
		when(database.openPooledConnection()).thenThrow(new SQLException("Test Exception"));
		try {
			databaseTableUpdateHandler.startTransaction();
		} finally {
			verify(databaseTableUpdateHandler).terminateTransaction();
			verify(databaseTableUpdateHandler).endTransaction();
		}
	}

	@Test(expected = ClassNotFoundException.class)
	public void testStartTransactionClassNotFoundException() throws Exception {
		when(database.openPooledConnection()).thenThrow(new ClassNotFoundException("Test Exception"));
		try {
			databaseTableUpdateHandler.startTransaction();
		} finally {
			verify(databaseTableUpdateHandler).terminateTransaction();
			verify(databaseTableUpdateHandler).endTransaction();
		}
	}

	@Test
	public void testAddQueryToTransaction()
			throws SQLException, InterruptedException, ClassNotFoundException {
		// Start the transaction
		databaseTableUpdateHandler.startTransaction();
		databaseTableUpdateHandler.addQueryToTransaction(query);
		verify(statement).addBatch(query);
	}

	@Test(expected = IllegalStateException.class)
	public void testAddQueryToTransactionNoTransaction() throws SQLException {
		try {
			databaseTableUpdateHandler.addQueryToTransaction(query);
		} finally {
			verify(databaseTableUpdateHandler).isTransactionInProgress();
		}
	}

	@Test(expected = SQLException.class)
	public void testAddQueryToTransactionSQLException()
			throws SQLException, InterruptedException, ClassNotFoundException {
		doThrow(new SQLException("Test Exception")).when(statement).addBatch(anyString());
		databaseTableUpdateHandler.startTransaction();
		try {
			databaseTableUpdateHandler.addQueryToTransaction(query);
		} finally {
			verify(databaseTableUpdateHandler, times(2)).isTransactionInProgress();
		}
	}

	@Test
	public void testCommitTransaction() throws Exception {
		databaseTableUpdateHandler.startTransaction();
		databaseTableUpdateHandler.commitTransaction();
		verify(statement).executeBatch();
		verify(databaseTableUpdateHandler).endTransaction();
	}

	@Test(expected = SQLException.class)
	public void testCommitTransactionSQLException() throws Exception {
		databaseTableUpdateHandler.startTransaction();
		doThrow(new SQLException("Test Exception")).when(statement).executeBatch();
		try {
			databaseTableUpdateHandler.commitTransaction();
		} finally {
			verify(statement).executeBatch();
			verify(databaseTableUpdateHandler).terminateTransaction();
			verify(databaseTableUpdateHandler).endTransaction();
		}
	}

	@Test
	public void testEndTransactionStatementSQLException()
			throws SQLException, InterruptedException, ClassNotFoundException {
		databaseTableUpdateHandler.startTransaction();
		doThrow(new SQLException("Test Exception")).when(statement).close();
		databaseTableUpdateHandler.commitTransaction();
		verify(statement).close();
		verify(database).closeConnection(connectionId);
	}
}
