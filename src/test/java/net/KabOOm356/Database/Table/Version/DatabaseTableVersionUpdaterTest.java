package net.KabOOm356.Database.Table.Version;

import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.sql.Statement;
import net.KabOOm356.Database.Database;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import test.test.MockitoTest;

public class DatabaseTableVersionUpdaterTest extends MockitoTest {
  private DatabaseTableVersionUpdater databaseTableVersionUpdater;

  @Mock private Database database;
  @Mock private Statement statement;

  @Before
  public void setupMocks() throws Exception {
    databaseTableVersionUpdater =
        mock(
            DatabaseTableVersionUpdater.class,
            withSettings()
                .defaultAnswer(CALLS_REAL_METHODS)
                .useConstructor(database, "version", "table name"));
    when(database.createStatement(anyInt())).thenReturn(statement);
  }

  @After
  public void verifyMocks() throws Exception {
    verify(statement).close();
  }

  @Test
  public void testUpdate() throws Exception {
    when(databaseTableVersionUpdater.needsToUpdate()).thenReturn(true);

    databaseTableVersionUpdater.update();
    verify(databaseTableVersionUpdater).startTransaction();
    verify(databaseTableVersionUpdater).apply();
  }

  @Test
  public void testUpdateNotNeeded() throws Exception {
    final DatabaseTableVersionUpdater databaseTableVersionUpdater =
        mock(
            DatabaseTableVersionUpdater.class,
            withSettings()
                .defaultAnswer(CALLS_REAL_METHODS)
                .useConstructor(database, "version", "table name"));
    when(databaseTableVersionUpdater.needsToUpdate()).thenReturn(false);
    databaseTableVersionUpdater.update();
    verify(databaseTableVersionUpdater, never()).apply();
  }

  @Test(expected = InterruptedException.class)
  public void testUpdateInterruptedException() throws Exception {
    final DatabaseTableVersionUpdater databaseTableVersionUpdater =
        mock(
            DatabaseTableVersionUpdater.class,
            withSettings()
                .defaultAnswer(CALLS_REAL_METHODS)
                .useConstructor(database, "version", "table name"));
    when(databaseTableVersionUpdater.needsToUpdate()).thenReturn(true);
    doThrow(new InterruptedException("Test Exception")).when(databaseTableVersionUpdater).apply();
    databaseTableVersionUpdater.update();
  }

  @Test(expected = SQLException.class)
  public void testUpdateSQLException() throws Exception {
    final DatabaseTableVersionUpdater databaseTableVersionUpdater =
        mock(
            DatabaseTableVersionUpdater.class,
            withSettings()
                .defaultAnswer(CALLS_REAL_METHODS)
                .useConstructor(database, "version", "table name"));
    when(databaseTableVersionUpdater.needsToUpdate()).thenReturn(true);
    doThrow(new SQLException("Test Exception")).when(databaseTableVersionUpdater).apply();
    databaseTableVersionUpdater.update();
  }

  @Test(expected = ClassNotFoundException.class)
  public void testUpdateClassNotFoundException() throws Exception {
    final DatabaseTableVersionUpdater databaseTableVersionUpdater =
        mock(
            DatabaseTableVersionUpdater.class,
            withSettings()
                .defaultAnswer(CALLS_REAL_METHODS)
                .useConstructor(database, "version", "table name"));
    when(databaseTableVersionUpdater.needsToUpdate()).thenReturn(true);

    doThrow(new ClassNotFoundException("Test Exception")).when(databaseTableVersionUpdater).apply();
    databaseTableVersionUpdater.update();
  }

  @Test(expected = IllegalStateException.class)
  public void testUpdateCommitIllegalStateException() throws Exception {
    final DatabaseTableVersionUpdater databaseTableVersionUpdater =
        mock(
            DatabaseTableVersionUpdater.class,
            withSettings()
                .defaultAnswer(CALLS_REAL_METHODS)
                .useConstructor(database, "version", "table name"));
    when(databaseTableVersionUpdater.needsToUpdate()).thenReturn(true);

    doThrow(new IllegalStateException("Test Exception")).when(statement).executeBatch();
    databaseTableVersionUpdater.update();
  }

  @Test(expected = SQLException.class)
  public void testUpdateCommitSQLException() throws Exception {
    final DatabaseTableVersionUpdater databaseTableVersionUpdater =
        mock(
            DatabaseTableVersionUpdater.class,
            withSettings()
                .defaultAnswer(CALLS_REAL_METHODS)
                .useConstructor(database, "version", "table name"));
    when(databaseTableVersionUpdater.needsToUpdate()).thenReturn(true);

    doThrow(new SQLException("Test Exception")).when(statement).executeBatch();
    databaseTableVersionUpdater.update();
  }
}
