package net.KabOOm356.Service;

import net.KabOOm356.Configuration.Entry;
import net.KabOOm356.Reporter.Configuration.Entry.ConfigurationEntries;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Runnable.Timer.ReportTimer;
import net.KabOOm356.Service.Store.type.PlayerReport;
import net.KabOOm356.Service.Store.type.PlayerReportQueue;
import net.KabOOm356.Util.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.Answer.ConfigurationAnswer;
import test.test.service.ServiceTest;

import java.util.PriorityQueue;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({ReportLimitService.class, PlayerReport.class, PlayerReportQueue.class, Player.class, Bukkit.class, BukkitUtil.class})
public class ReportLimitServiceTest extends ServiceTest {
	private final String playerName = "testPlayer";
	private final String reportedPlayerName = "reportedPlayer";
	// 2 minutes and 5 seconds
	private final int uuidTimerTime = 125;
	private final UUID playerUUID = UUID.randomUUID();
	private final UUID reportedPlayerUUID = UUID.randomUUID();
	private final PriorityQueue<ReportTimer> timers = new PriorityQueue<>();
	private ReportLimitService service;
	@Mock
	private Reporter plugin;
	@Mock
	private ConfigurationService configurationService;
	@Mock
	private PermissionService permissionService;
	@Mock
	private BukkitScheduler bukkitScheduler;
	@Mock
	private CommandSender sender;
	@Mock
	private OfflinePlayer offlinePlayer;
	@Mock
	private Player player;
	@Mock
	private OfflinePlayer reportedPlayer;
	@Mock
	private ReportTimer timer;
	private PlayerReportQueue reportQueue;

	@Override
	@Before
	public void setupMocks() throws Exception {
		super.setupMocks();
		mockStatic(Player.class);
		mockStatic(OfflinePlayer.class);
		mockStatic(Bukkit.class);
		mockStatic(BukkitUtil.class);
		when(Bukkit.getScheduler()).thenReturn(bukkitScheduler);
		when(BukkitUtil.getPlugin(Reporter.class.getSimpleName())).thenReturn(plugin);
		when(getModule().getConfigurationService()).thenReturn(configurationService);
		when(permissionService.hasPermission(any(Player.class), anyString())).thenReturn(false);
		when(getModule().getPermissionService()).thenReturn(permissionService);
		service = spy(new ReportLimitService(getModule()));

		when(BukkitUtil.isPlayer(sender)).thenReturn(true);
		when(BukkitUtil.isOfflinePlayer(sender)).thenReturn(true);
		when(BukkitUtil.isPlayer(player)).thenReturn(true);
		when(BukkitUtil.isOfflinePlayer(player)).thenReturn(true);
		when(BukkitUtil.formatPlayerName(reportedPlayer)).thenReturn(reportedPlayerName);
		when(Player.class.cast(sender)).thenReturn(player);
		when(OfflinePlayer.class.cast(sender)).thenReturn(offlinePlayer);
		when(Player.class.cast(player)).thenReturn(player);
		when(OfflinePlayer.class.cast(player)).thenReturn(player);
		when(sender.getName()).thenReturn(playerName);
		when(player.getName()).thenReturn(playerName);
		when(player.getUniqueId()).thenReturn(playerUUID);
		when(offlinePlayer.getUniqueId()).thenReturn(playerUUID);
		when(reportedPlayer.getName()).thenReturn(reportedPlayerName);
		when(reportedPlayer.getUniqueId()).thenReturn(reportedPlayerUUID);

		when(configurationService.get((Entry<?>)any(Entry.class))).thenAnswer(ConfigurationAnswer.instance);
		when(configurationService.get(ConfigurationEntries.limitReportsAgainstPlayers)).thenReturn(true);
		when(configurationService.get(ConfigurationEntries.alertConsoleWhenLimitReached)).thenReturn(true);
		when(configurationService.get(ConfigurationEntries.alertConsoleWhenLimitAgainstPlayerReached)).thenReturn(true);

		reportQueue = new PlayerReportQueue();
		getPlayerReport().put(playerUUID, reportQueue);

		when(timer.getTimeRemaining()).thenReturn(uuidTimerTime);
		timers.add(timer);
		reportQueue.put(reportedPlayer.getUniqueId(), timers);
	}

	@Test
	public void testCanReport() {
		assertTrue(service.canReport(sender));
	}

	@Test
	public void testCanReportOverLimit() {
		when(configurationService.get(ConfigurationEntries.reportLimit)).thenReturn(0);
		timers.add(mock(ReportTimer.class));
		assertFalse(service.canReport(player));
	}

