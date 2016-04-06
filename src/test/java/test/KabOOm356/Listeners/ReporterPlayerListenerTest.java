package test.KabOOm356.Listeners;

import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Listeners.ReporterPlayerListener;
import net.KabOOm356.Manager.MessageManager;
import net.KabOOm356.Reporter.Reporter;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.KabOOm356.PowerMockitoTest;

import java.util.HashMap;
import java.util.UUID;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest(ReporterPlayerListener.class)
public class ReporterPlayerListenerTest extends PowerMockitoTest {
	private static final String playerName = "playerName";

	@Mock
	private Player player;

	@Mock
	private Reporter reporter;

	@Mock
	private ReporterCommandManager reporterCommandManager;

	@Mock
	private MessageManager messageManager;

	@Mock
	private YamlConfiguration configuration;

	@Spy
	private HashMap<CommandSender, Integer> lastViewed = new HashMap<CommandSender, Integer>();

	private PlayerJoinEvent joinEvent;
	private PlayerQuitEvent quitEvent;
	private ReporterPlayerListener listener;

	@Before
	public void setupMocks() {
		final UUID playerUUID = UUID.randomUUID();
		when(player.getUniqueId()).thenReturn(playerUUID);
		when(player.getName()).thenReturn(playerName);
		joinEvent = spy(new PlayerJoinEvent(player, "JoinTest"));
		quitEvent = spy(new PlayerQuitEvent(player, "QuitTest"));
		when(messageManager.hasMessages(anyString())).thenReturn(false);
		when(reporterCommandManager.getLastViewed()).thenReturn(lastViewed);
		when(reporterCommandManager.getMessageManager()).thenReturn(messageManager);
		when(reporter.getCommandManager()).thenReturn(reporterCommandManager);
		when(configuration.getBoolean(anyString(), anyBoolean())).thenReturn(false);
		doReturn(configuration).when(reporter).getConfig();
		listener = spy(new ReporterPlayerListener(reporter));
	}

	@Test
	public void testOnPlayerJoin() {
		listener.onPlayerJoin(joinEvent);
		verify(lastViewed).put(player, -1);
	}

	@Test
	public void testOnPlayerJoinListOnLogin() throws Exception {
		when(configuration.getBoolean("general.messaging.listOnLogin.listOnLogin", true)).thenReturn(true);
		doNothing().when(listener, "listOnLogin", player);
		listener.onPlayerJoin(joinEvent);
		verify(lastViewed).put(player, -1);
		verifyPrivate(listener).invoke("listOnLogin", player);
	}

	@Test
	public void testOnPlayerJoinHasMessagesUUID() throws Exception {
		when(messageManager.hasMessages(player.getUniqueId().toString())).thenReturn(true);
		doNothing().when(listener, "sendMessages", player);
		listener.onPlayerJoin(joinEvent);
		verify(lastViewed).put(player, -1);
		verifyPrivate(listener).invoke("sendMessages", player);
	}

	@Test
	public void testOnPlayerJoinHasMessagesName() throws Exception {
		when(messageManager.hasMessages(player.getName())).thenReturn(true);
		doNothing().when(listener, "sendMessages", player);
		listener.onPlayerJoin(joinEvent);
		verify(lastViewed).put(player, -1);
		verifyPrivate(listener).invoke("sendMessages", player);
	}

	@Test
	public void testOnPlayerJoinAlertEnabled() {
		when(configuration.getBoolean("general.messaging.alerts.reportedPlayerLogin.enabled", true)).thenReturn(true);
		listener.onPlayerJoin(joinEvent);
		verify(lastViewed).put(player, -1);
	}

	@Test
	public void testOnPlayerJoinAlertToPlayerEnabledReported() throws Exception {
		when(configuration.getBoolean("general.messaging.alerts.reportedPlayerLogin.enabled", true)).thenReturn(true);
		when(configuration.getBoolean("general.messaging.alerts.reportedPlayerLogin.toPlayer", true)).thenReturn(true);
		doReturn(true).when(listener, "isPlayerReported", player);
		doNothing().when(listener, "alertThatReportedPlayerLogin", player);
		listener.onPlayerJoin(joinEvent);
		verify(lastViewed).put(player, -1);
		verifyPrivate(listener).invoke("isPlayerReported", player);
		verifyPrivate(listener).invoke("alertThatReportedPlayerLogin", player);
	}

	@Test
	public void testOnPlayerJoinAlertToConsoleEnabledReported() throws Exception {
		when(configuration.getBoolean("general.messaging.alerts.reportedPlayerLogin.enabled", true)).thenReturn(true);
		when(configuration.getBoolean("general.messaging.alerts.reportedPlayerLogin.toConsole", true)).thenReturn(true);
		doReturn(true).when(listener, "isPlayerReported", player);
		doNothing().when(listener, "alertThatReportedPlayerLogin", player);
		listener.onPlayerJoin(joinEvent);
		verify(lastViewed).put(player, -1);
		verifyPrivate(listener).invoke("isPlayerReported", player);
		verifyPrivate(listener).invoke("alertThatReportedPlayerLogin", player);
	}

	@Test
	public void testOnPlayerJoinAlertDisabledReported() throws Exception {
		when(configuration.getBoolean("general.messaging.alerts.reportedPlayerLogin.enabled", true)).thenReturn(false);
		when(configuration.getBoolean("general.messaging.alerts.reportedPlayerLogin.toPlayer", true)).thenReturn(true);
		when(configuration.getBoolean("general.messaging.alerts.reportedPlayerLogin.toConsole", true)).thenReturn(true);
		listener.onPlayerJoin(joinEvent);
		verify(lastViewed).put(player, -1);
		verifyPrivate(listener, never()).invoke("isPlayerReported", player);
		verifyPrivate(listener, never()).invoke("alertThatReportedPlayerLogin", player);
	}

	@Test
	public void testOnPlayerJoinAlertEnabledNotReported() throws Exception {
		when(configuration.getBoolean("general.messaging.alerts.reportedPlayerLogin.enabled", true)).thenReturn(true);
		when(configuration.getBoolean("general.messaging.alerts.reportedPlayerLogin.toPlayer", true)).thenReturn(true);
		when(configuration.getBoolean("general.messaging.alerts.reportedPlayerLogin.toConsole", true)).thenReturn(true);
		doReturn(false).when(listener, "isPlayerReported", player);
		listener.onPlayerJoin(joinEvent);
		verify(lastViewed).put(player, -1);
		verifyPrivate(listener).invoke("isPlayerReported", player);
		verifyPrivate(listener, never()).invoke("alertThatReportedPlayerLogin", player);
	}

	@Test
	public void testOnPlayerQuit() {
		listener.onPlayerQuit(quitEvent);
		verify(lastViewed).remove(player);
	}
}
