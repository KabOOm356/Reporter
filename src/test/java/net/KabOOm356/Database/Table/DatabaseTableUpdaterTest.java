package net.KabOOm356.Database.Table;

import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionUpdater;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import test.test.MockitoTest;

public class DatabaseTableUpdaterTest extends MockitoTest {
  @Mock(answer = Answers.CALLS_REAL_METHODS)
  private DatabaseTableUpdater databaseTableUpdater;

  @Mock private DatabaseTableVersionUpdater databaseTableVersionUpdater;

  @Mock private DatabaseTableVersionUpdater databaseTableVersionUpdater2;

  @Before
  public void setupMocks() throws Exception {
    final List<DatabaseTableVersionUpdater> databaseTableVersionUpdaterList = new ArrayList<>();
    databaseTableVersionUpdaterList.add(databaseTableVersionUpdater);
    databaseTableVersionUpdaterList.add(databaseTableVersionUpdater2);
    when(databaseTableUpdater.getDatabaseTableVersionUpdaters())
        .thenReturn(databaseTableVersionUpdaterList);
    when(databaseTableUpdater.getTableName()).thenReturn("TestTable");
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
    doThrow(new ClassNotFoundException("Test Exception"))
        .when(databaseTableVersionUpdater)
        .update();
    databaseTableUpdater.update();
  }

  @Test(expected = IllegalStateException.class)
  public void testUpdateCommitIllegalStateException() throws Exception {
    doThrow(new IllegalStateException("Test Exception"))
        .when(databaseTableUpdater)
        .commitTransaction();
    databaseTableUpdater.update();
  }

  @Test(expected = SQLException.class)
  public void testUpdateCommitSQLException() throws Exception {
    doThrow(new SQLException("Test Exception")).when(databaseTableUpdater).commitTransaction();
    databaseTableUpdater.update();
  }
}