	@Test
	public void testCanReportNotPlayer() {
		when(BukkitUtil.isPlayer(sender)).thenReturn(false);
		assertTrue(service.canReport(sender));
	}

	@Test
	public void testCanReportHasNotReported() {
		reportQueue.clear();
		assertTrue(service.canReport(sender));
	}

	@Test
	public void testCanReportDoNotLimitReports() {
		when(configurationService.get(ConfigurationEntries.limitReports)).thenReturn(false);
		assertTrue(service.canReport(sender));
	}

	@Test
	public void testCanReportSpecificPlayer() {
		assertTrue(service.canReport(sender, mock(OfflinePlayer.class)));
	}

	@Test
	public void testCanReportSpecificPlayerOverLimit() {
		when(configurationService.get(ConfigurationEntries.reportLimitAgainstPlayers)).thenReturn(1);
		reportQueue.put(reportedPlayer, mock(ReportTimer.class));
		reportQueue.put(reportedPlayer, mock(ReportTimer.class));
		assertFalse(service.canReport(sender, reportedPlayer));
	}

	@Test
	public void testCanReportSpecificPlayerNotPlayer() {
		when(BukkitUtil.isPlayer(sender)).thenReturn(false);
		assertTrue(service.canReport(sender, reportedPlayer));
	}

	@Test
	public void testCanReportSpecificPlayerHasNotReported() {
		reportQueue.clear();
		assertTrue(service.canReport(sender, reportedPlayer));
	}

	@Test
	public void testCanReportSpecificPlayerDoNotLimitReports() {
		when(configurationService.get(ConfigurationEntries.limitReportsAgainstPlayers)).thenReturn(false);
		assertTrue(service.canReport(sender, reportedPlayer));
	}

	@Test
	public void testHasReported() {
		doReturn(true).when(service).canReport(sender);
		doReturn(true).when(service).canReport(sender, reportedPlayer);
		when(permissionService.hasPermission(player, "reporter.report.nolimit")).thenReturn(false);
		service.hasReported(sender, reportedPlayer);
		verify(bukkitScheduler).runTaskLaterAsynchronously(any(JavaPlugin.class), any(ReportTimer.class), anyLong());
	}

	@Test
	public void testHasReportedNotPlayer() {
		when(BukkitUtil.isPlayer(sender)).thenReturn(false);
		doReturn(true).when(service).canReport(sender);
		doReturn(true).when(service).canReport(sender, reportedPlayer);
		service.hasReported(sender, reportedPlayer);
		verify(bukkitScheduler, never()).runTaskLaterAsynchronously(any(JavaPlugin.class), any(ReportTimer.class), anyLong());
	}

	@Test
	public void testHasReportedCantReport() {
		doReturn(false).when(service).canReport(sender);
		doReturn(true).when(service).canReport(sender, reportedPlayer);
		service.hasReported(sender, reportedPlayer);
		verify(bukkitScheduler, never()).runTaskLaterAsynchronously(any(JavaPlugin.class), any(ReportTimer.class), anyLong());
	}

	@Test
	public void testHasReportedCantReportPlayer() {
		doReturn(true).when(service).canReport(sender);
		doReturn(false).when(service).canReport(sender, reportedPlayer);
		service.hasReported(sender, reportedPlayer);
		verify(bukkitScheduler, never()).runTaskLaterAsynchronously(any(JavaPlugin.class), any(ReportTimer.class), anyLong());
	}

	@Test
	public void testGetRemainingTime() {
		assertEquals(uuidTimerTime, service.getRemainingTime(sender));
	}

	@Test
	public void testGetRemainingTimeEmpty() {
		assertEquals(0, service.getRemainingTime(mock(CommandSender.class)));
	}

	@Test
	public void testGetRemainingTimeReported() {
		assertEquals(uuidTimerTime, service.getRemainingTime(sender, reportedPlayer));
	}

	@Test
	public void testGetRemainingTimeReportEmpty() {
		assertEquals(0, service.getRemainingTime(mock(CommandSender.class), mock(OfflinePlayer.class)));
		assertEquals(0, service.getRemainingTime(sender, mock(OfflinePlayer.class)));
		assertEquals(0, service.getRemainingTime(mock(CommandSender.class), reportedPlayer));
	}

	@Test
	public void testLimitExpired() {
		when(timer.getPlayer()).thenReturn(player);
		when(timer.getReported()).thenReturn(reportedPlayer);
		assertTrue(reportQueue.get(reportedPlayer).contains(timer));
		service.limitExpired(timer);
		assertFalse(reportQueue.get(reportedPlayer).contains(timer));
	}
}