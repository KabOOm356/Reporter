package net.KabOOm356.Service;

import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Util.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.service.ServiceTest;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({ReportPermissionService.class, Bukkit.class, BukkitUtil.class, CommandSender.class})
public class ReportPermissionServiceTest extends ServiceTest {
	@Mock
	private CommandSender sender;

	@Mock
	private OfflinePlayer claimingPlayer;

	@Mock
	private CommandSender claimingPlayerSender;

	@Mock
	private ReportInformationService informationManager;

	@Mock
	private PlayerService playerService;

	private SQLResultSet resultSet;
	private ResultRow resultRow;

	private ReportPermissionService manager;

	@Override
	@Before
	public void setupMocks() throws Exception {
		super.setupMocks();
		mockStatic(Bukkit.class);
		mockStatic(BukkitUtil.class);
		mockStatic(CommandSender.class);
		when(BukkitUtil.formatPlayerName(sender)).thenReturn("sender");
		final int connectionId = 999;
		final String claimedBy = "player";
		final UUID claimedByUUID = UUID.randomUUID();
		resultSet = new SQLResultSet();
		resultRow = new ResultRow();
		resultRow.put("ClaimStatus", false);
		resultRow.put("ClaimedBy", claimedBy);
		resultRow.put("ClaimedByUUID", claimedByUUID.toString());
		resultRow.put("ClaimPriority", ModLevel.NORMAL.getLevel());
		resultSet.add(resultRow);
		when(getDatabaseHandler().openPooledConnection()).thenReturn(connectionId);
		when(getDatabaseHandler().sqlQuery(eq(connectionId), anyString())).thenReturn(resultSet);
		when(BukkitUtil.getOfflinePlayer(claimedByUUID, claimedBy)).thenReturn(claimingPlayer);
		when(CommandSender.class.cast(claimingPlayer)).thenReturn(claimingPlayerSender);
		when(getModule().getReportInformationService()).thenReturn(informationManager);
		when(getModule().getPlayerService()).thenReturn(playerService);
		manager = spy(new ReportPermissionService(getModule()));
	}

	@Test
	public void canAlterReportSenderOnly() throws InterruptedException, SQLException, ClassNotFoundException {
		doReturn(true).when(manager).canAlterReport(sender, 1, sender);
		manager.canAlterReport(sender, 1);
		verify(manager).canAlterReport(sender, 1, sender);
	}

	@Test
	public void canAlterReportNullPlayer() throws Exception {
		assertFalse(manager.canAlterReport(mock(CommandSender.class), 1, null));
	}

	@Test
	public void canAlterReportPriorityFail() throws Exception {
		doReturn(false).when(manager).requirePriority(sender, 1, sender);
		assertFalse(manager.canAlterReport(sender, 1, sender));
	}

	@Test
	public void canAlterReportClaimedPriorityFail() throws InterruptedException, SQLException, ClassNotFoundException {
		doReturn(true).when(manager).requirePriority(sender, 1, sender);
		doReturn(false).when(manager).requireUnclaimedOrPriority(sender, 1, sender);
		assertFalse(manager.canAlterReport(sender, 1, sender));
	}

	@Test
	public void canAlterReportPass() throws InterruptedException, SQLException, ClassNotFoundException {
		doReturn(true).when(manager).requirePriority(sender, 1, sender);
		doReturn(true).when(manager).requireUnclaimedOrPriority(sender, 1, sender);
		assertTrue(manager.canAlterReport(sender, 1, sender));
	}

