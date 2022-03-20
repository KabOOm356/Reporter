package net.KabOOm356.Service;

import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Permission.ModLevel;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import test.test.service.ServiceTest;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ReportInformationServiceTest extends ServiceTest {
	private static final int connectionId = 999;

	private ReportInformationService manager;

	private SQLResultSet sqlResultSet = new SQLResultSet();
	private ResultRow resultRow = new ResultRow();
	private ResultRow resultRow1 = new ResultRow();
	private ResultRow resultRow2 = new ResultRow();

	@Override
	@Before
	public void setupMocks() throws Exception {
		super.setupMocks();

		sqlResultSet = new SQLResultSet();
		resultRow = new ResultRow();
		resultRow1 = new ResultRow();
		resultRow2 = new ResultRow();
		resultRow.put("ID", 1);
		resultRow1.put("ID", 3);
		resultRow2.put("ID", 5);
		sqlResultSet.add(resultRow);
		sqlResultSet.add(resultRow1);
		sqlResultSet.add(resultRow2);
		when(getDatabaseHandler().openPooledConnection()).thenReturn(connectionId);
		manager = new ReportInformationService(getModule());
	}

	@After
	public void verifyDatabaseMocks() throws InterruptedException, SQLException, ClassNotFoundException {
		verify(getDatabaseHandler()).openPooledConnection();
		verify(getDatabaseHandler()).closeConnection(connectionId);
	}

	private void verifyReturned(final List<Integer> returned) {
		assertEquals(sqlResultSet.size(), returned.size());
		assertEquals(resultRow.get("ID"), returned.get(0));
		assertEquals(resultRow1.get("ID"), returned.get(1));
		assertEquals(resultRow2.get("ID"), returned.get(2));
	}

	@Test
	public void getViewableReportsNameLookup() throws InterruptedException, SQLException, ClassNotFoundException {
		final CommandSender commandSender = mock(CommandSender.class);
		when(getDatabaseHandler().preparedSQLQuery(eq(connectionId), anyString(), ArgumentMatchers.anyList())).thenReturn(sqlResultSet);
		try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			final List<Integer> returned = manager.getViewableReports(commandSender);
			verifyReturned(returned);
			verify(commandSender).getName();
			verify(getDatabaseHandler())
					.preparedSQLQuery(eq(connectionId), anyString(), ArgumentMatchers.anyList());
		}
	}

	@Test
	public void getViewableReportsUUIDLookup() throws InterruptedException, SQLException, ClassNotFoundException {
		final CommandSender commandSender =
				mock(CommandSender.class, withSettings().extraInterfaces(OfflinePlayer.class));
		when(((OfflinePlayer) commandSender).getUniqueId()).thenReturn(UUID.randomUUID());
		when(getDatabaseHandler().preparedSQLQuery(eq(connectionId), anyString(), ArgumentMatchers.anyList())).thenReturn(sqlResultSet);
		try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			final List<Integer> returned = manager.getViewableReports(commandSender);
			verifyReturned(returned);
			verify((OfflinePlayer) commandSender).getUniqueId();
			verify(getDatabaseHandler())
					.preparedSQLQuery(eq(connectionId), anyString(), ArgumentMatchers.anyList());
		}
	}

	@Test
	public void getCompletedReportIndexes() throws SQLException, InterruptedException, ClassNotFoundException {
		when(getDatabaseHandler().sqlQuery(eq(connectionId), anyString())).thenReturn(sqlResultSet);
		final List<Integer> returned = manager.getCompletedReportIndexes();
		verifyReturned(returned);
		verify(getDatabaseHandler()).sqlQuery(eq(connectionId), anyString());
	}

	@Test
	public void getIncompleteReportIndexes() throws InterruptedException, SQLException, ClassNotFoundException {
		when(getDatabaseHandler().sqlQuery(eq(connectionId), anyString())).thenReturn(sqlResultSet);
		final List<Integer> returned = manager.getIncompleteReportIndexes();
		verifyReturned(returned);
		verify(getDatabaseHandler()).sqlQuery(eq(connectionId), anyString());
	}

	@Test
	public void getIndexesOfPriority() throws InterruptedException, SQLException, ClassNotFoundException {
		when(getDatabaseHandler().sqlQuery(eq(connectionId), anyString())).thenReturn(sqlResultSet);
		final List<Integer> returned = manager.getIndexesOfPriority(ModLevel.HIGH);
		verifyReturned(returned);
		verify(getDatabaseHandler()).sqlQuery(eq(connectionId), anyString());
	}

	@Test
	public void getReportPriority() throws InterruptedException, SQLException, ClassNotFoundException {
		final int priority = 2;
		sqlResultSet = new SQLResultSet();
		resultRow = new ResultRow();
		resultRow.put("Priority", priority);
		sqlResultSet.add(resultRow);
		when(getDatabaseHandler().sqlQuery(eq(connectionId), anyString())).thenReturn(sqlResultSet);
		try (final MockedStatic<ModLevel> modLevel = mockStatic(ModLevel.class)) {
			modLevel.when(() -> ModLevel.getByLevel(priority)).thenReturn(ModLevel.NORMAL);
			assertEquals(ModLevel.NORMAL, manager.getReportPriority(5));
			verify(getDatabaseHandler()).sqlQuery(eq(connectionId), anyString());
		}
	}
}
