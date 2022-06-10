package net.KabOOm356.Service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.UUID;
import net.KabOOm356.Configuration.Entry;
import net.KabOOm356.Reporter.Configuration.Entry.ConfigurationEntries;
import net.KabOOm356.Runnable.Timer.ReportTimer;
import net.KabOOm356.Service.Store.type.PlayerReportQueue;
import net.KabOOm356.Util.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import test.test.Answer.ConfigurationAnswer;
import test.test.service.ServiceTest;

public class ReportLimitServiceTest extends ServiceTest {
  // 2 minutes and 5 seconds
  private final int uuidTimerTime = 125;
  private ReportLimitService service;
  @Mock private ConfigurationService configurationService;
  @Mock private PermissionService permissionService;

  @Override
  @Before
  public void setupMocks() throws Exception {
    super.setupMocks();

    when(getModule().getConfigurationService()).thenReturn(configurationService);
    when(permissionService.hasPermission(any(Player.class), anyString())).thenReturn(false);
    when(getModule().getPermissionService()).thenReturn(permissionService);
    service = spy(new ReportLimitService(getModule()));

    when(configurationService.get((Entry<?>) any(Entry.class))).then(ConfigurationAnswer.instance);
    when(configurationService.get(ConfigurationEntries.limitReports)).thenReturn(true);
    when(configurationService.get(ConfigurationEntries.limitReportsAgainstPlayers))
        .thenReturn(true);
    when(configurationService.get(ConfigurationEntries.alertConsoleWhenLimitReached))
        .thenReturn(true);
    when(configurationService.get(ConfigurationEntries.alertConsoleWhenLimitAgainstPlayerReached))
        .thenReturn(true);
  }

  @Test
  public void testCanReportDisabled() {
    final CommandSender sender = mock(CommandSender.class);
    when(configurationService.get(ConfigurationEntries.limitReports)).thenReturn(true);
    assertTrue(service.canReport(sender));
  }

