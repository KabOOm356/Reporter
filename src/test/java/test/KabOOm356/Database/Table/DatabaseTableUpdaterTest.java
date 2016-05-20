package test.KabOOm356.Database.Table;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.Table.DatabaseTableUpdater;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionUpdater;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.PowerMockitoTest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({DatabaseTableUpdater.class})
public class DatabaseTableUpdaterTest extends PowerMockitoTest {
	@Mock
	private DatabaseTableUpdater databaseTableUpdater;

	@Mock
	private Database database;

	@Mock
	private DatabaseTableVersionUpdater databaseTableVersionUpdater;

	@Mock
	private DatabaseTableVersionUpdater databaseTableVersionUpdater2;

	@Before
	public void setupMocks() throws Exception {
		final List<DatabaseTableVersionUpdater> databaseTableVersionUpdaterList = new ArrayList<DatabaseTableVersionUpdater>();
		databaseTableVersionUpdaterList.add(databaseTableVersionUpdater);
		databaseTableVersionUpdaterList.add(databaseTableVersionUpdater2);
		when(databaseTableUpdater.getDatabaseTableVersionUpdaters()).thenReturn(databaseTableVersionUpdaterList);
		when(databaseTableUpdater.getTableName()).thenReturn("TestTable");
		doCallRealMethod().when(databaseTableUpdater, "updateTable");
		doCallRealMethod().when(databaseTableUpdater).update();
	}

	@Test
	public void testUpdate() throws Exception {
		databaseTableUpdater.update();
		verify(databaseTableVersionUpdater).update();
		verify(databaseTableVersionUpdater2).update();
	}

	@Test(expected = InterruptedException.class)
	public void testUpdateInterruptedException() throws Exception {
		doThrow(new InterruptedException("Test Exception")).when(databaseTableVersionUpdater).update();
		databaseTableUpdater.update();
	}

	@Test(expected = SQLException.class)
	public void testUpdateSQLException() throws Exception {
		doThrow(new SQLException("Test Exception")).when(databaseTableVersionUpdater).update();
		databaseTableUpdater.update();
	}

	@Test(expected = ClassNotFoundException.class)
	public void testUpdateClassNotFoundException() throws Exception {
		doThrow(new ClassNotFoundException("Test Exception")).when(databaseTableVersionUpdater).update();
		databaseTableUpdater.update();
	}

	@Test(expected = IllegalStateException.class)
	public void testUpdateCommitIllegalStateException() throws Exception {
		doThrow(new IllegalStateException("Test Exception")).when(databaseTableUpdater, "commitTransaction");
		databaseTableUpdater.update();
	}

	@Test(expected = SQLException.class)
	public void testUpdateCommitSQLException() throws Exception {
		doThrow(new SQLException("Test Exception")).when(databaseTableUpdater, "commitTransaction");
		databaseTableUpdater.update();
	}
}
