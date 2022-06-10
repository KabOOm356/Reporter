package net.KabOOm356.Database.Table;

import static org.mockito.Mockito.*;

import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import test.test.MockitoTest;

public class DatabaseTableInitializerTest extends MockitoTest {
  @Mock(answer = Answers.CALLS_REAL_METHODS)
  private DatabaseTableInitializer databaseTableInitializer;

  @Mock private DatabaseTableCreator databaseTableCreator;

  @Mock private DatabaseTableMigrator databaseTableMigrator;

  @Mock private DatabaseTableUpdater databaseTableUpdater;

  @Before
  public void setupMocks() throws Exception {
    when(databaseTableInitializer.getCreator()).thenReturn(databaseTableCreator);
    when(databaseTableInitializer.getMigrator()).thenReturn(databaseTableMigrator);
    when(databaseTableInitializer.getUpdater()).thenReturn(databaseTableUpdater);
  }

  @Test
  public void testInitialize() throws InterruptedException, SQLException, ClassNotFoundException {
    databaseTableInitializer.initialize();
    verify(databaseTableCreator).create();
    verify(databaseTableMigrator).migrate();
    verify(databaseTableUpdater).update();
  }

  @Test
  public void testInitializeNoCreate() throws Exception {
    when(databaseTableInitializer.getCreator()).thenReturn(null);
    databaseTableInitializer.initialize();
    verify(databaseTableCreator, never()).create();
    verify(databaseTableMigrator).migrate();
    verify(databaseTableUpdater).update();
  }

  @Test
  public void testInitializeNoMigrate() throws Exception {
    when(databaseTableInitializer.getMigrator()).thenReturn(null);
    databaseTableInitializer.initialize();
    verify(databaseTableCreator).create();
    verify(databaseTableMigrator, never()).migrate();
    verify(databaseTableUpdater).update();
  }

  @Test
  public void testInitializeNoUpdate() throws Exception {
    when(databaseTableInitializer.getUpdater()).thenReturn(null);
    databaseTableInitializer.initialize();
    verify(databaseTableCreator).create();
    verify(databaseTableMigrator).migrate();
    verify(databaseTableUpdater, never()).update();
  }

  @Test(expected = InterruptedException.class)
  public void testInitializeInterruptedException()
      throws InterruptedException, SQLException, ClassNotFoundException {
    doThrow(new InterruptedException("Test Exception")).when(databaseTableCreator).create();
    databaseTableInitializer.initialize();
  }

  @Test(expected = SQLException.class)
  public void testInitializeSQLException()
      throws InterruptedException, SQLException, ClassNotFoundException {
    doThrow(new SQLException("Test Exception")).when(databaseTableCreator).create();
    databaseTableInitializer.initialize();
  }

  @Test(expected = ClassNotFoundException.class)
  public void testInitializeClassNotFoundException()
      throws InterruptedException, SQLException, ClassNotFoundException {
    doThrow(new ClassNotFoundException("Test Exception")).when(databaseTableCreator).create();
    databaseTableInitializer.initialize();
  }
}
