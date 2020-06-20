package net.KabOOm356.Database.Table.Version;

import net.KabOOm356.Database.Database;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.PowerMockitoTest;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.mockito.Mockito.never;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest(DatabaseTableVersionUpdater.class)
public class DatabaseTableVersionUpdaterTest extends PowerMockitoTest {
	private static final Method needsToUpdate = method(DatabaseTableVersionUpdater.class, "needsToUpdate");
	private static final Method startTransaction = method(DatabaseTableVersionUpdater.class, "startTransaction");
	private static final Method apply = method(DatabaseTableVersionUpdater.class, "apply");
	private static final Method commitTransaction = method(DatabaseTableVersionUpdater.class, "commitTransaction");
	@Mock
	private DatabaseTableVersionUpdater databaseTableVersionUpdater;
	@Mock
	private Database database;

	@Before
	public void setupMocks() throws Exception {
		doCallRealMethod().when(databaseTableVersionUpdater).update();
		doNothing().when(databaseTableVersionUpdater, startTransaction).withNoArguments();
		when(databaseTableVersionUpdater, needsToUpdate).withNoArguments().thenReturn(true);
		when(databaseTableVersionUpdater, "getDatabase").thenReturn(database);
		when(databaseTableVersionUpdater, "getColumns").thenReturn(new ArrayList<String>());
	}

	@After
	public void verifyMocks() throws Exception {
		verifyPrivate(databaseTableVersionUpdater).invoke(commitTransaction).withNoArguments();
	}

	@Test
	public void testUpdate() throws Exception {
		databaseTableVersionUpdater.update();
		verifyPrivate(databaseTableVersionUpdater).invoke(startTransaction).withNoArguments();
		verifyPrivate(databaseTableVersionUpdater).invoke(needsToUpdate).withNoArguments();
		verifyPrivate(databaseTableVersionUpdater).invoke(apply).withNoArguments();
	}

	@Test
	public void testUpdateNotNeeded() throws Exception {
		when(databaseTableVersionUpdater, needsToUpdate).withNoArguments().thenReturn(false);
		databaseTableVersionUpdater.update();
		verifyPrivate(databaseTableVersionUpdater, never()).invoke(apply).withNoArguments();
	}

	@Test(expected = InterruptedException.class)
	public void testUpdateInterruptedException() throws Exception {
		when(databaseTableVersionUpdater, apply).withNoArguments().thenThrow(new InterruptedException("Test Exception"));
		try {
			databaseTableVersionUpdater.update();
		} finally {
			verifyPrivate(databaseTableVersionUpdater).invoke(commitTransaction).withNoArguments();
		}
	}

	@Test(expected = SQLException.class)
	public void testUpdateSQLException() throws Exception {
		when(databaseTableVersionUpdater, apply).withNoArguments().thenThrow(new SQLException("Test Exception"));
		try {
			databaseTableVersionUpdater.update();
		} finally {
			verifyPrivate(databaseTableVersionUpdater).invoke(commitTransaction).withNoArguments();
		}
	}

	@Test(expected = ClassNotFoundException.class)
	public void testUpdateClassNotFoundException() throws Exception {
		when(databaseTableVersionUpdater, apply).withNoArguments().thenThrow(new ClassNotFoundException("Test Exception"));
		try {
			databaseTableVersionUpdater.update();
		} finally {
			verifyPrivate(databaseTableVersionUpdater).invoke(commitTransaction).withNoArguments();
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testUpdateCommitIllegalStateException() throws Exception {
		when(databaseTableVersionUpdater, commitTransaction).withNoArguments().thenThrow(new IllegalStateException("Test Exception"));
		try {
			databaseTableVersionUpdater.update();
		} finally {
			verifyPrivate(databaseTableVersionUpdater).invoke(commitTransaction).withNoArguments();
		}
	}

	@Test(expected = SQLException.class)
	public void testUpdateCommitSQLException() throws Exception {
		when(databaseTableVersionUpdater, commitTransaction).withNoArguments().thenThrow(new SQLException("Test Exception"));
		try {
			databaseTableVersionUpdater.update();
		} finally {
			verifyPrivate(databaseTableVersionUpdater).invoke(commitTransaction).withNoArguments();
		}
	}
}