	@Test(expected = InterruptedException.class)
	public void canAlterReportInterruptedException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new InterruptedException()).when(manager).requirePriority(sender, 1, sender);
		manager.canAlterReport(sender, 1, sender);
	}

	@Test(expected = SQLException.class)
	public void canAlterReportSQLException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new SQLException()).when(manager).requirePriority(sender, 1, sender);
		manager.canAlterReport(sender, 1, sender);
	}

	@Test(expected = ClassNotFoundException.class)
	public void canAlterReportClassNotFoundException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new ClassNotFoundException()).when(manager).requirePriority(sender, 1, sender);
		manager.canAlterReport(sender, 1, sender);
	}

	@Test
	public void requireUnclaimedOrPriorityIsOp() throws Exception {
		when(sender.isOp()).thenReturn(true);
		assertTrue(manager.requireUnclaimedOrPriority(sender, 1, sender));
	}

	@Test
	public void requireUnclaimedOrPriorityIsConsole() throws Exception {
		assertTrue(manager.requireUnclaimedOrPriority(mock(ConsoleCommandSender.class), 1, sender));
	}

	@Test
	public void requireUnclaimedOrPriorityUnclaimed() throws InterruptedException, SQLException, ClassNotFoundException {
		assertTrue(manager.requireUnclaimedOrPriority(sender, 1, sender));
	}

	@Test
	public void requireUnclaimedOrPriorityClaimedWithHigherPriority() throws InterruptedException, SQLException, ClassNotFoundException {
		resultRow.put("ClaimStatus", true);
		final CommandSender player = mock(CommandSender.class);
		when(playerService.getModLevel(player)).thenReturn(ModLevel.LOW);
		assertFalse(manager.requireUnclaimedOrPriority(sender, 1, player));
	}

	@Test
	public void requireUnclaimedOrPriorityClaimedWithLowerPriority() throws InterruptedException, SQLException, ClassNotFoundException {
		resultRow.put("ClaimStatus", true);
		final CommandSender player = mock(CommandSender.class);
		when(playerService.getModLevel(player)).thenReturn(ModLevel.HIGH);
		assertTrue(manager.requireUnclaimedOrPriority(sender, 1, player));
	}

	@Test
	public void requireUnclaimedOrPriorityClaimedBySender() throws InterruptedException, SQLException, ClassNotFoundException {
		final CommandSender player = mock(CommandSender.class);
		resultRow.put("ClaimStatus", true);
		when(BukkitUtil.playersEqual(sender, claimingPlayerSender)).thenReturn(true);
		assertTrue(manager.requireUnclaimedOrPriority(sender, 1, player));
	}

	@Test
	public void requireUnclaimedOrPriorityClaimedByPlayer() throws InterruptedException, SQLException, ClassNotFoundException {
		final CommandSender player = mock(CommandSender.class);
		resultRow.put("ClaimStatus", true);
		when(BukkitUtil.playersEqual(player, claimingPlayerSender)).thenReturn(true);
		assertTrue(manager.requireUnclaimedOrPriority(sender, 1, player));
	}

	@Test(expected = ClassNotFoundException.class)
	public void requireUnclaimedOrPriorityClassNotFoundException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new ClassNotFoundException()).when(getDatabaseHandler()).openPooledConnection();
		manager.requireUnclaimedOrPriority(sender, 1, sender);
	}

	@Test(expected = SQLException.class)
	public void requireUnclaimedOrPrioritySQLException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new SQLException()).when(getDatabaseHandler()).openPooledConnection();
		manager.requireUnclaimedOrPriority(sender, 1, sender);
	}

	@Test(expected = InterruptedException.class)
	public void requireUnclaimedOrPriorityInterruptedException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new InterruptedException()).when(getDatabaseHandler()).openPooledConnection();
		manager.requireUnclaimedOrPriority(sender, 1, sender);
	}

	@Test
	public void checkPriorityIsOp() throws Exception {
		when(sender.isOp()).thenReturn(true);
		assertTrue(manager.checkPriority(sender, 1));
	}

	@Test
	public void checkPriorityConsole() throws Exception {
		assertTrue(manager.checkPriority(mock(ConsoleCommandSender.class), 1));
	}

	@Test
	public void checkPriorityPass() throws Exception {
		when(playerService.getModLevel(sender)).thenReturn(ModLevel.HIGH);
		when(informationManager.getReportPriority(1)).thenReturn(ModLevel.NORMAL);
		assertTrue(manager.checkPriority(sender, 1));
	}

	@Test
	public void checkPriorityFail() throws Exception {
		when(playerService.getModLevel(sender)).thenReturn(ModLevel.LOW);
		when(informationManager.getReportPriority(1)).thenReturn(ModLevel.NORMAL);
		assertFalse(manager.checkPriority(sender, 1));
	}

	@Test(expected = InterruptedException.class)
	public void checkPriorityInterruptedException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new InterruptedException()).when(informationManager).getReportPriority(1);
		manager.checkPriority(sender, 1);
	}

	@Test(expected = ClassNotFoundException.class)
	public void checkPriorityClassNotFoundException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new ClassNotFoundException()).when(informationManager).getReportPriority(1);
		manager.checkPriority(sender, 1);
	}

	@Test(expected = SQLException.class)
	public void checkPrioritySQLException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new SQLException()).when(informationManager).getReportPriority(1);
		manager.checkPriority(sender, 1);
	}

	@Test
	public void requirePriority() throws Exception {
		doReturn(true).when(manager).checkPriority(sender, 1);
		assertTrue(manager.requirePriority(sender, 1, sender));
	}

	@Test
	public void requirePriorityFailSameSender() throws InterruptedException, SQLException, ClassNotFoundException {
		doReturn(false).when(manager).checkPriority(sender, 1);
		when(informationManager.getReportPriority(1)).thenReturn(ModLevel.NORMAL);
		when(BukkitUtil.playersEqual(sender, sender)).thenReturn(true);
		assertFalse(manager.requirePriority(sender, 1, sender));
		verify(playerService).displayModLevel(sender);
	}

	@Test
	public void requirePriorityFailDifferentSender() throws InterruptedException, SQLException, ClassNotFoundException {
		final CommandSender player = mock(CommandSender.class);
		doReturn(false).when(manager).checkPriority(player, 1);
		when(informationManager.getReportPriority(1)).thenReturn(ModLevel.NORMAL);
		when(BukkitUtil.playersEqual(sender, player)).thenReturn(false);
		assertFalse(manager.requirePriority(sender, 1, player));
		verify(playerService).displayModLevel(sender, player);
	}

	@Test(expected = SQLException.class)
	public void requirePrioritySQLException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new SQLException()).when(manager).checkPriority(sender, 1);
		manager.requirePriority(sender, 1, sender);
	}

	@Test(expected = ClassNotFoundException.class)
	public void requirePriorityClassNotFoundException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new ClassNotFoundException()).when(manager).checkPriority(sender, 1);
		manager.requirePriority(sender, 1, sender);
	}

	@Test(expected = InterruptedException.class)
	public void requirePriorityInterruptedException() throws InterruptedException, SQLException, ClassNotFoundException {
		doThrow(new InterruptedException()).when(manager).checkPriority(sender, 1);
		manager.requirePriority(sender, 1, sender);
	}
}