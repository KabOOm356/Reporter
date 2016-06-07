package net.KabOOm356.Service;

import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Permission.ModLevel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import test.test.service.ServiceTest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class ReportCountServiceTest extends ServiceTest {
	@Mock
	private ReportInformationService informationManager;

	private ReportCountService manager;

	@Override
	@Before
	public void setupMocks() throws Exception {
		super.setupMocks();
		when(getModule().getReportInformationService()).thenReturn(informationManager);
		manager = spy(new ReportCountService(getModule()));
	}

	@Test
	public void testGetCount() throws InterruptedException, SQLException, ClassNotFoundException {
		final int count = 5;
		final SQLResultSet resultSet = new SQLResultSet();
		final ResultRow resultRow = new ResultRow();
		resultRow.put("Count", count);
		resultSet.add(resultRow);
		final int connectionId = 999;
		when(getDatabaseHandler().openPooledConnection()).thenReturn(connectionId);
		when(getDatabaseHandler().sqlQuery(eq(connectionId), anyString())).thenReturn(resultSet);
		assertEquals(count, manager.getCount());
		verify(getDatabaseHandler()).openPooledConnection();
		verify(getDatabaseHandler()).sqlQuery(eq(connectionId), anyString());
		verify(getDatabaseHandler()).closeConnection(connectionId);
	}

	@Test
	public void testGetIncompleteReports() throws InterruptedException, SQLException, ClassNotFoundException {
		final List<Integer> indexes = new ArrayList<Integer>();
		indexes.add(1);
		indexes.add(3);
		indexes.add(5);
		when(informationManager.getIncompleteReportIndexes()).thenReturn(indexes);
		assertEquals(indexes.size(), manager.getIncompleteReports());
	}

	@Test
	public void testGetCompleteReports() throws InterruptedException, SQLException, ClassNotFoundException {
		final List<Integer> indexes = new ArrayList<Integer>();
		indexes.add(1);
		indexes.add(3);
		indexes.add(5);
		when(informationManager.getCompletedReportIndexes()).thenReturn(indexes);
		assertEquals(indexes.size(), manager.getCompletedReports());
	}

	@Test
	public void testGetNumberOfPriority() throws InterruptedException, SQLException, ClassNotFoundException {
		final List<Integer> indexes = new ArrayList<Integer>();
		indexes.add(1);
		indexes.add(3);
		indexes.add(5);
		when(informationManager.getIndexesOfPriority(ModLevel.NONE)).thenReturn(indexes);
		assertEquals(indexes.size(), manager.getNumberOfPriority(ModLevel.NONE));
	}
}