  @Test
  public void testCanReportOverLimit() {
    when(configurationService.get(ConfigurationEntries.reportLimit)).thenReturn(0);
    final CommandSender sender =
        mock(
            CommandSender.class, withSettings().extraInterfaces(OfflinePlayer.class, Player.class));
    final UUID uuid = UUID.randomUUID();
    when(((OfflinePlayer) sender).getUniqueId()).thenReturn(uuid);
    final PlayerReportQueue timers = new PlayerReportQueue();
    timers.put((OfflinePlayer) sender, mock(ReportTimer.class));
    doReturn(timers).when(getPlayerReports()).get(sender);
    try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      assertFalse(service.canReport(sender));
    }
  }

  @Test
  public void testCanReportNotPlayer() {
    final CommandSender sender = mock(CommandSender.class);
    final PlayerReportQueue timers = new PlayerReportQueue();
    doReturn(timers).when(getPlayerReports()).get(sender);
    try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      assertTrue(service.canReport(sender));
    }
  }

  @Test
  public void testCanReportHasNotReported() {
    final CommandSender sender =
        mock(
            CommandSender.class, withSettings().extraInterfaces(OfflinePlayer.class, Player.class));
    assertTrue(service.canReport(sender));
  }

  @Test
  public void testCanReportSpecificPlayer() {
    final CommandSender sender = mock(CommandSender.class);
    assertTrue(service.canReport(sender, mock(OfflinePlayer.class)));
  }

  @Test
  public void testCanReportSpecificPlayerOverLimit() {
    when(configurationService.get(ConfigurationEntries.reportLimitAgainstPlayers)).thenReturn(1);
    final CommandSender sender =
        mock(CommandSender.class, withSettings().extraInterfaces(Player.class));
    final OfflinePlayer reported = mock(OfflinePlayer.class);
    final PlayerReportQueue reportQueue = mock(PlayerReportQueue.class);
    final PriorityQueue<ReportTimer> queue = new PriorityQueue<>();
    queue.addAll(Arrays.asList(mock(ReportTimer.class), mock(ReportTimer.class)));
    when(reportQueue.get(reported)).thenReturn(queue);
    when(getPlayerReports().get(sender)).thenReturn(reportQueue);

    try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      assertFalse(service.canReport(sender, reported));
    }
  }

  @Test
  public void testCanReportSpecificPlayerNotPlayer() {
    final CommandSender sender = mock(CommandSender.class);
    final OfflinePlayer reported = mock(OfflinePlayer.class);
    assertTrue(service.canReport(sender, reported));
  }

  @Test
  public void testCanReportSpecificPlayerHasNotReported() {
    final CommandSender sender =
        mock(CommandSender.class, withSettings().extraInterfaces(Player.class));
    final OfflinePlayer reported = mock(OfflinePlayer.class);

    try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      assertTrue(service.canReport(sender, reported));
    }
  }

  @Test
  public void testCanReportSpecificPlayerDoNotLimitReports() {
    final CommandSender sender =
        mock(CommandSender.class, withSettings().extraInterfaces(Player.class));
    final OfflinePlayer reported = mock(OfflinePlayer.class);
    when(configurationService.get(ConfigurationEntries.limitReportsAgainstPlayers))
        .thenReturn(false);
    assertTrue(service.canReport(sender, reported));
  }

  @Test
  public void testHasReported() {
    final CommandSender sender =
        mock(CommandSender.class, withSettings().extraInterfaces(Player.class));
    final OfflinePlayer reported = mock(OfflinePlayer.class);
    doReturn(true).when(service).canReport(sender);
    doReturn(true).when(service).canReport(sender, reported);
    when(permissionService.hasPermission((Player) sender, "reporter.report.nolimit"))
        .thenReturn(false);

    try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
        final MockedStatic<BukkitUtil> bukkitUtil = mockStatic(BukkitUtil.class)) {
      final Plugin plugin = mock(Plugin.class);
      bukkitUtil.when(() -> BukkitUtil.isPlayer(sender)).thenReturn(true);
      bukkitUtil.when(() -> BukkitUtil.getPlugin(anyString())).thenReturn(plugin);
      final BukkitScheduler bukkitScheduler = mock(BukkitScheduler.class);
      bukkit.when(Bukkit::getScheduler).thenReturn(bukkitScheduler);
      service.hasReported(sender, reported);
      verify(bukkitScheduler)
          .runTaskLaterAsynchronously(eq(plugin), any(ReportTimer.class), anyLong());
    }
  }

  @Test
  public void testHasReportedNotPlayer() {
    final CommandSender sender = mock(CommandSender.class);
    final OfflinePlayer reported = mock(OfflinePlayer.class);
    doReturn(true).when(service).canReport(sender);
    doReturn(true).when(service).canReport(sender, reported);

    try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
        final MockedStatic<BukkitUtil> bukkitUtil = mockStatic(BukkitUtil.class)) {
      bukkitUtil.when(() -> BukkitUtil.isPlayer(sender)).thenReturn(false);
      final BukkitScheduler bukkitScheduler = mock(BukkitScheduler.class);
      bukkit.when(Bukkit::getScheduler).thenReturn(bukkitScheduler);
      service.hasReported(sender, reported);
      verify(bukkitScheduler, never())
          .runTaskLaterAsynchronously(any(JavaPlugin.class), any(ReportTimer.class), anyLong());
    }
  }

  @Test
  public void testGetRemainingTime() {
    final CommandSender sender = mock(CommandSender.class);
    final OfflinePlayer reported = mock(OfflinePlayer.class);
    when(reported.getUniqueId()).thenReturn(UUID.randomUUID());
    final PlayerReportQueue reportQueue = new PlayerReportQueue();
    final ReportTimer timer = mock(ReportTimer.class);
    reportQueue.put(reported, timer);

    when(timer.getTimeRemaining()).thenReturn(uuidTimerTime);
    when(getPlayerReports().get(sender)).thenReturn(reportQueue);

    assertEquals(uuidTimerTime, service.getRemainingTime(sender));
  }

  @Test
  public void testGetRemainingTimeEmpty() {
    assertEquals(0, service.getRemainingTime(mock(CommandSender.class)));
  }

  @Test
  public void testGetRemainingTimeReported() {
    final CommandSender sender = mock(CommandSender.class);
    final OfflinePlayer reported = mock(OfflinePlayer.class);
    when(reported.getUniqueId()).thenReturn(UUID.randomUUID());
    final PlayerReportQueue reportQueue = new PlayerReportQueue();
    final ReportTimer timer = mock(ReportTimer.class);
    reportQueue.put(reported, timer);

    when(timer.getTimeRemaining()).thenReturn(uuidTimerTime);
    when(getPlayerReports().get(sender)).thenReturn(reportQueue);

    assertEquals(uuidTimerTime, service.getRemainingTime(sender, reported));
  }

  @Test
  public void testGetRemainingTimeReportEmpty() {
    final CommandSender sender = mock(CommandSender.class);
    final OfflinePlayer reported = mock(OfflinePlayer.class);
    assertEquals(0, service.getRemainingTime(mock(CommandSender.class), mock(OfflinePlayer.class)));
    assertEquals(0, service.getRemainingTime(sender, mock(OfflinePlayer.class)));
    assertEquals(0, service.getRemainingTime(mock(CommandSender.class), reported));
  }

  @Test
  public void testLimitExpired() {
    final Player player = mock(Player.class);
    final OfflinePlayer reported = mock(OfflinePlayer.class);
    final ReportTimer timer = mock(ReportTimer.class);
    when(timer.getPlayer()).thenReturn(player);
    when(timer.getReported()).thenReturn(reported);

    service.limitExpired(timer);
    verify(getPlayerReports()).remove(player, reported, timer);
  }
}
