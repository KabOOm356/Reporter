package net.KabOOm356.Service;

import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Util.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.service.ServiceTest;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({ReportInformationService.class, Bukkit.class, BukkitUtil.class, OfflinePlayer.class, ModLevel.class})
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
		mockStatic(Bukkit.class);
		mockStatic(BukkitUtil.class);
		mockStatic(OfflinePlayer.class);
		mockStatic(ModLevel.class);
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
		manager = spy(new ReportInformationService(getModule()));
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
		when(BukkitUtil.isPlayer(commandSender)).thenReturn(false);
		when(getDatabaseHandler().preparedSQLQuery(eq(connectionId), anyString(), ArgumentMatchers.<String>anyList())).thenReturn(sqlResultSet);
		final List<Integer> returned = manager.getViewableReports(commandSender);
		verifyReturned(returned);
		verify(commandSender).getName();
		verify(getDatabaseHandler()).preparedSQLQuery(eq(connectionId), anyString(), ArgumentMatchers.<String>anyList());
	}

	@Test
	public void getViewableReportsUUIDLookup() throws InterruptedException, SQLException, ClassNotFoundException {
		final CommandSender commandSender = mock(CommandSender.class);
		final OfflinePlayer player = mock(OfflinePlayer.class);
		when(BukkitUtil.isPlayer(commandSender)).thenReturn(true);
		when(OfflinePlayer.class.cast(commandSender)).thenReturn(player);
		when(player.getUniqueId()).thenReturn(UUID.randomUUID());
		when(getDatabaseHandler().preparedSQLQuery(eq(connectionId), anyString(), ArgumentMatchers.<String>anyList())).thenReturn(sqlResultSet);
		final List<Integer> returned = manager.getViewableReports(commandSender);
		verifyReturned(returned);
		verify(player).getUniqueId();
		verify(getDatabaseHandler()).preparedSQLQuery(eq(connectionId), anyString(), ArgumentMatchers.<String>anyList());
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
		when(ModLevel.getByLevel(priority)).thenReturn(ModLevel.NORMAL);
		assertEquals(ModLevel.NORMAL, manager.getReportPriority(5));
		verify(getDatabaseHandler()).sqlQuery(eq(connectionId), anyString());
	}
}