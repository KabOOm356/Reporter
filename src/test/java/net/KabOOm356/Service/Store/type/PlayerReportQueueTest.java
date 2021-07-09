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

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({BukkitUtil.class, Bukkit.class, Player.class, PriorityQueue.class, PlayerReportQueue.class})
public class PlayerReportQueueTest extends PowerMockitoTest {
	private static final String playerName = "playerName";
	private static final UUID playerUUID = UUID.randomUUID();
	@Spy
	private final PlayerReportQueue playerReportQueue = new PlayerReportQueue();
	@Mock
	private OfflinePlayer player;
	@Mock
	private ReportTimer timer;

	@Before
	public void setupMocks() {
		mockStatic(Bukkit.class);
		mockStatic(BukkitUtil.class);
		mockStatic(Player.class);
		when(BukkitUtil.isPlayerValid(player)).thenReturn(true);
		when(player.getName()).thenReturn(playerName);
		when(player.getUniqueId()).thenReturn(playerUUID);
	}

	@Test
	public void testPut() {
		playerReportQueue.put(player, timer);
		assertEquals(timer, playerReportQueue.get(player).peek());
	}

	@Test
	public void testPutNotValidPlayer() {
		when(BukkitUtil.isPlayerValid(player)).thenReturn(false);

		playerReportQueue.put(player, timer);

		assertEquals(timer, playerReportQueue.get(player).peek());
	}

	@Test
	public void testPutExisting() {
        final PriorityQueue<ReportTimer> priorityQueue = spy(new PriorityQueue<>());
        playerReportQueue.put(playerUUID, priorityQueue);

        playerReportQueue.put(player, timer);
        verify(priorityQueue).add(timer);
    }

	@Test
	public void testGetEmpty() {
		final PriorityQueue<ReportTimer> returned = playerReportQueue.get(player);
		assertNull(returned);
	}

	@Test
	public void testGetUUID() {
		final PriorityQueue<ReportTimer> priorityQueue = new PriorityQueue<>();
		final ReportTimer timer = mock(ReportTimer.class);
		priorityQueue.add(timer);
		playerReportQueue.put(playerUUID, priorityQueue);

		final PriorityQueue<ReportTimer> returned = playerReportQueue.get(player);
        assertFalse(returned.isEmpty());
        assertEquals(1, returned.size());
        assertTrue(returned.contains(timer));
	}
}