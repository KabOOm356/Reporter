package net.KabOOm356.Database.Table;

import net.KabOOm356.Database.Database;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.PowerMockitoTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest(DatabaseTableUpdateHandlerTest.class)
public class DatabaseTableUpdateHandlerTest extends PowerMockitoTest {
	private static final int connectionId = 123456;
	private static final String query = "Test Query";
	private static final Method startTransaction = method(DatabaseTableUpdateHandler.class, "startTransaction");
	private static final Method addQueryToTransaction = method(DatabaseTableUpdateHandler.class, "addQueryToTransaction");
	private static final Method commitTransaction = method(DatabaseTableUpdateHandler.class, "commitTransaction");
	private static final Method terminateTransaction = method(DatabaseTableUpdateHandler.class, "terminateTransaction");
	private static final Method endTransaction = method(DatabaseTableUpdateHandler.class, "endTransaction");
	@Mock
	private DatabaseTableUpdateHandler databaseTableUpdateHandler;
	@Mock
	private Database database;
	@Mock
	private Statement statement;

	@Before
	public void setupMocks() throws Exception {
		doCallRealMethod().when(databaseTableUpdateHandler, startTransaction).withNoArguments();
		doCallRealMethod().when(databaseTableUpdateHandler, addQueryToTransaction).withArguments(query);
		doCallRealMethod().when(databaseTableUpdateHandler, commitTransaction).withNoArguments();
		doCallRealMethod().when(databaseTableUpdateHandler, terminateTransaction).withNoArguments();
		doCallRealMethod().when(databaseTableUpdateHandler, endTransaction).withNoArguments();
		when(databaseTableUpdateHandler.isTransactionInProgress()).thenCallRealMethod();
		when(databaseTableUpdateHandler, "getStatement").thenCallRealMethod();
		when(databaseTableUpdateHandler, "getDatabase").thenReturn(database);
		when(database.openPooledConnection()).thenReturn(connectionId);
		when(database.createStatement(anyInt())).thenReturn(statement);
	}

	@Test
	public void testStartTransaction() throws InvocationTargetException, IllegalAccessException, InterruptedException, SQLException, ClassNotFoundException {
		startTransaction.invoke(databaseTableUpdateHandler);
		verify(database).openPooledConnection();
		verify(database).createStatement(connectionId);
	}

	@Test(expected = InterruptedException.class)
	public void testStartTransactionInterruptedException() throws Exception {
		when(database.openPooledConnection()).thenThrow(new InterruptedException("Test Exception"));
		try {
			startTransaction.invoke(databaseTableUpdateHandler);
		} finally {
			verifyPrivate(databaseTableUpdateHandler).invoke(terminateTransaction).withNoArguments();
			verifyPrivate(databaseTableUpdateHandler).invoke(endTransaction).withNoArguments();
		}
	}

	@Test(expected = SQLException.class)
	public void testStartTransactionSQLException() throws Exception {
		when(database.openPooledConnection()).thenThrow(new SQLException("Test Exception"));
		try {
			startTransaction.invoke(databaseTableUpdateHandler);
		} finally {
			verifyPrivate(databaseTableUpdateHandler).invoke(terminateTransaction).withNoArguments();
			verifyPrivate(databaseTableUpdateHandler).invoke(endTransaction).withNoArguments();
		}
	}

	@Test(expected = ClassNotFoundException.class)
	public void testStartTransactionClassNotFoundException() throws Exception {
		when(database.openPooledConnection()).thenThrow(new ClassNotFoundException("Test Exception"));
		try {
			startTransaction.invoke(databaseTableUpdateHandler);
		} finally {
			verifyPrivate(databaseTableUpdateHandler).invoke(terminateTransaction).withNoArguments();
			verifyPrivate(databaseTableUpdateHandler).invoke(endTransaction).withNoArguments();
		}
	}

	@Test
	public void testAddQueryToTransaction() throws InvocationTargetException, IllegalAccessException, SQLException {
		// Start the transaction
		startTransaction.invoke(databaseTableUpdateHandler);
		addQueryToTransaction.invoke(databaseTableUpdateHandler, query);
		verify(statement).addBatch(query);
	}

	@Test(expected = IllegalStateException.class)
	public void testAddQueryToTransactionNoTransaction() throws InvocationTargetException, IllegalAccessException {
		try {
			addQueryToTransaction.invoke(databaseTableUpdateHandler, query);
		} finally {
			verify(databaseTableUpdateHandler).isTransactionInProgress();
		}
	}

	@Test(expected = SQLException.class)
	public void testAddQueryToTransactionSQLException() throws SQLException, InvocationTargetException, IllegalAccessException {
		doThrow(new SQLException("Test Exception")).when(statement).addBatch(anyString());
		startTransaction.invoke(databaseTableUpdateHandler);
		try {
			addQueryToTransaction.invoke(databaseTableUpdateHandler, query);
		} finally {
			verify(databaseTableUpdateHandler, times(2)).isTransactionInProgress();
		}
	}

	@Test
	public void testCommitTransaction() throws Exception {
		startTransaction.invoke(databaseTableUpdateHandler);
		commitTransaction.invoke(databaseTableUpdateHandler);
		verify(statement).executeBatch();
		verifyPrivate(databaseTableUpdateHandler).invoke(endTransaction).withNoArguments();
	}

	@Test(expected = SQLException.class)
	public void testCommitTransactionSQLException() throws Exception {
		startTransaction.invoke(databaseTableUpdateHandler);
		doThrow(new SQLException("Test Exception")).when(statement).executeBatch();
		try {
			commitTransaction.invoke(databaseTableUpdateHandler);
		} finally {
			verify(statement).executeBatch();
			verifyPrivate(databaseTableUpdateHandler).invoke(terminateTransaction).withNoArguments();
			verifyPrivate(databaseTableUpdateHandler).invoke(endTransaction).withNoArguments();
		}
	}

	@Test
	public void testEndTransactionStatementSQLException() throws InvocationTargetException, IllegalAccessException, SQLException {
		startTransaction.invoke(databaseTableUpdateHandler);
		doThrow(new SQLException("Test Exception")).when(statement).close();
		commitTransaction.invoke(databaseTableUpdateHandler);
		verify(statement).close();
		verify(database).closeConnection(connectionId);
	}
}
