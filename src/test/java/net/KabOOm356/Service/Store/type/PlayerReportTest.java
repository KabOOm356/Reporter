package net.KabOOm356.Service.Store.type;

import net.KabOOm356.Runnable.Timer.ReportTimer;
import net.KabOOm356.Util.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.PowerMockitoTest;

import java.util.PriorityQueue;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({PlayerReport.class, PlayerReportQueue.class, Player.class, Bukkit.class, BukkitUtil.class})
public class PlayerReportTest extends PowerMockitoTest {
	private static final String senderName = "senderName";
	private static final UUID senderUUID = UUID.randomUUID();
	private static final String reportedPlayerName = "reportedPlayer";
	private static final UUID reportedPlayerUUID = UUID.randomUUID();
	@Spy
	private final PlayerReport playerReport = new PlayerReport();
	@Spy
	private final PlayerReportQueue playerReportQueue = new PlayerReportQueue();
	@Mock
	private OfflinePlayer player;
	@Mock
	private OfflinePlayer reportedPlayer;
	@Mock
	private ReportTimer timer;

	@Before
	public void setupMocks() throws Exception {
		mockStatic(Bukkit.class);
		mockStatic(BukkitUtil.class);
		mockStatic(Player.class);
		when(player.getName()).thenReturn(senderName);
		when(player.getUniqueId()).thenReturn(senderUUID);
		when(reportedPlayer.getName()).thenReturn(reportedPlayerName);
		when(reportedPlayer.getUniqueId()).thenReturn(reportedPlayerUUID);
	}

	@Test
	public void put() throws Exception {
		mockNewPlayerReportQueue();
		playerReport.put(player, reportedPlayer, timer);
		verifyNew(PlayerReportQueue.class).withNoArguments();
		verify(playerReportQueue).put(reportedPlayer, timer);
		verify(playerReport).put(senderUUID, playerReportQueue);
	}

	@Test
	public void putExisting() throws Exception {
		mockNewPlayerReportQueue();
		final PlayerReportQueue existing = mock(PlayerReportQueue.class);
		when(playerReport.containsKey(senderUUID)).thenReturn(true);
		when(playerReport.get(senderUUID)).thenReturn(existing);
		playerReport.put(player, reportedPlayer, timer);
		verifyNew(PlayerReportQueue.class, never()).withNoArguments();
		verify(existing).put(reportedPlayer, timer);
	}

	@Test
	public void get() {
		final PlayerReportQueue playerReportQueueUUID = new PlayerReportQueue();
		final PriorityQueue<ReportTimer> timers = new PriorityQueue<ReportTimer>();
		timers.add(mock(ReportTimer.class));
		playerReport.put(senderUUID, playerReportQueueUUID);

		final PlayerReportQueue returned = playerReport.get(player);
		assertNotNull(returned);
		assertEquals(playerReportQueueUUID, returned);
	}

	@Test
	public void testRemove() {
		final PlayerReportQueue playerReportQueueUUID = mock(PlayerReportQueue.class);
		playerReport.put(senderUUID, playerReportQueueUUID);

		playerReport.remove(player, reportedPlayer, timer);
		verify(playerReportQueueUUID).remove(reportedPlayer, timer);
	}

	private void mockNewPlayerReportQueue() throws Exception {
		whenNew(PlayerReportQueue.class).withNoArguments().thenReturn(playerReportQueue);
	}
}