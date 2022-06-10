package net.KabOOm356.Service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import net.KabOOm356.Throwable.IndexOutOfRangeException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import test.test.service.ServiceTest;

public class ReportValidatorServiceTest extends ServiceTest {
  @Mock private ReportCountService countManager;

  private ReportValidatorService manager;

  @Override
  @Before
  public void setupMocks() throws Exception {
    super.setupMocks();
    when(getModule().getReportCountService()).thenReturn(countManager);
    when(countManager.getCount()).thenReturn(5);
    manager = spy(new ReportValidatorService(getModule()));
  }

  @Test(expected = IndexOutOfRangeException.class)
  public void requireReportIndexValidZero()
      throws ClassNotFoundException, SQLException, InterruptedException, IndexOutOfRangeException {
    manager.requireReportIndexValid(0);
  }

  @Test(expected = IndexOutOfRangeException.class)
  public void requireReportIndexValidAboveCount()
      throws ClassNotFoundException, SQLException, InterruptedException, IndexOutOfRangeException {
    manager.requireReportIndexValid(10);
  }

  @Test
  public void requireReportIndexValid()
      throws ClassNotFoundException, SQLException, InterruptedException, IndexOutOfRangeException {
    manager.requireReportIndexValid(2);
  }

  @Test
  public void isReportIndexValidZero()
      throws ClassNotFoundException, SQLException, InterruptedException, IndexOutOfRangeException {
    doThrow(new IndexOutOfRangeException("Test Exception"))
        .when(manager)
        .requireReportIndexValid(0);
    assertFalse(manager.isReportIndexValid(0));
  }

  @Test
  public void isReportIndexValidAboveCount()
      throws ClassNotFoundException, SQLException, InterruptedException, IndexOutOfRangeException {
    doThrow(new IndexOutOfRangeException("Test Exception"))
        .when(manager)
        .requireReportIndexValid(10);
    assertFalse(manager.isReportIndexValid(10));
  }

  @Test
  public void isReportIndexValid()
      throws ClassNotFoundException, SQLException, InterruptedException, IndexOutOfRangeException {
    doNothing().when(manager).requireReportIndexValid(2);
    assertTrue(manager.isReportIndexValid(2));
  }
}
