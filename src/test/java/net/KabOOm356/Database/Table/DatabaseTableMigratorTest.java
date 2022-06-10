package net.KabOOm356.Database.Table;

import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.KabOOm356.Database.Table.Version.DatabaseTableVersionMigrator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import test.test.MockitoTest;

public class DatabaseTableMigratorTest extends MockitoTest {
  @Mock(answer = Answers.CALLS_REAL_METHODS)
  private DatabaseTableMigrator databaseTableMigrator;

  @Mock private DatabaseTableVersionMigrator databaseTableVersionMigrator;

  @Mock private DatabaseTableVersionMigrator databaseTableVersionMigrator2;

  @Before
  public void setupMocks() throws Exception {
    final List<DatabaseTableVersionMigrator> databaseTableVersionMigratorList = new ArrayList<>();
    databaseTableVersionMigratorList.add(databaseTableVersionMigrator);
    databaseTableVersionMigratorList.add(databaseTableVersionMigrator2);
    when(databaseTableMigrator.getDatabaseTableVersionMigrators())
        .thenReturn(databaseTableVersionMigratorList);
  }

  @Test
  public void testMigrate() throws Exception {
    databaseTableMigrator.migrate();
    verify(databaseTableVersionMigrator).migrate();
    verify(databaseTableVersionMigrator2).migrate();
  }

  @Test(expected = InterruptedException.class)
  public void testMigrateInterruptedException()
      throws InterruptedException, SQLException, ClassNotFoundException {
    doThrow(new InterruptedException("Test Exception"))
        .when(databaseTableVersionMigrator)
        .migrate();
    databaseTableMigrator.migrate();
  }

  @Test(expected = SQLException.class)
  public void testMigrateSQLException()
      throws InterruptedException, SQLException, ClassNotFoundException {
    doThrow(new SQLException("Test Exception")).when(databaseTableVersionMigrator).migrate();
    databaseTableMigrator.migrate();
  }

  @Test(expected = ClassNotFoundException.class)
  public void testMigrateClassNotFoundException()
      throws InterruptedException, SQLException, ClassNotFoundException {
    doThrow(new ClassNotFoundException("Test Exception"))
        .when(databaseTableVersionMigrator)
        .migrate();
    databaseTableMigrator.migrate();
  }
}
