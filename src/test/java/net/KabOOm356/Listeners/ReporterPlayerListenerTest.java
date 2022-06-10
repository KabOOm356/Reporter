package net.KabOOm356.Listeners;

import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import net.KabOOm356.Command.Commands.ListCommand;
import net.KabOOm356.Command.Commands.ViewCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrase;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Service.LastViewedReportService;
import net.KabOOm356.Service.PermissionService;
import net.KabOOm356.Service.PlayerMessageService;
import net.KabOOm356.Service.ServiceModule;
import net.KabOOm356.Util.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import test.test.MockitoTest;

public class ReporterPlayerListenerTest extends MockitoTest {
  private static final String playerName = "playerName";
  @Mock private LastViewedReportService lastViewedReportService;
  @Mock private Player player;
  @Mock private Reporter reporter;
  @Mock private ReporterCommandManager reporterCommandManager;
  @Mock private ListCommand listCommand;
  @Mock private PlayerMessageService playerMessageService;
  @Mock private PermissionService permissionService;
  @Mock private YamlConfiguration configuration;
  @Mock private ExtendedDatabaseHandler databaseHandler;
  @Mock private Locale locale;
  private PlayerJoinEvent joinEvent;
  private PlayerQuitEvent quitEvent;
  private ReporterPlayerListener listener;

  @Before
  public void setupMocks() throws SQLException, ClassNotFoundException, InterruptedException {
    listener = spy(new ReporterPlayerListener(reporter));
    final UUID playerUUID = UUID.randomUUID();
    when(player.getUniqueId()).thenReturn(playerUUID);
    when(player.getName()).thenReturn(playerName);
    when(player.getDisplayName()).thenReturn(playerName);
    joinEvent = spy(new PlayerJoinEvent(player, "JoinTest"));
    quitEvent = spy(new PlayerQuitEvent(player, "QuitTest"));
    when(playerMessageService.hasMessages(anyString())).thenReturn(false);
    final ServiceModule serviceModule = mock(ServiceModule.class);
    when(reporterCommandManager.getServiceModule()).thenReturn(serviceModule);
    when(serviceModule.getLastViewedReportService()).thenReturn(lastViewedReportService);
    when(serviceModule.getPermissionService()).thenReturn(permissionService);
    when(serviceModule.getPlayerMessageService()).thenReturn(playerMessageService);
    when(reporter.getCommandManager()).thenReturn(reporterCommandManager);
    when(configuration.getBoolean(anyString(), anyBoolean())).thenReturn(false);
    doReturn(configuration).when(reporter).getConfig();
    when(reporter.getLocale()).thenReturn(locale);
    when(locale.getString(any(LocalePhrase.class))).thenReturn("locale phrase");
    when(reporterCommandManager.getCommand(anyString())).thenReturn(listCommand);

    when(reporter.getDatabaseHandler()).thenReturn(databaseHandler);
  }

  @Test
  public void testOnPlayerJoinListOnLogin() {
    when(configuration.getBoolean(eq("general.messaging.listOnLogin.listOnLogin"), anyBoolean()))
        .thenReturn(true);
    when(configuration.getBoolean(eq("general.messaging.listOnLogin.useDelay"), anyBoolean()))
        .thenReturn(false);
    when(listCommand.hasPermission(any(Player.class))).thenReturn(true);
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      final BukkitScheduler scheduler = mock(BukkitScheduler.class);
      bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
      listener.onPlayerJoin(joinEvent);
      verify(scheduler).runTaskAsynchronously(reporter, listCommand);
    }
  }

  @Test
  public void testOnPlayerJoinListOnLoginDelay() {
    when(configuration.getBoolean(eq("general.messaging.listOnLogin.listOnLogin"), anyBoolean()))
        .thenReturn(true);
    when(configuration.getBoolean(eq("general.messaging.listOnLogin.useDelay"), anyBoolean()))
        .thenReturn(true);
    when(configuration.getInt(eq("general.messaging.listOnLogin.delay"), anyInt())).thenReturn(5);
    when(listCommand.hasPermission(any(Player.class))).thenReturn(true);
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
        MockedStatic<BukkitUtil> bukkitUtil = mockStatic(BukkitUtil.class)) {
      final BukkitScheduler scheduler = mock(BukkitScheduler.class);
      bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
      bukkitUtil.when(() -> BukkitUtil.convertSecondsToServerTicks(anyInt())).thenReturn(10L);
      listener.onPlayerJoin(joinEvent);
      verify(scheduler).runTaskLaterAsynchronously(reporter, listCommand, 10L);
    }
  }

  @Test
  public void testOnPlayerJoinHasMessagesUUID() {
    when(playerMessageService.hasMessages(player.getUniqueId().toString())).thenReturn(true);
    when(permissionService.hasPermission(player, ViewCommand.getCommandPermissionNode()))
        .thenReturn(true);
    when(playerMessageService.getMessages(player.getUniqueId().toString()))
        .thenReturn(Arrays.asList("Test Message", "Test Message 2"));
    listener.onPlayerJoin(joinEvent);
    verify(player).sendMessage("Test Message");
    verify(player).sendMessage("Test Message 2");
    verify(playerMessageService).removePlayerMessages(player.getUniqueId().toString());
  }

  @Test
  public void testOnPlayerJoinHasMessagesName() {
    when(playerMessageService.hasMessages(player.getUniqueId().toString())).thenReturn(true);
    when(permissionService.hasPermission(player, ViewCommand.getCommandPermissionNode()))
        .thenReturn(true);
    when(playerMessageService.getMessages(player.getUniqueId().toString()))
        .thenReturn(new ArrayList<>());
    when(playerMessageService.getMessages(player.getName()))
        .thenReturn(Arrays.asList("Test Message", "Test Message 2"));
    listener.onPlayerJoin(joinEvent);
    verify(player).sendMessage("Test Message");
    verify(player).sendMessage("Test Message 2");
    verify(playerMessageService).removePlayerMessages(player.getName());
  }

  @Test
  public void testOnPlayerJoinAlertToPlayers() throws Exception {
    when(configuration.getBoolean(
            eq("general.messaging.alerts.reportedPlayerLogin.enabled"), anyBoolean()))
        .thenReturn(true);
    when(configuration.getBoolean(
            eq("general.messaging.alerts.reportedPlayerLogin.toPlayer"), anyBoolean()))
        .thenReturn(true);
    final SQLResultSet isPlayerReportedResult = new SQLResultSet();
    final ResultRow resultRow = new ResultRow();
    resultRow.put("ID", 1);
    resultRow.put("ClaimStatus", false);
    isPlayerReportedResult.add(resultRow);
    when(databaseHandler.sqlQuery(anyString())).thenReturn(isPlayerReportedResult);
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      final Player admin = mock(Player.class);
      bukkit.when(Bukkit::getOnlinePlayers).thenReturn(Collections.singletonList(admin));
      when(permissionService.hasPermission(admin, "reporter.alerts.onlogin.reportedPlayerLogin"))
          .thenReturn(true);
      listener.onPlayerJoin(joinEvent);
      verify(admin).sendMessage(anyString());
    }
  }

  @Test
  public void testOnPlayerQuit() {
    listener.onPlayerQuit(quitEvent);
    verify(lastViewedReportService).removeLastViewedReport(player);
  }
}
